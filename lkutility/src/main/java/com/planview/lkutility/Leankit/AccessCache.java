package com.planview.lkutility.leankit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class AccessCache {

	/**
	 * For now, we will put everything into HashMaps
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

	public void setCustomFields(CustomField[] cfm, String title) {
		if (customFieldMap.get(title) != null) {
			customFieldMap.remove(title);
		}
		customFieldMap.put(title, cfm);
	}

	public CustomField[] getCustomFields(String title) {
		CustomField[] cfm = customFieldMap.get(title);
		return cfm;
	}

	public void setBoard(Board brd) {
		if (boardMap.get(brd.title) != null) {
			boardMap.remove(brd.title);
		}
		boardMap.put(brd.title, brd);
	}

	public void unsetBoardByTitle(String title) {
		boardMap.remove(title);
	}	
	
	public void unsetBoardById(String id) {
		Board brd = null;
		Iterator<Map.Entry<String, Board>>  es = boardMap.entrySet().iterator();
		while (es.hasNext()){
			Entry<String, Board> ent = es.next();
			if (ent.getValue().id.equals(id)){
				brd = ent.getValue();
			}
		}

		if (brd != null) boardMap.remove(brd.title);
	}	

	public void unsetCardByTitle(String title) {
		cardMap.remove(title);
	}

	public void unsetCardById(String id) {
		Card crd = null;
		Iterator<Map.Entry<String, Card>>  es = cardMap.entrySet().iterator();
		while (es.hasNext()){
			Entry<String, Card> ent = es.next();
			if (ent.getValue().id.equals(id)){
				crd = ent.getValue();
			}
		}

		if (crd != null) cardMap.remove(crd.title);
	}

	public Board getBoardByTitle(String title) {
		Board brd = boardMap.get(title);
		return brd;
	}

	public Board getBoardById(String id) {
		Board brd = null;
		Iterator<Map.Entry<String, Board>>  es = boardMap.entrySet().iterator();
		while (es.hasNext()){
			Entry<String, Board> ent = es.next();
			if (ent.getValue().id.equals(id)){
				brd = ent.getValue();
			}
		}

		return brd;
	}

	public User getUserById(String id) {
		User user = userIdMap.get(id);
		return user;
	}

	public User getUserByName(String username) {
		User user = usernameMap.get(username);
		return user;
	}

	public void setUser(User user) {
		if (userIdMap.get(user.id) != null) {
			userIdMap.remove(user.id);
		}
		if (usernameMap.get(user.username) != null) {
			usernameMap.remove(user.username);
		}
		userIdMap.put(user.id, user);
		usernameMap.put(user.username, user);
	}

	public void setCard(Card card) {
		if (cardMap.get(card.id) != null) {
			cardMap.remove(card.id);
		}
		cardMap.put(card.title, card);
	}

	public Card getCard(String cardId) {
		Card card = cardMap.get(cardId);
		return card;
	}

	public void setTaskBoard(String cardId, ArrayList<Lane> lanes) {
		if (taskBoardMap.get(cardId) != null) {
			taskBoardMap.remove(cardId);
		}
		taskBoardMap.put(cardId, lanes);
	}

	public ArrayList<Lane> getTaskBoard(String cardId) {
		ArrayList<Lane> lanes = taskBoardMap.get(cardId);
		return lanes;
	}

	public void setBoardUsers(String BoardName, ArrayList<BoardUser> users) {
		if (boardUserMap.get(BoardName) != null) {
			boardUserMap.remove(BoardName);
		}
		boardUserMap.put(BoardName, users);
	}

	public ArrayList<BoardUser> getBoardUsers(String BoardName) {
		ArrayList<BoardUser> users = boardUserMap.get(BoardName);
		return users;
	}

	public void setCustomIcons(CustomIcon[] cfm, String boardId) {
		if (customIconMap.get(boardId) != null) {
			customIconMap.remove(boardId);
		}
		customIconMap.put(boardId, cfm);
	}

	public void unSetCustomIcons(String boardId){
		customIconMap.remove(boardId);
	}

	public CustomIcon[] getCustomIcons(String boardId) {
		CustomIcon[] cfm = customIconMap.get(boardId);
		return cfm;
	}

}
