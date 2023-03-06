package com.planview.lkutility.System;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class GermanLang {

	private HashMap<Integer, String> map;

	public GermanLang() {
		map = new HashMap<Integer, String>(Map.ofEntries(
				// Main message beginnings
				new AbstractMap.SimpleEntry<Integer, String>(LMS.INFO,
						"Info: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.VERBOSE,
						"Ausführlich: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.WARN,
						"Warnung: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.ERROR,
						"Fehler: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.NOTE,
						"Notiz: "),

				// Command line options
				new AbstractMap.SimpleEntry<Integer, String>(LMS.FILE_OPTION,
						"filename"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.FILE_OPTION_MSG,
						"XLSX Spreadsheet (must contain API config!)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMOVE_OPTION,
						"entfernen"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMOVE_OPTION_MSG,
						"Zieltafeln entfernen (löschen)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMAKE_OPTION,
						"neu"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMAKE_OPTION_MSG,
						"Erstellen Sie Zieltafeln neu, indem Sie alte archivieren und neue hinzufügen"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_OPTION,
						"wiederholung"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_OPTION_MSG,
						"Automatisches Ausführen des Zurücksetzens des Ziels während des Diff"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.COMPARE_OPTION,
						"vergleichen"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.COMPARE_OPTION_MSG,
						"Vergleichen Sie die Ziel-URL mit einer vorherigen Übertragung"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.IMPORT_OPTION,
						"importeur"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.IMPORT_OPTION_MSG,
						"Importprogramm ausführen"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.EXPORT_OPTION,
						"exporteur"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.EXPORT_OPTION_MSG,
						"Exportprogramm ausführen"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.DELETE_X_OPTION,
						"xlsx"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.DELETE_X_OPTION_MSG,
						"Delete cards on target boards (from spreadsheet)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.LANGUAGE_OPTION,
						"sprache"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.LANGUAGE_OPTION_MSG,
						"Language, langue, sprache (en, fr, de)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.DELETE_OPTION,
						"delete"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.DELETE_OPTION_MSG,
						"Delete all cards on target boards"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.TASKTOP_OPTION,
						"tasktop"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.TASKTOP_OPTION_MSG,
						"Follow External Links to delete remote artifacts"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.GROUP_OPTION,
						"group"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.GROUP_OPTION_MSG,
						"Identifier of group to process (if present)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.MOVE_OPTION,
						"move"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.MOVE_OPTION_MSG,
						"Lane to modify unwanted cards with (for compare only)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.DEBUG_OPTION,
						"debug"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.DEBUG_OPTION_MSG,
						"Print out loads of helpful stuff: 0 - Error, 1 - And Warnings, 2 - And Info, 3 - And Debugging, 4 - And Network"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.ARCHIVED_OPTION,
						"archived"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.ARCHIVED_OPTION_MSG,
						"Include older Archived cards in export (if present)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.TASKS_OPTION,
						"tasks"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.TASKS_OPTION_MSG,
						"Include Task cards in export (if present)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.ATTACHMENTS_OPTION,
						"attachments"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.ATTACHMENTS_OPTION_MSG,
						"Export card attachments in local filesystem (if present)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.COMMENTS_OPTION,
						"comments"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.COMMENTS_OPTION_MSG,
						"Export card comments in local filesystem (if present)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.ORIGIN_OPTION,
						"origin"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.ORIGIN_OPTION_MSG,
						"Add comment for source artifact recording"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.RO_OPTION,
						"ro"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.RO_OPTION_MSG,
						"Export Read Only fields (Not Imported!)"),

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
						"Ungültige Optionen angegeben (-r mit anderen). Standardmäßig in den Wiedergabemodus wechseln.\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.SET_REPLAY_MODE,
						"auf \"Wiedergabe\"-Modus stellen\n")

		));
	}

	public HashMap<Integer, String> getMap() {
		return map;
	}
}
