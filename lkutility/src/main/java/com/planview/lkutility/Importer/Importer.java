package com.planview.lkutility.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.planview.lkutility.ChangesColumns;
import com.planview.lkutility.Debug;
import com.planview.lkutility.InternalConfig;
import com.planview.lkutility.SupportedXlsxFields;
import com.planview.lkutility.Utils;
import com.planview.lkutility.leankit.AccessCache;
import com.planview.lkutility.leankit.BoardUser;
import com.planview.lkutility.leankit.Card;
import com.planview.lkutility.leankit.CustomField;
import com.planview.lkutility.leankit.CustomIcon;
import com.planview.lkutility.leankit.Lane;
import com.planview.lkutility.leankit.User;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.json.JSONObject;

public class Importer {
    Debug d = new Debug();

    InternalConfig cfg = null;

    public  Importer(InternalConfig config) {
        cfg = config;
    }

    public void go() {

        cfg.cache = new AccessCache(cfg, cfg.destination);
        d.setLevel(cfg.debugLevel);
        d.p(Debug.INFO, "Starting Import at: %s\n", new Date());
        /**
         * cfg might contain the sheet info for the importer if it came from the
         * exporter directly
         */

        if (cfg.changesSheet == null) {
            cfg.changesSheet = cfg.wb.getSheet(InternalConfig.CHANGES_SHEET_NAME + "_" + cfg.source.boardId);
        }

        if (null == cfg.changesSheet) {
            d.p(Debug.ERROR, "Cannot find required Changes sheet in file: %s\n", cfg.xlsxfn);
            System.exit(1);
        }
        ChangesColumns cc = Utils.checkChangeSheetColumns(cfg.changesSheet);
        if (cc == null) {
            System.exit(1);
        }

        // Find all the change records for today
        Iterator<Row> row = cfg.changesSheet.iterator();
        ArrayList<Row> todaysChanges = new ArrayList<Row>();

        // Nw add the rows of today to an array so we can give an info message

        row.next(); // Skip first row with headers
        while (row.hasNext()) {

            Row tr = row.next();
            if (tr.getCell(cc.group) != null) {
                if (tr.getCell(cc.group).getNumericCellValue() == cfg.group) {
                    todaysChanges.add(tr);
                }
            }
        }

        if (todaysChanges.size() == 0) {
            d.p(Debug.INFO, "No actions to take for group %d\n", cfg.group);
            return;
        } else {
            d.p(Debug.INFO, "%d actions to take for group %d\n", todaysChanges.size(), cfg.group);
        }
        // Now scan through the changes doing the actions
        Iterator<Row> cItor = todaysChanges.iterator();
        Row item = null;
        while (cItor.hasNext()) {
            Row change = cItor.next();
            // Get the item that this change refers to
            // First check the validity of the info
            if ((change.getCell(cc.row) == null) || (change.getCell(cc.action) == null)) {
                d.p(Debug.WARN, "Cannot decode change info in row \"%d\" - skipping\n", change.getRowNum());
                continue;
            }

            // In case the changes get repositioned, we use the formula to keep track of
            // where the row 'actually' is
            // Excel updates the formula for us if the user edits the changes
            String cf = change.getCell(cc.row).getCellFormula();
            CellReference ca = new CellReference(cf);
            XSSFSheet iSht = cfg.wb.getSheet(ca.getSheetName());
            item = iSht.getRow(ca.getRow());

            Integer idCol = Utils.findColumnFromSheet(iSht, "ID");
            Integer titleCol = Utils.findColumnFromSheet(iSht, "title");
            Integer typeCol = Utils.findColumnFromSheet(iSht, "type");

            if ((idCol == null) || (titleCol == null)) {
                d.p(Debug.WARN, "Cannot locate \"ID\" and \"title\" columns needed in sheet \"%s\" - skipping\n",
                        iSht.getSheetName());
                continue;
            }

            // Check title is present for a Create
            if ((change.getCell(cc.action).getStringCellValue().equals("Create"))
                    && ((item.getCell(titleCol) == null) || (item.getCell(titleCol).getStringCellValue().isEmpty()))) {
                d.p(Debug.WARN,
                        "Required \"title\" column/data missing in sheet \"%s\", row: %d for a Create - skipping\n",
                        iSht.getSheetName(), item.getRowNum());
                continue;
            }

            if ((typeCol == null) || (item.getCell(typeCol) == null)) {
                d.p(Debug.WARN, "Cannot locate \"type\" column on row:  %d  - using default for board\n",
                        item.getRowNum());
            }

            // Check import requirements against command line
            if (change.getCell(cc.action).getStringCellValue() == "Modify") {
                if (((change.getCell(cc.field).getStringCellValue() == "attachments") && !cfg.exportAttachments)
                        || ((change.getCell(cc.field).getStringCellValue() == "Task") && !cfg.exportTasks)
                        || ((change.getCell(cc.field).getStringCellValue() == "comments") && !cfg.exportComments)) {
                    d.p(Debug.WARN, "Ignoring action \"%s\" on item \"%s\", not set to import %s\n",
                            change.getCell(cc.action).getStringCellValue(), item.getCell(titleCol).getStringCellValue(),
                            change.getCell(cc.field).getStringCellValue());
                    continue; // Break out and try next change
                }
            }

            // If unset, it has a null value for the Leankit ID
            if ((item.getCell(idCol) == null) || (item.getCell(idCol).getStringCellValue() == "")) {
                // Check if this is a 'create' operation. If not, ignore and continue past.
                if (!change.getCell(cc.action).getStringCellValue().equals("Create")
                        && !(change.getCell(cc.action).getStringCellValue().equals("Modify")
                                && change.getCell(cc.field).getStringCellValue().equals("Task"))) {
                    d.p(Debug.WARN, "Ignoring action \"%s\" on item \"%s\" (no ID present in item row: %d)\n",
                            change.getCell(cc.action).getStringCellValue(), item.getCell(titleCol).getStringCellValue(),
                            item.getRowNum());
                    continue; // Break out and try next change
                }
            } else {
                // Check if this is a 'create' operation. If it is, ignore and continue past.
                if (change.getCell(cc.action).getStringCellValue().equals("Create")) {
                    d.p(Debug.WARN,
                            "Ignoring action \"%s\" on item \"%s\" (attempting create on existing ID in item row: %d)\n",
                            change.getCell(cc.action).getStringCellValue(), item.getCell(titleCol).getStringCellValue(),
                            item.getRowNum());
                    continue; // Break out and try next change
                }

            }
            String id = null;
            if (change.getCell(cc.action).getStringCellValue().equals("Create")) {
                id = doAction(change, item);
                if (item.getCell(idCol) == null) {

                    item.createCell(idCol);
                }
                item.getCell(idCol).setCellValue(id);
                XSSFFormulaEvaluator.evaluateAllFormulaCells(cfg.wb);

                d.p(Debug.INFO, "Create card \"%s\" (changes row %s)\n", id, change.getRowNum());
            } else {
                id = doAction(change, item);
                d.p(Debug.INFO, "Mod: \"%s\" on card \"%s\" (changes row %s)\n",
                        change.getCell(cc.field).getStringCellValue(), id, change.getRowNum());
            }

            if (id == null) {

                d.p(Debug.ERROR, "%s",
                        "Got null back from doAction(). Most likely card deleted, but ID still in spreadsheet!\n");
            } else {
                Utils.writeFile(cfg, cfg.xlsxfn, cfg.wb);
            }
        }
    }

    private String doAction(Row change, Row item) {

        ChangesColumns cc = Utils.checkChangeSheetColumns(cfg.changesSheet);
        String cf = change.getCell(cc.row).getCellFormula();
        CellReference ca = new CellReference(cf);
        XSSFSheet iSht = cfg.wb.getSheet(ca.getSheetName());

        /**
         * We need to get the header row for this sheet and work out which columns the
         * fields are in. It is possible that fields could be different between sheets,
         * so we have to do this every 'change'
         */

        Iterator<Row> iRow = iSht.iterator();
        Row iFirst = iRow.next();
        /**
         * Now iterate across the cells finding out which fields need to be set
         */
        JSONObject fieldLst = new JSONObject();
        Iterator<Cell> cItor = iFirst.iterator();
        Integer idCol = null;
        while (cItor.hasNext()) {
            Cell cl = cItor.next();
            String nm = cl.getStringCellValue();
            if (nm.toLowerCase().equals("id")) {
                idCol = cl.getColumnIndex();
                continue;
            } else if (nm.toLowerCase().equals("srcid")) {
                continue;
            }
            fieldLst.put(nm, cl.getColumnIndex());
        }

        if (change.getCell(cc.action).getStringCellValue().equalsIgnoreCase("Create")) {
            // Now 'translate' the spreadsheet name:col pairs to fieldName:value pairs

            JSONObject flds = Utils.jsonCardFromRow(cfg, cfg.destination, fieldLst, item, null);

            // We need to find the ID of the board that this is targetting for a card
            // creation
            if (!fieldLst.has("boardId")) {
                flds.put("boardId", cfg.destination.boardId);
            } else {
                flds.put("boardId", Utils.fetchCell(item, fieldLst.getInt("boardId")));
            }
            Card card = Utils.createCard(cfg, cfg.destination, flds); // Change from human readable to API fields on
            // the way
            if (card == null) {
                d.p(Debug.ERROR, "Could not create card on board \"%s\" with details: \"%s\"", flds.get("boardId"),
                        flds.toString());
                System.exit(16);
            }
            return card.id;

        } else if (change.getCell(cc.action).getStringCellValue().equalsIgnoreCase("Modify")) {
            // Fetch the ID from the item and then fetch that card
            Card card = Utils.getCard(cfg, item.getCell(idCol).getStringCellValue());
            Card newCard = null;

            if (card == null) {
                d.p(Debug.ERROR, "Could not locate card \"%s\"\n", item.getCell(idCol).getStringCellValue());
            } else {
                // Don't need this when modifying an existing item.
                if (fieldLst.has("boardId")) {
                    fieldLst.remove("boardId");
                }
                JSONObject fld = new JSONObject();
                JSONObject vals = new JSONObject();

                String field = change.getCell(cc.field).getStringCellValue();
                SupportedXlsxFields allFields = new SupportedXlsxFields();

                try {
                    //If its part of the fields we don't want, then ignore
                    (allFields.new ReadOnly()).getClass().getField(field);
                } catch (NoSuchFieldException e) {

                    switch (field) {
                        case "Task": {
                            // Get row for the task
                            String tcf = change.getCell(cc.value).getCellFormula();
                            CellReference tca = new CellReference(tcf);
                            XSSFSheet cSheet = cfg.wb.getSheet(tca.getSheetName());
                            Row task = cSheet.getRow(tca.getRow());

                            JSONObject jsonTask = Utils.jsonCardFromRow(cfg, cfg.destination, fieldLst, task, card.id);
                            if (task.getCell(idCol) == null) {

                                task.createCell(idCol);
                            }
                            task.getCell(idCol).setCellValue(Utils.addTask(cfg, cfg.destination, card.id, jsonTask).id);
                            break;
                        }
                        case "assignedUsers": {
                            /**
                             * We need to try and match the email address in the destination and fetch the
                             * userID
                             */
                            String usersList = change.getCell(cc.value).getStringCellValue();
                            if (usersList != null) {
                                ArrayList<BoardUser> boardUsers = Utils.fetchUsers(cfg, cfg.destination); // Fetch the
                                                                                                          // board
                                                                                                          // users
                                if (boardUsers != null) {

                                    if (usersList != null) {
                                        String[] users = usersList.split(",");
                                        ArrayList<String> usersToPut = new ArrayList<>();
                                        for (int i = 0; i < users.length; i++) {
                                            User realUser = Utils.fetchUser(cfg, cfg.destination, users[i]);

                                            // Check if they are a board user so we don't error.
                                            for (int j = 0; j < boardUsers.size(); j++) {
                                                if (realUser.id.equals(boardUsers.get(j).id)) {
                                                    usersToPut.add(realUser.id);
                                                }
                                            }
                                        }
                                        fld.put("assignedUserIds", usersToPut.toArray());
                                    }
                                }
                            }
                            break;
                        }
                        case "customIcon": {
                            // Incoming customIcon value is a name. We need to translate to
                            // an id
                            CustomIcon ci = Utils.findCustomIcon(cfg, cfg.destination, field);
                            vals.put("value", ci.id);
                            fld.put("customIconId", vals);
                            break;
                        }
                        case "lane": {
                            String[] bits = ((String) Utils.fetchCell(change, cc.value)).split(",");
                            Lane foundLane = Utils.findLaneFromBoard(cfg, cfg.destination, cfg.destination.boardId,
                                    bits[0]);
                            if (foundLane != null) {
                                vals.put("value", foundLane.id);
                                if (bits.length > 1) {
                                    vals.put("value2", bits[1]);
                                }
                                fld.put("Lane", vals);
                            }
                            break;
                        }

                        default: {
                            // Check if this is a standard/custom field and redo the 'put'

                            CustomField ctmf = Utils.findCustomField(cfg, cfg.destination, field);
                            if (ctmf != null) {
                                vals.put("value", field);
                                vals.put("value2", Utils.fetchCell(change, cc.value));
                                fld.put("CustomField", vals);
                            }
                            // May be special case or satandard field, so let it through.
                            else {
                                vals.put("value", Utils.fetchCell(change, cc.value));
                                fld.put(change.getCell(cc.field).getStringCellValue(), vals);
                            }

                            break;
                        }
                    }
                }

                newCard = Utils.updateCard(cfg, cfg.destination, card.id, fld);
                if (newCard == null) {
                    d.p(Debug.ERROR, "Could not modify card \"%s\" on board %s with details: %s", card.id,
                            cfg.destination.boardId, fld.toString());
                    System.exit(17);
                }
                return card.id;
            }
        }
        // Unknown option comes here
        return null;
    }

}
