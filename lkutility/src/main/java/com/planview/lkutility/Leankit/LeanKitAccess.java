package com.planview.lkutility.leankit;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planview.lkutility.Access;
import com.planview.lkutility.ColNames;
import com.planview.lkutility.Debug;

public class LeanKitAccess {

	Access config = null;
	String reqType = null;
	String reqUrl = null;
	HttpEntity reqEnt = null;
	ArrayList<BasicNameValuePair> reqHdrs = new ArrayList<>();
	ArrayList<NameValuePair> reqParams = new ArrayList<>();
	Board[] boards = null;
	PoolingHttpClientConnectionManager cm = null;
	Debug d = new Debug();

	public LeanKitAccess(Access configp, Integer debugLevel) {
		config = configp;
		d.setLevel(debugLevel);

		// Check URL has a trailing '/' and remove
		if (config.getUrl().endsWith("/")) {
			config.setUrl(config.getUrl().substring(0, config.getUrl().length() - 1));
		}
		// We need to set to https later on
		if (!config.getUrl().startsWith("http")) {
			config.setUrl("https://" + config.getUrl());
		} else if (config.getUrl().startsWith("http://")) {
			d.p(Debug.WARN, "http access to leankit not supported. Switching to https");
			config.setUrl("https://" + config.getUrl().substring(7));
		}

	}

	public String getCurrentUrl() {
		return config.getUrl();
	}

	public <T> ArrayList<T> read(Class<T> expectedResponseType) {

		reqHdrs.add(new BasicNameValuePair("Accept", "application/json"));
		reqHdrs.add(new BasicNameValuePair("Content-type", "application/json"));
		return readRaw(expectedResponseType);
	}

	public <T> ArrayList<T> readRaw(Class<T> expectedResponseType) {
		String bd = processRequest();
		if (bd == null) {
			return null;
		}
		JSONObject jresp = new JSONObject(bd);
		// Convert to a type to return to caller.

		if (jresp.has("error") || jresp.has("statusCode")) {
			d.p(Debug.ERROR, "\"%s\" gave response: \"%s\"\n", reqUrl, jresp.toString());
			System.exit(1);
		} else if (jresp.has("pageMeta")) {
			JSONObject pageMeta = new JSONObject(jresp.get("pageMeta").toString());

			int totalRecords = pageMeta.getInt("totalRecords");

			// Unfortunately, we need to know what sort of item to get out of the json
			// object. Doh!
			String fieldName = null;
			ArrayList<T> items = new ArrayList<T>();
			String[] typename = expectedResponseType.getName().split("\\.");
			switch (typename[typename.length - 1]) {
				case "Board":
					fieldName = "boards";
					break;
				case "BoardLevel":
					fieldName = "boardLevels";
					break;
				case "BoardUser":
					fieldName = "boardUsers";
					break;
				case "User":
					fieldName = "users";
					break;
				case "Card":
				case "Task":
					fieldName = "cards";
					break;
				case "Lane":
					fieldName = "lanes";
					break;
				case "Comment":
					fieldName = "comments";
					break;
				default:
					d.p(Debug.ERROR, "Unsupported item type requested from server API: %s\n", bd);
					return null;
			}
			// Got something to return
			ObjectMapper om = new ObjectMapper();
			om.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
			om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JSONArray p = (JSONArray) jresp.get(fieldName);
			Integer accumulatedCount = pageMeta.getInt("endRow");

			// if (accumulatedCount >= totalRecords) {
			// Add the returned items to the array
			for (int i = 0; i < p.length(); i++) {
				try {
					items.add(om.readValue(p.get(i).toString(), expectedResponseType));
				} catch (JsonProcessingException | JSONException e) {
					e.printStackTrace();
				}
			}
			// } else {
			/**
			 * We start off assuming that we begin at zero. If we find that there are less
			 * than there are available, we have to redo the processRequest with new offset
			 * and limit params
			 */
			// Length here may be limited to 200 by the API paging.
			d.p(Debug.VERBOSE, "Received %d %s (out of %d)\n", accumulatedCount,
					fieldName.substring(0,
							((accumulatedCount > 1) ? fieldName.length() : fieldName.length() - 1)),
					totalRecords);
			if (totalRecords > accumulatedCount) {

				Iterator<NameValuePair> it = reqParams.iterator();
				int acc = 0, offsetIdx = -1;
				while (it.hasNext()) {
					NameValuePair vp = it.next();
					if (vp.getName() == "offset") {
						offsetIdx = acc;
						break;
					}
					acc++;
				}

				if (offsetIdx >= 0) {
					reqParams.remove(offsetIdx);
					reqParams.add(new BasicNameValuePair("offset", accumulatedCount.toString()));
					/**
					 * This is slightly dangerous as it is a recursive call to get more stuff.
					 */
					ArrayList<T> childItems = readRaw(expectedResponseType);
					if (childItems != null) {
						items.addAll(childItems);
					}
					accumulatedCount = items.size();
				}
			}
			return items;
		} else {
			ArrayList<T> items = new ArrayList<T>();
			ObjectMapper om = new ObjectMapper();
			om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			switch (expectedResponseType.getSimpleName()) {
				case "CardType":
				case "Lane": {
					// Getting CardTypes comes here, for example.
					Iterator<String> sItor = jresp.keys();
					String iStr = sItor.next();
					JSONArray p = (JSONArray) jresp.get(iStr);
					for (int i = 0; i < p.length(); i++) {
						try {
							items.add(om.readValue(p.get(i).toString(), expectedResponseType));
						} catch (JsonProcessingException | JSONException e) {
							e.printStackTrace();
						}
					}
					break;
				}
				// Returning a single item from a search for example
				case "Board": {
					try {
						JSONObject bdj = new JSONObject(bd);
						// Cannot process one of these if we don't know what's going to be in there!!!!
						if (bdj.has("userSettings")) {
							bdj.remove("userSettings");
						}
						if (bdj.has("integrations")) {
							bdj.remove("integrations");
						}
						items.add(om.readValue(bdj.toString(), expectedResponseType));
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
					break;
				}
				case "BoardLevel": {
					JSONObject bdj = new JSONObject(bd);
					JSONArray bdlvls = (JSONArray) bdj.get("boardLevels");
					for (int i = 0; i < bdlvls.length(); i++) {
						try {
							items.add(om.readValue(bdlvls.get(i).toString(), expectedResponseType));
						} catch (JsonProcessingException | JSONException e) {
							e.printStackTrace();
						}
					}
					break;
				}
				default: {
					d.p(Debug.ERROR, "oops! don't recognise requested item type \"%s\"\n",
							expectedResponseType.getSimpleName());
					System.exit(2);
				}
			}
			return items;
		}
		return null;
	}

	/**
	 * 
	 * @param <T>
	 * @param expectedResponseType
	 * @return string value of Id
	 * 
	 *         Create something and return just the id to it.
	 */

	public <T> T execute(Class<T> expectedResponseType) {
		reqHdrs.clear();
		reqHdrs.add(new BasicNameValuePair("Accept", "application/json"));
		reqHdrs.add(new BasicNameValuePair("Content-type", "application/json"));

		String result = processRequest();
		if (result != null) {
			ObjectMapper om = new ObjectMapper();
			om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			om.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
			try {
				return om.readValue(result, expectedResponseType);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private String processRequest() {
		try {
			HttpEntity hpe = processRawRequest();
			if (hpe != null) {
				return EntityUtils.toString(hpe);
			}
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private HttpEntity processRawRequest() {

		// Deal with delays, retries and timeouts
		HttpClientBuilder cbldr = HttpClients.custom().setConnectionManager(cm);
		RequestConfig.Builder configBuilder = RequestConfig.custom();
		configBuilder.setSocketTimeout(40000); // Set all timeouts to 40sec.
		configBuilder.setConnectTimeout(40000);
		configBuilder.setConnectionRequestTimeout(40000);
		cbldr.setDefaultRequestConfig(configBuilder.build());
		CloseableHttpClient client = cbldr.build();
		HttpResponse httpResponse = null;
		HttpEntity result = null;
		try {
			// Add the user credentials to the request
			if (config.getApiKey() != null) {
				reqHdrs.add(new BasicNameValuePair("Authorization", "Bearer " + config.getApiKey()));
			} else {
				d.p(Debug.ERROR, "No valid apiKey provided to LKA\n");
			}
			HttpRequestBase request = null;
			switch (reqType) {
				case "POST": {
					request = new HttpPost(reqUrl);
					((HttpPost) request).setEntity(reqEnt);
					break;
				}
				case "PUT": {
					request = new HttpPut(reqUrl);
					((HttpPut) request).setEntity(reqEnt);
					break;
				}
				case "DELETE": {
					request = new HttpDelete(reqUrl);
					break;
				}
				case "PATCH": {
					request = new HttpPatch(reqUrl);
					((HttpPatch) request).setEntity(reqEnt);
					break;
				}
				default: {
					request = new HttpGet(reqUrl);
					break;
				}
			}

			for (int i = 0; i < reqHdrs.size(); i++) {
				request.addHeader(reqHdrs.get(i).getName(), reqHdrs.get(i).getValue());
			}
			String bldr = "";
			Iterator<NameValuePair> rpi = reqParams.iterator();
			while (rpi.hasNext()) {
				bldr = bldr + "&" + rpi.next().toString();
			}
			if (bldr.length() > 0) {
				bldr = "?" + bldr.substring(1);
			}
			request.setURI(new URI(config.getUrl() + reqUrl + bldr));
			d.p(Debug.VERBOSE, "%s\n", request.toString());
			if (reqEnt != null) { 
				d.p(Debug.VERBOSE, "Content: %s\n",IOUtils.toString(reqEnt.getContent(), "UTF-8")); 
			}
			httpResponse = client.execute(request);
			d.p(Debug.VERBOSE, "%s\n", httpResponse.getStatusLine());

			Boolean entityTaken = false;
			switch (httpResponse.getStatusLine().getStatusCode()) {
				case 200: // Card updated
				case 201: // Card created
				{
					result = httpResponse.getEntity();
					entityTaken = true;
					break;
				}
				case 204: // No response expected
				{
					break;
				}
				case 400: {
					d.p(Debug.ERROR, "Bad request: %s\n", request.toString());
					break;
				}
				case 401: {
					d.p(Debug.ERROR, "Unauthorised. Check Credentials in spreadsheet: %s\n", request.toString());
					break;
				}
				case 403: {
					d.p(Debug.ERROR, "Forbidden by server: %s\n", request.toString());
					break;
				}
				case 429: { // Flow control
					LocalDateTime retryAfter = LocalDateTime.parse(httpResponse.getFirstHeader("retry-after").getValue(),
							DateTimeFormatter.RFC_1123_DATE_TIME);
					LocalDateTime serverTime = LocalDateTime.parse(httpResponse.getFirstHeader("date").getValue(),
							DateTimeFormatter.RFC_1123_DATE_TIME);
					Long timeDiff = ChronoUnit.MILLIS.between(serverTime, retryAfter);
					d.p(Debug.INFO, "Received 429 status. waiting %.2f seconds\n", ((1.0 * timeDiff) / 1000.0));
					EntityUtils.consumeQuietly(httpResponse.getEntity());
					try {
						TimeUnit.MILLISECONDS.sleep(timeDiff);
					} catch (InterruptedException e) {
						d.p(Debug.ERROR, "(L2) %s\n", e.getMessage());
					}
					result = processRawRequest();
					break;
				}
				case 422: { // Unprocessable Parameter
					d.p(Debug.WARN, "Parameter Error in request: %s \n%s\n", request.toString(),
							EntityUtils.toString(httpResponse.getEntity()));
					break;
				}
				case 404: { // Item not found
					d.p(Debug.WARN, "Item not found: %s\n", httpResponse.toString());
					break;
				}
				case 409: { // Conflict
					d.p(Debug.WARN, "Conflict Error in request: %s \n%s\n", request.toString(),
							EntityUtils.toString(httpResponse.getEntity()));
					break;
				}
				case 408: // Request timeout - try your luck with another one....
				case 500: // Server fault
				case 502: // Bad Gateway
				case 503: // Service unavailable
				case 504: // Gateway timeout
				{
					d.p(Debug.ERROR, "Received %d status. retrying in 5 seconds\n",
							httpResponse.getStatusLine().getStatusCode());
					try {
						EntityUtils.consumeQuietly(httpResponse.getEntity());
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						d.p(Debug.ERROR, "(L1) %s\n", e.getMessage());
					}
					result = processRawRequest();
					break;
				}
				default: {
					d.p(Debug.ERROR, "Network fault: %s\n", httpResponse.toString());
					break;
				}
			}
			if (!entityTaken) {
				EntityUtils.consumeQuietly(httpResponse.getEntity()); // Tidy up because the java library has a
																		// 'feature'
			}
		} catch (IOException e) {
			d.p(Debug.ERROR, "(L3) %s\n", e.getMessage());
			System.exit(3);
		} catch (URISyntaxException e1) {
			// Should never happen, but to keep the compiler happy.....
			e1.printStackTrace();
		}
		return result;
	}

	public ArrayList<BoardLevel> fetchBoardLevels() {
		reqType = "GET";
		reqUrl = "/io/boardLevel";
		reqParams.clear();
		reqHdrs.clear();
		reqEnt = null;
		ArrayList<BoardLevel> levels = read(BoardLevel.class);
		return levels;
	}

	public ArrayList<CardType> fetchCardTypes(String boardId) {
		reqType = "GET";
		reqUrl = "/io/board/" + boardId + "/cardType";
		reqParams.clear();
		reqHdrs.clear();
		reqEnt = null;
		reqParams.add(new BasicNameValuePair("limit", "200"));
		reqParams.add(new BasicNameValuePair("offset", "0"));
		ArrayList<CardType> brd = read(CardType.class);
		if (brd != null) {
			if (brd.size() > 0) {
				return brd;
			}
		}
		return null;
	}

	public Lane[] fetchLanes(String boardId) {
		reqType = "GET";
		reqUrl = "/io/board/" + boardId + "/";
		reqParams.clear();
		reqHdrs.clear();
		reqEnt = null;
		ArrayList<Board> brd = read(Board.class);
		if (brd != null) {
			if (brd.size() > 0) {
				return brd.get(0).lanes;
			}
		}
		return null;
	}

	public ArrayList<Lane> fetchTaskLanes(String cardId) {
		reqType = "GET";
		reqUrl = "/io/card/" + cardId + "/taskboard";
		reqParams.clear();
		reqHdrs.clear();
		reqEnt = null;
		ArrayList<Lane> lanes = read(Lane.class);
		return lanes;
	}

	public ArrayList<Task> fetchTasks(String cardId) {
		reqType = "GET";
		reqUrl = "io/card/" + cardId + "/tasks";
		reqParams.clear();
		reqHdrs.clear();
		reqEnt = null;
		reqParams.add(new BasicNameValuePair("limit", "200"));
		reqParams.add(new BasicNameValuePair("offset", "0"));
		ArrayList<Task> tasks = read(Task.class);
		return tasks;
	}

	public ArrayList<Task> fetchTaskIds(String cardId) {
		reqType = "GET";
		reqUrl = "/io/card/" + cardId + "/tasks";
		reqParams.clear();
		reqParams.add(new BasicNameValuePair("only", "id"));
		reqHdrs.clear();
		reqEnt = null;
		reqParams.add(new BasicNameValuePair("limit", "200"));
		reqParams.add(new BasicNameValuePair("offset", "0"));
		ArrayList<Task> tasks = read(Task.class);
		return tasks;
	}

	private ArrayList<Board> fetchBoardsFromName(String name) {
		reqParams.clear();
		reqHdrs.clear();
		reqType = "GET";
		reqUrl = "/io/board";
		reqEnt = null;
		reqParams.add(new BasicNameValuePair("search", name));

		// Once you get the boards, you could cache them. There may be loads, but
		// shouldn't max
		// out memory.
		return read(Board.class);
	}

	public Board updateBoardById(String id, JSONObject updates) {
		reqHdrs.clear();
		reqType = "PATCH";
		reqUrl = "/io/board/" + id;
		reqEnt = new StringEntity(updates.toString(), "UTF-8");
		reqParams.clear();
		return execute(Board.class);
	}

	public String archiveBoard(String id) {
		reqType = "POST";
		reqUrl = "/io/board/" + id + "/archive";
		reqParams.clear();
		reqHdrs.clear();
		reqEnt = null;
		return processRequest();
	}

	public String deleteCard(String id) {
		reqType = "DELETE";
		reqHdrs.clear();
		reqParams.clear();
		reqUrl = "/io/card/" + id;
		reqEnt = null;
		return processRequest();
	}

	public String deleteBoard(String id) {
		reqType = "DELETE";
		reqHdrs.clear();
		reqParams.clear();
		reqUrl = "/io/board/" + id;
		reqEnt = null;
		return processRequest();
	}

	public ArrayList<Comment> fetchCommentsForCard(Card cd) {
		reqParams.clear();
		reqHdrs.clear();
		reqParams.add(new BasicNameValuePair("limit", "200"));
		reqParams.add(new BasicNameValuePair("offset", "0"));
		reqType = "GET";
		reqUrl = "/io/card/" + cd.id + "/comment";
		reqEnt = null;
		return read(Comment.class);
	}

	public ArrayList<Card> fetchCardIdsFromBoard(String id, Boolean includeArchived) {
		reqParams.add(new BasicNameValuePair("only", "id"));
		return fetchCardsFromBoard(id, includeArchived);
	}

	public ArrayList<Card> fetchCardsFromBoard(String id, Boolean includeArchived) {
		reqParams.clear();
		reqParams.add(new BasicNameValuePair("board", id));
		reqParams.add(new BasicNameValuePair("limit", "200"));
		reqParams.add(new BasicNameValuePair("offset", "0"));
		// We handle tasks by getting them on a card by card basis
		reqParams.add(new BasicNameValuePair("select", "cards"));

		reqHdrs.clear();
		reqType = "GET";
		reqUrl = "/io/card";
		reqEnt = null;
		if (includeArchived) {
			reqParams.add(new BasicNameValuePair("lane_class_types", "backlog,active,archive"));
			reqParams.add(new BasicNameValuePair("deleted", "0"));
		} else {
			reqParams.add(new BasicNameValuePair("lane_class_types", "backlog,active"));
			reqParams.add(new BasicNameValuePair("deleted", "0"));
		}
		return read(Card.class);
	}

	public Board fetchBoardFromTitle(String BoardName) {
		reqType = "GET";
		reqParams.clear();
		reqHdrs.clear();
		reqEnt = null;
		try {
			reqUrl = "/io/board?search=" + URLEncoder.encode(BoardName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			d.p(Debug.ERROR, "Cannot encode board name %s\n", BoardName);
			return null;
		}
		ArrayList<Board> boards = read(Board.class);
		for (int i = 0; i < boards.size(); i++) {
			if (boards.get(i).title.equals(BoardName)) {
				return boards.get(i);
			}
		}
		return null;
	}

	public Board fetchBoardFromId(String id) {
		reqType = "GET";
		reqParams.clear();
		reqHdrs.clear();
		reqUrl = "/io/board/" + id;
		reqEnt = null;
		reqParams.add(new BasicNameValuePair("returnFullRecord", "true"));

		ArrayList<Board> results = read(Board.class);
		if ((results != null) && (results.size() > 0)) {
			return results.get(0);
		}
		return null;
	}

	public byte[] fetchAttachment(String cardId, String attId) {
		reqType = "GET";
		reqParams.clear();
		reqHdrs.clear();
		reqEnt = null;
		reqUrl = "/io/card/" + cardId + "/attachment/" + attId + "/content";

		HttpEntity he = processRawRequest();
		String[] typeStr = he.getContentType().getValue().split(";");
		switch (typeStr[0]) {
			case "image/jpeg":
			default: {
				d.p(Debug.INFO, "Downloaded attachment type: %s\n", typeStr[0]);
				try {
					return EntityUtils.toByteArray(he);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		return null;
	}

	public Board fetchBoard(String name) {

		ArrayList<Board> brd = fetchBoardsFromName(name);
		Board bd = null;
		if (brd != null) {
			if (brd.size() > 0) {
				// We found one or more with this name search. First try to find an exact match
				Iterator<Board> bItor = brd.iterator();
				while (bItor.hasNext()) {
					Board b = bItor.next();
					if (b.title.equals(name)) {
						bd = b;
					}
				}
				// Then take the first if that fails
				if (bd == null)
					bd = brd.get(0);
				return fetchBoardFromId(bd.id);
			}
		}
		return null;
	}

	public User fetchUserById(String id) {
		reqUrl = "/io/user/" + id;
		reqType = "GET";
		reqParams.clear();
		reqHdrs.clear();

		return execute(User.class);
	}

	public User fetchUserByName(String username) {
		reqUrl = "/io/user";
		reqType = "GET";
		reqParams.clear();
		reqHdrs.clear();
		reqParams.add(new BasicNameValuePair("search", username));

		ArrayList<User> userd = read(User.class);
		User user = null;

		if (userd != null) {
			if (userd.size() > 0) {
				// We found one or more with this name search. First try to find an exact match
				Iterator<User> uItor = userd.iterator();
				while (uItor.hasNext()) {
					User u = uItor.next();
					if (u.username.equals(username)) {
						user = u;
					}
				}
				// Then take the first if that fails
				if (user == null)
					user = userd.get(0);
				return user;
			}
		}
		return null;
	}

	private String sendAttachment(String id, String filename) {
		reqType = "POST";
		reqUrl = "/io/card/" + id + "/attachment";
		reqParams.clear();
		reqHdrs.clear();
		File atchmt = new File(filename);
		FileBody fb = new FileBody(atchmt);
		MultipartEntityBuilder mpeb = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addTextBody("Description", "Auto-generated from Script").addPart("file", fb);
		reqEnt = mpeb.build();
		return processRequest();
	}

	private Card prioritiseCard(Card card, int idx) {
		reqType = "POST";
		reqUrl = "/io/card/move";
		reqParams.clear();
		reqHdrs.clear();
		JSONObject ent = new JSONObject();
		String[] ps = new String[1];
		ps[0] = card.id;
		ent.put("cardIds", ps);
		JSONObject dest = new JSONObject();
		dest.put("index", idx);
		dest.put("laneId", card.lane.id);
		ent.put("destination", dest);
		reqEnt = new StringEntity(ent.toString(), "UTF-8");
		return execute(Card.class);
	}

	private String postComment(String id, String comment) {
		reqType = "POST";
		reqUrl = "/io/card/" + id + "/comment";
		reqParams.clear();
		reqHdrs.clear();
		JSONObject ent = new JSONObject();
		ent.put("text", comment);
		reqEnt = new StringEntity(ent.toString(), "UTF-8");

		Comment c = execute(Comment.class);
		if (c != null) {
			return c.id;
		} else
			return null;
	}

	public Card fetchCard(String id) {
		reqType = "GET";
		reqUrl = "/io/card/" + id;
		reqParams.clear();
		reqHdrs.clear();
		reqParams.add(new BasicNameValuePair("returnFullRecord", "true"));
		return execute(Card.class);
	}

	/**
	 * Must have a unique title string or returns null
	 * 
	 * @param boardId
	 *                Restrict search to this board or null, if global
	 * @param title
	 * @return single card or null
	 */
	public Card fetchCardByTitle(String boardId, String title) {
		reqType = "GET";
		reqUrl = "/io/card";
		reqParams.clear();
		reqHdrs.clear();
		String encoded = null;
		try {
			// Quote the string so we don't get partial matches
			encoded = URLEncoder.encode("\"" + title + "\"", "UTF-8");

		} catch (UnsupportedEncodingException e) {
			// Encoder needs a try/catch, so we just print and continue
			e.printStackTrace();
		}
		reqParams.add(new BasicNameValuePair("search", encoded));
		reqParams.add(new BasicNameValuePair("board", boardId));
		ArrayList<Card> cards = read(Card.class);
		for (int i = 0; i < cards.size(); i++) {
			if (cards.get(i).title.equals(title)) {
				return cards.get(i);
			}
		}
		return null;
	}

	public ArrayList<BoardUser> fetchUsers(String boardId) {
		reqType = "GET";
		reqUrl = "/io/board/" + boardId + "/user";
		reqParams.clear();
		reqHdrs.clear();
		reqParams.add(new BasicNameValuePair("limit", "100"));
		reqParams.add(new BasicNameValuePair("offset", "0"));
		return read(BoardUser.class);
	}

	private Integer findTagIndex(Card card, String name) {
		Integer index = -1;
		String[] names = card.tags;
		if (names != null) {
			for (int i = 0; i < names.length; i++) {
				if (names[i].equals(name)) {
					index = i;
				}
			}
		}
		return index;
	}

	public Card updateCardFromId(Board brd, Card card, JSONObject updates) {

		// Create Leankit updates from the list
		JSONArray jsa = new JSONArray();
		Iterator<String> keys = updates.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			JSONObject values = (JSONObject) updates.get(key);
			switch (key) {
				case ColNames.BLOCKED_REASON: {
					if (values.get("value").toString().length() <= 1) {

						JSONObject upd = new JSONObject();
						upd.put("op", "replace");
						upd.put("path", "/isBlocked");
						upd.put("value", false);
						jsa.put(upd);

					} else if (values.get("value").toString().startsWith("-")) {
						// Make it startsWith rather than equals just
						// in case user forgets
						JSONObject upd1 = new JSONObject();
						upd1.put("op", "replace");
						upd1.put("path", "/isBlocked");
						upd1.put("value", false);
						jsa.put(upd1);
						JSONObject upd2 = new JSONObject();
						upd2.put("op", "add");
						upd2.put("path", "/blockReason");
						upd2.put("value", values.get("value").toString().substring(1));
						jsa.put(upd2);
					} else {
						JSONObject upd1 = new JSONObject();
						upd1.put("op", "replace");
						upd1.put("path", "/isBlocked");
						upd1.put("value", true);
						jsa.put(upd1);
						JSONObject upd2 = new JSONObject();
						upd2.put("op", "add");
						upd2.put("path", "/blockReason");
						upd2.put("value", values.get("value").toString());
						jsa.put(upd2);
					}
					break;
				}
				case "Parent": {
					if ((values.get("value") == null) || (values.get("value").toString() == "")
							|| (values.get("value").toString() == "0")) {
						d.p(Debug.ERROR, "Trying to set parent of %s to value \"%s\"\n", card.id,
								values.get("value").toString());
					} else if (values.get("value").toString().startsWith("-")) {
						JSONObject upd2 = new JSONObject();
						upd2.put("op", "remove");
						upd2.put("path", "/parentCardId");
						upd2.put("value", values.get("value").toString().substring(1));
						jsa.put(upd2);
					} else {
						JSONObject upd2 = new JSONObject();
						upd2.put("op", "add");
						upd2.put("path", "/parentCardId");
						upd2.put("value", values.get("value").toString());
						jsa.put(upd2);
					}
					break;
				}
				case "Lane": {
					// Need to find the lane on the board and set the card to be in it.
					JSONObject upd1 = new JSONObject();
					upd1.put("op", "replace");
					upd1.put("path", "/laneId");
					upd1.put("value", values.get("value").toString());
					jsa.put(upd1);
					if (values.has("value2")) {
						JSONObject upd2 = new JSONObject();
						upd2.put("op", "add");
						upd2.put("path", "/wipOverrideComment");
						upd2.put("value", values.get("value2").toString().trim());
						jsa.put(upd2);
					}
					break;
				}
				case ColNames.TAGS: {
					// Need to add or remove based on what we already have?
					// Or does add/remove ignore duplicate calls. Trying this first.....
					if (values.get("value").toString().toString().startsWith("-")) {
						Integer tIndex = findTagIndex(card, values.get("value").toString().substring(1));
						// If we found it, we can remove it
						if (tIndex >= 0) {
							JSONObject upd = new JSONObject();
							upd.put("op", "remove");
							upd.put("path", "/tags/" + tIndex);
							jsa.put(upd);
						}
					} else {
						JSONObject upd = new JSONObject();
						upd.put("op", "add");
						upd.put("path", "/tags/-");
						upd.put("value", values.get("value").toString());
						jsa.put(upd);
					}
					break;
				}
				case ColNames.ASSIGNED_USERS: {
					// Need to add or remove based on what we already have?
					// Or does add/remove ignore duplicate calls. Trying this first.....
					if (values.get("value").toString().startsWith("-")) {
						JSONObject upd = new JSONObject();
						upd.put("op", "remove");
						upd.put("path", "/assignedUserIds");
						upd.put("value", values.get("value").toString());
						jsa.put(upd);
					} else {
						JSONObject upd = new JSONObject();
						upd.put("op", "add");
						upd.put("path", "/assignedUserIds/-");
						upd.put("value", values.get("value").toString());
						jsa.put(upd);
					}
					break;
				}
				case ColNames.EXTERNAL_LINK: {
					JSONObject link = new JSONObject();
					JSONObject upd = new JSONObject();
					String[] bits = values.get("value").toString().split(",");
					if (bits.length != 2) {
						d.p(Debug.WARN, "Could not extract externalLink from %s (possible ',' in label?)",
								values.get("value").toString());
						break;
					}
					link.put("label", bits[0]);
					link.put("url", bits[1].trim());
					upd.put("op", "replace");
					upd.put("path", "/externalLink");
					upd.put("value", link);
					jsa.put(upd);
					break;
				}
				case ColNames.CUSTOM_ICON: {
					if (brd.classOfServiceEnabled) {
						if (brd.classesOfService != null) {
							for (int i = 0; i < brd.classesOfService.length; i++) {
								if (brd.classesOfService[i].name.equals(values.get("value"))) {
									JSONObject upd = new JSONObject();
									upd.put("op", "replace");
									upd.put("path", "/customIconId");
									upd.put("value", brd.classesOfService[i].id);
									jsa.put(upd);
									break;
								}
							}
						}
					}
					break;
				}
				case "attachments": {
					sendAttachment(card.id, values.get("value").toString());
					break;
				}

				case "comments": {
					postComment(card.id, values.get("value").toString());
					break;
				}
				case ColNames.INDEX: {
					prioritiseCard(card, values.getInt("value"));
				}
				case "Task": {
					break;
				}
				case "CustomField": {
					CustomField[] cflds = brd.customFields;
					if (cflds != null) {
						for (int i = 0; i < cflds.length; i++) {
							if (cflds[i].label.equals(values.get("value"))) {
								JSONObject upd = new JSONObject();
								JSONObject val = new JSONObject();

								val.put("fieldId", cflds[i].id);
								val.put("value", values.get("value2"));

								upd.put("op", "add");
								upd.put("path", "/customFields/-");
								upd.put("value", val);
								jsa.put(upd);
							}
						}
					}
					break;
				}
				// Mismatch between UI and database in LK.
				case ColNames.PRIORITY: {
					JSONObject upd = new JSONObject();
					upd.put("op", "replace");
					upd.put("path", "/" + key);
					upd.put("value", values.get("value").toString().toLowerCase());
					jsa.put(upd);
					break;
				}
				default: {
					JSONObject upd = new JSONObject();
					upd.put("op", "replace");
					upd.put("path", "/" + key);
					upd.put("value", values.get("value"));
					jsa.put(upd);
					break;
				}
			}
		}
		reqType = "PATCH";
		reqUrl = "/io/card/" + card.id;
		reqEnt = new StringEntity(jsa.toString(), "UTF-8");
		reqParams.clear();
		return execute(Card.class);

	}

	public Card createCard(JSONObject jItem) {

		reqType = "POST";
		reqUrl = "/io/card/";
		reqParams.clear();
		reqParams.add(new BasicNameValuePair("returnFullRecord", "true"));
		reqEnt = new StringEntity(jItem.toString(), "UTF-8");
		return execute(Card.class);
	}

	public Board createBoard(JSONObject jItem) {

		reqType = "POST";
		reqUrl = "/io/board/";
		reqParams.clear();
		reqParams.add(new BasicNameValuePair("returnFullRecord", "true"));
		reqEnt = new StringEntity(jItem.toString(), "UTF-8");
		return execute(Board.class);
	}

	public Card addTaskToCard(String cardId, JSONObject item) {
		reqType = "POST";
		reqParams.clear();
		reqUrl = "/io/card/" + cardId + "/tasks";
		reqEnt = new StringEntity(item.toString(), "UTF-8");
		return execute(Card.class);
	}

	public CustomFieldResult fetchCustomFields(String id) {
		reqType = "GET";
		reqUrl = "/io/board/" + id + "/customfield";
		reqParams.clear();
		reqHdrs.clear();
		String results = processRequest();
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			return om.readValue(results, CustomFieldResult.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public CustomIconResult fetchCustomIcons(String id) {
		reqType = "GET";
		reqUrl = "/io/board/" + id + "/customIcon";
		reqParams.clear();
		reqHdrs.clear();
		String results = processRequest();
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			return om.readValue(results, CustomIconResult.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public LevelIds updateBoardLevels(JSONObject levels) {
		reqType = "PUT";
		reqUrl = "/io/boardLevel/";
		reqParams.clear();
		reqHdrs.clear();
		reqEnt = new StringEntity(levels.toString(), "UTF-8");
		return execute(LevelIds.class);
	}

	public CustomIcon createCustomIcon(String bId, JSONObject ci) {
		reqType = "POST";
		reqUrl = "/io/board/" + bId + "/customIcon";
		reqParams.clear();
		reqHdrs.clear();
		reqEnt = new StringEntity(ci.toString(), "UTF-8");
		return execute(CustomIcon.class);
	}

	public Layout updateBoardLayout(String bId, JSONObject lyt) {
		reqType = "PUT";
		reqUrl = "/io/board/" + bId + "/layout";
		reqParams.clear();
		reqHdrs.clear();
		reqEnt = new StringEntity(lyt.toString(), "UTF-8");
		return execute(Layout.class);
	}

	public String deleteCardType(String brdId, String id) {
		reqType = "DELETE";
		reqHdrs.clear();
		reqParams.clear();
		reqUrl = "/io/board/" + brdId + "/cardType/" + id;
		reqEnt = null;
		return processRequest();
	}
	
	public CardType updateCardType(String brdId, String id, JSONObject updates) {
		reqHdrs.clear();
		reqType = "PATCH";
		reqUrl = "/io/board/" + brdId + "/cardType/" + id;
		reqEnt = new StringEntity(updates.toString(), "UTF-8");
		reqParams.clear();
		return execute(CardType.class);
	}

	public CardType addCardType(String bId, JSONObject card) {
		reqType = "POST";
		reqUrl = "/io/board/" + bId + "/cardType";
		reqParams.clear();
		reqHdrs.clear();
		reqEnt = new StringEntity(card.toString(), "UTF-8");
		return execute(CardType.class);
	}

	public CustomField updateCustomField(String brdId, JSONArray op) {
		reqHdrs.clear();
		reqType = "PATCH";
		reqUrl = "/io/board/" + brdId + "/customField";
		reqEnt = new StringEntity(op.toString(), "UTF-8");
		reqParams.clear();
		return execute(CustomField.class);
	}

	public Lane updateLane(String brdId, String id, JSONObject updates) {
		reqHdrs.clear();
		reqType = "PATCH";
		reqUrl = "/io/board/" + brdId + "/lane/" + id;
		reqEnt = new StringEntity(updates.toString(), "UTF-8");
		reqParams.clear();
		return execute(Lane.class);
	}
}
