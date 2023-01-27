package com.planview.lkutility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.planview.lkutility.diff.Diff;
import com.planview.lkutility.exporter.Exporter;
import com.planview.lkutility.importer.Importer;
import com.planview.lkutility.leankit.AccessCache;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Main {
	static Debug d = null;

	/**
	 * This defaults to true so we behave as an exporter if it is not otherwise
	 * specified. If the command line contains a -i flag, this is set to false.
	 */
	static Boolean setToExport = true;
	static Boolean setToImport = false;
	static Boolean setToDiff = false;

	/**
	 * One line sheet that contains the credentials to access the Leankit Server.
	 * Must contain columns "url", "username", "password" and "apiKey", but not
	 * necessarily have data in all of them - see getConfigFromFile()
	 */
	static XSSFSheet configSht = null;

	/**
	 * The expectation is that there is a common config for the while execution.
	 * Therefore this is extracted once and passed to all sub tasks
	 */
	static InternalConfig config = new InternalConfig();

	public static void main(String[] args) {
		d = new Debug();
		getCommandLine(args);

		checkXlsx();
		HashMap<String, Integer> fieldMap = chkConfigFromFile();
		if (configSht != null) {
			Iterator<Row> rowItr = configSht.iterator();
			Row row = rowItr.next(); // Move past headers
			while (rowItr.hasNext()) {
				row = rowItr.next();
				config = XlUtils.setConfig(config, row, fieldMap);

				config.source.setCache(new AccessCache());
				config.destination.setCache(new AccessCache());

				if (config.replay && !setToDiff) {
					config.changesSheet = config.wb.getSheet("replay_" + config.destination.getBoardName());
					if (config.changesSheet != null) {
						Importer impt = new Importer(config);
						impt.go();
					} else {
						d.p(Debug.ERROR, " Replay sheet not found. Run with -d before (or with) -r\n");
					}
				} else {
					if (setToExport == true) {
						Exporter expt = new Exporter(config);
						expt.go();
					}

					if (setToImport == true) {
						Importer impt = new Importer(config);
						impt.go();
					}

					if (setToDiff == true) {
						Diff diff = new Diff(config);
						diff.go();
					}
				}
				try {
					config.wb.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		d.p(Debug.ALWAYS, "Finished at: %s\n", new Date());
	}

	public static void getCommandLine(String[] args) {

		CommandLineParser p = new DefaultParser();
		HelpFormatter hf = new HelpFormatter();
		CommandLine impExpCl = null;

		Options impExpOpt = new Options();
		Option impO = new Option("i", "import", false, "run importer");
		Option expO = new Option("e", "export", false, "run exporter");
		Option tnsO = new Option("t", "transfer", false, "run transfer");
		Option diffO = new Option("d", "diff", false,
				"compare dst URL to a previous transfer");
		diffO.setRequired(false);
		Option repO = new Option("r", "replay", false,
				"auto-run the reset of the destination during diff");
		repO.setRequired(false);
		impO.setRequired(false);
		expO.setRequired(false);
		tnsO.setRequired(false);
		impExpOpt.addOption(impO);
		impExpOpt.addOption(expO);
		impExpOpt.addOption(tnsO);
		impExpOpt.addOption(diffO);
		impExpOpt.addOption(repO);

		impExpOpt.addRequiredOption("f", "filename", true, "XLSX Spreadsheet (must contain API config!)");

		Option groupOpt = new Option("g", "group", true, "Identifier of group to process (if present)");
		groupOpt.setRequired(false);
		impExpOpt.addOption(groupOpt);

		Option moveOpt = new Option("m", "move", true, "Lane to modify unwanted cards with (for diff only)");
		moveOpt.setRequired(false);
		impExpOpt.addOption(moveOpt);

		Option dbp = new Option("x", "debug", true,
				"Print out loads of helpful stuff: 0 - Error, 1 - And Warnings, 2 - And Info, 3 - And Debugging, 4 - And Network");
		dbp.setRequired(false);
		impExpOpt.addOption(dbp);
		Option archiveOpt = new Option("O", "archived", false, "Include older Archived cards in export (if present)");
		archiveOpt.setRequired(false);
		impExpOpt.addOption(archiveOpt);
		Option tasksOpt = new Option("T", "tasks", false, "Include Task cards in export (if present)");
		tasksOpt.setRequired(false);
		impExpOpt.addOption(tasksOpt);
		Option attsOpt = new Option("A", "attachments", false,
				"Export card attachments in local filesystem (if present)");
		attsOpt.setRequired(false);
		impExpOpt.addOption(attsOpt);
		Option comsOpt = new Option("C", "comments", false, "Export card comments in local filesystem (if present)");
		comsOpt.setRequired(false);
		impExpOpt.addOption(comsOpt);
		Option originOpt = new Option("S", "origin", false, "Add comment for source artifact recording");
		originOpt.setRequired(false);
		impExpOpt.addOption(originOpt);
		Option readOnlyOpt = new Option("R", "ro", false, "Export Read Only fields (Not Imported!)");
		readOnlyOpt.setRequired(false);
		impExpOpt.addOption(readOnlyOpt);
		Option nameResolver = new Option("n", "names", false, "Debug Use Only!");
		nameResolver.setRequired(false);
		impExpOpt.addOption(nameResolver);

		try {
			impExpCl = p.parse(impExpOpt, args, true);

		} catch (ParseException e) {
			// Not expecting to ever come here, but compiler needs something....
			d.p(Debug.ERROR, "(13): %s\n", e.getMessage());
			hf.printHelp(" ", impExpOpt);
			System.exit(5);
		}

		if (impExpCl.hasOption("ro")) {
			config.roFieldExport = true;
		}

		if (impExpCl.hasOption("replay")) {
			config.replay = true;
		}

		if (impExpCl.hasOption("names")) {
			config.nameResolver = true;
		}

		if (impExpCl.hasOption("debug")) {
			String optVal = impExpCl.getOptionValue("debug");
			if (optVal != null) {
				config.debugLevel = Integer.parseInt(optVal);
				d.setLevel(config.debugLevel);
			} else {
				config.debugLevel = 99;
			}
		}

		/**
		 * If we are doing a diff, we don't want to think about import/export via the
		 * normal route. We need to export the board again into a temporary sheet and
		 * then
		 * scan it for differences between it and the original.
		 * We then can use the diff to create a 'reset' changes sheet. Any cards in
		 * excess of
		 * those in the original can be moved to the archive lane
		 */
		if (impExpCl.hasOption("diff")) {
			d.p(Debug.INFO, "Setting to diff mode.\n");
			config.diffMode = impExpCl.getOptionValue("diff");
			setToDiff = true;
			setToExport = false;
			if (impExpCl.hasOption("move")) {
				config.archive = impExpCl.getOptionValue("move");
			}
		} else {
			if (impExpCl.hasOption("replay")) {
				if (impExpCl.hasOption("transfer") || impExpCl.hasOption("export") || impExpCl.hasOption("import")) {
					d.p(Debug.INFO, "Invalid options specified (-r with another). Defaulting to Replay mode.\n");
				} else {
					d.p(Debug.INFO, "Setting to Replay mode.\n");
				}
				config.replay = true;
			}
			/**
			 * Import takes precedence if option present, then export, then transfer
			 */
			else if (impExpCl.hasOption("import")) {
				if (impExpCl.hasOption("transfer") || impExpCl.hasOption("export")) {
					d.p(Debug.INFO, "Invalid options specified (-i with another). Defaulting to Import mode.\n");
				} else {
					d.p(Debug.INFO, "Setting to Import mode.\n");
				}
				setToExport = false;
				setToImport = true;
			} else {
				if (impExpCl.hasOption("export")) {
					if (impExpCl.hasOption("transfer")) {
						d.p(Debug.INFO, "Invalid options specified (-e and -t) Defaulting to Export mode\n");
					}
				} else if (impExpCl.hasOption("transfer")) {
					d.p(Debug.INFO, "Setting to Dual mode\n");
					setToImport = true;
				}
			}
		}

		config.xlsxfn = impExpCl.getOptionValue("filename");

		// We now need to check for all the other unique options

		if (impExpCl.hasOption("group")) {
			config.group = Integer.parseInt(impExpCl.getOptionValue("group"));
		}
		if (impExpCl.hasOption("archived")) {
			config.exportArchived = true;
		}
		if (impExpCl.hasOption("tasks")) {
			config.exportTasks = true;
		}
		if (impExpCl.hasOption("comments")) {
			config.exportComments = true;
		}
		if (impExpCl.hasOption("attachments")) {
			config.exportAttachments = true;
		}
		if (impExpCl.hasOption("origin")) {
			config.addComment = true;
		}

	}

	/**
	 * Check if the XLSX file provided has the correct sheets and we can parse the
	 * details we need
	 */
	public static void checkXlsx() {
		// Check we can open the file
		FileInputStream xlsxfis = null;
		try {
			xlsxfis = new FileInputStream(new File(config.xlsxfn));
		} catch (FileNotFoundException e) {
			d.p(Debug.ERROR, "(4) %s", e.getMessage());

			System.exit(6);

		}
		try {
			config.wb = new XSSFWorkbook(xlsxfis);
			xlsxfis.close();
		} catch (IOException e) {
			d.p(Debug.ERROR, "(5) %s", e.getMessage());
			System.exit(7);
		}

		configSht = config.wb.getSheet("Config");
		if (configSht == null) {
			d.p(Debug.ERROR, "%s", "Did not detect required sheet in the spreadsheet: \"Config\"");
			System.exit(8);
		}
	}

	private static Boolean parseRow(Row drRow, AccessConfig cfg, Field[] p, HashMap<String, Object> fieldMap,
			ArrayList<String> cols) {
		String cv = drRow.getCell((int) (fieldMap.get(cols.get(0)))).getStringCellValue();
		if (cv != null) {

			for (int i = 0; i < cols.size(); i++) {
				String idx = cols.get(i);
				Object obj = fieldMap.get(idx);
				String val = obj.toString();
				try {
					Cell cell = drRow.getCell(Integer.parseInt(val));

					if (cell != null) {
						switch (cell.getCellType()) {
							case STRING:
								// When you copy'n'paste on WIndows, it sometimes picks up the whitespace too -
								// so remove it.
								p[i].set(cfg,
										(cell != null ? drRow.getCell(Integer.parseInt(val)).getStringCellValue().trim()
												: ""));
								break;
							case NUMERIC:
								p[i].set(cfg, (cell != null ? drRow.getCell(Integer.parseInt(val)).getNumericCellValue()
										: ""));
								break;
							default:
								break;
						}
					} else {
						p[i].set(cfg, (p[i].getType().equals(String.class)) ? "" : 0.0);
					}

				} catch (IllegalArgumentException | IllegalAccessException e) {
					d.p(Debug.ERROR, "Conversion error on \"%s\": Verify cell type in Excel\n %s\n", idx,
							e.getMessage());
					System.exit(12);
				}

			}
			return true;
		}
		return false;
	}

	private static void getConfigFromFile() {
		// Make the contents of the file lower case before comparing with these.
		Field[] p = (new Configuration()).getClass().getDeclaredFields();

		ArrayList<String> cols = new ArrayList<String>();
		for (int i = 0; i < p.length; i++) {
			p[i].setAccessible(true); // Set this up for later
			cols.add(p[i].getName().toLowerCase());
		}
		HashMap<String, Object> fieldMap = new HashMap<>();

		// Assume that the titles are the first row
		Iterator<Row> ri = configSht.iterator();
		if (!ri.hasNext()) {
			d.p(Debug.ERROR, "%s", "Did not detect any header info on Config sheet (first row!)");
			System.exit(9);
		}
		Row hdr = ri.next();
		Iterator<Cell> cptr = hdr.cellIterator();

		while (cptr.hasNext()) {
			Cell cell = cptr.next();
			Integer idx = cell.getColumnIndex();
			String cellName = cell.getStringCellValue().trim().toLowerCase();
			if (cols.contains(cellName)) {
				fieldMap.put(cellName, idx); // Store the column index of the field
			}
		}

		if (fieldMap.size() != cols.size()) {
			d.p(Debug.ERROR, "%s", "Did not detect correct columns on Config sheet: " + cols.toString());
			System.exit(10);
		}

		if (!ri.hasNext()) {
			d.p(Debug.ERROR, "%s",
					"Did not detect any field info on Config sheet (first cell must be non-blank, e.g. url to a real host)");
			System.exit(11);
		}

		while (ri.hasNext() && ((config.source.getUrl() == null) || (config.destination.getUrl() == null))) {
			Row rtc = ri.next();
			if (rtc.getCell(0).getStringCellValue().equals("src")) {
				if (config.source.getUrl() == null) {
					parseRow(rtc, config.source, p, fieldMap, cols);
				}
			}
			if (rtc.getCell(0).getStringCellValue().equals("dst")) {
				if (config.destination.getUrl() == null) {
					parseRow(rtc, config.destination, p, fieldMap, cols);
				}
			}
		}

		// Creds are now found and set. If not, you're buggered.

		/**
		 * We can opt to use username/password or apikey.
		 **/
		if ((config.source.getUrl() != null) && setToExport) {
			if ((config.source.getApiKey() == null) || (config.source.getBoardName() == null)) {
				d.p(Debug.ERROR, "%s", "Did not detect enough source info: apikey required");
				System.exit(13);
			}
		}
		if ((config.destination.getUrl() != null) && setToImport) {
			if ((config.destination.getApiKey() == null) || (config.destination.getBoardName() == null)) {
				d.p(Debug.ERROR, "%s", "Did not detect enough destination info: apikey required");
				System.exit(13);
			}
		}

		return;
	}

	public static InternalConfig setConfig(InternalConfig config, Row row, HashMap<String, Integer> fieldMap) {

		if (config.nameExtension) {
			NumberFormat nf = DecimalFormat.getInstance();
			nf.setMaximumFractionDigits(0);
			switch (row.getCell(fieldMap.get(InternalConfig.DESTINATION_TID_COLUMN)).getCellType()) {
				case NUMERIC: {
					config.oldExtension = nf.format(
							row.getCell(fieldMap.get(InternalConfig.DESTINATION_TID_COLUMN)).getNumericCellValue());
					break;
				}
				default: {
					config.oldExtension = row.getCell(fieldMap.get(InternalConfig.DESTINATION_TID_COLUMN))
							.getStringCellValue();
				}
			}

			row.getCell(fieldMap.get(InternalConfig.DESTINATION_TID_COLUMN)).setCellValue(config.extension);
			XSSFFormulaEvaluator.evaluateAllFormulaCells(config.wb);
		}

		config.source = new AccessConfig(
				row.getCell(fieldMap.get(InternalConfig.SOURCE_URL_COLUMN)).getStringCellValue(),
				row.getCell(fieldMap.get(InternalConfig.SOURCE_BOARDNAME_COLUMN)).getStringCellValue(),
				row.getCell(fieldMap.get(InternalConfig.SOURCE_APIKEY_COLUMN)).getStringCellValue());
		config.destination = new AccessConfig(
				row.getCell(fieldMap.get(InternalConfig.DESTINATION_URL_COLUMN)).getStringCellValue(),
				row.getCell(fieldMap.get(InternalConfig.DESTINATION_BOARDNAME_COLUMN)).getStringCellValue(),
				row.getCell(fieldMap.get(InternalConfig.DESTINATION_APIKEY_COLUMN)).getStringCellValue());

		if (config.ignoreCards) {
			// Find if column "Import Ignore" exists
			Integer ignCol = XlUtils.findColumnFromSheet(config.wb.getSheet("Config"), ColNames.IGNORE_LIST);
			if (ignCol != null) {
				Cell cl = row.getCell(ignCol);
				if (cl != null) {
					String typesString = row.getCell(ignCol).getStringCellValue();
					// Does the cell have anything in it?
					if (typesString != null) {
						config.ignTypes = typesString.split(",");
						// Trim all whitespace that the user might have left in
						for (int i = 0; i < config.ignTypes.length; i++) {
							config.ignTypes[i] = config.ignTypes[i].trim();
						}
					}
				}
			}
		}
		return config;
	}

	private static HashMap<String, Integer> chkConfigFromFile() {
		// Make the contents of the file lower case before comparing with these.

		Field[] p = (new Access()).getClass().getDeclaredFields();

		// Use fields as the ones we need to have in the spreadsheet for both src and
		// dst
		ArrayList<String> cols = new ArrayList<String>();
		for (int i = 0; i < p.length; i++) {
			p[i].setAccessible(true); // Set this up for later
			cols.add("src" + p[i].getName());
			cols.add("dst" + p[i].getName());
		}
		cols.add(InternalConfig.DESTINATION_TID_COLUMN);

		HashMap<String, Integer> fieldMap = new HashMap<>();

		// Assume that the titles are the first row
		Iterator<Row> ri = configSht.iterator();
		if (!ri.hasNext()) {
			d.p(Debug.ERROR, "%s", "Did not detect any header info on Config sheet (first row!)");
			System.exit(9);
		}
		Row hdr = ri.next();
		Iterator<Cell> cptr = hdr.cellIterator();

		while (cptr.hasNext()) {
			Cell cell = cptr.next();
			Integer idx = cell.getColumnIndex();
			String cellName = cell.getStringCellValue().trim();
			if (cols.contains(cellName)) {
				fieldMap.put(cellName, idx); // Store the column index of the field
			}
		}

		if (fieldMap.size() != cols.size()) {
			d.p(Debug.ERROR, "%s", "Did not detect correct columns on Config sheet: " + cols.toString());
			System.exit(10);
		}

		if (!ri.hasNext()) {
			d.p(Debug.ERROR, "%s",
					"Did not detect any potential transfer info on Config sheet (first cell must be non-blank, e.g. url to a real host)");
			System.exit(11);
		}

		return fieldMap;
	}
}
