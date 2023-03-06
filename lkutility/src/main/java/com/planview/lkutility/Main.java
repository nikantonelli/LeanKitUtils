package com.planview.lkutility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.planview.lkutility.Leankit.AccessCache;
import com.planview.lkutility.System.Access;
import com.planview.lkutility.System.AccessConfig;
import com.planview.lkutility.System.ColNames;
import com.planview.lkutility.System.Debug;
import com.planview.lkutility.System.InternalConfig;
import com.planview.lkutility.System.LMS;
import com.planview.lkutility.System.Languages;
import com.planview.lkutility.Utils.BoardArchiver;
import com.planview.lkutility.Utils.BoardCreator;
import com.planview.lkutility.Utils.BoardDeleter;
import com.planview.lkutility.Utils.CardDeleter;
import com.planview.lkutility.Utils.Diff;
import com.planview.lkutility.Utils.Exporter;
import com.planview.lkutility.Utils.Importer;
import com.planview.lkutility.Utils.XlUtils;

public class Main {
	static Debug d = null;

	/**
	 * This defaults to true so we behave as an exporter if it is not otherwise
	 * specified. If the command line contains a -i flag, this is set to false.
	 */

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
		// Must be here to set a default for config.msg (used later on)
		// Will set to English if user.language is not set or unknown
		config.msg = new Languages(System.getProperty("user.language"));

		d = new Debug();
		d.setMsgr(config.msg);
		getCommandLine(args);
		d.p(LMS.ALWAYS, config.msg.getMsg(LMS.START_PROGRAM), new Date());

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
						d.p(LMS.ERROR, config.msg.getMsg(LMS.REPLAY_SHEET_NOT_FOUND));
						System.exit(-1);
					}
				} else if (!setToDiff) {
					Boolean ok = true;
					if (config.exporter) {
						// 2 & 3 (Exporter does check for board)
						Exporter exp = new Exporter(config);
						exp.go();
					}

					// Now we need to check/reset the destination board if needed

					if ((config.deleteCards || config.deleteXlsx) && !config.remakeBoard) {
						CardDeleter cd = new CardDeleter(config);
						cd.go();
					}

					if (config.remakeBoard && !config.eraseBoard) {
						BoardArchiver ba = new BoardArchiver(config);
						ba.go();
					}

					if (config.eraseBoard) {
						BoardDeleter bd = new BoardDeleter(config);
						bd.go();
					}

					if ((config.remakeBoard) || (config.updateLayout)) {
						BoardCreator bd = new BoardCreator(config);
						ok = bd.go();
					}

					if (ok && config.importer) {
						Importer imp = new Importer(config);
						imp.go();
					}
				} else {
					if (setToDiff == true) {
						Diff diff = new Diff(config);
						diff.go();
					}
				}

			}
		}
		try {
			config.wb.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		d.p(LMS.ALWAYS, config.msg.getMsg(LMS.FINISH_PROGRAM), new Date());
	}

	public static void getCommandLine(String[] args) {

		CommandLineParser p = new DefaultParser();
		HelpFormatter hf = new HelpFormatter();
		CommandLine impExpCl = null;

		/**
		 * 
		 * OPTIONS LETTER IS CONSTANT ACROSS LANGUAGES!
		 * Helps with portability
		 * 
		 */
		Options cmdOpts = new Options();
		
		cmdOpts.addRequiredOption("f", config.msg.getMsg(LMS.FILE_OPTION), true, config.msg.getMsg(LMS.FILE_OPTION_MSG));
		//cmdOpts.addRequiredOption("f", "filename", true, "XLSX Spreadsheet (must contain API config!)");

		Option impO = new Option("i", config.msg.getMsg(LMS.IMPORT_OPTION), false,
				config.msg.getMsg(LMS.IMPORT_OPTION_MSG));
		//Option impO = new Option("i", "import", false, "run importer");
		impO.setRequired(false);
		cmdOpts.addOption(impO);
		
		Option expO = new Option("e", config.msg.getMsg(LMS.EXPORT_OPTION), false,
				config.msg.getMsg(LMS.EXPORT_OPTION_MSG));
		//Option expO = new Option("e", "export", false, "run exporter");
		expO.setRequired(false);
		cmdOpts.addOption(expO);
		
		Option diffO = new Option("c", config.msg.getMsg(LMS.COMPARE_OPTION), false,
				config.msg.getMsg(LMS.COMPARE_OPTION_MSG));
		diffO.setRequired(false);
		cmdOpts.addOption(diffO);
		
		Option repO = new Option("a", config.msg.getMsg(LMS.REPLAY_OPTION), false,
				config.msg.getMsg(LMS.REPLAY_OPTION_MSG));
		repO.setRequired(false);
		cmdOpts.addOption(repO);

		Option remakeOpt = new Option("r", config.msg.getMsg(LMS.REMAKE_OPTION), false,
				config.msg.getMsg(LMS.REMAKE_OPTION_MSG));
		//Option remakeOpt = new Option("r", "remake", false, "Remake target boards by archiving old and adding new");
		remakeOpt.setRequired(false);
		cmdOpts.addOption(remakeOpt);

		Option removeOpt = new Option("R", config.msg.getMsg(LMS.REMOVE_OPTION), false,
				config.msg.getMsg(LMS.REMOVE_OPTION_MSG));
		// Option removeOpt = new Option("R", "remove", false, "Remove target boards");
		removeOpt.setRequired(false);
		cmdOpts.addOption(removeOpt);

		Option deleteOpt = new Option("X", config.msg.getMsg(LMS.DELETE_X_OPTION), false,
				config.msg.getMsg(LMS.DELETE_X_OPTION_MSG));
		//Option deleteOpt = new Option("X", "xlsx", false, "Delete cards on target boards (from spreadsheet)");
		deleteOpt.setRequired(false);
		cmdOpts.addOption(deleteOpt);

		Option langOpt = new Option("L", config.msg.getMsg(LMS.LANGUAGE_OPTION), true,
				config.msg.getMsg(LMS.LANGUAGE_OPTION_MSG));
		//Option langOpt = new Option("L", "language", true, "Language, langue, sprache, idioma, etc.");
		langOpt.setRequired(false);
		cmdOpts.addOption(langOpt);

		Option eraseOpt = new Option("d", config.msg.getMsg(LMS.DELETE_OPTION), false,
				config.msg.getMsg(LMS.DELETE_OPTION_MSG));
		//Option eraseOpt = new Option("d", "delete", false, "Delete all cards on target boards");
		eraseOpt.setRequired(false);
		cmdOpts.addOption(eraseOpt);

		Option tasktopOpt = new Option("t", config.msg.getMsg(LMS.TASKTOP_OPTION), false,
				config.msg.getMsg(LMS.TASKTOP_OPTION_MSG));
		//Option tasktopOpt = new Option("t", "tasktop", false, "Follow External Links to delete remote artifacts");
		tasktopOpt.setRequired(false);
		cmdOpts.addOption(tasktopOpt);

		Option groupOpt = new Option("g", config.msg.getMsg(LMS.GROUP_OPTION), true,
				config.msg.getMsg(LMS.GROUP_OPTION_MSG));
		//Option groupOpt = new Option("g", "group", true, "Identifier of group to process (if present)");
		groupOpt.setRequired(false);
		cmdOpts.addOption(groupOpt);

		Option moveOpt = new Option("m", config.msg.getMsg(LMS.MOVE_OPTION), true,
				config.msg.getMsg(LMS.MOVE_OPTION_MSG));
		//Option moveOpt = new Option("m", "move", true, "Lane to modify unwanted cards with (for compare only)");
		moveOpt.setRequired(false);
		cmdOpts.addOption(moveOpt);

		Option dbp = new Option("x", config.msg.getMsg(LMS.DEBUG_OPTION), true,
				config.msg.getMsg(LMS.DEBUG_OPTION_MSG));
		//Option dbp = new Option("x", "debug", true, "Print out loads of helpful stuff: 0 - Error, 1 - And Warnings, 2 - And Info, 3 - And Debugging, 4 - And Network");
		dbp.setRequired(false);
		cmdOpts.addOption(dbp);

		Option archiveOpt = new Option("O", config.msg.getMsg(LMS.ARCHIVED_OPTION), false,
				config.msg.getMsg(LMS.ATTACHMENTS_OPTION_MSG));
		//Option archiveOpt = new Option("O", "archived", false, "Include older Archived cards in export (if present)");
		archiveOpt.setRequired(false);
		cmdOpts.addOption(archiveOpt);
		
		Option tasksOpt = new Option("T", config.msg.getMsg(LMS.TASKS_OPTION), false,
				config.msg.getMsg(LMS.TASKS_OPTION_MSG));
		//Option tasksOpt = new Option("T", "tasks", false, "Include Task cards in export (if present)");
		tasksOpt.setRequired(false);
		cmdOpts.addOption(tasksOpt);

		Option attsOpt = new Option("A", config.msg.getMsg(LMS.ATTACHMENTS_OPTION), false,
				config.msg.getMsg(LMS.ATTACHMENTS_OPTION_MSG));
		//Option attsOpt = new Option("A", "attachments", false, "Export card attachments in local filesystem (if present)");
		attsOpt.setRequired(false);
		cmdOpts.addOption(attsOpt);

		Option comsOpt = new Option("C", config.msg.getMsg(LMS.COMMENTS_OPTION), false,
				config.msg.getMsg(LMS.COMMENTS_OPTION_MSG));
		//Option comsOpt = new Option("C", "comments", false, "Export card comments in local filesystem (if present)");
		comsOpt.setRequired(false);
		cmdOpts.addOption(comsOpt);

		Option originOpt = new Option("S", config.msg.getMsg(LMS.ORIGIN_OPTION), false,
				config.msg.getMsg(LMS.ORIGIN_OPTION_MSG));
		//Option originOpt = new Option("S", "origin", false, "Add comment for source artifact recording");
		originOpt.setRequired(false);
		cmdOpts.addOption(originOpt);

		Option readOnlyOpt = new Option("P", config.msg.getMsg(LMS.RO_OPTION), false,
				config.msg.getMsg(LMS.RO_OPTION_MSG));
		//Option readOnlyOpt = new Option("P", "ro", false, "Export Read Only fields (Not Imported!)");
		readOnlyOpt.setRequired(false);
		cmdOpts.addOption(readOnlyOpt);

		try {
			impExpCl = p.parse(cmdOpts, args, true);

		} catch (ParseException e) {
			// Not expecting to ever come here, but compiler needs something....
			d.p(LMS.ERROR, config.msg.getMsg(LMS.COMMANDLINE_ERROR), e.getMessage());
			hf.printHelp(" ", cmdOpts);
			System.exit(-2);
		}

		if (impExpCl.hasOption("ro")) {
			config.roFieldExport = true;
		}

		if (impExpCl.hasOption("replay")) {
			config.replay = true;
		}

		if (impExpCl.hasOption("xlsx")) {
			config.deleteXlsx = true;
		}

		if (impExpCl.hasOption("language")) {
			String optVal = impExpCl.getOptionValue("language");
			if (optVal != null) {
				d.p(LMS.ALWAYS, config.msg.getMsg(LMS.SETTING_LANGUAGE), optVal);
				config.msg = new Languages(optVal);
				d.setMsgr(config.msg);
			}
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
		 * If we are doing a compare, we don't want to think about import/export via the
		 * normal route. We need to export the board again into a temporary sheet and
		 * then
		 * scan it for differences between it and the original.
		 * We then can use the compare to create a 'reset' changes sheet. Any cards in
		 * excess of
		 * those in the original can be moved to the archive lane
		 */
		if (impExpCl.hasOption("compare")) {
			d.p(LMS.INFO, config.msg.getMsg(LMS.SET_COMPARE_MODE));
			config.diffMode = impExpCl.getOptionValue("compare");
			setToDiff = true;
			config.exporter = false;
			if (impExpCl.hasOption("move")) {
				config.archive = impExpCl.getOptionValue("move");
			}
		} else {
			if (impExpCl.hasOption("replay")) {
				if (impExpCl.hasOption("export") || impExpCl.hasOption("import")) {
					d.p(LMS.INFO, config.msg.getMsg(LMS.INVALID_OPTIONS));
				} else {
					d.p(LMS.INFO, config.msg.getMsg(LMS.SET_REPLAY_MODE));
				}
				config.replay = true;
			}
			/**
			 * Import takes precedence if option present, then export, then transfer
			 */
			else {
				if (impExpCl.hasOption("import"))
					config.importer = true;
				if (impExpCl.hasOption("export"))
					config.exporter = true;
				if (impExpCl.hasOption("delete"))
					config.deleteCards = true;
				if (impExpCl.hasOption(config.msg.getMsg(LMS.REMAKE_OPTION)))
					config.remakeBoard = true;
				if (impExpCl.hasOption("layout"))
					config.updateLayout = true;
				if (impExpCl.hasOption(config.msg.getMsg(LMS.REMOVE_OPTION)))
					config.eraseBoard = true;
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
			d.p(LMS.ERROR, "(-3) %s", e.getMessage());

			System.exit(-3);

		}
		try {
			config.wb = new XSSFWorkbook(xlsxfis);
			xlsxfis.close();
		} catch (IOException e) {
			d.p(LMS.ERROR, "(-4) %s", e.getMessage());
			System.exit(-4);
		}

		configSht = config.wb.getSheet("Config");
		if (configSht == null) {
			d.p(LMS.ERROR, "(-5) Did not detect required sheet in the spreadsheet: \"Config\"");
			System.exit(-5);
		}
	}

	public static InternalConfig setConfig(InternalConfig config, Row row, HashMap<String, Integer> fieldMap) {

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
			d.p(LMS.ERROR, "%s", "Did not detect any header info on Config sheet (first row!)");
			System.exit(-6);
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
			d.p(LMS.ERROR, "%s", "Did not detect correct columns on Config sheet: " + cols.toString());
			System.exit(-7);
		}

		if (!ri.hasNext()) {
			d.p(LMS.ERROR, "%s",
					"Did not detect any potential transfer info on Config sheet (first cell must be non-blank, e.g. url to a real host)");
			System.exit(-8);
		}

		return fieldMap;
	}
}
