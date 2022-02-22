package com.planview.lkutility.diff;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;

import com.planview.lkutility.Debug;
import com.planview.lkutility.InternalConfig;
import com.planview.lkutility.Utils;
import com.planview.lkutility.exporter.Exporter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class Diff {
    Debug d = new Debug();
    InternalConfig cfg = null;

    public Diff(InternalConfig config) {
        cfg = config;
    }

    public void go() {
        d.setLevel(cfg.debugLevel);
        /**
         * Check to see if correct sheet for the dst exists. If so, rename the existing
         * sheet something temporary,
         * create a new one from the dst URL, and do the one to one compare. This needs
         * to create a 'diff' sheet
         * that would 'reset' the dst.
         * 
         * If no dst sheet exists, try to get a src sheet to compare the dst to. If that
         * doesn't exists,
         * get the src from the URL and then try.
         * Fetch all the dst URL info to compare the chosen first item to
         * 
         * Option First Item Second Item
         * ====== ========== ===========
         * ..1....dst sheet..dst URL
         * ..2....src sheet..dst URL
         * ..3....src URL....dst URL
         */
        Integer firstShtIdx = cfg.wb.getSheetIndex(cfg.source.boardId); // First item sheets go in here
        Integer firstChgSht = cfg.wb.getSheetIndex(InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.source.boardId);
        Integer secondShtIdx = null; // second item sheets go in here
        Integer secondChgSht = null;

        Boolean found = false;

        String dateNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));

        if ((firstShtIdx  > -1) && (firstChgSht  > -1)) {
            found = true;
        }
        if (!found) {
            d.p(Debug.ERROR, "diff option 2: incorrect sheets found for src board: %s\n", cfg.source.boardId);
        }

        if ((firstChgSht == null) || (firstShtIdx == null)) {
            d.p(Debug.ERROR, " Cannot locate required data to compare\n");
            System.exit(0);
        }

        found = false;
        // For all cases, we should be set up to move the dst sheet away if present
        if ((secondShtIdx = cfg.wb.getSheetIndex(cfg.destination.boardId)) > -1) {
            cfg.wb.setSheetName(secondShtIdx, "orig_" + cfg.destination.boardId + dateNow);
            if ((secondChgSht = cfg.wb.getSheetIndex(
                    InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.destination.boardId)) > -1) {
                cfg.wb.setSheetName(secondChgSht,
                        "orig_" + InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.destination.boardId + dateNow);
                found = true;
            }
        }
        if (found) {
            d.p(Debug.INFO, "Saved sheets found for dst board: %s (to fetch new data)\n", cfg.destination.boardId);
        }

        /**
         * Create new "Reset" sheet of changes that can put stuff back to where it
         * should be.
         * Any cards that are extra in the destination can be moved to the default
         * droplane or
         * to the lane specified by the -m option
         */
        Integer resetSht = -1;
        if ((resetSht = cfg.wb.getSheetIndex("reset_" + cfg.destination.boardId)) > -1) {
            cfg.wb.removeSheetAt(resetSht);
        }

        cfg.resetSheet = cfg.wb.createSheet("reset_" + cfg.destination.boardId);
        int chgCellIdx = 0;
        Row chgHdrRow = cfg.resetSheet.createRow(chgCellIdx);
         // These next lines are the fixed format of the Changes sheet
         chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Group");
         chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Item Row");
         chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Action");
         chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Field");
         chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Value");;
 

        // Now create a config to pass to the exporter so that we get a new destination
        // board data set. This is to maintain the standard exporter that only uses
        // the source cfg record

        InternalConfig icfg = new InternalConfig();
        icfg.debugLevel = cfg.debugLevel;
        icfg.wb = cfg.wb;
        icfg.source = cfg.destination;
        icfg.xlsxfn = cfg.xlsxfn;

        Exporter iExpt = new Exporter(icfg);

        // Fire off exporter to get second item
        iExpt.doExport(iExpt.setUpNewSheets(iExpt.getSheetName())); // Do not use go() as it cleans out stuff.

        /**
         * We should now have two set of sheets to compare: first item, second item
         */
        found = false;
        if ((secondShtIdx = cfg.wb.getSheetIndex(cfg.destination.boardId)) > -1) {
            String sheetName = cfg.destination.boardId + "_" + dateNow;
            cfg.wb.setSheetName(secondShtIdx, sheetName);
            if ((secondChgSht = cfg.wb.getSheetIndex(
                    InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.destination.boardId)) > -1) {
                cfg.wb.setSheetName(secondChgSht, InternalConfig.CHANGES_SHEET_NAME + "_" + sheetName);
                found = true;
            }
        }
        if (!found) {
            d.p(Debug.ERROR, "Oops! fetch of new data for board: %s failed\n", cfg.destination.boardId);
            // Don't need to undo anything as we haven't written the file out yet.
            System.exit(0);
        }

        /**
         * The previous "transfer" should have created IDs in the firstSht
         * We scan down the srdID of the secondItmSht and check whether they are in the
         * firstItmSht under the ID column
         * If not, we either move to the -m location or to the default droplane.
         * If they are, we check whether any of the data has changed.
         * The column titles MUST match or we throw a wobbly.
         */

        HashMap<String, Integer> missing = new HashMap<String, Integer>();
        HashMap<String, Integer> extras = new HashMap<String, Integer>();
        HashMap<String, Integer> common = new HashMap<String, Integer>();
        XSSFSheet firstSht = cfg.wb.getSheetAt(firstShtIdx);
        XSSFSheet secondSht = cfg.wb.getSheetAt(secondShtIdx);
        Iterator<Row> firstShtRow = firstSht.iterator();
        Iterator<Row> secondShtRow = secondSht.iterator();

        /**
         * Parse the header rows individually so we can match if someone has modified
         * the spreadsheet manually
         */

        HashMap<String, Integer> firstCols = new HashMap<String, Integer>();
        HashMap<String, Integer> secondCols = new HashMap<String, Integer>();

        Row firstHdrRow = firstShtRow.next(); // then move to after title row
        Row secondHdrRow = secondShtRow.next(); // then move to after title row

        Iterator<Cell> fhrCell = firstHdrRow.iterator();
        Iterator<Cell> shrCell = secondHdrRow.iterator();

        while (fhrCell.hasNext()) {
            Cell cl = fhrCell.next();
            firstCols.put(cl.getStringCellValue(), cl.getColumnIndex());
        }

        while (shrCell.hasNext()) {
            Cell cl = shrCell.next();
            secondCols.put(cl.getStringCellValue(), cl.getColumnIndex());
        }

        /**
         * First get all the IDs in the firstItmSht so that we can delete them as we
         * find them. If we get to the end and find that we have any left over, these
         * mean that they must have been deleted in the destination - do we recreate?
         * missing: in the first sheet but not in the destination
         * extras: in the destination and not in the source
         * common: in both ends, so check for diffs
         */

        while (firstShtRow.hasNext()) {
            Row tr = firstShtRow.next();
            missing.put(tr.getCell(firstCols.get("ID")).getStringCellValue(), tr.getRowNum() + 1);
        }
        while (secondShtRow.hasNext()) {
            Row tr = secondShtRow.next();
            missing.computeIfAbsent(tr.getCell(secondCols.get("srcID")).getStringCellValue(), s -> {
                extras.put(s, tr.getRowNum() + 1);
                return null;
            });
            missing.computeIfPresent(tr.getCell(secondCols.get("srcID")).getStringCellValue(), (s, i) -> {
                common.put(s, i);
                return null;
            });
        }

        extras.forEach((itm, idx) -> {

            // Create a 'Modify' row to move to the default drop or the defined -m value
            String lane = "";
            if (cfg.archive != null) {
                lane = cfg.archive;
            }
            Row dr = cfg.resetSheet.createRow(cfg.resetSheet.getLastRowNum()+1);
            Integer localCellIdx = 0;
            dr.createCell(localCellIdx++, CellType.STRING).setCellValue(cfg.group);
            dr.createCell(localCellIdx++, CellType.FORMULA)
                    .setCellFormula("'" + secondSht.getSheetName() + "'!B" + idx);
            dr.createCell(localCellIdx++, CellType.STRING).setCellValue("Modify"); // "Action"
            dr.createCell(localCellIdx++, CellType.STRING).setCellValue("lane");
            dr.createCell(localCellIdx++, CellType.STRING).setCellValue(lane);
        });

        missing.forEach((itm,idx) -> {
            Row sr = cfg.wb.getSheetAt(firstChgSht).getRow(idx-1);
            Iterator<Cell> srci = sr.iterator();
            Row dr = cfg.resetSheet.createRow(cfg.resetSheet.getLastRowNum()+1);
            int cellIdx = 0;
            while (srci.hasNext()){
                Cell sc = srci.next();
                Cell dc = dr.createCell(cellIdx++, sc.getCellType());
                
                switch (sc.getCellType()){
                    case STRING: {
                        dc.setCellValue(sc.getStringCellValue());
                        break;
                    }
                    case NUMERIC: {
                        dc.setCellValue(sc.getNumericCellValue());
                        break;
                    }
                    case FORMULA: {
                        dc.setCellFormula(sc.getCellFormula());
                        break;
                    }
                    default: {
                        dc.setCellValue("");
                        break;
                    }
                }
            }
            XSSFSheet itmSht = cfg.wb.getSheetAt(firstShtIdx);
            sr = itmSht.getRow(idx-1);
            Cell srcCell = sr.getCell(Utils.findColumnFromSheet(itmSht, "ID"));
            srcCell.setCellValue("");
        });
     
        /**
         * Open the output stream and send the file back out.
         */
        Utils.writeFile(cfg, cfg.xlsxfn, cfg.wb);
    }
}
