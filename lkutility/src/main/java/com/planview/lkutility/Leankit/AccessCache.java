package com.planview.lkutility.leankit;

import java.util.ArrayList;
import java.util.HashMap;

import com.planview.lkutility.Configuration;
import com.planview.lkutility.InternalConfig;

public class AccessCache {

    /** For now, we will put everything into HashMaps
     * At some later date, we could create a better organisation of info
     */
    HashMap<String, Board> boardMap = new HashMap<>();
    HashMap<String, Card> cardMap = new HashMap<>();
    HashMap<String, User> userIdMap = new HashMap<>();
    HashMap<String, User> usernameMap = new HashMap<>();
    HashMap<String, CustomField[]> customFieldMap = new HashMap<>();
    HashMap<String, CustomIcon[]> customIconMap = new HashMap<>();
    HashMap<String, ArrayList<Lane>> taskBoardMap = new HashMap<>();
    HashMap<String, ArrayList<BoardUser>> boardUserMap = new HashMap<>();
    HashMap<String, Task> taskMap = new HashMap<>();
    InternalConfig iCfg;
    Configuration accessCfg;
    
    public AccessCache(InternalConfig cfg, Configuration accCfg){
        iCfg = cfg;
        accessCfg = accCfg;
    }

    public void setCustomFields(CustomField[] cfm, String boardId) {
        if (customFieldMap.get(boardId) != null){
            customFieldMap.remove(boardId);
        }
        customFieldMap.put(boardId, cfm);
    }


    public CustomField[] getCustomFields(){
        return getCustomFields(accessCfg.boardId);
    }

    public CustomField[] getCustomFields(String boardId){
        CustomField[] cfm = customFieldMap.get(boardId);
        if (cfm == null) {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
            cfm = lka.fetchCustomFields(boardId).customFields;
            if (cfm != null) {
                setCustomFields(cfm, boardId);
            }
        }
        return cfm;
    }
    
    public void setBoard(Board brd) {
        if (boardMap.get(brd.id) != null){
            boardMap.remove(brd.id);
        }
        boardMap.put(brd.id, brd);
    }

    public Board getBoard(String boardId){
        Board brd = boardMap.get(boardId);
        if (brd == null) {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
            brd = lka.fetchBoardFromId(boardId);
            if (brd != null) {
                setBoard(brd);
            }
        }
        return brd;
    }
    
    public User getUserById(String id){
        User user = userIdMap.get(id);
        if (user == null) {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
            user = lka.fetchUserById(id);
            if (user != null) {
                setUser(user);
            }
        }
        return user;
    }
    public User getUserByName(String username){
        User user = usernameMap.get(username);
        if (user == null) {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
            user = lka.fetchUserByName(username);
            if (user != null) {
                setUser(user);
            }
        }
        return user;
    }

    public void setUser(User user) {
        if (userIdMap.get(user.id) != null){
            userIdMap.remove(user.id);
        }
        if (usernameMap.get(user.username) != null){
            usernameMap.remove(user.username);
        }
        userIdMap.put(user.id, user);
        usernameMap.put(user.username, user);
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

    public void setCustomIcons(CustomIcon[] cfm, String boardId) {
        if (customIconMap.get(boardId) != null){
            customIconMap.remove(boardId);
        }
        customIconMap.put(boardId, cfm);
    }

    public CustomIcon[] getCustomIcons(String boardId){
        CustomIcon[] cfm = customIconMap.get(boardId);
        if (cfm == null) {
            LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel, iCfg.cm);
            cfm = lka.fetchCustomIcons(boardId).customIcons;
            if (cfm != null) {
                setCustomIcons(cfm, boardId);
            }
        }
        return cfm;
    }

}
