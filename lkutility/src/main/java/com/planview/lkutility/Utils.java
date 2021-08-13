package com.planview.lkutility;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import com.planview.lkutility.leankit.Board;
import com.planview.lkutility.leankit.Card;
import com.planview.lkutility.leankit.CardType;
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

    public static ArrayList<Card> readCardsFromBoard(InternalConfig iCfg, Configuration accessCfg) {
        LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);

        /**
         * Fetch the number of cards on the board and get the pages of them until we have them all.
         */

        //Board brd = lka.fetchBoardFromId(accessCfg.boardId);
        
        ArrayList<Card> cards = lka.fetchCardsFromBoard(accessCfg.boardId, iCfg.exportArchived, iCfg.exportTasks); 
        return cards;
    }
}
