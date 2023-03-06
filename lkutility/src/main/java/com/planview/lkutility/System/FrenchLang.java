package com.planview.lkutility.System;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Need to set chcp 1252 on Windows CMD to get correct characters
 */

public class FrenchLang {

	private HashMap<Integer, String> map;

	public FrenchLang() {
		map = new HashMap<Integer, String>(Map.ofEntries(
				// Main message beginnings
				new AbstractMap.SimpleEntry<Integer, String>(LMS.INFO,
						"Info:"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.VERBOSE,
						"Verbeaux:"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.WARN,
						"Avertissement:"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.ERROR,
						"Erreur:"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.NOTE,
						"Avis:"),

				// Command line options
				new AbstractMap.SimpleEntry<Integer, String>(LMS.FILE_OPTION,
						"filename"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.FILE_OPTION_MSG,
						"XLSX Spreadsheet (must contain API config!)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMOVE_OPTION,
						"supprimer"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMOVE_OPTION_MSG,
						"Supprimer les tableaux cibles (delete)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_OPTION,
						"repeter"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_OPTION_MSG,
						"Exécuter automatiquement la réinitialisation de la destination pendant le diff"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMAKE_OPTION, "refaire"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMAKE_OPTION_MSG,
						"Refaire les tableaux cibles en archivant les anciens et en ajoutant de nouveaux"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.COMPARE_OPTION,
						"comparer"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.COMPARE_OPTION_MSG,
						"Comparer l'URL de destination à un transfert précédent"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.IMPORT_OPTION,
						"importer"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.IMPORT_OPTION_MSG,
						"Exécuter le programme d'importation"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.EXPORT_OPTION,
						"exporter"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.EXPORT_OPTION_MSG,
						"Exécuter le programme d'exportation"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.DELETE_X_OPTION,
						"xlsx"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.DELETE_X_OPTION_MSG,
						"Delete cards on target boards (from spreadsheet)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.LANGUAGE_OPTION,
						"langue"),
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
						"Terminé à : %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.START_PROGRAM,
						"Commencé à: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.SETTING_LANGUAGE,
						"Mettre la langue en: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.COMMANDLINE_ERROR,
						"D'option de ligne de commande: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_SHEET_NOT_FOUND,
						"Feuille de relecture introuvable. Exécuter avec -c avant (ou avec) -a\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.SET_COMPARE_MODE,
						"Mise en mode \"comparer\"\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.INVALID_OPTIONS,
						"Options non valides spécifiées (-r avec un autre). Par défaut en mode \"repeter\".\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.SET_REPLAY_MODE,
						"Passe en mode \"repeter\"\n")));
	}

	public HashMap<Integer, String> getMap() {
		return map;
	}
}
