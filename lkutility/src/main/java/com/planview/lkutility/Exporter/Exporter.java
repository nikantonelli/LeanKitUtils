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
import com.planview.lkutility.leankit.CardType;
import com.planview.lkutility.leankit.Lane;
import com.planview.lkutility.leankit.Task;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
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

    XSSFSheet changeSht = null;
    Integer chShtIdx = -1; // Set to invalid as a precaution for misuse.
    XSSFSheet itemSht = null;

    public void go(InternalConfig config) {

        /**
         * Check that the workbook doesn't have the "Changes" sheet in
         */

        cfg = config;

        Integer chShtIdx = cfg.wb.getSheetIndex(InternalConfig.CHANGES_SHEET_NAME);
        if (chShtIdx >= 0) {
            cfg.wb.removeSheetAt(chShtIdx);
        }
        changeSht = cfg.wb.createSheet(InternalConfig.CHANGES_SHEET_NAME);

        Integer itemShtIdx = cfg.wb.getSheetIndex(cfg.source.boardId);
        if (itemShtIdx >= 0) {
            cfg.wb.removeSheetAt(itemShtIdx);
        }
        itemSht = cfg.wb.createSheet(cfg.source.boardId);

        /**
         * Create the Changes Sheet layout
         */

        int chgCellIdx = 0;

        Row chgHdrRow = changeSht.createRow(chgRowIdx++);

        // These next lines are the fixed format of the Changes sheet
        chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Group");
        chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Item Sheet");
        chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Item Row");
        chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Action");
        chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Field");
        chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue("Value");

        /**
         * Now create the Item Sheet layout
         */

        Row itmHdrRow = itemSht.createRow(itmRowIdx++);

        int itmCellIdx = 0;
        itmHdrRow.createCell(itmCellIdx, CellType.STRING).setCellValue("ID");
        // Put all the fields into a map for later on
        HashMap<String, Object> fieldMap = new HashMap<>();
        fieldMap.put("ID", itmCellIdx++);

        // Now write out the fields
        Field[] pbFields = (new SupportedXlsxFields()).getClass().getFields();

        for (int i = 0; i < pbFields.length; i++) {
            itmHdrRow.createCell(itmCellIdx, CellType.STRING).setCellValue(pbFields[i].getName());
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
            // We can only write out cards here. Tasks are handled differently

            createChangeRow(chgRowIdx, itmRowIdx, "Create", "", "");
            createItemRowFromCard(chgRowIdx, itmRowIdx, c, pbFields);

            // Do these after because we might have changed the index in the subr calls
            chgRowIdx++;
            itmRowIdx++;

            /**
             * Open the output stream and send the file back out.
             */
            Utils.writeFile(cfg.xlsxfn, cfg.wb);
        }
    }

    /**
     * All fields handled here must have mirror in createItemRowFromTask (which
     * probably does nothing) This is because we use the one list of fields from
     * SupportedXlsxField.java The importer will select the fields correctly for the
     * type of item
     */
    public void createItemRowFromCard(Integer CRIdx, Integer IRIdx, Card c, Field[] pbFields) {

        Integer chrRowIncr = 0;
        Integer itmRowIncr = 0;
        Row itmRow = itemSht.createRow(IRIdx);
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
                            createChangeRow(CRIdx + chrRowIncr, IRIdx, "Modify", "Comment",
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
                        if (cfg.exportTasks && (c.taskBoardStats != null)) {
                            ArrayList<Task> tasks = Utils.readTasksFromCard(cfg, cfg.source, c.id);
                            for (int j = 0; j < tasks.size(); j++) {
                                chrRowIncr++;
                                itmRowIncr++;
                                createChangeRow(CRIdx + chrRowIncr, IRIdx, "Modify", "Task",
                                        "='" + cfg.source.boardId + "'!A" + (IRIdx + itmRowIncr + 1));
                                // Now create the item row itself
                                createItemRowFromTask(CRIdx + chrRowIncr, IRIdx + itmRowIncr, tasks.get(j), pbFields);
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

    /**
     * All fields handled here must have mirror in createItemRowFromCard (which
     * probably does nothing)
     */
    public void createItemRowFromTask(Integer CRIdx, Integer IRIdx, Task c, Field[] pbFields) {

        Integer chrRowIncr = 0;
        Integer itmRowIncr = 0;
        Row itmRow = itemSht.createRow(IRIdx);
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
                            createChangeRow(CRIdx + chrRowIncr, IRIdx, "Modify", "Comment",
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

                    case "type": {
                        Object fv = c.getClass().getField("cardType").get(c);
                        if (fv != null) {
                            itmRow.createCell(i + 1, CellType.STRING).setCellValue(((CardType) fv).name);
                            break;
                        }
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

    private void createChangeRow(Integer CRIdx, Integer IRIdx, String action, String field, String value) {
        Integer localCellIdx = 0;
        Row chgRow = changeSht.createRow(CRIdx);
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(cfg.group);
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(cfg.source.boardId);
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(IRIdx + 1);
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(action); // "Action"
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(field); // "Field"
        
        if (value.startsWith("=")){
            FormulaEvaluator evaluator = cfg.wb.getCreationHelper().createFormulaEvaluator();  
            Cell cell = chgRow.createCell(localCellIdx++);
            cell.setCellFormula(value.substring(1));
            evaluator.evaluateFormulaCell(cell);
        } else {
            chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(value); // "Value"
        }
    }

}
