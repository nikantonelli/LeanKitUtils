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

import com.planview.lkutility.exporter.Exporter;
import com.planview.lkutility.importer.Importer;

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
        getConfigFromFile();
        config.cm.setMaxTotal(5); // Recommendation fron LeanKit

        if (setToExport == true) {
            Exporter expt = new Exporter();
            expt.go(config);
            if (setToImport == true) {
                Importer impt = new Importer();
                impt.go(config);
            }
        } else if (setToImport == true) {
            Importer impt = new Importer();
            impt.go(config);
        } else {

        }
        try {
            config.wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        d.p(Debug.INFO, "Finished at: %s\n", new Date());
    }

    public static void getCommandLine(String[] args) {

        CommandLineParser p = new DefaultParser();
        HelpFormatter hf = new HelpFormatter();
        CommandLine impExpCl = null;

        Options impExpOpt = new Options();
        Option impO = new Option("i", "import", false, "run importer");
        Option expO = new Option("e", "export", false, "run exporter");
        Option tnsO = new Option("t", "transfer", false, "run transfer");
        Option diffO = new Option("d", "diff", true, "compare to previous transfer: (s)rc or (d)est");

        diffO.setRequired(false);
        impO.setRequired(false);
        expO.setRequired(false);
        tnsO.setRequired(false);
        impExpOpt.addOption(impO);
        impExpOpt.addOption(expO);
        impExpOpt.addOption(tnsO);
        impExpOpt.addOption(diffO);
        impExpOpt.addRequiredOption("f", "filename", true, "XLSX Spreadsheet (must contain API config!)");

        Option groupOpt = new Option("g", "group", true, "Identifier of group to process (if present)");
        groupOpt.setRequired(false);
        impExpOpt.addOption(groupOpt);

        Option moveOpt = new Option("m", "move", true, "Lane to modify unwanted cards with (for diff only)");
        moveOpt.setRequired(false);
        impExpOpt.addOption(moveOpt);

        Option dbp = new Option("x", "debug", true,
                "Print out loads of helpful stuff: 0 - Error, 1 - And Info, 2 - And Warnings, 3 - And Debugging, 4 - Verbose");
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
         * normal route. We need to export the board again into a temporary sheet and then
         * scan it for differences between it and the original.
         * We then can use the diff to create a 'reset' changes sheet. Any cards in excess of
         * those in the original can be moved to the archive lane
         */
        if (impExpCl.hasOption("diff")) {
            d.p(Debug.INFO, "Setting to diff mode.\n");
            setToDiff = true;
            setToExport = false;
            if (impExpCl.hasOption("move")) {
                config.archive = impExpCl.getOptionValue("move");
            }
            System.exit(0);
        } else {
            /**
             * Import takes precedence if option present, then export, then transfer
             */
            if (impExpCl.hasOption("import")) {
                if (impExpCl.hasOption("transfer") || impExpCl.hasOption("export")){
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

    private static Boolean parseRow(Row drRow, Configuration cfg, Field[] p, HashMap<String, Object> fieldMap,
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

        while (ri.hasNext() && ((config.source.url == null) || (config.destination.url == null))) {
            Row rtc = ri.next();
            if (rtc.getCell(0).getStringCellValue().equals("src")) {
                if (config.source.url == null) {
                    parseRow(rtc, config.source, p, fieldMap, cols);
                }
            }
            if (rtc.getCell(0).getStringCellValue().equals("dst")) {
                if (config.destination.url == null) {
                    parseRow(rtc, config.destination, p, fieldMap, cols);
                }
            }
        }

        // Creds are now found and set. If not, you're buggered.

        /**
         * We can opt to use username/password or apikey.
         **/
        if ((config.source.url != null) && setToExport) {
            if (((config.source.apiKey == null) || (config.source.boardId == null))
                    && ((config.source.username == null) || (config.source.password == null))) {
                d.p(Debug.ERROR, "%s", "Did not detect enough source info: apikey or username/password pair");
                System.exit(13);
            }
        }
        if ((config.destination.url != null) && setToImport) {
            if (((config.destination.apiKey == null) || (config.destination.boardId == null))
                    && ((config.destination.username == null) || (config.destination.password == null))) {
                d.p(Debug.ERROR, "%s", "Did not detect enough destination info: apikey or username/password pair");
                System.exit(13);
            }
        }

        return;
    }
}
