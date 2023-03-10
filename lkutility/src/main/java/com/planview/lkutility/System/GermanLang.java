package com.planview.lkutility.System;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class GermanLang {

	/**
	 * Need to set chcp on Windows CMD to get correct characters
	 * 
	 * Excuse the Google Translate usage.... :-)
	 * 
	 */
	private HashMap<Integer, String> map;

	public GermanLang() {
		try {
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
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ALWAYS,
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
							"Karten auf Zieltafeln löschen (aus Tabellenkalkulation)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.LANGUAGE_OPTION,
							"sprache"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.LANGUAGE_OPTION_MSG,
							"Sprache, language, langue (de, en, fr)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DELETE_OPTION,
							"delete"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DELETE_OPTION_MSG,
							"Löschen Sie alle Karten auf den Zieltafeln"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.TASKTOP_OPTION,
							"tasktop"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.TASKTOP_OPTION_MSG,
							"Folgen Sie externen Links, um Remoteartefakte zu löschen"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.GROUP_OPTION,
							"group"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.GROUP_OPTION_MSG,
							"Kennung der zu verarbeitenden Gruppe (falls vorhanden)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.MOVE_OPTION,
							"move"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.MOVE_OPTION_MSG,
							"Lane zum Ändern unerwünschter Karten (nur zum Vergleich)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DEBUG_OPTION,
							"debug"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DEBUG_OPTION_MSG,
							"Drucken Sie jede Menge hilfreiche Dinge aus: 0 – Fehler, 1 – und Warnungen, 2 – und Info, 3 – und Debugging, 4 – und Netzwerk"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ARCHIVED_OPTION,
							"archived"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ARCHIVED_OPTION_MSG,
							"Ältere archivierte Karten in den Export einbeziehen (falls vorhanden)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.TASKS_OPTION,
							"tasks"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.TASKS_OPTION_MSG,
							"Aufgabenkarten in den Export einbeziehen (falls vorhanden)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ATTACHMENTS_OPTION,
							"attachments"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ATTACHMENTS_OPTION_MSG,
							"Kartenanhänge in lokales Dateisystem exportieren (falls vorhanden)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.COMMENTS_OPTION,
							"comments"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.COMMENTS_OPTION_MSG,
							"Kartenkommentare in lokales Dateisystem exportieren (falls vorhanden)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ORIGIN_OPTION,
							"origin"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ORIGIN_OPTION_MSG,
							"Fügen Sie einen Kommentar für die Aufnahme von Quellenartefakten hinzu"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.RO_OPTION,
							"ro"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.RO_OPTION_MSG,
							"Schreibgeschützte Felder exportieren (nicht importiert!)"),

					// Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.FINISH_PROGRAM,
							"Fertig um: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.START_PROGRAM,
							"Fing an bei: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SETTING_LANGUAGE,
							"Sprache auf %s einstellen\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.COMMANDLINE_ERROR,
							"Befehlszeilenoptionsfehler: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SHEET_NOTFOUND_ERROR,
							"Erforderliches Blatt in der Tabelle nicht erkannt: \"Config\""),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SHT_HDR_ERROR,
							"Keine Kopfzeileninformationen auf dem Konfigurationsblatt (erste Zeile!)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SHT_ITR_ERROR,
							"Keine potenziellen Übertragungsinformationen auf dem Konfigurationsblatt erkannt (erste Zelle darf nicht leer sein, z. B. URL zu einem echten Host)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_SHEET_NOT_FOUND,
							"Wiedergabeblatt nicht gefunden. Führen Sie mit -c vor (oder mit) -a aus\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SET_COMPARE_MODE,
							"Einstellung auf \"Vergleichsmodus.\"\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.INVALID_OPTIONS,
							"Ungültige Optionen angegeben (-r mit anderen). Standardmäßig in den Wiedergabemodus wechseln.\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SET_REPLAY_MODE,
							"Auf \"Wiedergabe\"-Modus stellen\n"),

					// LeanKitAccess.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.NO_HTTP,
							"HTTP-Zugriff auf AgilePlace wird nicht unterstützt.\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.STATUSCODE_ERROR,
							"\"%s\" gab Antwort: \"%s\"\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.UNKNOWN_API_TYPE_ERROR,
							"Nicht unterstützter Elementtyp von der Server-API angefordert: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.RECEIVED_DATA,
							"%d %s (von  %d) erhalten\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.UNRECOGNISED_TYPE,
							"Hoppla! Erkenne den angeforderten Elementtyp nicht: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.CANNOT_ENCODE_BOARD,
							"Boardname kann nicht kodiert werden\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DOWNLOAD_ATT_TYPE,
							"Heruntergeladener Anhangstyp ist:  %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.AUTO_FROM_SCRIPT,
							"Automatisch aus Skript generiert"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SETTING_PARENT,
							"Es wird versucht, das übergeordnete Element von %s auf den Wert \"%s\" zu setzen \n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.EXTLINK_ERROR,
							"ExternalLink konnte nicht aus %s extrahiert werden (mögliches ',' im Label?)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.NEED_SECURE_MODE,
							"http-Zugriff nicht unterstützt. Umstellung auf https"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.APIKEY_ERROR,
							"Kein gültiger apiKey angegeben\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.CREDS_ERROR,
							"Unbefugt. Überprüfen Sie die Anmeldeinformationen in der Tabelle: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.NETWORK_FAULT_ERROR,
							"Netzwerkfehler: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.BOARD_COPY_ERROR,
							"Kann nicht lokal von \"%s\" nach \"%s\" duplizieren\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.BOARD_NOT_FOUND_ERROR,
							"Board mit Titel kann nicht gefunden werden: \"%s\"\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.BOARD_CREATE_ERROR,
							"Zieltafel konnte nicht erstellt/gefunden werden"),

					// AzureDeleter.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.AZURE_DELETE,
							"%s Gelöscht\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.AZURE_DELETE_FAIL,
							"Löschen fehlgeschlagen %s\n"),

					// JiraDeleter.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.JIRA_DELETE,
							"Löschen versucht %s\n")

			));
		} catch (Exception e) {
			System.out.printf("German Language Map Error: %s\n", e.getMessage());
		}
	}

	public HashMap<Integer, String> getMap() {
		return map;
	}
}
