package com.planview.lkutility;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import com.planview.lkutility.leankit.Card;
import com.planview.lkutility.leankit.CardType;
import com.planview.lkutility.leankit.Lane;
import com.planview.lkutility.leankit.LeanKitAccess;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Utils {
    static public void writeFile(String xlsxfn, XSSFWorkbook wb) {
        Debug d = new Debug();
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
                d.p(Debug.ERROR, "File \"%s\" in use. Please close to let this program continue\n", xlsxfn);
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

    public static String getTaskLanePathFromId(InternalConfig iCfg, Configuration accessCfg, String cardId, String laneId){
        String lanePath = null;
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
        Lane[] lanes = (Lane[])lka.fetchTaskLanes(cardId).toArray();
        Lane foundLane = findLaneFromId(lanes, laneId);
        if (foundLane != null) {
            lanePath = foundLane.name;
        }
        return lanePath;
    }

    public static String getLanePathFromId(InternalConfig iCfg, Configuration accessCfg, String laneId){
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
        Lane[] lanes = lka.fetchLanes(accessCfg.boardId);
        Lane lane = findLaneFromId(lanes, laneId);
        String lanePath = lane.name;
        while (lane.parentLaneId != null) {
            Lane parentLane = findLaneFromId(lanes, lane.parentLaneId);
            if (parentLane != null) {
                lanePath = parentLane.name + "|"+lanePath;
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
        return null;
    }

    public static String getUrl(InternalConfig iCfg, Configuration accessCfg) {
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
        return lka.getCurrentUrl();
    }

    public static ArrayList<Card> readCardsFromBoard(InternalConfig iCfg, Configuration accessCfg) {
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
        ArrayList<Card> cards = lka.fetchCardsFromBoard(accessCfg.boardId, iCfg.exportArchived, iCfg.exportTasks); 
        return cards;
    }

    public static ArrayList<Card> readTasksFromCard(InternalConfig iCfg, Configuration accessCfg, String cardId) {
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
        ArrayList<Card> cards = lka.fetchTasks(cardId); 
        return cards;
    }

    public static ArrayList<CardType> readCardsTypesFromBoard(InternalConfig iCfg, Configuration accessCfg) {
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
        ArrayList<CardType> types = lka.fetchCardTypes(accessCfg.boardId); 
        return types;
    }

    public static CardType findCardTypeFromList( ArrayList<CardType> cardTypes, String id) {
        Iterator<CardType> cti = cardTypes.iterator();
        while (cti.hasNext()) {
            CardType ct = cti.next();
            if (ct.id.equals(id)) {
                return ct;
            }
        }
        return null;
    }
}
