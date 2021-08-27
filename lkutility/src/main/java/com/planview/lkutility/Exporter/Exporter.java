package com.planview.lkutility.exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.planview.lkutility.Changes;
import com.planview.lkutility.Debug;
import com.planview.lkutility.InternalConfig;
import com.planview.lkutility.SupportedXlsxFields;
import com.planview.lkutility.Utils;
import com.planview.lkutility.leankit.AccessCache;
import com.planview.lkutility.leankit.Attachment;
import com.planview.lkutility.leankit.BlockedStatus;
import com.planview.lkutility.leankit.Card;
import com.planview.lkutility.leankit.CardType;
import com.planview.lkutility.leankit.Comment;
import com.planview.lkutility.leankit.CustomField;
import com.planview.lkutility.leankit.CustomId;
import com.planview.lkutility.leankit.ExternalLink;
import com.planview.lkutility.leankit.ItemType;
import com.planview.lkutility.leankit.Lane;
import com.planview.lkutility.leankit.ParentCard;
import com.planview.lkutility.leankit.ParentChild;
import com.planview.lkutility.leankit.Task;
import com.planview.lkutility.leankit.User;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;

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

    Integer chShtIdx = -1; // Set to invalid as a precaution for misuse.

    ArrayList<ParentChild> parentChild = new ArrayList<>();
    Debug d = new Debug();

    public void go(InternalConfig config) {

        /**
         * Check that the workbook doesn't have the "Changes" sheet in
         */

        cfg = config;
        d.setLevel(config.debugLevel);
        cfg.cache = new AccessCache(cfg, cfg.source);

        d.p(Debug.INFO, "Starting Export at: %s\n", new Date());
        Integer chShtIdx = null;
        if (!cfg.dualFlow) {
            chShtIdx = cfg.wb.getSheetIndex(InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.source.boardId);
        } else {
            chShtIdx = cfg.wb.getSheetIndex(InternalConfig.CHANGES_SHEET_NAME);
        }

        if (chShtIdx >= 0) {
            cfg.wb.removeSheetAt(chShtIdx);
        }

        if (!cfg.dualFlow) {
            cfg.changesSheet = cfg.wb.createSheet(InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.source.boardId);
        } else {
            cfg.changesSheet = cfg.wb.createSheet(InternalConfig.CHANGES_SHEET_NAME);
        }

        Integer itemShtIdx = cfg.wb.getSheetIndex(cfg.source.boardId);
        if (itemShtIdx >= 0) {
            cfg.wb.removeSheetAt(itemShtIdx);
        }
        cfg.itemSheet = cfg.wb.createSheet(cfg.source.boardId);

        /**
         * Create the Changes Sheet layout
         */

        int chgCellIdx = 0;

        Row chgHdrRow = cfg.changesSheet.createRow(chgRowIdx++);

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

        Row itmHdrRow = cfg.itemSheet.createRow(itmRowIdx++);

        int itmCellIdx = 0;
        itmHdrRow.createCell(itmCellIdx, CellType.STRING).setCellValue("ID");
        // Put all the fields into a map for later on
        HashMap<String, Object> fieldMap = new HashMap<>();
        fieldMap.put("ID", itmCellIdx++);

        // Now write out the fields
        Field[] xlsxFields = (new SupportedXlsxFields()).getClass().getFields();     //Public fields that will be written as columns
        Field[] validFields = (new SupportedXlsxFields()).getClass().getDeclaredFields(); //Inlcudes private that will cause alternative actions
        CustomField[] customFields = Utils.fetchCustomFields(cfg, cfg.source);

        String[] outFields = new String[xlsxFields.length + customFields.length];
        String[] checkFields = new String[validFields.length + customFields.length];

        for ( int i = 0; i < xlsxFields.length; i++){
            outFields[i] = xlsxFields[i].getName();
        }
        for ( int i = 0; i < validFields.length; i++){
            checkFields[i] = validFields[i].getName();
        }

        for ( int i = 0; i < customFields.length; i++){
            outFields[xlsxFields.length + i] = customFields[i].label;
            checkFields[validFields.length + i] = customFields[i].label;
        }

        //Store the indexes for the spreadsheet columns
        for (int i = 0; i < outFields.length; i++) {
            itmHdrRow.createCell(itmCellIdx, CellType.STRING).setCellValue(outFields[i]);
            fieldMap.put(outFields[i], itmCellIdx++);
        }

        /**
         * Read all the normal cards on the board - up to a limit?
         */
        ArrayList<Card> cards = Utils.readCardIdsFromBoard(cfg, cfg.source);
        Collections.sort(cards);
        /**
         * Write all the cards out to the cfg.itemSheet
         */
        Iterator<Card> ic = cards.iterator();
        while (ic.hasNext()) {
            Card c = ic.next();

            /**
             * Due to the seemingly brain-dead api, we have to re-fetch the cards to get the
             * relevant parent information.
             */
            c = Utils.getCard(cfg, c.id);

            /* Write a 'Create' line to the changes sheet */
            // We can only write out cards here. Tasks are handled differently

            createChangeRow(chgRowIdx, itmRowIdx, "Create", "", "");
            Changes changeTotal = createItemRowFromCard(chgRowIdx, itmRowIdx, c, checkFields); // checkFields contains
                                                                                               // extra

            chgRowIdx = changeTotal.getChangeRow();
            itmRowIdx = changeTotal.getItemRow();

            // Do these after because we have changed the index in the subr calls
            chgRowIdx++;
            itmRowIdx++;

        }

        /**
         * Now scan the parent/child register and add "Modify" lines
         */

        // ArrayList<Connection> Utils.getConnectionsForThisBoard(cfg.source.boardId);

        Iterator<ParentChild> pci = parentChild.iterator();
        while (pci.hasNext()) {
            ParentChild pc = pci.next();
            Integer parentRow = Utils.findRowBySourceId(cfg.itemSheet, pc.parentId);
            Integer childRow = Utils.findRowBySourceId(cfg.itemSheet, pc.childId);

            if ((parentRow == null) || (childRow == null)) {
                d.p(Debug.WARN, "Unexpected row result from %s/%s. Is parent archived?", pc.parentId, pc.childId);
            } else {
                createChangeRow(chgRowIdx, childRow, "Modify", "Parent",
                        "='" + cfg.source.boardId + "'!A" + (parentRow + 1));
                chgRowIdx++;
            }
        }

        // Collections.sort(cards);
        // ic = cards.iterator();
        // while (ic.hasNext()){
        //     Card card = ic.next();
        //     Integer cardRow = Utils.findRowBySourceId(cfg.itemSheet, card.id);
        //     createChangeRow(chgRowIdx, cardRow, "Modify", "index", card.index);
        //         chgRowIdx++;
        // }

        /**
         * Open the output stream and send the file back out.
         */
        Utils.writeFile(cfg, cfg.xlsxfn, cfg.wb);
    }

    /**
     * All fields handled here must have mirror in createItemRowFromTask (which
     * probably does nothing) This is because we use the one list of fields from
     * SupportedXlsxField.java The importer will select the fields correctly for the
     * type of item
     * 
     * Got a chick-and-egg situation with the indexes.
     */
    public Changes createItemRowFromCard(Integer chgRow, Integer itmRow, Card c, String[] pbFields) {

        Integer item = itmRow;

        Row iRow = cfg.itemSheet.createRow(itmRow);
        d.p(Debug.INFO, "Creating row for id: %s (%s)\n", c.id, (c.customId.value != null)?c.customId.value:c.title);
        //We need to keep a separate counter for the fields we actually write out
        Integer fieldCounter = 1;
        for (int i = 0; i < pbFields.length; i++) {
            try {
                switch (pbFields[i]) {

                    case "assignedUsers": {
                        Object fv = c.getClass().getField(pbFields[i]).get(c);
                        String outStr = "";
                        if (fv != null) {
                            User[] au = (User[])fv;
                            for (int j = 0; j < au.length; j++){
                                /** Another case of brain-deadness. I have to fetch the realuser
                                 * because the assignedUser != boardUser != user
                                 */
                                User realUser = Utils.fetchUser(cfg, cfg.source, au[j].id);
                                if (realUser != null) {
                                    outStr += ((outStr.length()>0)?",":"") + realUser.username;
                                }
                            }
                            if (outStr.length() > 0){
                                iRow.createCell(fieldCounter, CellType.STRING).setCellValue(outStr);
                            }
                        }
                        fieldCounter++;
                        break;
                    }
                    case "attachments": {
                        Object fv = c.getClass().getField(pbFields[i]).get(c);
                        if ((fv != null) && cfg.exportAttachments) {
                            Attachment[] atts = ((Attachment[]) fv);
                            if (atts.length > 0) {
                                /**
                                 * If the attachments length is greater than zero, try to create a sub folder in
                                 * the current directory called attachments. Then try to make a sub-subfolder
                                 * based on the card id. Then add a file entitled based on the attachment of
                                 */
                                Files.createDirectories(Paths.get("attachments/" + c.id));

                            }
                            for (int j = 0; j < atts.length; j++) {
                                File af = new File("attachments/" + c.id + "/" + atts[j].name);
                                FileOutputStream fw = new FileOutputStream(af);
                                byte[] data = (byte[]) Utils.getAttachment(cfg, cfg.source, c.id, atts[j].id);
                                d.p(Debug.INFO, "Saving attachment %s\n", af.getPath());
                                fw.write(data, 0, data.length);
                                fw.flush();
                                fw.close();
                                chgRow++;
                                
                                createChangeRow(chgRow, item, "Modify", "attachments", af.getPath());
                            }
                        }
                        break;
                    }
                    case "comments": {
                        Object fv = c.getClass().getField(pbFields[i]).get(c);
                        if ((fv != null) && cfg.exportComments) {
                            Comment[] cmts = (Comment[]) fv;
                            for (int j = 0; j < cmts.length; j++) {
                                chgRow++;
                                createChangeRow(chgRow, item, "Modify", "comments", String.format("%s : %s wrote: \n",
                                        cmts[j].createdOn, cmts[j].createdBy.fullName) + cmts[j].text);
                            }
                        }
                        break;
                    }
                    /**
                     * We need to extract the blockedStatus type and re-create a blockReason
                     */
                    case "blockReason": {
                        // Get blockedStatus from card
                        Object fv = c.getClass().getField("blockedStatus").get(c);
                        if (fv != null) {
                            if (((BlockedStatus) fv).isBlocked) {
                                iRow.createCell(fieldCounter, CellType.STRING).setCellValue(((BlockedStatus) fv).reason);
                            } else {
                                iRow.createCell(fieldCounter, CellType.STRING).setCellValue("");
                            }
                        }
                        fieldCounter++;
                        break;
                    }
                    case "customId": {
                        Object fv = c.getClass().getField(pbFields[i]).get(c);
                        if (fv != null) {
                            iRow.createCell(fieldCounter, CellType.STRING).setCellValue(((CustomId) fv).value);
                        }
                        fieldCounter++;
                        break;
                    }
                    
                    case "externalLink": {
                        Object fv = c.getClass().getField("externalLinks").get(c);
                        if (fv != null) {
                            ExternalLink[] extlnks = (ExternalLink[])fv;
                            if (extlnks.length > 0){
                                iRow.createCell(fieldCounter, CellType.STRING).setCellValue(
                                    extlnks[0].label.replace(",", " ") + "," + extlnks[0].url);
                            }
                        }
                        fieldCounter++;
                        break;
                    }
                    case "lane": {
                        Object fv = c.getClass().getField(pbFields[i]).get(c);
                        if (fv != null) {   //Might be a task 
                            CardType ct = Utils.findCardTypeFromBoard(cfg,  cfg.source, c.type.title);
                            if (ct.isTaskType){
                                Lane taskLane = (Lane)fv;
                                iRow.createCell(fieldCounter, CellType.STRING)
                                    .setCellValue(taskLane.laneType);
                            }  else { 
                                iRow.createCell(fieldCounter, CellType.STRING)
                                    .setCellValue(Utils.getLanePathFromId(cfg, cfg.source, ((Lane) fv).id));
                            }
                        }
                        fieldCounter++;
                        break;
                    }
                    case "parentCards": {
                        Object fv = c.getClass().getField(pbFields[i]).get(c);
                        if (fv != null) {
                            /**
                             * We will only worry about parents on the same board. We cannot deal with
                             * parents on other boards here. At some future date, we could add a comment to
                             * indicate this
                             */
                            ParentCard[] pcs = (ParentCard[]) fv;
                            for (int j = 0; j < pcs.length; j++) {
                                if (pcs[j].boardId.equals(cfg.source.boardId)) {
                                    parentChild.add(new ParentChild(pcs[j].cardId, c.id));
                                }
                            }
                        }
                        break;
                    }
                    case "srcID": {
                        iRow.createCell(fieldCounter, CellType.STRING).setCellValue(c.id);
                        if (cfg.addComment) {
                            chgRow++;
                            createChangeRow(chgRow, item, "Modify", "comments",
                                    Utils.getUrl(cfg, cfg.source) + "/card/" + c.id);
                        }
                        fieldCounter++;
                        break;
                    }
                    case "tags": {
                        Object fv = c.getClass().getField(pbFields[i]).get(c);
                        if (fv != null) {
                            iRow.createCell(fieldCounter, CellType.STRING).setCellValue(String.join(",", ((String[]) fv)));
                        }
                        fieldCounter++;
                        break;
                    }

                    // Pseudo-field that does something different
                    case "taskBoardStats": {
                        // If the task count is non zero, get the tasks for this card and
                        // resolve the lanes for the tasks,
                        // Add the tasks to the items and put some Modify statements in.
                        if (cfg.exportTasks && (c.taskBoardStats != null)) {
                            ArrayList<Task> tasks = Utils.readTaskIdsFromCard(cfg, cfg.source, c.id);
                            for (int j = 0; j < tasks.size(); j++) {
                                chgRow++;
                                Card task = Utils.getCard(cfg, tasks.get(j).id);
                                // Increment the row index ready for the item row create
                                itmRow++;
                                createChangeRow(chgRow, item, "Modify", "Task",
                                        "='" + cfg.source.boardId + "'!A" + (itmRow + 1));

                                // Now create the item row itself
                                // Changes changesMade = new Changes(0,0); //Testing!
                                Changes childChanges = createItemRowFromCard(chgRow, itmRow, task, pbFields);
                                // Need to pick up the indexes again as we might have created task entries

                                chgRow = childChanges.getChangeRow();
                                itmRow = childChanges.getItemRow();
                            }
                            // itmRowIncr += tasks.size();
                        }

                        break;
                    }
                    default: {
                        Object fv;

                        try {
                            fv = c.getClass().getField(pbFields[i]).get(c);
                            if (fv != null) {
                                switch (fv.getClass().getSimpleName()) {
                                    case "String": {
                                        iRow.createCell(fieldCounter, CellType.STRING).setCellValue(fv.toString());
                                        break;
                                    }
                                    case "Boolean": {
                                        iRow.createCell(fieldCounter, CellType.BOOLEAN).setCellValue(((Boolean) fv));
                                        break;
                                    }
                                    case "Integer": {
                                        iRow.createCell(fieldCounter, CellType.NUMERIC).setCellValue(((Integer) fv));
                                        break;
                                    }
                                    case "ItemType": {
                                        iRow.createCell(fieldCounter, CellType.STRING).setCellValue(((ItemType) fv).title);
                                        break;
                                    }
                                    case "Date": {
                                        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
                                        Cell cl = iRow.createCell(fieldCounter);
                                        cl.setCellValue(dtf.format(((Date) fv)).toString());
    
                                        break;
                                    }
                                    // Ignore these pseudo-fields
                                    case "ParentCard": {
                                        break;
                                    }
                                    default: {
                                        System.out.printf("Unknown class: %s", fv.getClass().getSimpleName());
                                        break;
                                    }
                                }
    
                            }
                        } catch (NoSuchFieldException e) {
                            //This is probably a custom field so look for it in the customFields
                            // array
                            Object cfa = c.getClass().getField("customFields").get(c);
                            CustomField[] cfs = (CustomField[])cfa;
                            CustomField foundField = null;
                            for (int j = 0; j < cfs.length; j++){
                                if (cfs[j].label.equals(pbFields[i])){
                                    foundField = cfs[j];
                                }
                            }
                            //We now know that the field is part of the Custom Field set, so find the value in the cards array
                            //of 
                            if (foundField != null){
                                if (foundField.value != null) {
                                switch (foundField.type){
                                    case "number":{
                                        iRow.createCell(fieldCounter, CellType.NUMERIC).setCellValue(Integer.parseInt((String)foundField.value));
                                        break;
                                    }
                                    default:{
                                        iRow.createCell(fieldCounter, CellType.STRING).setCellValue((String)foundField.value);
                                        break;
                                    }
                                }
                            }
                            }
                        }                     
                        fieldCounter++;
                        break;
                       
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return new Changes(chgRow, itmRow);
    }

    private void createChangeRow(Integer CRIdx, Integer IRIdx, String action, String field, String value) {
        Integer localCellIdx = 0;
        Row chgRow = cfg.changesSheet.createRow(CRIdx);
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(cfg.group);
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(cfg.source.boardId);
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(IRIdx + 1);
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(action); // "Action"
        chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(field); // "Field"

        if (value.startsWith("=")) {
            FormulaEvaluator evaluator = cfg.wb.getCreationHelper().createFormulaEvaluator();
            Cell cell = chgRow.createCell(localCellIdx++);
            cell.setCellFormula(value.substring(1));
            evaluator.evaluateFormulaCell(cell);
        } else {
            chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(value); // "Value"
        }
    }

    // private void createChangeRow(Integer CRIdx, Integer IRIdx, String action, String field, Integer value) {
    //     Integer localCellIdx = 0;
    //     Row chgRow = cfg.changesSheet.createRow(CRIdx);
    //     chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(cfg.group);
    //     chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(cfg.source.boardId);
    //     chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(IRIdx + 1);
    //     chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(action); // "Action"
    //     chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(field); // "Field"
    //     chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(value); // "Value"    
    // }
}
