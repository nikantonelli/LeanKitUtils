package com.planview.leankit;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planview.app.Configuration;
import com.planview.app.Debug;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
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
import java.net.URI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LeanKitAccess {

    Configuration config = null;
    String reqType = null;
    String reqUrl = null;
    HttpEntity reqEnt = null;
    ArrayList<BasicNameValuePair> reqHdrs = new ArrayList<>();
    ArrayList<NameValuePair> reqParams = new ArrayList<>();
    Board[] boards = null;
    PoolingHttpClientConnectionManager cm = null;
    Integer debugPrint = 0;

    public LeanKitAccess(Configuration configp, Integer dbp, PoolingHttpClientConnectionManager cmp) {
        config = configp;
        cm = cmp;
        // Check URL has a trailing '/'
        if (config.url.endsWith("/")) {
            config.url = config.url.substring(0, config.url.length() - 1);
        }
        // We need to set to https later on
        if (config.url.startsWith("https://")) {
            config.url = config.url.substring(8);
        } else if (config.url.startsWith("http://")) {
            dpf(Debug.WARN, "http access to leankit not supported. Switching to https");
            config.url = config.url.substring(7);
        }

        debugPrint = dbp;
    }

    private void dpf(Integer level, String fmt, Object... parms) {
        String lp = null;
        switch (level) {
            case 0: {
                lp = "INFO: ";
                break;
            }
            case 1: {
                lp = "ERROR: ";
                break;
            }
            case 2: {
                lp = "WARN: ";
                break;
            }
            case 3: {
                lp = "DEBUG: ";
                break;
            }
            case 4: {
                lp = "VERBOSE: ";
                break;
            }
        }
        if (level <= debugPrint) {
            System.out.printf(lp + fmt, parms);
        }
    }

    public <T> ArrayList<T> read(Class<T> expectedResponseType) {
        reqHdrs.clear();
        reqHdrs.add(new BasicNameValuePair("Accept", "application/json"));
        reqHdrs.add(new BasicNameValuePair("Content-type", "application/json"));
        String bd = processRequest();
        if (bd == null) {
            return null;
        }
        JSONObject jresp = new JSONObject(bd);
        // Convert to a type to return to caller.
        if (bd != null) {
            if (jresp.has("error") || jresp.has("statusCode")) {
                dpf(Debug.ERROR, "\"%s\" gave response: \"%s\"\n", reqUrl, jresp.toString());
                System.exit(1);
            } else if (jresp.has("pageMeta")) {
                JSONObject pageMeta = new JSONObject(jresp.get("pageMeta").toString());

                int totalReturned = pageMeta.getInt("totalRecords");
                // Unfortunately, we need to know what sort of item to get out of the json
                // object. Doh!
                String fieldName = null;
                ArrayList<T> items = new ArrayList<T>();
                String[] typename = expectedResponseType.getName().split("\\.");
                switch (typename[typename.length - 1]) {
                    case "Board":
                        fieldName = "boards";
                        break;
                    case "User":
                        fieldName = "users";
                        break;
                    case "Card":
                        fieldName = "cards";
                        break;
                    case "Comment":
                        fieldName = "comments";
                        break;
                    default:
                        dpf(Debug.ERROR, "Unsupported item type returned from server API: %s\n", bd);
                        return null;

                }
                if (fieldName != null) {
                    // Got something to return
                    ObjectMapper om = new ObjectMapper();
                    om.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
                    JSONArray p = (JSONArray) jresp.get(fieldName);
                    // Length here may be limited to 200 by the API paging.
                    if (totalReturned != p.length()) {
                        dpf(Debug.WARN, "Paging required for \"%s\" call. Processing %d when %d available\n", reqUrl,
                                p.length(), totalReturned);
                    }
                    for (int i = 0; i < p.length(); i++) {
                        try {
                            items.add(om.readValue(p.get(i).toString(), expectedResponseType));
                        } catch (JsonProcessingException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return items;
                }

            } else {
                ArrayList<T> items = new ArrayList<T>();
                ObjectMapper om = new ObjectMapper();
                switch (expectedResponseType.getSimpleName()) {
                    case "CardType": {
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
                    default: {
                        dpf(Debug.ERROR, "oops! don't recognise requested item type\n");
                        System.exit(2);
                    }
                }
                return items;
            }
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
            try {
                return om.readValue(result, expectedResponseType);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String processRequest() {

        // Deal with delays, retries and timeouts
        HttpClientBuilder cbldr = HttpClients.custom().setConnectionManager(cm);
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        configBuilder.setSocketTimeout(40000); // Set all timeouts to 40sec.
        configBuilder.setConnectTimeout(40000);
        configBuilder.setConnectionRequestTimeout(40000);
        cbldr.setDefaultRequestConfig(configBuilder.build());
        CloseableHttpClient client = cbldr.build();
        HttpResponse httpResponse = null;
        String result = null;
        try {
            // Add the user credentials to the request
            if (config.apiKey != null) {
                reqHdrs.add(new BasicNameValuePair("Authorization", "Bearer " + config.apiKey));
            } else {
                String creds = config.username + ":" + config.password;
                reqHdrs.add(new BasicNameValuePair("Authorization",
                        "Basic " + Base64.getEncoder().encode(creds.getBytes())));
            }
            HttpRequestBase request = null;
            switch (reqType) {
                case "POST": {
                    request = new HttpPost(reqUrl);
                    ((HttpPost) request).setEntity(reqEnt);
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
            URIBuilder bldr = new URIBuilder();
            bldr.setParameters(reqParams);
            request.setURI(new URI("https://" + config.url + "/"+ reqUrl + bldr.toString()));
            dpf(Debug.VERBOSE, "%s\n", request.toString());
            httpResponse = client.execute(request);
            dpf(Debug.VERBOSE, "%s\n", httpResponse.toString());

            Boolean entityTaken = false;
            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 200: // Card updated
                case 201: // Card created
                {
                    result = EntityUtils.toString(httpResponse.getEntity());
                    entityTaken = true;
                    break;
                }
                case 204: // No response expected
                {
                    break;
                }
                case 400: {
                    dpf(Debug.ERROR, "Bad request: %s\n", request.toString());
                    break;
                }
                case 401: {
                    dpf(Debug.ERROR, "Unauthorised. Check Credentials in spreadsheet: %s\n", request.toString());
                    break;
                }
                case 403: {
                    dpf(Debug.ERROR, "Forbidden by server: %s\n", request.toString());
                    break;
                }
                case 429: { // Flow control
                    LocalDateTime retryAfter = LocalDateTime.parse(httpResponse.getHeaders("retry-after")[0].getValue(),
                            DateTimeFormatter.RFC_1123_DATE_TIME);
                    LocalDateTime serverTime = LocalDateTime.parse(httpResponse.getHeaders("date")[0].getValue(),
                            DateTimeFormatter.RFC_1123_DATE_TIME);
                    Long timeDiff = ChronoUnit.MILLIS.between(serverTime, retryAfter);
                    dpf(Debug.INFO, "Received 429 status. waiting %.2f seconds\n", ((1.0 * timeDiff) / 1000.0));
                    EntityUtils.consumeQuietly(httpResponse.getEntity());
                    try {
                        TimeUnit.MILLISECONDS.sleep(timeDiff);
                    } catch (InterruptedException e) {
                        dpf(Debug.ERROR, "(L2) %s\n", e.getMessage());
                    }
                    result = processRequest();
                    break;
                }
                case 422: { // Unprocessable Parameter
                    dpf(Debug.WARN, "Parameter Error in request: %s (%s)\n", request.toString(),
                            httpResponse.toString());
                    break;
                }
                case 404: { // Item not found
                    dpf(Debug.WARN, "Item not found: %s\n", httpResponse.toString());
                    break;
                }
                case 408: //Request timeout - try your luck with another one....
                case 500: // Server fault
                case 502: // Bad Gateway
                case 503:  // Service unavailable
                case 504:   // Gateway timeout
                {
                    dpf(Debug.ERROR, "Received %d status. retrying in 5 seconds\n",
                            httpResponse.getStatusLine().getStatusCode());
                    try {
                        EntityUtils.consumeQuietly(httpResponse.getEntity());
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        dpf(Debug.ERROR, "(L1) %s\n", e.getMessage());
                    }
                    result = processRequest();
                    break;
                }
                default: {
                    dpf(Debug.ERROR, "Network fault: %s\n", httpResponse.toString());
                    break;
                }
            }
            if (!entityTaken) {
                EntityUtils.consumeQuietly(httpResponse.getEntity()); // Tidy up because the java library has a
                                                                      // 'feature'
            }
        } catch (IOException e) {
            dpf(Debug.ERROR, "(L3) %s\n", e.getMessage());
            System.exit(3);
        } catch (URISyntaxException e1) {
            // Should never happen, but to keep the compiler happy.....
            e1.printStackTrace();
        }
        return result;
    }

    public ArrayList<CardType> fetchCardTypes(String boardId) {
        reqType = "GET";
        reqUrl = "io/board/" + boardId + "/cardType";
        reqParams.clear();
        reqHdrs.clear();
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
        reqUrl = "io/board/" + boardId + "/";
        reqParams.clear();
        reqHdrs.clear();
        ArrayList<Board> brd = read(Board.class);
        if (brd != null) {
            if (brd.size() > 0) {
                return brd.get(0).lanes;
            }
        }
        return null;
    }

    private ArrayList<Board> fetchBoardsFromName(String name) {
        reqParams.clear();
        reqHdrs.clear();
        reqType = "GET";
        reqUrl = "io/board";
        reqParams.add(new BasicNameValuePair("search", name));

        // Once you get the boards, you could cache them. There may be loads, but
        // shouldn't max
        // out memory.
        return read(Board.class);
    }

    public void deleteCards(ArrayList<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            dpf(Debug.INFO, "Deleting card %s\n", cards.get(i).id);

            reqType = "DELETE";
            reqHdrs.clear();
            reqParams.clear();
            reqUrl = "io/card/" + cards.get(i).id;
            processRequest();
        }
    }

    public ArrayList<Comment> fetchCommentsForCard(Card cd) {
        reqParams.clear();
        reqHdrs.clear();
        reqType = "GET";
        reqUrl = "io/card/" + cd.id + "/comment";
        return read(Comment.class);
    }

    public ArrayList<Card> fetchCardIDsFromBoard(String id, Integer rewind) {
        LocalDateTime sinceDate = LocalDateTime.now();
        sinceDate.minus(rewind, ChronoUnit.DAYS);
        reqParams.clear();
        reqHdrs.clear();
        if (rewind >= 0) {
            reqType = "GET";
            reqUrl = "io/card?board=" + id + "&deleted=0&only=id&since="
                    + sinceDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "T00:00:00Z";
        } else {
            reqType = "GET";
            reqUrl = "io/card?board=" + id + "&deleted=0&only=id";
        }
        // Once you get the boards, you could cache them. There may be loads, but
        // shouldn't max
        // out memory.
        return read(Card.class);
    }

    public Board fetchBoardFromId(String id) {
        reqType = "GET";
        reqParams.clear();
        reqHdrs.clear();
        reqUrl = "io/board/" + id;
        reqParams.add(new BasicNameValuePair("returnFullRecord", "true"));

        ArrayList<Board> results = read(Board.class);
        if (results != null) {
            return results.get(0);
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

    public String fetchUserId(String emailAddress) {
        reqUrl = "io/user";
        reqType = "GET";
        reqParams.clear();
        reqHdrs.clear();
        reqParams.add(new BasicNameValuePair("search", emailAddress));

        ArrayList<User> userd = read(User.class);
        User user = null;

        if (userd != null) {
            if (userd.size() > 0) {
                // We found one or more with this name search. First try to find an exact match
                Iterator<User> uItor = userd.iterator();
                while (uItor.hasNext()) {
                    User u = uItor.next();
                    if (u.emailAddress.equals(emailAddress)) {
                        user = u;
                    }
                }
                // Then take the first if that fails
                if (user == null)
                    user = userd.get(0);
                return user.id;
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
                .addTextBody("Description", "Auto-generated from Script").addPart(filename, fb);
        reqEnt = mpeb.build();

        String status = processRequest();
        return status;
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

    public User fetchUser(String id) {
        reqType = "GET";
        reqUrl = "/io/user/" + id;
        reqParams.clear();
        reqHdrs.clear();
        reqParams.add(new BasicNameValuePair("returnFullRecord", "true"));
        return execute(User.class);
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
                case "blockReason": {
                    if (values.get("value1").toString().length() <= 1) {

                        JSONObject upd = new JSONObject();
                        upd.put("op", "replace");
                        upd.put("path", "/isBlocked");
                        upd.put("value", false);
                        jsa.put(upd);

                    } else if (values.get("value1").toString().startsWith("-")) {
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
                        upd2.put("value", values.get("value1").toString().substring(1));
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
                        upd2.put("value", values.get("value1").toString());
                        jsa.put(upd2);
                    }
                    break;
                }
                case "Parent": {
                    if ((values.get("value1") == null) || (values.get("value1").toString() == "")
                            || (values.get("value1").toString() == "0")) {
                        dpf(Debug.ERROR, "Trying to set parent of %s to value \"%s\"\n", card.id,
                                values.get("value1").toString());
                    } else if (values.get("value1").toString().startsWith("-")) {
                        JSONObject upd2 = new JSONObject();
                        upd2.put("op", "remove");
                        upd2.put("path", "/parentCardId");
                        upd2.put("value", values.get("value1").toString().substring(1));
                        jsa.put(upd2);
                    } else {
                        JSONObject upd2 = new JSONObject();
                        upd2.put("op", "add");
                        upd2.put("path", "/parentCardId");
                        upd2.put("value", values.get("value1").toString());
                        jsa.put(upd2);
                    }
                    break;
                }
                case "Lane": {
                    // Need to find the lane on the board and set the card to be in it.
                    JSONObject upd1 = new JSONObject();
                    upd1.put("op", "replace");
                    upd1.put("path", "/laneId");
                    upd1.put("value", values.get("value1").toString());
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
                case "tags": {
                    // Need to add or remove based on what we already have?
                    // Or does add/remove ignore duplicate calls. Trying this first.....
                    if (values.get("value1").toString().toString().startsWith("-")) {
                        Integer tIndex = findTagIndex(card, values.get("value1").toString().substring(1));
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
                        upd.put("value", values.get("value1").toString());
                        jsa.put(upd);
                    }
                    break;
                }
                case "assignedUsers": {
                    // Need to add or remove based on what we already have?
                    // Or does add/remove ignore duplicate calls. Trying this first.....
                    if (values.get("value1").toString().startsWith("-")) {
                        JSONObject upd = new JSONObject();
                        upd.put("op", "remove");
                        upd.put("path", "/assignedUserIds");
                        upd.put("value", fetchUserId(values.get("value1").toString().substring(1)));
                        jsa.put(upd);
                    } else {
                        JSONObject upd = new JSONObject();
                        upd.put("op", "add");
                        upd.put("path", "/assignedUserIds/-");
                        upd.put("value", fetchUserId(values.get("value1").toString()));
                        jsa.put(upd);
                    }
                    break;
                }
                case "externalLink": {
                    JSONObject link = new JSONObject();
                    JSONObject upd = new JSONObject();
                    String[] bits = values.get("value1").toString().split(",");
                    if (bits.length != 2) {
                        dpf(Debug.WARN, "Could not extract externalLink from %s (possible ',' in label?)",
                                values.get("value1").toString());
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
                case "customIcon": {
                    if (brd.classOfServiceEnabled) {
                        if (brd.classesOfService != null) {
                            for (int i = 0; i < brd.classesOfService.length; i++) {
                                if (brd.classesOfService[i].name.equals(values.get("value1"))) {
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
                    sendAttachment(card.id, values.get("value1").toString());
                    break;
                }

                case "comments": {
                    postComment(card.id, values.get("value1").toString());
                    break;
                }

                case "CustomField": {
                    CustomField[] cflds = brd.customFields;
                    if (cflds != null) {
                        for (int i = 0; i < cflds.length; i++) {
                            if (cflds[i].label.equals(values.get("value1"))) {
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
                case "priority": {
                    JSONObject upd = new JSONObject();
                    upd.put("op", "replace");
                    upd.put("path", "/" + key);
                    upd.put("value", values.get("value1").toString().toLowerCase());
                    jsa.put(upd);
                    break;
                }
                default: {
                    JSONObject upd = new JSONObject();
                    upd.put("op", "replace");
                    upd.put("path", "/" + key);
                    upd.put("value", values.get("value1"));
                    jsa.put(upd);
                    break;
                }
            }
        }
        reqType = "PATCH";
        reqUrl = "io/card/" + card.id;
        reqEnt = new StringEntity(jsa.toString(), "UTF-8");
        reqParams.clear();
        reqHdrs.clear();
        return execute(Card.class);

    }

    public Card createCard(String boardId, JSONObject jItem) {
        reqType = "POST";
        reqUrl = "io/card/";
        reqParams.add(new BasicNameValuePair("returnFullRecord", "true"));
        jItem.put("boardId", boardId);

        if (!jItem.has("title")) {
            jItem.put("title", "dummy title"); // Used when we are testing a create to get back the card structure
        }
        reqEnt = new StringEntity(jItem.toString(), "UTF-8");
        return execute(Card.class);
    }

    public Id createCardID(String boardId) {
        JSONObject jItem = new JSONObject();
        reqType = "POST";
        reqParams.add(new BasicNameValuePair("returnFullRecord", "true"));
        reqUrl = "io/card/";
        jItem.put("boardId", boardId);
        reqEnt = new StringEntity(jItem.toString(), "UTF-8");
        return execute(Id.class);
    }
}
