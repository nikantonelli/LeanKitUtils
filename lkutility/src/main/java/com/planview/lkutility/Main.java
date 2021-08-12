package com.planview.lkutility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.planview.lkutility.Exporter.Exporter;
import com.planview.lkutility.Importer.Importer;
import com.planview.lkutility.Transporter.Transporter;

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

    static Integer group = 0;
    static Boolean dualFlow = false;

    public static void main(String[] args) {
        d = new Debug();
        getCommandLine(args);

        checkXlsx();
        getConfigFromFile();
        config.cm.setMaxTotal(5); // Recommendation fron LeanKit

        if (setToExport == true) {
            if (dualFlow == false) {
                Exporter expt = new Exporter();
                expt.go(config, debugPrint);
            } else {
                Transporter trpt = new Transporter();
                trpt.go(config, debugPrint);
            }
        } else {
            Importer impt = new Importer();
            impt.go(config, debugPrint);
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
            impExpCl = p.parse(impExpOpt, args);

        } catch (ParseException e) {
            // Not expecting to ever come here, but compiler needs something....
            d.p(Debug.ERROR, "(3): %s\n", e.getMessage());
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
                dualFlow = true;
            }
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
            Option groupOpt = new Option("g", "group", true, "Identifier of group to process (if present)");
            groupOpt.setRequired(false);
            impOpts.addOption(groupOpt);

            CommandLine impCl = null;

            try {
                impCl = p.parse(impOpts, args);
            } catch (ParseException e) {
                d.p(Debug.ERROR, "(3): %s", e.getMessage());
                d.p(Debug.INFO, "Importer Options (with -i as first option)\n");
                hf.printHelp(" ", impOpts);
                System.exit(5);
            }
            if (impCl.hasOption("group")) {
                group = Integer.parseInt(impCl.getOptionValue("group"));
            }
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
                    String idx = cols.get(i);
                    Object obj = fieldMap.get(idx);
                    String val = obj.toString();
                    try {
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
                        d.p(Debug.ERROR, "(6) Conversion error on \"%s\": Verify cell type in Excel\n %s\n", idx, e.getMessage());
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
