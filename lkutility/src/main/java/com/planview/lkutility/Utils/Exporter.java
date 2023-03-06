package com.planview.lkutility.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.planview.lkutility.Leankit.Attachment;
import com.planview.lkutility.Leankit.BlockedStatus;
import com.planview.lkutility.Leankit.Card;
import com.planview.lkutility.Leankit.CardType;
import com.planview.lkutility.Leankit.Comment;
import com.planview.lkutility.Leankit.CustomField;
import com.planview.lkutility.Leankit.CustomIcon;
import com.planview.lkutility.Leankit.CustomId;
import com.planview.lkutility.Leankit.ExternalLink;
import com.planview.lkutility.Leankit.ItemType;
import com.planview.lkutility.Leankit.Lane;
import com.planview.lkutility.Leankit.ParentCard;
import com.planview.lkutility.Leankit.ParentChild;
import com.planview.lkutility.Leankit.PlanningIncrement;
import com.planview.lkutility.Leankit.Task;
import com.planview.lkutility.Leankit.User;
import com.planview.lkutility.System.Changes;
import com.planview.lkutility.System.ColNames;
import com.planview.lkutility.System.Debug;
import com.planview.lkutility.System.InternalConfig;
import com.planview.lkutility.System.LMS;
import com.planview.lkutility.System.SupportedXlsxFields;

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

    Integer chgShtIdx = -1; // Set to invalid as a precaution for misuse.

    ArrayList<ParentChild> parentChild = new ArrayList<>();
    Debug d = new Debug();

    public Exporter(InternalConfig config) {
        cfg = config;
        d.setLevel(cfg.debugLevel);
		d.setMsgr(cfg.msg);
    }

    public void go() {

        d.p(LMS.ALWAYS, "Starting Export of \"%s\" at: %s\n", cfg.source.getBoardName(), new Date());
        doExport(setUpNewSheets(cleanSheets()));
    }

    public String getSheetName() {
		return XlUtils.validateSheetName(InternalConfig.CHANGES_SHEET_NAME + cfg.source.getBoardName());
	}

	public String cleanSheets() {
		Integer shtIdx = null;
		String cShtName = getSheetName();

		shtIdx = cfg.wb.getSheetIndex(cShtName);
		if (shtIdx >= 0) {
			cfg.wb.removeSheetAt(shtIdx);
		}

		// Now make sure we don't have any left over item information
		shtIdx = cfg.wb.getSheetIndex(XlUtils.validateSheetName(cfg.source.getBoardName()));
		if (shtIdx >= 0) {
			cfg.wb.removeSheetAt(shtIdx);
		}
		return cShtName;

	}

    public String[] setUpNewSheets(String cShtName){
        cfg.changesSheet = XlUtils.newChgSheet(cfg,cShtName);
        chgRowIdx = 1;    //Start after header row
        return newItmSheet();
    }


    public String[] newItmSheet(){
        cfg.itemSheet = cfg.wb.createSheet(XlUtils.validateSheetName(cfg.source.getBoardName()));
        d.setLevel(cfg.debugLevel); //Do this again here because we can bypass it above in go()
        /**
         * Now create the Item Sheet layout
         */

        Row itmHdrRow = cfg.itemSheet.createRow(itmRowIdx++);

        int itmCellIdx = 0;
        itmHdrRow.createCell(itmCellIdx++, CellType.STRING).setCellValue(ColNames.ID);

        /** Now write out the fields
         * There are user accessible fields - some are r/w, some are r/o (outFields)
         * There are also fields we might want to check as part of the program, but are
         * not exported directly (checkFields). They actually cause other things to happen, 
         * e.g. a Modify line is added to the changes sheet.
         * 
         * The outFields and checkFields are kept in line by this software. If you change something,
         * make sure they are aligned
         * 
         * */

        SupportedXlsxFields allFields = new SupportedXlsxFields();
        Field[] rwFields = (allFields.new Modifiable()).getClass().getFields(); // Public fields that will be written
                                                                                // as columns
        Field[] roFields = (allFields.new ReadOnly()).getClass().getFields();

        Field[] pseudoFields = (allFields.new Pseudo()).getClass().getFields(); // Inlcudes pseudo fields that
                                                                                        // will
                                                                                        // cause alternative actions
        CustomField[] customFields = LkUtils.getCustomFields(cfg, cfg.source);

        Integer checkFieldsLength = rwFields.length + customFields.length + pseudoFields.length;
        Integer outFieldsLength   = rwFields.length + customFields.length;

        if (cfg.roFieldExport){
            checkFieldsLength += roFields.length;
            outFieldsLength   += roFields.length;
        }

        String[] checkFields = new String[checkFieldsLength];
        Integer cfi = 0;
        String[] outFields = new String[outFieldsLength];
        Integer ofi = 0;

        for (int i = 0; i < rwFields.length; i++) {
            outFields[ofi++] = checkFields[cfi++] = rwFields[i].getName();
        }

        if (cfg.roFieldExport) {
            for (int i = 0; i < roFields.length; i++) {
                outFields[ofi++] = checkFields[cfi++] = roFields[i].getName();
            }
        }

        for (int i = 0; i < customFields.length; i++) {
            outFields[ofi++] = checkFields[cfi++] = customFields[i].label;
        }

        for (int i = 0; i < pseudoFields.length; i++) {
            checkFields[cfi++] = pseudoFields[i].getName();
        }

        //Put column headers out
        for (int i = 0; i < outFieldsLength; i++) {
            itmHdrRow.createCell(itmCellIdx++, CellType.STRING).setCellValue(outFields[i]);
        }

        Integer col = XlUtils.firstColumnFromSheet(cfg.itemSheet, ColNames.ID);
        cfg.itemSheet.setColumnWidth(col, 18*256);    //First two columns are usually ID and srcID
        col = XlUtils.firstColumnFromSheet(cfg.itemSheet, ColNames.SOURCE_ID);
        cfg.itemSheet.setColumnWidth(col, 18*256);

        return checkFields;
    }
    
    public void doExport(String[] checkFields) {
        /**
         * Read all the normal cards on the board - up to a limit?
         */
        ArrayList<Card> cards = LkUtils.getCardsFromBoard(cfg, cfg.source);
        Collections.sort(cards);
        /**
         * Write all the cards out to the cfg.itemSheet
         */
        Iterator<Card> ic = cards.iterator();
        while (ic.hasNext()) {
            Card c = ic.next();

            /**
             * We have to re-fetch the cards to get the relevant parent information.
             */
            c = LkUtils.getCard(cfg, cfg.source, c.id);

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

        Iterator<ParentChild> pci = parentChild.iterator();
        while (pci.hasNext()) {
            ParentChild pc = pci.next();
            Integer parentShtIdx = cfg.wb.getSheetIndex(XlUtils.validateSheetName(pc.boardName));
            if (parentShtIdx >= 0) {
                XSSFSheet pSht = cfg.wb.getSheetAt(parentShtIdx);
                Integer parentRow = XlUtils.firstRowIdxByStringValue(pSht, ColNames.SOURCE_ID, pc.parentId);
                Integer childRow = XlUtils.firstRowIdxByStringValue(cfg.itemSheet, ColNames.SOURCE_ID, pc.childId);

                if ((parentRow == null) || (childRow == null)) {
                    d.p(LMS.WARN, "Ignoring parent/child relationship for: %s/%s. Is parent archived?\n",
                            pc.parentId, pc.childId);
                } else {
                    Integer col = XlUtils.findColumnFromSheet(cfg.itemSheet, ColNames.TITLE);
						String letter = CellReference.convertNumToColString(col);
						d.p(LMS.INFO, "Creating parent/child relationship for: %s/%s\n",
								pc.parentId, pc.childId);createChangeRow(chgRowIdx++, childRow, "Modify", "Parent",
								"='" + XlUtils.validateSheetName(pc.boardName) + "'!" + letter + (parentRow + 1));
                    chgRowIdx++;
                }
            }
        }

        /**
         * Open the output stream and send the file back out.
         */
        XlUtils.writeFile(cfg, cfg.xlsxfn, cfg.wb);
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
        d.p(LMS.INFO, "Creating row %d for id: %s (%s)\n", itmRow, c.id,
                (c.customId.value != null) ? c.customId.value : c.title);
        // We need to keep a separate counter for the fields we actually write out
        Integer fieldCounter = 1;
        for (int i = 0; i < pbFields.length; i++) {
            try {
                switch (pbFields[i]) {

                    case "assignedUsers": {
                        Object fv = c.getClass().getField(pbFields[i]).get(c);
                        String outStr = "";
                        if (fv != null) {
                            User[] au = (User[]) fv;
                            for (int j = 0; j < au.length; j++) {
                                /**
                                 * I have to fetch the realuser because the assignedUser != boardUser != user
                                 */
                                User realUser = LkUtils.getUser(cfg, cfg.source, au[j].id);
                                if (realUser != null) {
                                    outStr += ((outStr.length() > 0) ? "," : "") + realUser.username;
                                }
                            }
                            if (outStr.length() > 0) {
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
                                byte[] data = (byte[]) LkUtils.getAttachment(cfg, cfg.source, c.id, atts[j].id);
                                d.p(LMS.INFO, "Saving attachment %s\n", af.getPath());
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
                                createChangeRow(chgRow, item, "Modify", "comments",
                                        String.format("%s : %s wrote: \n", cmts[j].createdOn,
                                                cmts[j].createdBy.fullName)
                                                + cmts[j].text);
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
                                iRow.createCell(fieldCounter, CellType.STRING)
                                        .setCellValue(((BlockedStatus) fv).reason);
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
                            ExternalLink[] extlnks = (ExternalLink[]) fv;
                            if ((extlnks.length > 0) && (extlnks[0].url != null)) {
                                if (extlnks[0].label != null) {
                                    iRow.createCell(fieldCounter, CellType.STRING)
                                            .setCellValue(extlnks[0].label.replace(",", " ") + "," + extlnks[0].url);
                                } else {
                                    iRow.createCell(fieldCounter, CellType.STRING).setCellValue("," + extlnks[0].url);
                                }
                            }
                        }
                        fieldCounter++;
                        break;
                    }
                    case "lane": {
                        Object fv = c.getClass().getField(pbFields[i]).get(c);
                        if (fv != null) { // Might be a task
                            CardType ct = LkUtils.getCardTypeFromBoard(cfg, cfg.source, c.type.title,
                                    cfg.source.getBoardName());
                            if (ct.getIsTaskType()) {
                                Lane taskLane = (Lane) fv;
                                if (taskLane.laneType.equals("untyped")) {
                                    String lane = LkUtils.getLanePathFromId(cfg, cfg.source, ((Lane) fv).id);
                                    d.p(LMS.ERROR,
                                            "Invalid card type - check \"Task\" setting on \"%s\". Opting to use lane \"%s\"\n",
                                            c.type.title, lane);
                                    iRow.createCell(fieldCounter, CellType.STRING)
                                            .setCellValue(LkUtils.getLanePathFromId(cfg, cfg.source, ((Lane) fv).id));

                                } else {
                                    iRow.createCell(fieldCounter, CellType.STRING).setCellValue(taskLane.laneType);
                                }
                            } else {
                                iRow.createCell(fieldCounter, CellType.STRING)
                                        .setCellValue(LkUtils.getLanePathFromId(cfg, cfg.source, ((Lane) fv).id));
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
                                parentChild.add(new ParentChild(pcs[j].boardId, pcs[j].cardId, c.id));
                            }
                        }
                        break;
                    }
                    case ColNames.SOURCE_ID: {
                        iRow.createCell(fieldCounter, CellType.STRING).setCellValue(c.id);
                        if (cfg.addComment) {
                            chgRow++;
                            createChangeRow(chgRow, item, "Modify", "comments",
                                    LkUtils.getUrl(cfg, cfg.source) + "/card/" + c.id);
                        }
                        fieldCounter++;
                        break;
                    }
                    case "tags": {
                        Object fv = c.getClass().getField(pbFields[i]).get(c);
                        if (fv != null) {
                            iRow.createCell(fieldCounter, CellType.STRING)
                                    .setCellValue(String.join(",", ((String[]) fv)));
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
                            ArrayList<Task> tasks = LkUtils.getTaskIdsFromCard(cfg, cfg.source, c.id);
                            for (int j = 0; j < tasks.size(); j++) {
                                chgRow++;
                                Card task = LkUtils.getCard(cfg, cfg.source, tasks.get(j).id);
                                // Increment the row index ready for the item row create
                                itmRow++;
                                createChangeRow(chgRow, item, "Modify", "Task",
                                        "='" + cfg.source.getBoardName() + "'!A" + (itmRow + 1));

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
                                        iRow.createCell(fieldCounter, CellType.STRING)
                                                .setCellValue(((ItemType) fv).title);
                                        break;
                                    }
                                    case "CustomIcon": {
                                        iRow.createCell(fieldCounter, CellType.STRING)
                                                .setCellValue(((CustomIcon) fv).title);
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
                                    case "User": {
                                        iRow.createCell(fieldCounter, CellType.STRING)
                                                .setCellValue(((User) fv).emailAddress);
                                        break;
                                    }
									case "PlanningIncrement[]":{
										PlanningIncrement[] pia = (PlanningIncrement[]) fv;
										ArrayList<String> ids = new ArrayList<>();
										for (int p=0; p < pia.length; p++){
											ids.add(pia[p].label);
										}
										iRow.createCell(fieldCounter, CellType.STRING)
                                                .setCellValue(ids.toString());
                                        break;
									}
                                    default: {
                                        System.out.printf("Unknown class: %s\n", fv.getClass().getSimpleName());
                                        break;
                                    }
                                }

                            }
                        } catch (NoSuchFieldException e) {
                            // This is probably a custom field so look for it in the customFields
                            // array
                            Object cfa = c.getClass().getField("customFields").get(c);
                            CustomField[] cfs = (CustomField[]) cfa;
                            CustomField foundField = null;
                            for (int j = 0; j < cfs.length; j++) {
                                if (cfs[j].label.equals(pbFields[i])) {
                                    foundField = cfs[j];
                                }
                            }
                            // We now know that the field is part of the Custom Field set, so find the value
                            // in the cards array
                            // of
                            if (foundField != null) {
                                if (foundField.value != null) {
                                    switch (foundField.type) {
                                        case "number": {
                                            Double number = Double.parseDouble((String) foundField.value);
											iRow.createCell(fieldCounter, CellType.NUMERIC)
													.setCellValue(number);
                                            break;
                                        }
                                        default: {
                                            iRow.createCell(fieldCounter, CellType.STRING)
                                                    .setCellValue((String) foundField.value);
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
		String cellFormula = "'" + XlUtils.validateSheetName(cfg.source.getBoardName()) + "'!"
				+ XlUtils.findColumnLetterFromSheet(cfg.itemSheet, "title")
				+ (IRIdx + 1);
		Row chgRow = cfg.changesSheet.createRow(CRIdx);
		chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(cfg.group);
		chgRow.createCell(localCellIdx++, CellType.FORMULA)
				// .setCellFormula("'" + cfg.source.BoardName + "'!B" + (IRIdx + 1));
				.setCellFormula(cellFormula);
		chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(action);
		chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(field);

		if (value.startsWith("=")) {
			FormulaEvaluator evaluator = cfg.wb.getCreationHelper().createFormulaEvaluator();
			Cell cell = chgRow.createCell(localCellIdx++);
			cell.setCellFormula(value.substring(1));
			evaluator.evaluateFormulaCell(cell);
		} else {
			chgRow.createCell(localCellIdx++, CellType.STRING).setCellValue(value);
		}
	}
}
