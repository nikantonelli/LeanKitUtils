package com.planview.lkutility.leankit;

import java.util.HashMap;

import com.planview.lkutility.Configuration;
import com.planview.lkutility.InternalConfig;

public class AccessCache {
    HashMap<String, Board> boardMap = new HashMap<>();
    InternalConfig iCfg;
    Configuration accessCfg;
    
    public AccessCache(InternalConfig cfg, Configuration accCfg){
        iCfg = cfg;
        accessCfg = accCfg;
    }

    public void setBoard(Board brd) {
        if (boardMap.get(brd.id) != null){
            boardMap.remove(brd.id);
        }
        boardMap.put(brd.id, brd);
    }

    public Board getBoard(){
        Board brd = boardMap.get(accessCfg.boardId);
        if (brd == null) {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
            brd = lka.fetchBoardFromId(accessCfg.boardId);
            if (brd != null) {
                setBoard(brd);
            }
        }
        return brd;
    }
}
