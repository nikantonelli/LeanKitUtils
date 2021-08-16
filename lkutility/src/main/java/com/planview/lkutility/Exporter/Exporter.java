package com.planview.lkutility.exporter;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.planview.lkutility.InternalConfig;
import com.planview.lkutility.SupportedXlsxFields;
import com.planview.lkutility.Utils;
import com.planview.lkutility.leankit.BlockedStatus;
import com.planview.lkutility.leankit.Card;
import com.planview.lkutility.leankit.CustomId;
import com.planview.lkutility.leankit.ExternalLink;
import com.planview.lkutility.leankit.ItemType;
import com.planview.lkutility.leankit.Lane;
import com.planview.lkutility.leankit.Task;

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
    public static InternalConfig cfg = null;

    int itmRowIdx = 0;
    int chgRowIdx = 0;

    public void go(InternalConfig config) {

        /**
         * Check that the workbook doesn't have the "Changes" sheet in
         */

        cfg = config;
        Integer chShtIdx = cfg.wb.getSheetIndex(InternalConfig.CHANGES_SHEET_NAME);
        if (chShtIdx >= 0) {
            cfg.wb.removeSheetAt(chShtIdx);
        }

        Integer itemShtIdx = cfg.wb.getSheetIndex(cfg.source.boardId);
        if (itemShtIdx >= 0) {
            cfg.wb.removeSheetAt(itemShtIdx);
        }

        /**
         * Create the Changes Sheet layout
         */

        int chgRowIdx = 0;
        int chgCellIdx = 0;

        XSSFSheet chgSht = cfg.wb.createSheet(InternalConfig.CHANGES_SHEET_NAME);
        Row nextChgRow = chgSht.createRow(chgRowIdx++);

        // These next lines are the fixed format of the Changes sheet
        nextChgRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Group");
        nextChgRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Item Sheet");
        nextChgRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Item Row");
        nextChgRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Action");
        nextChgRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Field");
        nextChgRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Value");

        /**
         * Now create the Item Sheet layout
         */

        XSSFSheet itemSht = cfg.wb.createSheet(cfg.source.boardId);
        Row nextItemRow = itemSht.createRow(itmRowIdx++);

        int itmCellIdx = 0;
        nextItemRow.createCell(itmCellIdx, CellType.STRING).setCellValue("ID");
        // Put all the fields into a map for later on
        HashMap<String, Object> fieldMap = new HashMap<>();
        fieldMap.put("ID", itmCellIdx++);

        // Now write out the fields
        Field[] pbFields = (new SupportedXlsxFields()).getClass().getFields();

        for (int i = 0; i < pbFields.length; i++) {
            nextItemRow.createCell(itmCellIdx, CellType.STRING).setCellValue(pbFields[i].getName());
            fieldMap.put(pbFields[i].getName(), itmCellIdx++);
        }
        /**
         * Read all the normal cards on the board - up to a limit?
         */
        ArrayList<Card> cards = Utils.readCardsFromBoard(cfg, cfg.source);

        /**
         * Write all the cards out to the itemSht
         */
        Iterator<Card> ic = cards.iterator();
        while (ic.hasNext()) {
            Card c = ic.next();

            /* Write a 'Create' line to the changes sheet */

            nextChgRow = chgSht.createRow(chgRowIdx);

            // These next lines are the fixed format of the Changes sheet
            chgCellIdx = 0;

            // We can only write out cards directly. Tasks are handled differently

            createChangeRow(chgSht, chgRowIdx, itmRowIdx + 1, "Create", "", "");
            chgRowIdx++; // Do this after because we might have changed the index in the subr call
            createItemRowFromCard(chgSht, chgRowIdx, itemSht, itmRowIdx, c, pbFields);
            itmRowIdx++; // Do this after because we might have changed the index in the subr call

            /**
             * Open the output stream and send the file back out.
             */
            Utils.writeFile(cfg.xlsxfn, cfg.wb);
        }
    }

    /** All fields handled here must have mirror in createItemRowFromTask (which probably does nothing)
     * This is because we use the one list of fields from SupportedXlsxField.java
     * The importer will select the fields correctly for the type of item
     */
    public static void createItemRowFromCard(XSSFSheet chgSht, Integer chgRowIdx, XSSFSheet itemSht, Integer itmRowIdx,
            Card c, Field[] pbFields) {

        Integer chrRowIncr = 0;
        Integer itmRowIncr = 0;
        Row itmRow = itemSht.createRow(itmRowIdx);
        for (int i = 0; i < pbFields.length; i++) {
            chrRowIncr = 0;
            itmRowIncr = 0;
            try {
                switch (pbFields[i].getName()) {
                    /**
                     * We need to extract the blockedStatus type and re-create a blockReason
                     */
                    case "blockReason": {
                        // Get blockedStatus from card
                        Object fv = c.getClass().getField("blockedStatus").get(c);
                        if (fv != null) {
                            if (((BlockedStatus) fv).isBlocked) {
                                itmRow.createCell(i + 1, CellType.STRING).setCellValue(((BlockedStatus) fv).reason);
                            } else {
                                itmRow.createCell(i + 1, CellType.STRING).setCellValue("");
                            }
                        }
                        break;
                    }
                    case "customId": {
                        Object fv = c.getClass().getField(pbFields[i].getName()).get(c);
                        if (fv != null) {
                            itmRow.createCell(i + 1, CellType.STRING).setCellValue(((CustomId) fv).value);
                        }
                        break;
                    }
                    // Not sure why this is in the plural as I have only ever seen one
                    case "externalLinks": {
                        Object fv = c.getClass().getField(pbFields[i].getName()).get(c);
                        if (fv != null) {
                            itmRow.createCell(i + 1, CellType.STRING).setCellValue(
                                    ((ExternalLink) fv).label.replace(",", " ") + "," + ((ExternalLink) fv).url);
                        }
                        break;
                    }
                    case "lane": {
                        Object fv = c.getClass().getField(pbFields[i].getName()).get(c);
                        if (fv != null) {
                            itmRow.createCell(i + 1, CellType.STRING)
                                    .setCellValue(Utils.getLanePathFromId(cfg, cfg.source, ((Lane) fv).id));
                        }
                        break;
                    }
                    case "srcID": {
                        itmRow.createCell(i + 1, CellType.STRING).setCellValue(c.id);
                        if (cfg.addComment) {
                            chrRowIncr++;
                            createChangeRow(chgSht, chgRowIdx + chrRowIncr, itmRowIdx, "Modify", "Comment",
                                    Utils.getUrl(cfg, cfg.source) + "/card/" + c.id);
                        }
                        break;
                    }
                    case "tags": {
                        Object fv = c.getClass().getField(pbFields[i].getName()).get(c);
                        if (fv != null) {
                            itmRow.createCell(i + 1, CellType.STRING).setCellValue(String.join(",", ((String[]) fv)));
                        }
                        break;
                    }
                    case "taskBoardStats": {
                        // If the task count is non zero, get the tasks for this card and
                        // resolve the lanes for the tasks,
                        // Add the tasks to the items and put some Modify statements in.
                        if (c.taskBoardStats != null) {
                            ArrayList<Task> tasks = Utils.readTasksFromCard(cfg, cfg.source, c.id);
                            for (int j = 0; j < tasks.size(); j++) {
                                chrRowIncr++;
                                itmRowIncr++;
                                createChangeRow(chgSht, chgRowIdx + chrRowIncr, itmRowIdx, "Modify", "Comment",
                                        "='" + cfg.source.boardId + "'!A" + (itmRowIdx + itmRowIncr));
                                // Now create the item row itself 
                                createItemRowFromTask(chgSht, chgRowIdx, itemSht, itmRowIdx + itmRowIncr, tasks.get(i), pbFields);
                            }
                        }

                        break;
                    }
                    default: {
                        Object fv = c.getClass().getField(pbFields[i].getName()).get(c);
                        if (fv != null) {
                            switch (fv.getClass().getSimpleName()) {
                                case "String": {
                                    itmRow.createCell(i + 1, CellType.STRING).setCellValue(fv.toString());
                                    break;
                                }
                                case "Boolean": {
                                    itmRow.createCell(i + 1, CellType.BOOLEAN).setCellValue(((Boolean) fv));
                                    break;
                                }
                                case "Integer": {
                                    itmRow.createCell(i + 1, CellType.NUMERIC).setCellValue(((Integer) fv));
                                    break;
                                }
                                case "ItemType": {
                                    itmRow.createCell(i + 1, CellType.STRING).setCellValue(((ItemType) fv).title);
                                    break;
                                }
                                case "Date": {
                                    SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
                                    Cell cl = itmRow.createCell(i + 1);
                                    cl.setCellValue(dtf.format(((Date) fv)).toString());

                                    break;
                                }

                                default: {
                                    System.out.printf("Unknown class: %s", fv.getClass().getSimpleName());
                                }
                            }

                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        itmRowIdx += itmRowIncr;
        chgRowIdx += chrRowIncr;
    }

    /** All fields handled here must have mirror in createItemRowFromCard (which probably does nothing) */
    public static void createItemRowFromTask(XSSFSheet chgSht, Integer chgRowIdx, XSSFSheet itemSht, Integer itmRowIdx,
            Task c, Field[] pbFields) {

        Integer chrRowIncr = 0;
        Integer itmRowIncr = 0;
        Row itmRow = itemSht.createRow(itmRowIdx);
        for (int i = 0; i < pbFields.length; i++) {
            chrRowIncr = 0;
            itmRowIncr = 0;
            try {
                switch (pbFields[i].getName()) {
                    /**
                     * We need to extract the blockedStatus type and re-create a blockReason
                     */
                    case "blockReason": {
                        // Get blockedStatus from card
                        Object fv = c.getClass().getField("blockedStatus").get(c);
                        if (fv != null) {
                            if (((BlockedStatus) fv).isBlocked) {
                                itmRow.createCell(i + 1, CellType.STRING).setCellValue(((BlockedStatus) fv).reason);
                            } else {
                                itmRow.createCell(i + 1, CellType.STRING).setCellValue("");
                            }
                        }
                        break;
                    }
                    case "customId": {
                        Object fv = c.getClass().getField(pbFields[i].getName()).get(c);
                        if (fv != null) {
                            itmRow.createCell(i + 1, CellType.STRING).setCellValue(((CustomId) fv).value);
                        }
                        break;
                    }
                    // Not sure why this is in the plural as I have only ever seen one
                    case "externalLinks": {
                        Object fv = c.getClass().getField(pbFields[i].getName()).get(c);
                        if (fv != null) {
                            itmRow.createCell(i + 1, CellType.STRING).setCellValue(
                                    ((ExternalLink) fv).label.replace(",", " ") + "," + ((ExternalLink) fv).url);
                        }
                        break;
                    }
                    case "lane": {
                        Object fv = c.getClass().getField(pbFields[i].getName()).get(c);
                        if (fv != null) {
                            itmRow.createCell(i + 1, CellType.STRING)
                                    .setCellValue(Utils.getLanePathFromId(cfg, cfg.source, ((Lane) fv).id));
                        }
                        break;
                    }
                    case "srcID": {
                        itmRow.createCell(i + 1, CellType.STRING).setCellValue(c.id);
                        if (cfg.addComment) {
                            chrRowIncr++;
                            createChangeRow(chgSht, chgRowIdx + chrRowIncr, itmRowIdx, "Modify", "Comment",
                                    Utils.getUrl(cfg, cfg.source) + "/card/" + c.id);
                        }
                        break;
                    }
                    case "tags": {
                        Object fv = c.getClass().getField(pbFields[i].getName()).get(c);
                        if (fv != null) {
                            itmRow.createCell(i + 1, CellType.STRING).setCellValue(String.join(",", ((String[]) fv)));
                        }
                        break;
                    }
                    case "taskBoardStats": {
                        break;
                    }
                    default: {
                        Object fv = c.getClass().getField(pbFields[i].getName()).get(c);
                        if (fv != null) {
                            switch (fv.getClass().getSimpleName()) {
                                case "String": {
                                    itmRow.createCell(i + 1, CellType.STRING).setCellValue(fv.toString());
                                    break;
                                }
                                case "Boolean": {
                                    itmRow.createCell(i + 1, CellType.BOOLEAN).setCellValue(((Boolean) fv));
                                    break;
                                }
                                case "Integer": {
                                    itmRow.createCell(i + 1, CellType.NUMERIC).setCellValue(((Integer) fv));
                                    break;
                                }
                                case "ItemType": {
                                    itmRow.createCell(i + 1, CellType.STRING).setCellValue(((ItemType) fv).title);
                                    break;
                                }
                                case "Date": {
                                    SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
                                    Cell cl = itmRow.createCell(i + 1);
                                    cl.setCellValue(dtf.format(((Date) fv)).toString());

                                    break;
                                }

                                default: {
                                    System.out.printf("Unknown class: %s", fv.getClass().getSimpleName());
                                }
                            }

                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        itmRowIdx += itmRowIncr;
        chgRowIdx += chrRowIncr;
    }

    public static void createChangeRow(XSSFSheet chgSht, Integer chgRowIdx, Integer itmRowIdx, String action,
            String field, String value) {
        Integer localCellIdx = 0;
        Row chgRow = chgSht.createRow(chgRowIdx);
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(cfg.group);
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(cfg.source.boardId);
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(itmRowIdx);
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(action); // "Action"
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(field); // "Field"
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(value); // "Value"
    }

}
