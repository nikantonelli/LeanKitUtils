package com.planview.lkutility.diff;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.planview.lkutility.Debug;
import com.planview.lkutility.InternalConfig;
import com.planview.lkutility.Utils;
import com.planview.lkutility.exporter.Exporter;

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
        Integer firstItmSht = null; // First item sheets go in here
        Integer firstChgSht = null;
        Integer secondItmSht = null; // second item sheets go in here
        Integer secondChgSht = null;
        
        Boolean found = false;

        String dateNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hhmmss"));

        // Do we have a destination sheet already. If so, move it away but save.
        switch (cfg.diffMode) {
            case "1": {
                if ((firstItmSht = cfg.wb.getSheetIndex(cfg.destination.boardId)) > -1) {
                    if ((firstChgSht = cfg.wb.getSheetIndex(
                            InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.destination.boardId)) > -1) {
                        found = true;
                    }
                }
                break;
            }
            case "2": {
                if ((firstItmSht = cfg.wb.getSheetIndex(cfg.source.boardId)) > -1) {
                    if ((firstChgSht = cfg.wb.getSheetIndex(
                            InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.source.boardId)) > -1) {
                        found = true;
                    }
                }
                if (!found) {
                    d.p(Debug.ERROR, "diff option 2: incorrect sheets found for src board: %s\n", cfg.source.boardId);
                }
                break;
            }
            case "3": {
                if ((firstItmSht = cfg.wb.getSheetIndex(cfg.source.boardId)) > -1) {
                    cfg.wb.setSheetName(firstItmSht,  dateNow + "_" + cfg.source.boardId);
                    if ((firstChgSht = cfg.wb.getSheetIndex(
                            InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.source.boardId)) > -1) {
                        cfg.wb.setSheetName(firstChgSht,
                                 dateNow + "_" + InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.source.boardId);
                        found = true;
                    }
                }
                if (found) {
                    d.p(Debug.INFO, "diff option 3: saving sheets for src board: %s (to fetch new data)\n",
                            cfg.source.boardId);
                }
                // We now need to export from the source, so we can just use the normal exporter
                // for the first item
                Exporter exp = new Exporter(cfg);
                exp.go();

                // Redo what we would have for option 2
                if ((firstItmSht = cfg.wb.getSheetIndex(cfg.source.boardId)) > -1) {
                    if ((firstChgSht = cfg.wb.getSheetIndex(
                            InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.source.boardId)) > -1) {
                        found = true;
                    }
                }
                if (!found) {
                    d.p(Debug.ERROR,
                            "diff option 3: attempted re-export but incorrect sheets found for src board: %s\n",
                            cfg.source.boardId);
                }
                break;
            }
        }

        if ((firstChgSht == null) || (firstItmSht == null)) {
            d.p(Debug.ERROR, " Cannot locate required data to compare\n");
            System.exit(0);
        }

        found = false;
        // For all cases, we should be set up to move the dst sheet away if present
        if ((firstItmSht = cfg.wb.getSheetIndex(cfg.destination.boardId)) > -1) {
            cfg.wb.setSheetName(firstItmSht,  dateNow + "_" + cfg.destination.boardId);
            if ((firstChgSht = cfg.wb.getSheetIndex(
                    InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.destination.boardId)) > -1) {
                cfg.wb.setSheetName(firstChgSht,
                         dateNow + "_" + InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.destination.boardId);
                found = true;
            }
        }
        if (found) {
            d.p(Debug.INFO, "Saved sheets found for dst board: %s (to fetch new data)\n", cfg.destination.boardId);
        }

        // Now create a config to pass to the exporter so that we get a new destination
        // board data set.
        InternalConfig icfg = new InternalConfig();
        icfg.debugLevel = cfg.debugLevel;
        icfg.wb = cfg.wb;
        icfg.source = cfg.destination;
        icfg.xlsxfn = cfg.xlsxfn;
        String cShtName = InternalConfig.CHANGES_SHEET_NAME + "_" + icfg.source.boardId;

        // Fire off exporter to get second item
        Exporter iExpt = new Exporter(icfg);
        iExpt.newSheets(cShtName);  //Do not use go() as it cleans out stuff.

        /**
         * We should now have two set of sheets to compare: first item, second item
         */
        found = false;
        if ((secondItmSht = cfg.wb.getSheetIndex(cfg.destination.boardId)) > -1) {
            cfg.wb.setSheetName(secondItmSht, "n_" + cfg.destination.boardId);
            if ((secondChgSht = cfg.wb.getSheetIndex(
                    InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.destination.boardId)) > -1) {
                cfg.wb.setSheetName(secondChgSht, "n_" +
                        InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.destination.boardId);
                found = true;
            }
        }
        if (!found) {
            d.p(Debug.ERROR, "Oops! fetch of new data for board: %s failed\n", cfg.destination.boardId);
            //Don't need to undo anything as we haven't written the file out yet.
            System.exit(0);
        }

        /**
         * We should have to item sheets that should have the same set of cards, but no direct connection between them.
         * It could be that we are comparing data from the same original set of cards, if that is the case, then the
         * srcID dataset will be the same.
         */
        /**
         * Open the output stream and send the file back out.
         */
        Utils.writeFile(cfg, cfg.xlsxfn, cfg.wb);  
    }
}
