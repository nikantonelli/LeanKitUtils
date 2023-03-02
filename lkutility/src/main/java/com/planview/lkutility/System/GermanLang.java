package com.planview.lkutility.System;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class GermanLang {

	private HashMap<Integer, String> map;

	public GermanLang() {
		map = new HashMap<Integer, String>(Map.ofEntries(
			new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.INFO, "Info:\n"),
			new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.VERBOSE, "Ausführlich:\n"),
			new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.WARN, "Warnung:\n"),
			new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.ERROR, "Fehler:\n"),
			new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.NOTE, "Notiz:\n"),
			new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.FINISH_PROGRAM, "Fertig um: %s\n"),
			new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.START_PROGRAM, "Fing an bei: %s\n"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.REMAKE_OPTION,  "wiederholen"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.REMAKE_OPTION_MSG, 
				"Erstellen Sie Zieltafeln neu, indem Sie alte archivieren und neue hinzufügen")
		));
	}

	public HashMap<Integer, String> getMap(){
		return map;
	}
}
