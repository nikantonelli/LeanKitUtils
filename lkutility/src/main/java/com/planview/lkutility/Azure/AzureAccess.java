package com.planview.lkutility.Azure;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import com.planview.lkutility.Network.NetworkAccess;
import com.planview.lkutility.System.AccessConfig;

public class AzureAccess extends NetworkAccess {
	
	public AzureAccess(AccessConfig configp, Integer debugLevel) {
		config = configp;
		d.setLevel(debugLevel);
	}

	public String deleteTicket(String url){
		if (url != null) {
			//Split the URL into bits
			url = url.substring(8);
			String[] urlBits = url.split("/");
			reqUrl = "https://"+ urlBits[0] + "/_apis/wit/$batch";
			reqEnt = new StringEntity("[{\"method\":\"DELETE\",\"uri\":\"/_apis/wit/workItems/" + urlBits[urlBits.length-1] + "?api-version=7.0\",\"headers\":{\"Content-Type\":\"application/json-patch+json\"}}]", ContentType.APPLICATION_JSON);
			reqParams.clear();
			reqHdrs.clear();
			reqHdrs.add(new BasicNameValuePair("Accept", "application/json;api-version=4.0-preview;excludeUrls=true"));
			reqHdrs.add(new BasicNameValuePair("Content-type", "application/json"));
			reqType = "POST";
			return processRequest();
		}
		return null;
	}
}
