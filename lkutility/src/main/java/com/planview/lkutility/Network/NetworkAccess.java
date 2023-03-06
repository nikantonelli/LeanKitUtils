package com.planview.lkutility.Network;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.planview.lkutility.System.AccessConfig;
import com.planview.lkutility.System.Debug;
import com.planview.lkutility.System.LMS;

public class NetworkAccess {
	protected AccessConfig config = null;
	protected String reqType = null;
	protected String reqUrl = null;
	protected HttpEntity reqEnt = null;
	protected Debug d = new Debug();

	PoolingHttpClientConnectionManager cm = null;

	protected ArrayList<BasicNameValuePair> reqHdrs = new ArrayList<>();
	protected ArrayList<NameValuePair> reqParams = new ArrayList<>();

	protected void configCheck() {
		// Check URL has a trailing '/' and remove
		if (config.getUrl().endsWith("/")) {
			config.setUrl(config.getUrl().substring(0, config.getUrl().length() - 1));
		}
		// We need to set to https later on
		if (!config.getUrl().startsWith("http")) {
			config.setUrl("https://" + config.getUrl());
		} else if (config.getUrl().startsWith("http://")) {
			d.p(LMS.WARN, "http access not supported. Switching to https");
			config.setUrl("https://" + config.getUrl().substring(7));
		}
	}

	protected String processRequest() {
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

	protected HttpEntity processRawRequest() {

		// Deal with delays, retries and timeouts
		HttpClientBuilder cbldr = HttpClients.custom().setConnectionManager(cm);
		RequestConfig.Builder configBuilder = RequestConfig.custom();
		configBuilder.setSocketTimeout(40000); // Set all timeouts to 40sec.O
		configBuilder.setConnectTimeout(40000);
		configBuilder.setConnectionRequestTimeout(40000);
		cbldr.setDefaultRequestConfig(configBuilder.build());
		CloseableHttpClient client = cbldr.build();
		CloseableHttpResponse httpResponse = null;
		HttpEntity result = null;
		try {

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
					// This may be AP specific. If so, need to move out of here
					if (reqEnt != null) {
						request = new HttpPost();
						((HttpPost) request).setEntity(reqEnt);
						request.addHeader("X-HTTP-Method-Override", "DELETE");
					} else {
						request = new HttpDelete();
					}
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
				d.p(LMS.VERBOSE, "Adding Header \"%s\" as \"%s\"\n", reqHdrs.get(i).getName(),
						reqHdrs.get(i).getValue());
			}
			// Add the user credentials to the request
			if ((config.getApiKey() != null) && (config.getUser() == null)) {
				// Standard API key handling
				request.addHeader("Authorization", "Bearer " + config.getApiKey());
				d.p(LMS.VERBOSE, "Adding Bearer starting with \"%s...\"\n", config.getApiKey().substring(0, 5));
			} else if ((config.getApiKey() != null) && (config.getUser() != null)) {
				// ADO TOKEN handling
				String token = config.getUser() + ":" + config.getApiKey();
				token = Base64.getEncoder().encodeToString(token.getBytes());
				request.addHeader("Authorization", "Basic " + token);
				d.p(LMS.VERBOSE, "Adding Basic starting with \"%s...\"\n", config.getApiKey().substring(0, 5));
			} else {
				d.p(LMS.ERROR, "No valid apiKey provided\n");
				System.exit(-13);
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
			d.p(LMS.VERBOSE, "%s\n", request.toString());
			if (reqEnt != null) {
				d.p(LMS.VERBOSE, "Content: %s\n", IOUtils.toString(reqEnt.getContent(), "UTF-8"));
			}
			httpResponse = client.execute(request);
			d.p(LMS.VERBOSE, "%s\n", httpResponse.getStatusLine());

			Boolean entityTaken = false;
			switch (httpResponse.getStatusLine().getStatusCode()) {
				case 200: // Card updated
				case 201: // Card created
				{
					result = httpResponse.getEntity();
					entityTaken = true;
					break;
				}
				case 204: // No response expected. but return affirmative
				{

					break;
				}
				case 400: {
					d.p(LMS.WARN, "Bad request: %s\n", request.toString());
					break;
				}
				case 401: {
					d.p(LMS.ERROR, "Unauthorised. Check Credentials in spreadsheet: %s\n", request.toString());
					System.exit(-14);
				}
				case 403: {
					d.p(LMS.WARN, "Forbidden by server: %s\n", request.toString());
					break;
				}
				case 405: {
					d.p(LMS.WARN, "Method not Allowed: %s\n", request.toString());
					break;
				}
				case 429: { // Flow control
					LocalDateTime retryAfter = LocalDateTime.parse(
							httpResponse.getFirstHeader("retry-after").getValue(),
							DateTimeFormatter.RFC_1123_DATE_TIME);
					LocalDateTime serverTime = LocalDateTime.parse(httpResponse.getFirstHeader("date").getValue(),
							DateTimeFormatter.RFC_1123_DATE_TIME);
					Long timeDiff = ChronoUnit.MILLIS.between(serverTime, retryAfter);
					d.p(LMS.INFO, "Received 429 status. waiting %.2f seconds\n", ((1.0 * timeDiff) / 1000.0));
					EntityUtils.consumeQuietly(httpResponse.getEntity());
					try {
						TimeUnit.MILLISECONDS.sleep(timeDiff);
					} catch (InterruptedException e) {
						d.p(LMS.ERROR, "(L2) %s\n", e.getMessage());
						System.exit(-15);
					}
					result = processRawRequest();
					break;
				}
				case 422: { // Unprocessable Parameter
					d.p(LMS.WARN, "Parameter Error in request: %s \n%s\n", request.toString(),
							EntityUtils.toString(httpResponse.getEntity()));
					break;
				}
				case 404: { // Item not found
					d.p(LMS.WARN, "Item not found: %s\n", httpResponse.toString());
					break;
				}
				case 409: { // Conflict
					d.p(LMS.WARN, "Conflict Error in request: %s \n%s\n", request.toString(),
							EntityUtils.toString(httpResponse.getEntity()));
					break;
				}
				case 408: // Request timeout - try your luck with another one....
				case 500: // Server fault
				case 502: // Bad Gateway
				case 503: // Service unavailable
				case 504: // Gateway timeout
				{
					d.p(LMS.ERROR, "Received %d status. retrying in 5 seconds\n",
							httpResponse.getStatusLine().getStatusCode());
					try {
						EntityUtils.consumeQuietly(httpResponse.getEntity());
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						d.p(LMS.WARN, "(L1) %s\n", e.getMessage());
					}
					result = processRawRequest();
					break;
				}
				default: {
					d.p(LMS.ERROR, "Network fault: %s\n", httpResponse.toString());
					System.exit(-16);
					break;
				}
			}
			if (!entityTaken) {
				EntityUtils.consumeQuietly(httpResponse.getEntity()); // Tidy up because the java library has a
																		// 'feature'
			}
		} catch (IOException e) {
			d.p(LMS.ERROR, "(L3) %s\n", e.getMessage());
			try {
				if (httpResponse != null) {
					EntityUtils.consumeQuietly(httpResponse.getEntity());
				}
				TimeUnit.MILLISECONDS.sleep(5000);
			} catch (InterruptedException e1) {

			}
			return processRawRequest();
		} catch (URISyntaxException e1) {
			// Should never happen, but to keep the compiler happy.....
			e1.printStackTrace();
		}
		return result;
	}

}
