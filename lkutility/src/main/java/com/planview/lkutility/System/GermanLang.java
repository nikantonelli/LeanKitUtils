package com.planview.lkutility.System;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class GermanLang {

	private HashMap<Integer, String> map;

	public GermanLang() {
		map = new HashMap<Integer, String>(Map.ofEntries(
				// Main message beginnings
				new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.INFO, "Info: "),
				new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.VERBOSE, "Ausführlich: "),
				new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.WARN, "Warnung: "),
				new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.ERROR, "Fehler: "),
				new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.NOTE, "Notiz: "),

				// Command line options
				new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.REMOVE_OPTION, "entfernen"),
				new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.REMOVE_OPTION_MSG,
						"Zieltafeln entfernen (löschen)"),
				new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.REMAKE_OPTION, "wiederholen"),
				new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.REMAKE_OPTION_MSG,
						"Erstellen Sie Zieltafeln neu, indem Sie alte archivieren und neue hinzufügen"),

				// Output Messages
				new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.FINISH_PROGRAM, "Fertig um: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.START_PROGRAM, "Fing an bei: %s\n")

		));
	}

	public HashMap<Integer, String> getMap() {
		return map;
	}
}
