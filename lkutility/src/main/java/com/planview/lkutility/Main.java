package com.planview.lkutility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.planview.lkutility.exporter.Exporter;
import com.planview.lkutility.importer.Importer;
import com.planview.lkutility.transporter.Transporter;

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
            if (config.dualFlow == true) {
                Importer impt = new Importer();
                impt.go(config);
            }
        } else {
            Importer impt = new Importer();
            impt.go(config);
        }
        try {
            config.wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getCommandLine(String[] args) {

        CommandLineParser p = new DefaultParser();
        HelpFormatter hf = new HelpFormatter();
        CommandLine impExpCl = null;

        Options impExpOpt = new Options();
        Option impO = new Option("i", "import", false, "run importer");
        Option expO = new Option("e", "export", false, "run exporter");
        Option tnsO = new Option("t", "transfer", false, "run transfer");
        impO.setRequired(false);
        expO.setRequired(false);
        tnsO.setRequired(false);
        impExpOpt.addOption(impO);
        impExpOpt.addOption(expO);
        impExpOpt.addOption(tnsO);
        impExpOpt.addRequiredOption("f", "filename", true, "XLSX Spreadsheet (must contain API config!)");

        Option dbp = new Option("x", "debug", true,
                "Print out loads of helpful stuff: 0 - Info, 1 - And Errors, 2 - And Warnings, 3 - And Debugging, 4 - Verbose");
        dbp.setRequired(false);
        impExpOpt.addOption(dbp);

        try {
            impExpCl = p.parse(impExpOpt, args, true);

        } catch (ParseException e) {
            // Not expecting to ever come here, but compiler needs something....
            d.p(Debug.ERROR, "(13): %s\n", e.getMessage());
            hf.printHelp(" ", impExpOpt);
            System.exit(5);
        }

        /**
         * Import takes precedence if option present, then export, then transfer
         */
        if (impExpCl.hasOption("import")) {
            d.p(Debug.INFO, "Setting to Import mode\n");
            setToExport = false;
        } else if (!impExpCl.hasOption("export")) {
            if (!impExpCl.hasOption("transfer")) {
                d.p(Debug.INFO, "Defaulting to Export mode\n");
            } else {
                d.p(Debug.INFO, "Setting to Dual mode\n");
                config.dualFlow = true;
            }
        }

        config.xlsxfn = impExpCl.getOptionValue("filename");

        if (impExpCl.hasOption("debug")) {
            String optVal = impExpCl.getOptionValue("debug");
            if (optVal != null) {
                config.debugLevel = Integer.parseInt(optVal);
            } else {
                config.debugLevel = 99;
            }
        }

        // We now need to check for all the other unique options

        if (config.dualFlow == false) {
            Option groupOpt = new Option("g", "group", true, "Identifier of group to process (if present)");
            groupOpt.setRequired(false);
            impExpOpt.addOption(groupOpt);

            CommandLine cl = null;

            if (setToExport == true) {
                Option archiveOpt = new Option("oo", "archived", false,
                        "Include older Archived cards in export (if present)");
                archiveOpt.setRequired(false);
                impExpOpt.addOption(archiveOpt);
                Option tasksOpt = new Option("ot", "tasks", false, "Include Task cards in export (if present)");
                tasksOpt.setRequired(false);
                impExpOpt.addOption(tasksOpt);
                Option attsOpt = new Option("oa", "attachments", false,
                        "Export card attachments in local filesystem (if present)");
                attsOpt.setRequired(false);
                impExpOpt.addOption(attsOpt);
                Option comsOpt = new Option("oc", "comments", false,
                        "Export card comments in local filesystem (if present)");
                comsOpt.setRequired(false);
                impExpOpt.addOption(comsOpt);
                Option originOpt = new Option("os", "origin", false, "Add comment for source artifact recording");
                originOpt.setRequired(false);
                impExpOpt.addOption(originOpt);
            }
            try {
                cl = p.parse(impExpOpt, args);
            } catch (ParseException e) {
                d.p(Debug.ERROR, "(23): %s", e.getMessage());
                d.p(Debug.INFO, "Exporter Options (with -e as first option)\n");
                hf.printHelp(" ", impExpOpt);
                System.exit(5);
            }

            if (cl.hasOption("group")) {
                config.group = Integer.parseInt(cl.getOptionValue("group"));
            }
            if (cl.hasOption("archived")) {
                config.exportArchived = true;
            }
            if (cl.hasOption("tasks")) {
                config.exportTasks = true;
            }
            if (cl.hasOption("comments")) {
                config.exportComments = true;
            }
            if (cl.hasOption("attachments")) {
                config.exportAttachments = true;
            }
            if (cl.hasOption("origin")) {
                config.addComment = true;
            }

        } else {
            // TODO: Set transfer opts here
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
                                p[i].set(cfg, (cell != null ? drRow.getCell(Integer.parseInt(val)).getStringCellValue()
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
         * We can opt to use username/password or apikey. Unfortunately, we have to hard
         * code the field names in here, even though I was trying to use the fields from
         * the Configuration class.
         **/
        if ((config.source.url != null) && (setToExport || config.dualFlow)){
            if (((config.source.apiKey == null) || (config.source.boardId == null))
                    && ((config.source.username == null) || (config.source.password == null))) {
                d.p(Debug.ERROR, "%s", "Did not detect enough source info: apikey or username/password pair");
                System.exit(13);
            }
        }
        if ((config.destination.url != null) && ((!setToExport) || config.dualFlow)){
            if (((config.destination.apiKey == null) || (config.destination.boardId == null))
                    && ((config.destination.username == null) || (config.destination.password == null))) {
                d.p(Debug.ERROR, "%s", "Did not detect enough destination info: apikey or username/password pair");
                System.exit(13);
            }
        }

        return;
    }
}
