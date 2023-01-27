package com.planview.lkutility;

/**
 * Field names are precusor-ed with src or dst as column in spreadsheet
 */
public class Access {
	 String Url;
	 String BoardName;
	 String ApiKey;
	 

	public String getUrl() {
		return Url;
	}

	public void setUrl(String url) {
		Url = url;
	}

	public String getBoardName() {
		return BoardName;
	}

	public void setBoardName(String boardName) {
		BoardName = boardName;
	}

	public String getApiKey() {
		return ApiKey;
	}

	public void setApiKey(String apiKey) {
		ApiKey = apiKey;
	}

	public Access(){

	}
	
}
