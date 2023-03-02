package com.planview.lkutility.System;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class GermanLang {

	private HashMap<Integer, String> map;

	public GermanLang() {
		map = new HashMap<Integer, String>(Map.ofEntries(
				// Main message beginnings
				new AbstractMap.SimpleEntry<Integer, String>(LMS.INFO, "Info: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.VERBOSE, "Ausführlich: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.WARN, "Warnung: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.ERROR, "Fehler: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.NOTE, "Notiz: "),

				// Command line options
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMOVE_OPTION, "entfernen"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMOVE_OPTION_MSG,
						"Zieltafeln entfernen (löschen)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMAKE_OPTION, "wiederholen"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMAKE_OPTION_MSG,
						"Erstellen Sie Zieltafeln neu, indem Sie alte archivieren und neue hinzufügen"),

				// Output Messages
				new AbstractMap.SimpleEntry<Integer, String>(LMS.FINISH_PROGRAM,
						"Fertig um: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.START_PROGRAM,
						"Fing an bei: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.SETTING_LANGUAGE,
						"Sprache auf %s einstellen\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.COMMANDLINE_ERROR,
						"Befehlszeilenoptionsfehler: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_SHEET_NOT_FOUND,
						"Wiedergabeblatt nicht gefunden. Führen Sie mit -c vor (oder mit) -a aus\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.SET_COMPARE_MODE,
						"Einstellung auf \"Vergleichsmodus.\"\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.INVALID_OPTIONS,
						"Ungültige Optionen angegeben (-r mit anderen). Standardmäßig in den Wiedergabemodus wechseln.\n")

		));
	}

	public HashMap<Integer, String> getMap() {
		return map;
	}
}
