package com.planview.lkutility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.planview.lkutility.leankit.AccessCache;
import com.planview.lkutility.leankit.Board;
import com.planview.lkutility.leankit.BoardLevel;
import com.planview.lkutility.leankit.BoardUser;
import com.planview.lkutility.leankit.Card;
import com.planview.lkutility.leankit.CardType;
import com.planview.lkutility.leankit.CustomField;
import com.planview.lkutility.leankit.CustomIcon;
import com.planview.lkutility.leankit.Lane;
import com.planview.lkutility.leankit.Layout;
import com.planview.lkutility.leankit.LeanKitAccess;
import com.planview.lkutility.leankit.Task;
import com.planview.lkutility.leankit.User;

public class LkUtils {

	/**
	 * Next up is all the Leankit access routines
	 * 
	 */
	static String LANE_DIVIDER_CHAR = "^";

	public static Lane[] getLanesFromBoardName(InternalConfig iCfg, AccessConfig accessCfg, String brdName) {
		Lane[] lanes = {};
		Board brd = null;
		AccessCache cache = accessCfg.getCache();
		if (cache != null) {
			brd = cache.getBoardByTitle(brdName);
			if (brd != null) {
				lanes = brd.lanes;
			}
		}
		if (brd == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			brd = lka.fetchBoardFromTitle(brdName);
			if (brd != null) {
				brd = lka.fetchBoardFromId(brd.id);
				if (cache != null) {
					cache.setBoard(brd);
				}
				lanes = brd.lanes;
			}
		}
		return lanes;
	}

	public static String getLanePathFromId(InternalConfig iCfg, AccessConfig accessCfg, String laneId) {

		Lane[] lanes = null;

		lanes = getLanesFromBoardName(iCfg, accessCfg, accessCfg.BoardName);
		return getLanePathFromLanes(lanes, laneId);
	}

	public static String getLanePathFromLanes(Lane[] lanes, String laneId) {

		Lane lane = getLaneFromId(lanes, laneId);
		String lanePath = "";
		if (lane == null) {
			return lanePath;
		} else {
			lanePath = lane.name;
		}

		while (lane.parentLaneId != null) {
			Lane parentLane = getLaneFromId(lanes, lane.parentLaneId);
			if (parentLane != null) {
				lanePath = parentLane.name + LANE_DIVIDER_CHAR + lanePath;
			}
			lane = parentLane;
		}
		return lanePath;
	}

	public static Lane getLaneFromId(Lane[] lanes, String id) {
		for (int i = 0; i < lanes.length; i++) {
			if (lanes[i].id.equals(id)) {
				return lanes[i];
			}
		}

		return null;
	}

	public static Lane[] getLanesByParentId(Lane[] lanes, String pId) {
		Lane[] pLanes = {};
		for (int i = 0; i < lanes.length; i++) {
			if ((pId == null) && (lanes[i].parentLaneId == null)) {
				pLanes = (Lane[]) ArrayUtils.add(pLanes, lanes[i]);
			} else if ((lanes[i].parentLaneId != null) && lanes[i].parentLaneId.equals(pId)) {
				pLanes = (Lane[]) ArrayUtils.add(pLanes, lanes[i]);
			}
		}
		return pLanes;
	}

	static Lane addLaneChildren(Lane[] flatMapLanes, Lane lane) {
		lane.children = getLanesByParentId(flatMapLanes, lane.id);
		for (int i = 0; i < lane.children.length; i++) {
			lane.children[i] = addLaneChildren(flatMapLanes, lane.children[i].copy());
			// remove the id after use
			lane.children[i].id = null;
		}
		return lane;
	}

	public static Layout createLaneTree(Lane[] flatMapLanes) {
		Layout treeLanes = new Layout();
		// Get the root lanes
		treeLanes.lanes = getLanesByParentId(flatMapLanes, null);
		for (int i = 0; i < treeLanes.lanes.length; i++) {
			treeLanes.lanes[i] = addLaneChildren(flatMapLanes, treeLanes.lanes[i].copy());
			// remove the id after use
			treeLanes.lanes[i].id = null;
		}
		// for (int i = 0; i < lanes.length; i++) {
		// treeLanes.lanes[i].id = null;
		// }
		return treeLanes;
	}

	public static Layout updateBoardLayout(InternalConfig cfg, AccessConfig accessCfg, Layout newLayout) {
		JSONObject lyt = new JSONObject(newLayout);
		LeanKitAccess lka = new LeanKitAccess(accessCfg, cfg.debugLevel);
		Board brd = lka.fetchBoardFromTitle(cfg.destination.BoardName);
		if (brd != null) {
			// Clear from cache
			AccessCache cache = accessCfg.getCache();
			if (cache != null) {
				cache.unsetBoardById(brd.id);
			}
			return lka.updateBoardLayout(brd.id, lyt);
		}
		return null;
	}

	public static String getUrl(InternalConfig iCfg, AccessConfig accessCfg) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		return lka.getCurrentUrl();
	}

	public static Card getCard(InternalConfig iCfg, AccessConfig accessCfg, String id) {
		Card card = null;
		AccessCache cache = accessCfg.getCache();

		if (cache != null) {
			card = cache.getCard(id);
		}
		if (card == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			card = lka.fetchCard(id);
			if ((card != null) && (cache != null))
				cache.setCard(card);
		}
		return card;
	}

	public static byte[] getAttachment(InternalConfig iCfg, AccessConfig accessCfg, String cardId, String attId) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		return lka.fetchAttachment(cardId, attId);
	}

	public static Board getBoardByTitle(InternalConfig iCfg, AccessConfig accessCfg) {
		Board brd = null;
		AccessCache cache = accessCfg.getCache();
		if (cache != null) {
			brd = cache.getBoardByTitle(accessCfg.BoardName);
		}
		if (brd == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			brd = lka.fetchBoardFromTitle(accessCfg.BoardName);
			if (brd != null) {
				brd = lka.fetchBoardFromId(brd.id); //Refetch to get full board
				if ((cache != null) && (brd != null))
					cache.setBoard(brd);
			}
		}
		return brd;
	}

	public static Board getBoardById(InternalConfig iCfg, AccessConfig accessCfg, String id) {
		Board brd = null;
		AccessCache cache = accessCfg.getCache();
		if (cache != null) {
			brd = cache.getBoardById(id);
		}
		if (brd == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			brd = lka.fetchBoardFromId(id);
			if ((brd != null) && (cache != null)) {
				cache.setBoard(brd);
			}
		}
		return brd;
	}

	public static ArrayList<Card> getCardIdsFromBoard(InternalConfig iCfg, AccessConfig accessCfg) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		Board brd = getBoardByTitle(iCfg, accessCfg);
		ArrayList<Card> cards = null;
		if (brd != null) {
			cards = lka.fetchCardIdsFromBoard(brd.id, iCfg.exportArchived);
		}
		return cards;
	}

	public static ArrayList<Task> getTaskIdsFromCard(InternalConfig iCfg, AccessConfig accessCfg, String cardId) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		ArrayList<Task> tasks = lka.fetchTaskIds(cardId);
		return tasks;
	}

	public static ArrayList<Task> getTasksFromCard(InternalConfig iCfg, AccessConfig accessCfg, String cardId) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		ArrayList<Task> tasks = lka.fetchTasks(cardId);
		return tasks;
	}

	public static ArrayList<CardType> getCardTypesFromBoard(InternalConfig iCfg, AccessConfig accessCfg) {
		Board brd = getBoardByTitle(iCfg, accessCfg);
		if (brd != null) {
			return getCardTypesFromBoard(iCfg, accessCfg, brd.title);
		}
		return null;
	}

	public static ArrayList<CardType> getCardTypesFromBoard(InternalConfig iCfg, AccessConfig accessCfg,
			String boardName) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		ArrayList<CardType> types = null;
		AccessCache cache = accessCfg.getCache();
		Board brd = null;
		if (cache != null) {
			brd = cache.getBoardByTitle(boardName);
			if (brd != null) {
				types = cache.getCardTypes(brd.id);
				if (types == null) {
					types = lka.fetchCardTypes(brd.id);
					if (types != null) {
						cache.setCardTypes(brd.id, types);
					}
				}
			}
		}
		if (brd == null) {
			brd = lka.fetchBoardFromTitle(boardName);
			if (brd != null) {
				if (cache != null) {
					brd = lka.fetchBoardFromId(brd.id); //Have to refetch the whole thing before we put it into cache
					cache.setBoard(brd);
				}
				types = lka.fetchCardTypes(brd.id);
				if ((types != null) && (cache != null)) {
					cache.setCardTypes(brd.id, types);
				}
			}
		}
		return types;
	}

	public static Card getCardByTitle(InternalConfig iCfg, AccessConfig accessCfg, String boardName, String title) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		AccessCache cache = accessCfg.getCache();
		Board brd = null;
		Card cd = null;
		if (cache != null) {
			cd = cache.getCardByTitle(title);
			if (cd != null) {
				return cd;
			}
			brd = cache.getBoardByTitle(boardName);
		}
		if (brd == null) { 
			brd = lka.fetchBoardFromTitle(boardName);
		}
		if (brd != null) {
			cd = lka.fetchCardByTitle(brd.id, title);
			if (cache != null) {
				cache.setCard(cd);
			}
		}
		return cd;
	}

	public static Card getCardByTitle(InternalConfig iCfg, AccessConfig accessCfg, String title) {
		return getCardByTitle(iCfg, accessCfg, accessCfg.BoardName, title);
	}

	public static CardType getCardTypeFromBoard(InternalConfig iCfg, AccessConfig accessCfg, String name,
			String boardName) {
		return getCardTypeFromList(getCardTypesFromBoard(iCfg, accessCfg, boardName), name);
	}

	public static CardType getCardTypeFromList(ArrayList<CardType> cardTypes, String name) {
		if (cardTypes != null) {
			Iterator<CardType> cti = cardTypes.iterator();
			while (cti.hasNext()) {
				CardType ct = cti.next();
				if (ct.getName().equals(name)) {
					return ct;
				}
			}
		}
		return null;
	}

	public static boolean removeCardTypeFromBoard(InternalConfig iCfg, AccessConfig accessCfg, CardType cardType) {
		Board brd = getBoardByTitle(iCfg, accessCfg);
		if (brd != null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			String retval = lka.deleteCardType(brd.id, cardType.getId());
			if (retval != null) {
				// Debug
			}
		}
		return false;
	}

	public static Card createCard(InternalConfig iCfg, AccessConfig accessCfg, JSONObject fieldLst) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		// First create an empty card and get back the full structure
		AccessCache cache = accessCfg.getCache();

		Card newCard = lka.createCard(fieldLst);
		if (newCard != null) {
			if (cache != null) {
				cache.setCard(newCard);
			}
		}
		return newCard;
	}

	public static Card updateCard(InternalConfig iCfg, AccessConfig accessCfg, String cardId, JSONObject updates) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		Card card = null;
		Board brd = null;
		AccessCache cache = accessCfg.getCache();

		if (cache != null) {
			card = cache.getCard(cardId);
			brd = cache.getBoardByTitle(accessCfg.BoardName);
		}
		if (card == null) {
			card = lka.fetchCard(cardId);
			if ((card != null) && (cache != null))
				cache.setCard(card);
		}
		if (brd == null) {
			brd = lka.fetchBoardFromId(accessCfg.BoardName);
			if ((brd != null) && (cache != null))
				cache.setBoard(brd);
		}

		if ((card != null) && (brd != null)) {
			card = lka.updateCardFromId(brd, card, updates);
			if ((cache != null) && (card != null)){
				cache.setCard(card);
			}
		}

		return card;
	}

	public static Board updateBoard(InternalConfig iCfg, AccessConfig accessCfg, String boardId, JSONObject updates) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		Board brd = null;
		AccessCache cache = accessCfg.getCache();

		if (cache != null) {
			brd = cache.getBoardById(boardId);
		}
		if (brd == null) {
			brd = lka.fetchBoardFromTitle(accessCfg.BoardName);
		}
		if (brd != null) {
			lka.updateBoardById(brd.id, updates); // returns 204 No Content
			brd = lka.fetchBoardFromId(brd.id); // so refetch
			if (brd != null) {
				if (cache != null) {
					cache.setBoard(brd);
				}
			}
		}
		return brd;
	}

	public static void archiveBoardById(InternalConfig iCfg, AccessConfig accessCfg, String boardId) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		AccessCache cache = accessCfg.getCache();

		if (cache != null) {
			cache.unsetBoardById(boardId);
		}
		lka.archiveBoard(boardId);
	}

	public static boolean deleteBoard(InternalConfig iCfg, AccessConfig accessCfg) {
		Board brd = getBoardByTitle(iCfg, accessCfg);
		if (brd != null) {
			deleteBoardById(iCfg, accessCfg, brd.id);
			return true;
		}
		return false;
	}

	public static void deleteBoardById(InternalConfig iCfg, AccessConfig accessCfg, String boardId) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		AccessCache cache = accessCfg.getCache();

		if (cache != null) {
			cache.unsetBoardById(boardId);
		}
		lka.deleteBoard(boardId);
	}

	public static void deleteCard(InternalConfig iCfg, AccessConfig accessCfg, String title) {
		Card crd = getCardByTitle(iCfg, accessCfg, title);
		if (crd != null)
			deleteCardById(iCfg, accessCfg, crd.id);
	}

	public static void deleteCardById(InternalConfig iCfg, AccessConfig accessCfg, String cardId) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		AccessCache cache = accessCfg.getCache();
		if (cache != null) {
			cache.unsetCardById(cardId);
		}
		lka.deleteCard(cardId);
	}

	static ArrayList<Lane> getLanesFromName(ArrayList<Lane> lanes, String name) {
		ArrayList<Lane> ln = new ArrayList<>();
		for (int i = 0; i < lanes.size(); i++) {
			String laneName = lanes.get(i).name;
			if (laneName.equals(name)) {
				ln.add(lanes.get(i));
			}
		}
		return ln;
	}

	public static Lane getLaneFromBoardTitle(InternalConfig iCfg, AccessConfig accessCfg, String boardName,
			String name) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		Board brd = null;
		AccessCache cache = accessCfg.getCache();
		if (cache != null) {
			brd = cache.getBoardByTitle(boardName);
		} else {
			brd = lka.fetchBoardFromTitle(boardName);
			if (brd != null) {
				brd = lka.fetchBoardFromId(brd.id);

			}
		}

		if (brd != null) {
			if (cache != null) {
				cache.setBoard(brd);
			}
			return getLaneFromString(brd, name);
		}

		return null;
	}

	public static Lane getLaneFromBoardId(InternalConfig iCfg, AccessConfig accessCfg, String id, String name) {
		Board brd = null;
		AccessCache cache = accessCfg.getCache();
		if (cache != null) {
			brd = cache.getBoardById(id);
		} else {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			brd = lka.fetchBoardFromId(id);
		}
		if (brd != null) {
			if (cache != null) {
				cache.setBoard(brd);
			}
			return getLaneFromString(brd, name);
		}
		return null;
	}

	static Lane getLaneFromId(ArrayList<Lane> allLanes, String id) {
		Lane lane = null;
		Iterator<Lane> iter = allLanes.iterator();
		while (iter.hasNext()) {
			Lane ln = iter.next();
			if (ln.id.equals(id)) {
				lane = ln;
				break;
			}
		}
		return lane;
	}

	static Lane getParentLane(ArrayList<Lane> searchLanes, Lane lane) {
		Lane parentLane = null;
		if (lane.parentLaneId != null) {
			parentLane = getLaneFromId(searchLanes, lane.parentLaneId);
		}
		return parentLane;
	}

	static Lane getLaneFromString(Board brd, String name) {
		// Split lane in spreadhseet into bits
		String[] lanes = name.split("\\^");

		// Get the list of lanes in the target board
		ArrayList<Lane> searchLanes = new ArrayList<>(Arrays.asList(brd.lanes));

		// Work out the default drop lane in case we can't locate the right lane
		Lane foundLane = null;
		Lane defaultLane = null;
		Iterator<Lane> ddlIter = searchLanes.iterator();
		while (ddlIter.hasNext()) {
			Lane cl = ddlIter.next();
			if (cl.isDefaultDropLane) {
				defaultLane = cl;
				break;
			}
		}

		// For each possible lane, check up its hierarchy to see if it matches the bits
		// we have
		int j = lanes.length - 1;
		// Find those lanes that match the 'bit'
		ArrayList<Lane> lanesToCheck = getLanesFromName(searchLanes, lanes[j]);

		Iterator<Lane> ltcIt = lanesToCheck.iterator();

		while (ltcIt.hasNext()) {
			Boolean found = true;
			Lane thisLane = ltcIt.next();
			Lane parentLane = getParentLane(searchLanes, thisLane);
			int k = j;
			while (parentLane != null) {
				if ((k > 0) && parentLane.name.equals(lanes[--k])) {
					parentLane = getParentLane(searchLanes, parentLane);
				} else {
					found = false;
					break;
				}
				if ((k > 0) && (parentLane == null)) {
					found = false;
				}
			}
			if ((k == 0) && found) {
				foundLane = thisLane;
				break;
			}
		}

		if (foundLane == null) {
			return defaultLane;
		}

		return foundLane;
	}

	public static Lane getLaneFromCard(InternalConfig iCfg, AccessConfig accessCfg, String cardId, String laneType) {
		Lane lane = null;
		ArrayList<Lane> lanes = null;
		AccessCache cache = accessCfg.getCache();

		if (cache != null) {
			lanes = cache.getTaskBoard(cardId);
		}
		if (lanes == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			lanes = lka.fetchTaskLanes(cardId);
		}
		if (lanes != null) {
			if (cache != null) {
				cache.setTaskBoard(cardId, lanes);
			}
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

	public static User getUser(InternalConfig iCfg, AccessConfig accessCfg, String id) {
		User user = null;
		AccessCache cache = accessCfg.getCache();
		if (cache != null) {
			user = cache.getUserById(id);
		}
		if (user == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			user = lka.fetchUserById(id);
			if ((user != null) && (cache != null)) {
				cache.setUser(user);
			}
		}
		return user;
	}

	public static ArrayList<BoardLevel> getBoardLevels(InternalConfig iCfg, AccessConfig accessCfg) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		return lka.fetchBoardLevels();
	}

	public static CustomField getCustomField(InternalConfig iCfg, AccessConfig accessCfg, String name) {
		CustomField[] cfs = getCustomFields(iCfg, accessCfg);
		for (int j = 0; j < cfs.length; j++) {
			if (cfs[j].label.equals(name)) {
				return cfs[j];
			}
		}
		return null;
	}

	public static CustomIcon getCustomIcon(InternalConfig iCfg, AccessConfig accessCfg, String name) {
		CustomIcon[] cis = getCustomIcons(iCfg, accessCfg);
		if (cis != null) {
			for (int j = 0; j < cis.length; j++) {
				if (cis[j].name.equals(name)) {
					return cis[j];
				}
			}
		}
		return null;
	}

	public static CustomIcon getCustomIcon(InternalConfig iCfg, AccessConfig accessCfg, String name, String boardId) {
		CustomIcon[] cis = getCustomIcons(iCfg, accessCfg, boardId);
		for (int j = 0; j < cis.length; j++) {
			if (cis[j].name.equals(name)) {
				return cis[j];
			}
		}
		return null;
	}

	public static CustomField[] getCustomFields(InternalConfig iCfg, AccessConfig accessCfg) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		CustomField[] fields = null;
		Board brd = null;
		AccessCache cache = accessCfg.getCache();
		if (cache != null) { // and store it in the cache if we have one
			brd = cache.getBoardByTitle(accessCfg.BoardName);
		}
		if (brd == null) {
			brd = lka.fetchBoardFromTitle(accessCfg.BoardName);
			if (brd != null) {
				brd = lka.fetchBoardFromId(brd.id); // Fetch the FULL listing of the board this time
				if ((brd != null) && (cache != null)) {
					cache.setBoard(brd);
				}
			}
		}
		if (brd != null)
			fields = getCustomFields(iCfg, accessCfg, brd.id);
		return fields;
	}

	public static CustomField[] getCustomFields(InternalConfig iCfg, AccessConfig accessCfg, String boardId) {
		CustomField[] fields = null;
		AccessCache cache = accessCfg.getCache();
		if (cache != null) {
			fields = cache.getCustomFields(boardId);
		}
		if (fields == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			fields = lka.fetchCustomFields(boardId).customFields;
			if ((fields != null) && (cache != null)) {
				cache.setCustomFields(fields, boardId);
			}
		}
		return fields;
	}

	public static CustomIcon[] getCustomIcons(InternalConfig iCfg, AccessConfig accessCfg, String boardId) {
		CustomIcon[] icons = null;

		AccessCache cache = accessCfg.getCache();
		if (cache != null) {
			icons = cache.getCustomIcons(boardId);
		}
		if (icons == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			Board brd = lka.fetchBoardFromId(boardId);
			if (brd != null)
				icons = lka.fetchCustomIcons(brd.id).customIcons;
			if ((icons != null) && (cache != null)) {
				cache.setCustomIcons(icons, boardId);
			}
		}
		return icons;
	}

	public static CustomIcon[] getCustomIcons(InternalConfig iCfg, AccessConfig accessCfg) {
		Board brd = getBoardByTitle(iCfg, accessCfg);
		if (brd != null)
			return getCustomIcons(iCfg, accessCfg, brd.id);
		return null;
	}

	public static User getUserByName(InternalConfig iCfg, AccessConfig accessCfg, String username) {
		User user = null;
		AccessCache cache = accessCfg.getCache();
		if (cache != null) {
			user = cache.getUserByName(username);
		}
		if (user == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			user = lka.fetchUserByName(username);
			if ((user != null) && (cache != null)) {
				cache.setUser(user);
			}
		}
		return user;
	}

	public static ArrayList<BoardUser> getUsers(InternalConfig iCfg, AccessConfig accessCfg) {
		return getUsers(iCfg, accessCfg, accessCfg.BoardName);
	}

	public static ArrayList<BoardUser> getUsers(InternalConfig iCfg, AccessConfig accessCfg, String boardName) {
		ArrayList<BoardUser> users = null;
		AccessCache cache = accessCfg.getCache();
		if (cache != null) {
			users = cache.getBoardUsers(boardName);
		}
		if (users == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			Board brd = lka.fetchBoardFromTitle(boardName);
			if (brd != null)
				users = lka.fetchUsers(brd.id);
			if ((users != null) && (cache != null)) {
				cache.setBoardUsers(brd.title, users);
			}
		}
		return users;
	}

	public static Card addTask(InternalConfig iCfg, AccessConfig accessCfg, String cardId, JSONObject item) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		Card card = lka.addTaskToCard(cardId, item);
		AccessCache cache = accessCfg.getCache();
		if ((cache != null) && (card != null)) {
			cache.setCard(card);
		}
		return card;
	}

	public static Board duplicateBoard(InternalConfig cfg) {
		LeanKitAccess lka = new LeanKitAccess(cfg.source, cfg.debugLevel);
		Board brd = lka.fetchBoardFromTitle(cfg.source.BoardName);
		if (brd != null) {
			JSONObject details = new JSONObject();
			details.put("title", cfg.destination.BoardName);
			details.put("fromBoardId", brd.id);
			details.put("includeExistingUsers", true);
			details.put("includeCards", false);
			details.put("isShared", true);
			details.put("sharedBoardRole", "boardUser");
			details.put("excludeCompletedAndArchiveViolations", true);
			return lka.createBoard(details);
		} else {
			return null;
		}
	}

	public static Board createBoard(InternalConfig cfg, AccessConfig accessCfg) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, cfg.debugLevel);
		JSONObject details = new JSONObject();
		details.put("title", accessCfg.BoardName);
		return lka.createBoard(details);
	}

	public static Boolean enableCustomIcons(InternalConfig cfg, AccessConfig accessCfg) {
		LeanKitAccess lka = new LeanKitAccess(cfg.destination, cfg.debugLevel);
		AccessCache cache = accessCfg.getCache();
		Board brd = null;
		if (cache != null) {
			brd = cache.getBoardByTitle(accessCfg.BoardName);
		}
		if (brd == null) {
			brd = lka.fetchBoardFromTitle(cfg.destination.BoardName);
			if ((brd != null) && (cache != null)) {
				cache.setBoard(brd);
			}
		}
		Boolean state = false;
		if (brd != null) {
			state = lka.fetchCustomIcons(brd.id) != null;
			JSONObject details = new JSONObject();
			details.put("enableCustomIcon", true);
			updateBoard(cfg, accessCfg, brd.id, details);
		}
		return state;
	}

	public static CustomIcon createCustomIcon(InternalConfig cfg, AccessConfig accessCfg, CustomIcon customIcon) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, cfg.debugLevel);
		AccessCache cache = accessCfg.getCache();
		Board brd = null;
		if (cache != null) {
			brd = cache.getBoardByTitle(accessCfg.BoardName);
		}
		if (brd == null) {
			brd = lka.fetchBoardFromTitle(accessCfg.BoardName);
		}
		if (brd != null) {
			JSONObject ci = new JSONObject(customIcon);
			ci.remove("id");
			ci.remove("iconPath");
			if (cache != null) {
				cache.unsetBoardById(brd.id);	//Need to refetch classesOfService (icons) next time
				cache.unSetCustomIcons(brd.id);
			}
			return lka.createCustomIcon(brd.id, ci);
		}
		return null;
	}

	public static void setBoardLevels(InternalConfig cfg, AccessConfig accessCfg, BoardLevel[] srcLevels) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, cfg.debugLevel);
		JSONObject levels = new JSONObject();
		levels.put("boardLevels", srcLevels);
		lka.updateBoardLevels(levels);
	}

	public static CardType updateCardType(InternalConfig cfg, AccessConfig accessCfg, String cardTypeId,
			CardType updates) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, cfg.debugLevel);
		AccessCache cache = accessCfg.getCache();
		Board brd = null;
		if (cache != null) {
			brd = cache.getBoardByTitle(accessCfg.BoardName);
		}
		if (brd == null) {
			brd = lka.fetchBoardFromTitle(accessCfg.BoardName);
		}
		if (brd != null) {
			return lka.updateCardType(brd.id, cardTypeId, new JSONObject(updates));
		}
		return null;
	}

	public static CardType addCardTypeToBoard(InternalConfig cfg, AccessConfig accessCfg, CardType card) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, cfg.debugLevel);
		AccessCache cache = accessCfg.getCache();
		Board brd = null;
		if (cache != null) {
			brd = cache.getBoardByTitle(accessCfg.BoardName);
		}
		if (brd == null) {
			brd = lka.fetchBoardFromTitle(cfg.destination.BoardName);
		}
		if (brd != null) {
			if (cache != null) {
				cache.unsetBoardById(brd.id);
			}
			return lka.addCardType(brd.id, new JSONObject(card));
		}
		return null;
	}

	public static CustomField updateCustomField(InternalConfig cfg, AccessConfig accessCfg, JSONArray op) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, cfg.debugLevel);
		AccessCache cache = accessCfg.getCache();
		Board brd = null;
		if (cache != null) {
			brd = cache.getBoardByTitle(accessCfg.BoardName);
		}
		if (brd == null) {
			brd = lka.fetchBoardFromTitle(cfg.destination.BoardName);
		}
		if (brd != null) {
			// Clear from cache

			if (cache != null) {
				cache.unsetBoardById(brd.id);
			}
			return lka.updateCustomField(brd.id, op);
		}
		return null;
	}

	public static void setSortedLanes(InternalConfig cfg, AccessConfig destination, Lane[] srcLanes, Layout newLayout) {

		// For each member in the layout tree we need to find the lane, from the path,
		// in the destination
		// And then PATCH the lane
		for (int i = 0; i < srcLanes.length; i++) {
			String srcLanePath = getLanePathFromLanes(srcLanes, srcLanes[i].id);
			Lane dLane = getLaneFromPath(newLayout.lanes, srcLanePath);
			if (dLane != null) {
				if (srcLanes[i].sortBy != null) {
					JSONObject updates = new JSONObject();
					updates.put("sortBy", srcLanes[i].sortBy);
					LeanKitAccess lka = new LeanKitAccess(destination, cfg.debugLevel);
					Board brd = lka.fetchBoardFromTitle(destination.getBoardName());
					if (brd != null)
						lka.updateLane(brd.id, dLane.id, updates);
				}

			}
		}
	}

	private static Lane getLaneFromPath(Lane[] laneTree, String srcLanePath) {
		String thisBit = srcLanePath;
		String leftOver = null;
		int thisIdx = srcLanePath.indexOf(LANE_DIVIDER_CHAR);
		if (thisIdx > 0) {
			thisBit = srcLanePath.substring(0, thisIdx);
			leftOver = srcLanePath.substring(thisIdx + 1);
		}
		// Find thisBit
		String testBit = thisBit;
		Lane ln = Arrays.stream(laneTree).filter(cf -> cf.title.equals(testBit)).findFirst()
				.orElse(null);
		Lane lnC = null;
		if (ln != null) {
			if (leftOver != null) {
				lnC = getLaneFromPath(ln.children, leftOver);
			}
		}
		return (lnC == null) ? ln : lnC;
	}

	static Lane[] flattenLanes(Lane[] laneTree) {
		Lane[] tree = {};
		for (Lane ln : laneTree) {
			tree = (Lane[]) ArrayUtils.add(tree, ln);
			if (ln.children != null) {
				tree = (Lane[]) ArrayUtils.addAll(tree, flattenLanes(ln.children));
			}
		}
		return tree;
	}

}
