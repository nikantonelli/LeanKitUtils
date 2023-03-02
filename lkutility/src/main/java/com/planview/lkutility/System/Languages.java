package com.planview.lkutility.System;

import java.util.HashMap;

public class Languages {

	String language;
	HashMap<Integer, String> msg;

	public Languages(){
		msg = new EnglishLang().getMap();
	}

	public Languages(String lang){
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
			default: {
				System.out.println("Unknown language. Defaulting to English\n");
			}
			//Fallthrough
			case "en":{
				msg = new EnglishLang().getMap();
				break;
			}
		}

	}

	public String getMsg(Integer msgID){
		String returnMsg = msg.get(msgID);
		return (returnMsg == null) ? "Unknown error , unbekannter Fehler , erreur inconnue \n" : returnMsg;
	}
	
}
