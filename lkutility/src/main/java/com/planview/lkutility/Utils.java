package com.planview.lkutility;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import com.planview.lkutility.leankit.Board;
import com.planview.lkutility.leankit.BoardUser;
import com.planview.lkutility.leankit.Card;
import com.planview.lkutility.leankit.CardType;
import com.planview.lkutility.leankit.CustomField;
import com.planview.lkutility.leankit.CustomIcon;
import com.planview.lkutility.leankit.Lane;
import com.planview.lkutility.leankit.LeanKitAccess;
import com.planview.lkutility.leankit.Task;
import com.planview.lkutility.leankit.User;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;


public class Utils {
    public static Debug d = new Debug();    //Use setLevel in your top level code

    /**
     * First put all the spreadsheet related routines here:
     */

    public static ArrayList<Row> getRowsByStringValue(InternalConfig cfg, XSSFSheet sht, String name, String value) {
        ArrayList<Row> list = new ArrayList<>();

        // Check for daft stuff.
        if (sht == null) {
            d.p(Debug.ERROR, "getRowsByStringValue() passed null sheet\n");
            return new ArrayList<>();
        }
        Integer cellIdx = Utils.firstColumnFromSheet(sht, name);
        if (cellIdx < 0) {
            d.p(Debug.ERROR, "getRowsByStringValue() passed incorrect field name\n");
            return new ArrayList<>();
        }

        Iterator<Row> iRow = sht.iterator();
        while (iRow.hasNext()) {
            Row row = iRow.next();
            Cell rCell = row.getCell(cellIdx);
            FormulaEvaluator evaluator = cfg.wb.getCreationHelper().createFormulaEvaluator();
            CellValue cValue = evaluator.evaluate(rCell);
            if (cValue != null) {
                if (cValue.getCellType().equals(CellType.STRING)) {
                    if (cValue.getStringValue().equals(value)) {
                        list.add(row);
                    }
                }else if (cValue.getCellType().equals(CellType.NUMERIC)){
                    if (Double.toString(cValue.getNumberValue()).equals(value)){
                        list.add(row);
                    }
                }
            }
        }
        return list;
    }

    public static XSSFSheet newChgSheet(InternalConfig cfg, String cShtName) {
        // Make a new one
        XSSFSheet changesSheet = cfg.wb.createSheet(cShtName);

        /**
         * Create the Changes Sheet layout
         */

        int chgCellIdx = 0;
        Row chgHdrRow = changesSheet.createRow(0);

        // These next lines are the fixed format of the Changes sheet
        chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue(ColNames.GROUP);

        Integer col = chgCellIdx;
        chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue(ColNames.ITEM_ROW);
        changesSheet.setColumnWidth(col, 18 * 256); // Set the width so that the ID string is fully visible

        chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue(ColNames.ACTION);
        chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue(ColNames.FIELD);

        col = chgCellIdx;
        chgHdrRow.createCell(chgCellIdx++, CellType.STRING).setCellValue(ColNames.VALUE);
        changesSheet.setColumnWidth(col, 30 * 256);

        return changesSheet;
    }

    public static ChangesColumns checkChangeSheetColumns(XSSFSheet changesSht) {
        if (changesSht == null)
            return null;
        ChangesColumns cc = new ChangesColumns();
        cc.group = firstColumnFromSheet(changesSht, ColNames.GROUP);
        cc.row = firstColumnFromSheet(changesSht, ColNames.ITEM_ROW);
        cc.action = firstColumnFromSheet(changesSht, ColNames.ACTION);
        cc.field = firstColumnFromSheet(changesSht, ColNames.FIELD);
        cc.value = firstColumnFromSheet(changesSht, ColNames.VALUE);

        if ((cc.group == null) || (cc.row == null) || (cc.action == null) || (cc.field == null)
                || (cc.value == null)) {
            d.p(Debug.ERROR,
                    "Could not find all required columns in %s sheet: \"%s\", \"%s\", \"%s\", \"%s\", \"%s\"\n",
                    changesSht.getSheetName(),
                    ColNames.GROUP,
                    ColNames.ITEM_ROW,
                    ColNames.ACTION,
                    ColNames.FIELD,
                    ColNames.VALUE);
            return null;
        }
        return cc;
    }

    public static Integer firstRowIdxByStringValue(XSSFSheet itemSht, String fieldName, String value) {
        for (int rowIndex = 1; rowIndex <= itemSht.getLastRowNum(); rowIndex++) {
            Row row = itemSht.getRow(rowIndex);
            if (row != null
                    && row.getCell(firstColumnFromSheet(itemSht, fieldName)).getStringCellValue().equals(value)) {
                return rowIndex;
            }
        }
        return null;
    }

    public static Row firstRowByStringValue(XSSFSheet itemSht, String fieldName, String value) {
        for (int rowIndex = 1; rowIndex <= itemSht.getLastRowNum(); rowIndex++) {
            Row row = itemSht.getRow(rowIndex);
            if ((row != null)
                && (row.getCell(firstColumnFromSheet(itemSht, fieldName)) != null)
                    && row.getCell(firstColumnFromSheet(itemSht, fieldName)).getStringCellValue().equals(value)) {
                return row;
            }
        }
        return null;
    }

    public static Integer firstColumnFromName(Row firstRow, String name) {
        Iterator<Cell> frtc = firstRow.iterator();
        // First, find the column that the "Day Delta" info is in
        int dayCol = -1;
        int td = 0;
        if (!frtc.hasNext()) {
            return dayCol;
        }

        while (frtc.hasNext()) {
            Cell tc = frtc.next();
            if (!tc.getStringCellValue().equals(name)) {
                td++;
            } else {
                dayCol = td;
                break;
            }
        }
        return dayCol;
    }

    public static Integer firstColumnFromSheet(XSSFSheet sht, String name) {
        Iterator<Row> row = sht.iterator();
        if (!row.hasNext()) {
            return null;
        }
        Row firstRow = row.next(); // Get the header row
        Integer col = firstColumnFromName(firstRow, name);
        if (col < 0) {
            return null;
        }
        return col;
    }

    public static Object fetchCell(Row change, Integer col) {

        if (change.getCell(col) != null) {
            // Need to get the correct type of field
            if (change.getCell(col).getCellType() == CellType.FORMULA) {
                if (change.getCell(col).getCachedFormulaResultType() == CellType.STRING) {
                    return change.getCell(col).getStringCellValue();
                } else if (change.getCell(col).getCachedFormulaResultType() == CellType.NUMERIC) {
                    if (DateUtil.isCellDateFormatted(change.getCell(col))) {
                        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = change.getCell(col).getDateCellValue();
                        return dtf.format(date).toString();
                    } else {
                        return (int) change.getCell(col).getNumericCellValue();
                    }
                }
            } else if (change.getCell(col).getCellType() == CellType.STRING) {
                return change.getCell(col).getStringCellValue();
            } else if (change.getCell(col).getCellType() == CellType.BLANK) {
                return null;
            } else {
                if (DateUtil.isCellDateFormatted(change.getCell(col))) {
                    SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = change.getCell(col).getDateCellValue();
                    return dtf.format(date).toString();
                } else {
                    return change.getCell(col).getNumericCellValue();
                }
            }
        }
        return null;
    }

    public static void copyRow(Row row, Row ldr) {
        Iterator<Cell> lsrci = row.iterator();
        int lcellIdx = 0;
        while (lsrci.hasNext()) {
            Cell srcCell = lsrci.next();
            Cell dstCell = ldr.createCell(lcellIdx++, srcCell.getCellType());

            switch (srcCell.getCellType()) {
                case STRING: {
                    dstCell.setCellValue(srcCell.getStringCellValue());
                    break;
                }
                case NUMERIC: {
                    dstCell.setCellValue(srcCell.getNumericCellValue());
                    break;
                }
                case FORMULA: {
                    dstCell.setCellFormula(srcCell.getCellFormula());
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    /**
     * @param iCfg
     * @param xlsxfn
     * @param wb
     */
    static public void writeFile(InternalConfig iCfg, String xlsxfn, XSSFWorkbook wb) {

        Boolean donePrint = true;
        Integer loopCnt = 12;
        while (loopCnt > 0) {
            FileOutputStream oStr = null;
            try {
                oStr = new FileOutputStream(xlsxfn);
                try {
                    wb.write(oStr);
                    try {
                        oStr.close();
                        oStr = null;
                        loopCnt = 0;
                    } catch (IOException e) {
                        d.p(Debug.ERROR, "%s while closing file %s\n", e, xlsxfn);
                    }
                } catch (IOException e) {
                    d.p(Debug.ERROR, "%s while writing file %s\n", e, xlsxfn);
                    oStr.close(); // If this fails, just give up!
                }
            } catch (IOException e) {
                d.p(Debug.ERROR, "%s while opening/closing file %s\n", e, xlsxfn);
            }
            if (loopCnt == 0) {
                break;
            }

            Calendar now = Calendar.getInstance();
            Calendar then = Calendar.getInstance();
            then.add(Calendar.SECOND, 5);
            Long timeDiff = then.getTimeInMillis() - now.getTimeInMillis();
            if (donePrint) {
                d.p(Debug.WARN, "File \"%s\" in use. Please close to let this program continue\n", xlsxfn);
                donePrint = false;
            }
            try {
                Thread.sleep(timeDiff);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            --loopCnt;
        }
    }

    /**
     * Next up is all the Leankit access routines
     * 
     */

    public static String getLanePathFromId(InternalConfig iCfg, Configuration accessCfg, String laneId) {
        Lane[] lanes = null;
        if (iCfg.cache != null) {
            Board brd = iCfg.cache.getBoard(accessCfg.boardId);
            if (brd != null) {
                lanes = brd.lanes;
            }
        }

        Lane lane = findLaneFromId(lanes, laneId);
        String lanePath = "";
        if (lane == null) {
            return lanePath;
        } else {
            lanePath = lane.name;
        }

        while (lane.parentLaneId != null) {
            Lane parentLane = findLaneFromId(lanes, lane.parentLaneId);
            if (parentLane != null) {
                lanePath = parentLane.name + "|" + lanePath;
            }
            lane = parentLane;
        }
        return lanePath;
    }

    private static Lane findLaneFromId(Lane[] lanes, String id) {
        for (int i = 0; i < lanes.length; i++) {
            if (lanes[i].id.equals(id)) {
                return lanes[i];
            }
        }
        d.p(Debug.ERROR, "Failed to find lane %s in board\n", id);
        return null;
    }

    public static String getUrl(InternalConfig iCfg, Configuration accessCfg) {
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
        return lka.getCurrentUrl();
    }

    public static Card getCard(InternalConfig iCfg, String id) {
        Card card = null;
        if (iCfg.cache != null) {
            card = iCfg.cache.getCard(id);
        }
        return card;
    }

    public static byte[] getAttachment(InternalConfig iCfg, Configuration accessCfg, String cardId, String attId) {
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
        return lka.fetchAttachment(cardId, attId);
    }

    public static Board getBoard(InternalConfig iCfg, Configuration accessCfg) {
        Board brd = null;
        if (iCfg.cache != null) {
            brd = iCfg.cache.getBoard(accessCfg.boardId);
        }
        return brd;
    }

    public static ArrayList<Card> readCardIdsFromBoard(InternalConfig iCfg, Configuration accessCfg) {
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
        ArrayList<Card> cards = lka.fetchCardIdsFromBoard(accessCfg.boardId, iCfg.exportArchived);
        return cards;
    }

    public static ArrayList<Task> readTaskIdsFromCard(InternalConfig iCfg, Configuration accessCfg, String cardId) {
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
        ArrayList<Task> tasks = lka.fetchTaskIds(cardId);
        return tasks;
    }

    public static ArrayList<Task> readTasksFromCard(InternalConfig iCfg, Configuration accessCfg, String cardId) {
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
        ArrayList<Task> tasks = lka.fetchTasks(cardId);
        return tasks;
    }

    public static ArrayList<CardType> readCardsTypesFromBoard(InternalConfig iCfg, Configuration accessCfg,
            String boardId) {
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
        ArrayList<CardType> types = null;
        if (iCfg.cache != null) {
            Board brd = iCfg.cache.getBoard(boardId);
            types = new ArrayList<>();
            for (int i = 0; i < brd.cardTypes.length; i++) {
                types.add(brd.cardTypes[i]);
            }
        } else {
            types = lka.fetchCardTypes(boardId);
        }
        return types;
    }

	public static Card findCardByTitle(InternalConfig iCfg, Configuration accessCfg, String title){
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		return lka.fetchCardByTitle(title);
	}

    public static CardType findCardTypeFromBoard(InternalConfig iCfg, Configuration accessCfg, String name,
            String boardId) {
        return findCardTypeFromList(readCardsTypesFromBoard(iCfg, accessCfg, boardId), name);
    }

    public static CardType findCardTypeFromList(ArrayList<CardType> cardTypes, String name) {
        Iterator<CardType> cti = cardTypes.iterator();
        while (cti.hasNext()) {
            CardType ct = cti.next();
            if (ct.name.equals(name)) {
                return ct;
            }
        }
        d.p(Debug.ERROR, "Failed to find CardType %s in board\n", name);
        return null;
    }

    public static Card createCard(InternalConfig iCfg, Configuration accessCfg, JSONObject fieldLst) {
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
        // First create an empty card and get back the full structure

        Card newCard = lka.createCard(fieldLst);
        if (newCard != null) {
            if (iCfg.cache != null) {
                iCfg.cache.setCard(newCard);
            }
        }
        return newCard;
    }

    public static Card updateCard(InternalConfig iCfg, Configuration accessCfg, String cardId, JSONObject updates) {
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
        Card card = null;
        Board brd = null;

        if (iCfg.cache != null) {
            card = iCfg.cache.getCard(cardId);
            brd = iCfg.cache.getBoard(accessCfg.boardId);
        } else {
            card = lka.fetchCard(cardId);
            brd = lka.fetchBoardFromId(accessCfg.boardId);
        }
        card = lka.updateCardFromId(brd, card, updates);
        if (card != null) {
            if (iCfg.cache != null) {
                iCfg.cache.setCard(card);
            }
        }
        return card;
    }

    private static ArrayList<Lane> findLanesFromName(ArrayList<Lane> lanes, String name) {
        ArrayList<Lane> ln = new ArrayList<>();
        for (int i = 0; i < lanes.size(); i++) {
            if (lanes.get(i).name.equals(name)) {
                ln.add(lanes.get(i));
                break;
            }
        }
        return ln;
    }

    private static ArrayList<Lane> findLanesFromParentId(Lane[] lanes, String id) {
        ArrayList<Lane> ln = new ArrayList<>();
        for (int i = 0; i < lanes.length; i++) {
            if (lanes[i].parentLaneId != null) {
                if (lanes[i].parentLaneId.equals(id)) {
                    ln.add(lanes[i]);
                }
            }
        }
        return ln;
    }

    public static Lane findLaneFromBoard(InternalConfig iCfg, Configuration accessCfg, String boardId, String name) {
        Board brd = null;
        if (iCfg.cache != null) {
            brd = iCfg.cache.getBoard(boardId);
        } else {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
            brd = lka.fetchBoardFromId(boardId);
        }
        if (brd != null)
            return findLaneFromString(brd, name);
        else
            return null;
    }

    private static Lane findLaneFromString(Board brd, String name) {
        String[] lanes = name.split("\\|");

        // Is this VS Code failing to handle the '|' character gracefully....
        if (lanes.length == 1) {
            String[] vsLanes = name.split("%");
            if (vsLanes.length > 1) {
                lanes = vsLanes;
            }
        }
        ArrayList<Lane> searchLanes = new ArrayList<>(Arrays.asList(brd.lanes));
        int j = 0;
        ArrayList<Lane> lanesToCheck = findLanesFromName(searchLanes, lanes[j]);
        Lane defaultDropLane = null;
        Iterator<Lane> ddlIter = searchLanes.iterator();
        while (ddlIter.hasNext()) {
            Lane cl = ddlIter.next();
            if (cl.isDefaultDropLane) {
                defaultDropLane = cl;
                break;
            }
        }
        do {
            if (++j >= lanes.length) {
                searchLanes = lanesToCheck;
                break;
            }
            Iterator<Lane> lIter = lanesToCheck.iterator();
            while (lIter.hasNext()) {
                ArrayList<Lane> foundLanes = new ArrayList<>();
                Lane ln = lIter.next();
                ArrayList<Lane> childLanes = findLanesFromParentId(brd.lanes, ln.id);
                Iterator<Lane> clIter = childLanes.iterator();
                while (clIter.hasNext()) {
                    Lane cl = clIter.next();
                    if (cl.name.equals(lanes[j])) {
                        foundLanes.add(cl);
                    }
                }
                if (foundLanes.size() > 0) {
                    lanesToCheck = foundLanes;
                }
            }

        } while (true);

        if (searchLanes.size() == 0) {
            if (defaultDropLane != null) {
                d.p(Debug.INFO, "Cannot find lane \"%s\" on board \"%s\" - defaulting to \"%s\"\n", name, brd.title, defaultDropLane.name);
                return defaultDropLane;
            }
            d.p(Debug.ERROR, "Cannot find lane \"%s\"on board \"%s\"\n", name, brd.title);
            return null;
        }
        if (searchLanes.size() > 1) {
            d.p(Debug.WARN, "Ambiguous lane name \"%s\"on board \"%s\" using lane \"%s\"\n", name, brd.title,
                    searchLanes.get(0).id);
        }

        return searchLanes.get(0);
    }

    public static Lane findLaneFromCard(InternalConfig iCfg, Configuration accessCfg, String cardId, String laneType) {
        Lane lane = null;
        ArrayList<Lane> lanes = null;

        if (iCfg.cache != null) {
            lanes = iCfg.cache.getTaskBoard(cardId);
        } else {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
            lanes = lka.fetchTaskLanes(cardId);
        }
        if (lanes != null) {
            Iterator<Lane> lIter = lanes.iterator();
            while (lIter.hasNext()) {
                Lane laneToCheck = lIter.next();
                if (laneToCheck.laneType.equals(laneType)) {
                    lane = laneToCheck;
                }
            }
        }
        return lane;
    }

    public static User fetchUser(InternalConfig iCfg, Configuration accessCfg, String id) {
        User user = null;
        if (iCfg.cache != null) {
            user = iCfg.cache.getUserById(id);
        } else {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
            user = lka.fetchUserById(id);
        }
        return user;
    }

    public static CustomField findCustomField(InternalConfig iCfg, Configuration accessCfg, String name) {
        CustomField[] cfs = fetchCustomFields(iCfg, accessCfg);
        for (int j = 0; j < cfs.length; j++) {
            if (cfs[j].label.equals(name)) {
                return cfs[j];
            }
        }
        return null;
    }

    public static CustomIcon findCustomIcon(InternalConfig iCfg, Configuration accessCfg, String name) {
        CustomIcon[] cfs = fetchCustomIcons(iCfg, accessCfg, accessCfg.boardId);
        for (int j = 0; j < cfs.length; j++) {
            if (cfs[j].name.equals(name)) {
                return cfs[j];
            }
        }
        return null;
    }

    public static CustomIcon findCustomIcon(InternalConfig iCfg, Configuration accessCfg, String name, String boardId) {
        CustomIcon[] cfs = fetchCustomIcons(iCfg, accessCfg, boardId);
        for (int j = 0; j < cfs.length; j++) {
            if (cfs[j].name.equals(name)) {
                return cfs[j];
            }
        }
        return null;
    }

    public static CustomField[] fetchCustomFields(InternalConfig iCfg, Configuration accessCfg) {
        return fetchCustomFields(iCfg, accessCfg, accessCfg.boardId);
    }

    public static CustomField[] fetchCustomFields(InternalConfig iCfg, Configuration accessCfg, String boardId) {
        CustomField[] fields = null;
        if (iCfg.cache != null) {
            fields = iCfg.cache.getCustomFields(boardId);
        } else {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
            fields = lka.fetchCustomFields(boardId).customFields;
        }
        return fields;
    }

    public static CustomIcon[] fetchCustomIcons(InternalConfig iCfg, Configuration accessCfg, String boardId) {
        CustomIcon[] fields = null;
        if (iCfg.cache != null) {
            fields = iCfg.cache.getCustomIcons(boardId);
        } else {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
            fields = lka.fetchCustomIcons(boardId).customIcons;
        }
        return fields;
    }

    public static User fetchUserByName(InternalConfig iCfg, Configuration accessCfg, String username) {
        User user = null;
        if (iCfg.cache != null) {
            user = iCfg.cache.getUserByName(username);
        } else {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
            user = lka.fetchUserByName(username);
        }
        return user;
    }

    public static ArrayList<BoardUser> fetchUsers(InternalConfig iCfg, Configuration accessCfg) {

        ArrayList<BoardUser> users = null;
        if (iCfg.cache != null) {
            users = iCfg.cache.getBoardUsers(accessCfg.boardId);
        } else {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
            users = lka.fetchUsers(accessCfg.boardId);
        }
        return users;
    }

    /**
     * If cardId is null, we assume this is a card on a board If non-null, then this
     * is a task on a card
     * 
     * @param cfg
     * @param accessCfg
     * @param fieldLst
     * @param item
     * @param cardId
     * @return JSONObject ready for passing to a LeanKitAccess call.
     * 
     */
    public static JSONObject jsonCardFromRow(InternalConfig cfg, Configuration accessCfg, JSONObject fieldLst, Row item,
            String cardId) {
        JSONObject flds = new JSONObject();

        ArrayList<CustomField> customF = new ArrayList<>();
        Iterator<String> keyIt = fieldLst.keys();
        while (keyIt.hasNext()) {
            String key = keyIt.next();
            switch (key) {
                /**
                 * Don't include if not present
                 * 
                 */
                case "assignedUsers": {
                    /**
                     * We need to try and match the email address in the destination and fetch the
                     * userID
                     */
                    Object fv = Utils.fetchCell(item, fieldLst.getInt(key));

                    if (fv != null) {
                        String usersList = (String) fv;
                        ArrayList<BoardUser> boardUsers = fetchUsers(cfg, accessCfg); // Fetch the board users
                        if (boardUsers != null) {

                            if (usersList != null) {
                                String[] users = usersList.split(",");
                                ArrayList<String> usersToPut = new ArrayList<>();
                                for (int i = 0; i < users.length; i++) {
                                    User realUser = fetchUserByName(cfg, accessCfg, users[i]);
                                    if (realUser != null) {
                                        // Check if they are a board user so we don't error.
                                        for (int j = 0; j < boardUsers.size(); j++) {
                                            if (realUser.id.equals(boardUsers.get(j).userId)) {
                                                usersToPut.add(realUser.id);
                                            }
                                        }
                                    } else {
                                        d.p(Debug.WARN, "Cannot locate assignedUser: %s\n", users[i]);
                                    }
                                }
                                if (usersToPut.size() > 0) {
                                    flds.put("assignedUserIds", usersToPut.toArray());
                                }
                            }
                        }
                    }
                    break;
                }

                case "blockReason": {
                    String reason = (String) Utils.fetchCell(item, fieldLst.getInt(key));
                    if ((reason != null) && !reason.equals("")) {

                        flds.put(key, reason);
                    } else {
                        continue;
                    }
                    break;
                }
                case "customIcon": {
                    // Incoming customIcon value is a name. We need to translate to
                    // an id
                    String iconName = (String) Utils.fetchCell(item, fieldLst.getInt(key));
                    CustomIcon ci = null;
                    if (fieldLst.has("boardId")) {
                        ci = Utils.findCustomIcon(cfg, cfg.destination, iconName,
                                item.getCell(fieldLst.getInt("boardId")).getStringCellValue());
                    } else {
                        ci = Utils.findCustomIcon(cfg, cfg.destination, iconName);
                    }

                    if (ci != null) {
                        flds.put("customIconId", ci.id);
                    }
                    break;
                }
                case "externalLink": {
                    String link = (String) Utils.fetchCell(item, fieldLst.getInt(key));
                    if (link != null) {
                        if (!link.isBlank()) {
                            String[] bits = link.split(",");
                            JSONObject el = new JSONObject();

                            switch (bits.length) {
                                case 2: {
                                    el.put("label", bits[0]);
                                    el.put("url", bits[1]);
                                    break;
                                }
                                case 1: {
                                    el.put("label", "");
                                    el.put("url", bits[0]);
                                    break;
                                }
                                default:
                                    break;
                            }
                            if (el.has("url")) {
                                flds.put(key, el);
                            }
                        }
                    }
                    break;
                }
                case "lane": {
                    Lane lane = null;
                    String laneType = (String) Utils.fetchCell(item, fieldLst.getInt(key));
                    if (cardId != null) {
                        if (laneType.isBlank()) {
                            laneType = "ready";
                        }
                        lane = Utils.findLaneFromCard(cfg, accessCfg, cardId, laneType);
                        if (lane != null) {
                            flds.put("laneType", lane.laneType);
                        } else {
                            flds.put("laneType", laneType);
                        }
                    } else {
                        String[] bits = laneType.split("^");
                        if (fieldLst.has("boardId")) {
                            lane = Utils.findLaneFromBoard(cfg, accessCfg,
                                    item.getCell(fieldLst.getInt("boardId")).getStringCellValue(), bits[0]);
                        } else {
                            lane = Utils.findLaneFromBoard(cfg, accessCfg, accessCfg.boardId, bits[0]);
                        }
                        if (lane != null) {
                            flds.put("laneId", lane.id);
                            if (bits.length > 1) {
                                flds.put("wipOverrideComment", bits[1]);
                            }
                        }
                    }
                    break;
                }
                case "size":
                    // The index will be set by the exporter in extra 'Modify' rows. This is here
                    // for manually created (import) spreadsheets
                case "index": {
                    Integer digits = ((Double) Utils.fetchCell(item, fieldLst.getInt(key))).intValue();
                    if (digits != null) {
                        flds.put(key, digits);
                    }
                    break;
                }

                /**
                 * Tags need to be as an array of strings
                 */
                case "tags": {
                    String tagLine = (String) Utils.fetchCell(item, fieldLst.getInt(key));
                    if ((tagLine != null) && !tagLine.equals("")) {
                        String[] tags = tagLine.split(",");
                        flds.put("tags", tags);
                    }
                    break;
                }
                case "type": {
                    String cardtype = (String) Utils.fetchCell(item, fieldLst.getInt(key));
                    ArrayList<CardType> cts = null;
                    if (fieldLst.has("boardId")) {
                        cts = Utils.readCardsTypesFromBoard(cfg, cfg.destination,
                                item.getCell(fieldLst.getInt("boardId")).getStringCellValue());
                    } else {
                        cts = Utils.readCardsTypesFromBoard(cfg, cfg.destination, cfg.destination.boardId);
                    }
                    CardType ct = Utils.findCardTypeFromList(cts, cardtype);
                    if (ct != null) {
                        flds.put("typeId", ct.id);
                    }
                    break;
                }
                default: {
                    // See if the field is part of the standard list of fields. If not, it's a
                    // custom field

                    Card c = new Card();
                    Field[] validFields = c.getClass().getFields();
                    Boolean found = false;
                    for (int i = 0; i < validFields.length; i++) {
                        if (validFields[i].getName().equals(key)) {
                            found = true;
                        }
                    }
                    if (found) {
                        if (item.getCell(fieldLst.getInt(key)) != null) {
                            Object obj = Utils.fetchCell(item, fieldLst.getInt(key));
                            if (obj != null)
                                flds.put(key, obj);
                        }
                    } else {
                        CustomField[] customFields = null;

                        if (fieldLst.has("boardId")) {
                            customFields = Utils.fetchCustomFields(cfg, cfg.destination,
                                    item.getCell(fieldLst.getInt("boardId")).getStringCellValue());
                        } else {
                            customFields = Utils.fetchCustomFields(cfg, cfg.destination);
                        }
                        CustomField cf = new CustomField();
                        for (int i = 0; i < customFields.length; i++) {
                            if (customFields[i].label.equals(key)) {
                                cf.fieldId = customFields[i].id;
                                cf.value = Utils.fetchCell(item, fieldLst.getInt(key));
                                if (cf.value != null) {
                                    customF.add(cf);
                                }
                            }
                        }
                    }

                    break;
                }
            }

        }

        if (customF.size() > 0) {
            // Create a entry to push the custom fields in
            JSONArray jsa = new JSONArray();
            for (int i = 0; i < customF.size(); i++) {
                JSONObject jso = new JSONObject();
                jso.put("fieldId", customF.get(i).fieldId);
                jso.put("value", customF.get(i).value);
                jsa.put(jso);
            }
            flds.put("customFields", jsa);
        }
        return flds;
    }

    public static Card addTask(InternalConfig iCfg, Configuration accessCfg, String cardId, JSONObject item) {
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
        Card card = lka.addTaskToCard(cardId, item);
        if (iCfg.cache != null) {
            iCfg.cache.setCard(card);
        }
        return card;
    }
}
