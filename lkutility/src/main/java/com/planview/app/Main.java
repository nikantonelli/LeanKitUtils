package com.planview.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.planview.exporter.Exporter;

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
    static Integer debugPrint = -1;
    static Debug d = null;

    /**
     * This defaults to true so we behave as an exporter if it is not otherwise
     * specified. If the command line contains a -i flag, this is set to false.
     */
    static Boolean setToExport = true;

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

    static Boolean useCron = false;
    static String statusFile = "";
    static String moveLane = null;
    static String flagMove = "";
    static String deleteItems = "";
    static Integer flagDelete = -1;
    static Integer updatePeriod = 60 * 60 * 24;
    static Boolean useUpdatePeriod = false;
    static Integer startDay = -1;
    static Boolean cycleOnce = false;

    public static void main(String[] args) {
        d = new Debug();
        getCommandLine(args);

        getConfigFromFile();

        if (setToExport == true) {
            Exporter expt = new Exporter();
            expt.go(config, debugPrint);
        }

    }

    public static void getCommandLine(String[] args) {

        CommandLineParser p = new DefaultParser();
        HelpFormatter hf = new HelpFormatter();
        CommandLine impExpCl = null;

        Options impExpOpt = new Options();
        Option impO = new Option("i", "import", false, "run importer");
        Option expO = new Option("e", "export", false, "run exporter");
        //Option tnsO = new Option("t", "transfer", false, "run transfer");
        impO.setRequired(false);
        expO.setRequired(false);
        impExpOpt.addOption(impO);
        impExpOpt.addOption(expO);
        impExpOpt.addRequiredOption("f", "filename", true, "XLSX Spreadsheet (must contain API config!)");

        Option dbp = new Option("x", "debug", true,
                "Print out loads of helpful stuff: 0 - Info, 1 - And Errors, 2 - And Warnings, 3 - And Debugging, 4 - Verbose");
        dbp.setRequired(false);
        impExpOpt.addOption(dbp);

        try {
            impExpCl = p.parse(impExpOpt, args);

        } catch (ParseException e) {
            // Not expecting to ever come here, but compiler needs something....
            d.p(Debug.ERROR, "(3): %s", e.getMessage());
            hf.printHelp(" ", impExpOpt);
            System.exit(5);
        }

        if (impExpCl.hasOption("import")) {
            setToExport = false;
        } else if (!impExpCl.hasOption("export")) {
            d.p(Debug.INFO, "Defaulting to Export mode\n");
        }

        config.xlsxfn = impExpCl.getOptionValue("filename");

        if (impExpCl.hasOption("debug")) {
            String optVal = impExpCl.getOptionValue("debug");
            if (optVal != null) {
                debugPrint = Integer.parseInt(optVal);
            } else {
                debugPrint = 99;
            }
        }

        // We now need to check for all the other unique options
        Options impOpts = new Options();

        if (setToExport == false) {
            Option cronIt = new Option("c", "cron", false, "Program is called by cron job");
            cronIt.setRequired(false);
            impOpts.addOption(cronIt);
            Option statusFn = new Option("s", "status", true, "Status file used when called by cron job");
            statusFn.setRequired(false);
            impOpts.addOption(statusFn);
            Option updateRate = new Option("u", "update", true,
                    "Rate to process updates (in seconds). Defaults to 1 day");
            updateRate.setRequired(false);
            impOpts.addOption(updateRate);
            Option beginDay = new Option("b", "begin", true, "Day to begin updates. Defaults to day 0");
            beginDay.setRequired(false);
            impOpts.addOption(beginDay);
            Option doOnce = new Option("o", "once", false, "Run updates from Day 0 to end only once (do not loop)");
            doOnce.setRequired(false);
            impOpts.addOption(doOnce);
            Option deleteCycle = new Option("d", "delete", true,
                    "Delete all artifacts on start of day or end of cycle");
            deleteCycle.setRequired(false);
            impOpts.addOption(deleteCycle);

            Option moveCycle = new Option("m", "move", true, "Move all artifacts on end of cycle to this lane");
            moveCycle.setRequired(false);
            impOpts.addOption(moveCycle);

            CommandLine impCl = null;

            try {
                impCl = p.parse(impOpts, args);
            } catch (ParseException e) {
                d.p(Debug.ERROR, "(3): %s", e.getMessage());
                d.p(Debug.INFO, "Importer Options (use -i as first option)\n");
                hf.printHelp(" ", impOpts);
                System.exit(5);
            }
            useCron = impCl.hasOption("cron");
            if (impCl.hasOption("status")) {
                statusFile = impCl.getOptionValue("status");
            }

            if (impCl.hasOption("update")) {
                useCron = false;
                useUpdatePeriod = true;
                updatePeriod = Integer.parseInt(impCl.getOptionValue("update"));
            }

            if (impCl.hasOption("begin")) {
                startDay = Integer.parseInt(impCl.getOptionValue("begin"));
            }

            // Move items to a lane and then delete them if needed - or just delete.
            if (impCl.hasOption("move")) {
                moveLane = impCl.getOptionValue("move");
            }

            if (impCl.hasOption("once")) {
                cycleOnce = true;
            }

            if (impCl.hasOption("delete")) {
                deleteItems = impCl.getOptionValue("delete");
            }
        }
    }

    /**
     * Check if the XLSX file provided has the correct sheets and we can parse the
     * details we need
     */
    public void parseXlsx() {
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

        // These two should come first in the file and must be present

        configSht = config.wb.getSheet("Config");
        if (configSht == null) {
            d.p(Debug.ERROR, "%s", "Did not detect required sheet in the spreadsheet: \"Config\"");
            System.exit(8);
        }

        /**
         * Check Config sheet has the correct columns and data in them
         */
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
        // Now we know which columns contain the data, scan down the sheet looking for a
        // row with data in the 'url' cell
        while (ri.hasNext()) {
            Row drRow = ri.next();
            String cv = drRow.getCell((int) (fieldMap.get(cols.get(0)))).getStringCellValue();
            if (cv != null) {

                for (int i = 0; i < cols.size(); i++) {

                    try {
                        String idx = cols.get(i);
                        Object obj = fieldMap.get(idx);
                        String val = obj.toString();
                        Cell cell = drRow.getCell(Integer.parseInt(val));

                        if (cell != null) {
                            switch (cell.getCellType()) {
                                case STRING:
                                    p[i].set(config,
                                            (cell != null ? drRow.getCell(Integer.parseInt(val)).getStringCellValue()
                                                    : ""));
                                    break;
                                case NUMERIC:
                                    p[i].set(config,
                                            (cell != null ? drRow.getCell(Integer.parseInt(val)).getNumericCellValue()
                                                    : ""));
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            p[i].set(config, (p[i].getType().equals(String.class)) ? "" : 0.0);
                        }

                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        d.p(Debug.ERROR, "(6) %s", e.getMessage());
                        System.exit(12);
                    }

                }

                break; // Exit out of while loop as we are only interested in the first one we find
            }
        }

        // Creds are now found and set. If not, you're buggered.
        /**
         * We can opt to use username/password or apikey. Unfortunately, we have to hard
         * code the field names in here, even though I was trying to use the fields from
         * the Configuration class.
         **/

        if ((config.apiKey == null) && ((config.username == null) || (config.password == null))) {
            d.p(Debug.ERROR, "%s", "Did not detect enough user info: apikey or username/password pair");
            System.exit(13);
        }

        return;
    }
}
