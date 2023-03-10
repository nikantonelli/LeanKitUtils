package com.planview.lkutility.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.planview.lkutility.System.ColNames;
import com.planview.lkutility.System.Debug;
import com.planview.lkutility.System.InternalConfig;
import com.planview.lkutility.System.LMS;

public class Diff {
    Debug d = new Debug();
    InternalConfig cfg = null;

    public Diff(InternalConfig config) {
        cfg = config;
        d.setLevel(cfg.debugLevel);
		d.setMsgr(cfg.msgr);
    }

    public void go() {

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
        d.p(LMS.ALWAYS, "Starting diff at: %s\n", new Date());
        
        Integer firstShtIdx = cfg.wb.getSheetIndex(cfg.source.getBoardName()); // First item sheets go in here
        Integer firstChgIdx = cfg.wb.getSheetIndex(InternalConfig.CHANGES_SHEET_NAME + cfg.source.getBoardName());
        Integer secondShtIdx = null; // second item sheets go in here
        Integer secondChgIdx = null;

        Boolean found = false;

        String dateNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss")); // Sheet names can only
                                                                                                // be 31 chars.

        if ((firstShtIdx > -1) && (firstChgIdx > -1)) {
            found = true;
        }
        if (!found) {
            d.p(LMS.ERROR, "(-20) %s", "diff: incorrect sheets found for src board: %s\n", cfg.source.getBoardName());
			System.exit(-20);
        }

        if ((firstChgIdx == null) || (firstShtIdx == null)) {
            d.p(LMS.ERROR, " Cannot locate required data to compare\n");
            System.exit(-21);
        }

        found = false;
        // For all transfer cases, we should be set up to move the dst sheet away if
        // present
        Integer saveShtIdx = -1;
        Integer saveCfgIdx = -1;
        if ((saveShtIdx = cfg.wb.getSheetIndex(cfg.destination.getBoardName())) > -1) {
            cfg.wb.setSheetName(saveShtIdx, cfg.destination.getBoardName() + "_save");
            if ((saveCfgIdx = cfg.wb.getSheetIndex(
                    InternalConfig.CHANGES_SHEET_NAME + cfg.destination.getBoardName())) > -1) {
                cfg.wb.setSheetName(saveCfgIdx,
                        InternalConfig.CHANGES_SHEET_NAME + cfg.destination.getBoardName() + "_save");
                found = true;
            }
        }
        if (found) {
            d.p(LMS.INFO, "Saved sheets found for dst board: %s (to fetch new data)\n", cfg.destination.getBoardName());
        }

        /**
         * Create new "Reset" sheet of changes that can put stuff back to where it
         * should be.
         * Any cards that are extra in the destination can be moved to the default
         * droplane or
         * to the lane specified by the -m option
         */
        Integer replaySht = -1;
        if ((replaySht = cfg.wb.getSheetIndex("replay_" + cfg.destination.getBoardName())) > -1) {
            cfg.wb.removeSheetAt(replaySht);
        }

        cfg.replaySheet = XlUtils.newChgSheet(cfg, "replay_" + cfg.destination.getBoardName());

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
        if ((secondShtIdx = cfg.wb.getSheetIndex(cfg.destination.getBoardName())) > -1) {
            cfg.wb.setSheetName(secondShtIdx, cfg.destination.getBoardName() + "_" + dateNow);
            if ((secondChgIdx = cfg.wb.getSheetIndex(
                    InternalConfig.CHANGES_SHEET_NAME + cfg.destination.getBoardName())) > -1) {
                cfg.wb.setSheetName(secondChgIdx,
                        InternalConfig.CHANGES_SHEET_NAME + cfg.destination.getBoardName() + "_" + dateNow);
                found = true;
            }
        }
        if (!found) {
            d.p(LMS.ERROR, "Oops! fetch of new data for board: %s failed\n", cfg.destination.getBoardName());
            // Don't need to undo anything as we haven't written the file out yet.
            System.exit(-22);
        } else {
            if (saveShtIdx >= 0)
                cfg.wb.setSheetName(saveShtIdx, cfg.destination.getBoardName());
            if (saveCfgIdx >= 0)
                cfg.wb.setSheetName(saveCfgIdx, InternalConfig.CHANGES_SHEET_NAME + cfg.destination.getBoardName());
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

        LinkedHashMap<String, Integer> firstCols = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> secondCols = new LinkedHashMap<>();

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
            Cell id = tr.getCell(firstCols.get(ColNames.ID));
            // On replay, we might forget to update the ID field from the srcID field
            // if we are going back to the same board
            if (id == null) {
                id = tr.getCell(firstCols.get(ColNames.SOURCE_ID));
            }
            missing.put(id.getStringCellValue(), tr.getRowNum());
        }
        while (secondShtRow.hasNext()) {
            Row tr = secondShtRow.next();
            missing.computeIfAbsent(tr.getCell(secondCols.get(ColNames.SOURCE_ID)).getStringCellValue(), s -> {
                extras.put(s, tr.getRowNum());
                return null;
            });
            missing.computeIfPresent(tr.getCell(secondCols.get(ColNames.SOURCE_ID)).getStringCellValue(), (s, i) -> {
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
            Row dr = cfg.replaySheet.createRow(cfg.replaySheet.getLastRowNum() + 1);
            Integer localCellIdx = 0;
            dr.createCell(localCellIdx++, CellType.STRING).setCellValue(cfg.group);
            dr.createCell(localCellIdx++, CellType.FORMULA)
                    .setCellFormula("'" + secondSht.getSheetName() + "'!B" + (idx + 1)); // RowNum is 1-based not
                                                                                         // 0-based
            dr.createCell(localCellIdx++, CellType.STRING).setCellValue("Modify");
            dr.createCell(localCellIdx++, CellType.STRING).setCellValue("lane");
            dr.createCell(localCellIdx++, CellType.STRING).setCellValue(lane);

            Cell a = secondSht.getRow(idx).getCell(XlUtils.firstColumnFromSheet(secondSht, ColNames.SOURCE_ID));
            Cell b = secondSht.getRow(idx).getCell(XlUtils.firstColumnFromSheet(secondSht, ColNames.ID));
            if (b == null) {
                b = secondSht.getRow(idx).createCell(XlUtils.firstColumnFromSheet(secondSht, ColNames.ID));
            }
            switch (a.getCellType()) {
                case NUMERIC: {
                    b.setCellValue(a.getNumericCellValue());
                    break;
                }
                case STRING: {
                    b.setCellValue(a.getStringCellValue());
                    break;
                }
                case FORMULA: {
                    String cf = a.getCellFormula();
                    CellReference ca = new CellReference(cf);
                    XSSFSheet iSht = cfg.wb.getSheet(ca.getSheetName());
                    Row item = iSht.getRow(ca.getRow());
                    Cell cell = item.getCell(ca.getCol());
                    b.setCellValue(cell.getStringCellValue());
                    break;
                }
                default: {
                    break;
                }
            }
        });

        // Hackady doodees alert! I am assuming that idx is the same between itmsht and
        // chgsht!
        ArrayList<Row> createRows = new ArrayList<>();
        ArrayList<Row> modifyRows = new ArrayList<>();

        missing.forEach((itm, idx) -> {
            Row sr = cfg.wb.getSheetAt(firstChgIdx).getRow(idx);
//            Row dr = cfg.replaySheet.createRow(cfg.replaySheet.getLastRowNum() + 1);
//            Utils.copyRow(sr, dr);

            // We could potentially clear out the ID to indicate which ones need replaying
            // or we could ignore the check (in Importer.java) if we are doing a replay - I
            // chose this option

            // sr = firstSht.getRow(idx);
            // Cell srcCell = sr.getCell(Utils.findColumnFromSheet(firstSht, ColNames.ID));
            // if (cfg.replay)
            // srcCell.setCellValue("");

            /**
             * We need to remake the parent/child relationships that the orginal had - OMG!
             */
            // First extract from this row the value of the one that is going to be
            // replicated
            int col = XlUtils.firstColumnFromSheet(cfg.replaySheet, ColNames.ITEM_ROW);

            String original = null;
            if (sr.getCell(col).getCellType().equals(CellType.STRING)) {
                original = sr.getCell(col).getStringCellValue();
            } else if (sr.getCell(col).getCellType().equals(CellType.NUMERIC)) {
                original = Double.toString(sr.getCell(col).getNumericCellValue());
            } else {
                FormulaEvaluator evaluator = cfg.wb.getCreationHelper().createFormulaEvaluator();
                CellValue cValue = evaluator.evaluate(sr.getCell(col));
                original = cValue.getStringValue();
            }

            // Then by using the mapped new ID....
            sr = XlUtils.firstRowByStringValue(firstSht, ColNames.SOURCE_ID, original);
            Integer lcol = XlUtils.firstColumnFromSheet(firstSht, ColNames.ID);
            Cell lcl = sr.getCell(lcol);
            if (lcl == null) {
                lcol = XlUtils.firstColumnFromSheet(firstSht, ColNames.SOURCE_ID);
                lcl = sr.getCell(lcol);
            }
            String newOne = lcl.getStringCellValue();

            // .... find all those rows that have "Modify" for that item.
            createRows.addAll(XlUtils.getRowsByStringValue(icfg, icfg.wb.getSheetAt(firstChgIdx), ColNames.ITEM_ROW,
                    newOne));

            modifyRows.addAll(XlUtils.getRowsByStringValue(icfg, icfg.wb.getSheetAt(firstChgIdx), ColNames.VALUE,
                    newOne));
        });
        //Need to put all 'Create' rows before 'Modify' rows because of parentage.
        createRows.forEach((row) -> {
            if (row.getCell(XlUtils.firstColumnFromSheet(icfg.wb.getSheetAt(firstChgIdx), ColNames.ACTION))
                    .getStringCellValue().equals("Create")) {
                Row ldr = cfg.replaySheet.createRow(cfg.replaySheet.getLastRowNum() + 1);
                XlUtils.copyRow(row, ldr);
            }
        });
        modifyRows.forEach((row) -> {
            if (row.getCell(XlUtils.firstColumnFromSheet(icfg.wb.getSheetAt(firstChgIdx), ColNames.ACTION))
                    .getStringCellValue().equals("Modify")) {
                Row ldr = cfg.replaySheet.createRow(cfg.replaySheet.getLastRowNum() + 1);
                XlUtils.copyRow(row, ldr);
            }
        });


        common.forEach((itm, idx) -> {

            /**
             * Need to compare the records
             */
            Row lsrc = XlUtils.firstRowByStringValue(firstSht, ColNames.ID, itm);
            // On replay, we might be board to same board, so ID will not be set
            // by an importer
            if (lsrc == null) {
                lsrc = XlUtils.firstRowByStringValue(firstSht, ColNames.SOURCE_ID, itm);
            }
            // src must be virtual 'final' in the lambda below
            Row src = lsrc;
            Row dst = XlUtils.firstRowByStringValue(secondSht, ColNames.SOURCE_ID, itm);

            firstCols.forEach((item, indx) -> {
                // src and ID are internal so we ignore
                if (!item.equals(ColNames.SOURCE_ID) && !item.equals(ColNames.ID)) {
                    // If cells are not equivalent....
                    String srcCellStr = null;
                    Double srcCellDbl = null;
                    Cell srcCell = src.getCell(indx);
                    if (srcCell != null) { // Empty cells can be blank or null
                        switch (srcCell.getCellType()) {
                            case STRING: {
                                srcCellStr = srcCell.getStringCellValue();
                                break;
                            }
                            case NUMERIC: {
                                srcCellDbl = srcCell.getNumericCellValue();
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                        Cell dstCell = dst.getCell(XlUtils.firstColumnFromSheet(secondSht, item));
                        String dstCellStr = null;
                        Double dstCellDbl = null;
                        boolean compTruth = false;
                        switch (dstCell.getCellType()) {
                            case NUMERIC: {
                                dstCellDbl = dstCell.getNumericCellValue();
                                if (dstCellDbl.equals(srcCellDbl)) {
                                    compTruth = true;
                                }
                                break;
                            }
                            case STRING: {
                                dstCellStr = dstCell.getStringCellValue();
                                if (dstCellStr.equals(srcCellStr)) {
                                    compTruth = true;
                                }
                                break;
                            }
                            case BLANK: {
                                break;
                            }
                            case FORMULA: {
                                dstCell.setCellFormula(srcCell.getCellFormula());
                                break;
                            }
                            default: {
                                d.p(LMS.WARN, "Default called in error for dstCell type\n");
                                break;
                            }
                        }

                        if (!compTruth) {
                            // ...write out modify line
                            if (!srcCell.getCellType().equals(CellType.BLANK)) {
                                Row dr = cfg.replaySheet.createRow(cfg.replaySheet.getLastRowNum() + 1);
                                Integer localCellIdx = 0;
                                dr.createCell(localCellIdx++, CellType.STRING).setCellValue(cfg.group);
                                // RowNum is 1-based not 0-based
                                dr.createCell(localCellIdx++, CellType.FORMULA)
                                        .setCellFormula("'" + firstSht.getSheetName() + "'!B" + (idx + 1));
                                dr.createCell(localCellIdx++, CellType.STRING).setCellValue("Modify");
                                dr.createCell(localCellIdx++, CellType.STRING).setCellValue(item);
                                switch (srcCell.getCellType()) {
                                    case STRING: {
                                        dr.createCell(localCellIdx++, CellType.STRING).setCellValue(srcCellStr);
                                        break;
                                    }
                                    case NUMERIC: {
                                        dr.createCell(localCellIdx++, CellType.NUMERIC).setCellValue(srcCellDbl);
                                        break;
                                    }
                                    default: {
                                        d.p(LMS.DEBUG, "Unknown srcCell type %s\n", srcCell.getCellType().toString());
                                        dr.createCell(localCellIdx++);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            });
        });

        /**
         * Now we have a sheet called replay_<getBoardName()> which we can get an Importer to
         * execute if the
         * replay flag is set on the commandline
         */
        if (cfg.replay) {
            cfg.changesSheet = cfg.replaySheet;
            Importer ipmt = new Importer(cfg);
            ipmt.go();
        }
        /**
         * Open the output stream and send the file back out.
         */
        XlUtils.writeFile(cfg, cfg.xlsxfn, cfg.wb);
    }
}
