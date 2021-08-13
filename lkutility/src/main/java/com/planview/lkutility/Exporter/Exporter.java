package com.planview.lkutility.exporter;

import java.util.ArrayList;
import java.util.Iterator;

import com.planview.lkutility.Configuration;
import com.planview.lkutility.InternalConfig;
import com.planview.lkutility.Utils;
import com.planview.lkutility.leankit.Card;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * We need to get a unique board ID from the user or allow for a selection
 * mechanism Then retrieve all the 'cards' on the board. We don't need to know
 * what type they are during selection, but need the info for the export.
 * 
 * The importer program gets data from an excel spreadsheet. We cannot create
 * all the info needed for the config sheet. What we could do though, is to
 * create a sheet for each card type (e.g. Epic, Stories, Tasks).
 * 
 * We can ask each card whether it has subtasks and create entrires to add those
 * (when importer has that functionality added).
 * 
 * Parents are easy - we can do it in a single pass: 1- when parents is found
 * with children cards check whether the children exist and make the connection
 * 2- when cards with parents are found check whether the parent(s) exists and
 * add the child
 * 
 */
public class Exporter {

    public void go(InternalConfig cfg) {

        /**
         * Check that the workbook doesn't have the "Changes" sheet in
         */

        Integer chShtIdx = cfg.wb.getSheetIndex(InternalConfig.CHANGES_SHEET_NAME);
        if (chShtIdx >= 0) {
            cfg.wb.removeSheetAt(chShtIdx);
        }
        
        Integer itemShtIdx = cfg.wb.getSheetIndex(cfg.source.boardId);
        if (itemShtIdx >= 0) {
            cfg.wb.removeSheetAt(itemShtIdx);
        }

        /** 
         * Add in blank changes sheet and blank item sheet
         */

        XSSFSheet chgSht = cfg.wb.createSheet(InternalConfig.CHANGES_SHEET_NAME);
        XSSFSheet itemSht = cfg.wb.createSheet(cfg.source.boardId);

        /**
         * Write out the first header line with the fields
         */
        Row hdrRow = itemSht.createRow(0);

        int cellIdx = 0;
        Cell currentCell = hdrRow.createCell(cellIdx, CellType.STRING);
        currentCell.setCellValue("ID");
        /**
         * Read all the items on the board - up to a limit
         */
        Configuration accessCfg = cfg.source; 
        ArrayList<Card> cards = Utils.readCardsFromBoard(cfg, accessCfg);

        /**
         * Write all the cards out to the itemSht
         */
        Iterator<Card> ic = cards.iterator();

        /**
         * Open the output stream and send the file back out.
         */
        Utils.writeFile(cfg.xlsxfn, cfg.wb);
    }

}
