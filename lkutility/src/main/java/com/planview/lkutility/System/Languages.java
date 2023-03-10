package com.planview.lkutility.System;

import java.util.HashMap;

public class Languages {

	private String language;

	public String getLanguage() {
		return language;
	}

	private final String known_languages = "en, fr, de";
	
	HashMap<Integer, String> msg;

	public String getKnown_languages() {
		return known_languages;
	}

	public Languages(){
		msg = new EnglishLang().getMap();
	}

	public Languages(String lang){

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
				System.out.printf("Unknown language: %s (%s) Defaulting to English (en)\n", lang, known_languages);
				lang = "en";
			}
			//Fallthrough
			case "en":{
				msg = new EnglishLang().getMap();
				break;
			}
		}
		language = lang;
	}

	public String getMsg(Integer msgID){
		if (msg == null) return "";
		String returnMsg = msg.get(msgID);
		if (returnMsg == null){
			switch (language) {
				case "fr":{
					returnMsg = "Message d'erreur inconnu\n";
					break;
				}
				case "de":{
					returnMsg = "Unbekannte Fehlermeldung\n";
					break;
				}
				default:
				case "en":{
					returnMsg = "Unknown Error Message\n";
					break;
				}
				
			}
		}
		return returnMsg;
	}
	
}
