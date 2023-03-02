package com.planview.lkutility.System;

import java.util.HashMap;

public class Messages {

	String language;
	HashMap<Integer, String> msg;

	public Messages(){
		msg = new EnglishLang().getMap();
	}

	public Messages(String lang){
		language = lang;
		switch(lang){
			case "fr": {
				msg = new FrenchLang().getMap();
				break;
			}
			case "de": {
				msg = new GermanLang().getMap();
				break;
			}
			case "en":
			default: {
				msg = new EnglishLang().getMap();
			}
		}

	}

	public String getMsg(Integer msgID){
		String returnMsg = msg.get(msgID);
		return (returnMsg == null) ? "Unknown error , unbekannter Fehler , erreur inconnue \n" : returnMsg;
	}
	
}
