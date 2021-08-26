package com.planview.lkutility.leankit;

import java.util.ArrayList;
import java.util.HashMap;

import com.planview.lkutility.Configuration;
import com.planview.lkutility.InternalConfig;

public class AccessCache {
    HashMap<String, Board> boardMap = new HashMap<>();
    HashMap<String, Card> cardMap = new HashMap<>();
    HashMap<String, ArrayList<Lane>> taskBoardMap = new HashMap<>();
    HashMap<String, ArrayList<BoardUser>> boardUserMap = new HashMap<>();
    HashMap<String, Task> taskMap = new HashMap<>();
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
    
    public void setCard(Card card) {
        if (cardMap.get(card.id) != null){
            cardMap.remove(card.id);
        }
        cardMap.put(card.id, card);
    }

    public Card getCard(String cardId){
        Card card = cardMap.get(cardId);
        if (card == null) {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
        card = lka.fetchCard(cardId);
            if (card != null) {
                setCard(card);
            }
        }
        return card;
    }
    
    public void setTaskBoard(String cardId, ArrayList<Lane> lanes) {
        if (taskBoardMap.get(cardId) != null){
            taskBoardMap.remove(cardId);
        }
        taskBoardMap.put(cardId, lanes);
    }

    public ArrayList <Lane> getTaskBoard(String cardId){
        ArrayList<Lane> lanes = taskBoardMap.get(cardId);
        if ( lanes == null) {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
            lanes = lka.fetchTaskLanes(cardId);
            if (lanes != null) {
                setTaskBoard(cardId, lanes);
            }
        }
        return lanes;
    }

    public void setBoardUsers(String brdId, ArrayList<BoardUser> users) {
        if (boardUserMap.get(brdId) != null){
            boardUserMap.remove(brdId);
        }
        boardUserMap.put(brdId, users);
    }

    public ArrayList <BoardUser> getBoardUsers(String brdId){
        ArrayList<BoardUser> users = boardUserMap.get(brdId);
        if ( users == null) {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
            users = lka.fetchUsers(brdId);
            if (users != null) {
                setBoardUsers(brdId, users);
            }
        }
        return users;
    }
}
