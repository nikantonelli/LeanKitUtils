package com.planview.lkutility.System;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class FrenchLang {

	private HashMap<Integer, String> map;

	public FrenchLang() {
		map = new HashMap<Integer, String>(Map.ofEntries(
				new AbstractMap.SimpleEntry<Integer, String>(LMS.INFO, "Info:"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.VERBOSE, "Verbeaux:"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.WARN, "Avertissement:"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.ERROR, "Erreur:"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.NOTE, "Avis:"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.FINISH_PROGRAM, "Terminé à : %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.START_PROGRAM, "Commencé à: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMOVE_OPTION, "supprimer"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMOVE_OPTION_MSG,
						"Supprimer les tableaux cibles (delete)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMAKE_OPTION, "refaire"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMAKE_OPTION_MSG,
						"Refaire les tableaux cibles en archivant les anciens et en ajoutant de nouveaux"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.SETTING_LANGUAGE,
						"Mettre la langue en: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.COMMANDLINE_ERROR,
						"D'option de ligne de commande: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_SHEET_NOT_FOUND,
						"Feuille de relecture introuvable. Exécuter avec -c avant (ou avec) -a\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.SET_COMPARE_MODE,
						"Mise en mode \"comparer\"\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.INVALID_OPTIONS,
						"Options non valides spécifiées (-r avec un autre). Par défaut en mode Replay.\n")));
	}

	public HashMap<Integer, String> getMap() {
		return map;
	}
}
