package com.planview.lkutility.System;

import com.planview.lkutility.Leankit.AccessCache;

public class AccessConfig extends Access {
	AccessCache cache = null;
	
	public AccessConfig() {

	}
	
	public AccessCache getCache() {
		return cache;
	}

	public void setCache(AccessCache cache) {
		this.cache = cache;
	}

	public AccessConfig( String url, String boardname, String apikey) {
		Url = url;
		BoardName = boardname;
		ApiKey = apikey;
	}
}
