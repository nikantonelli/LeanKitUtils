package com.planview.lkutility.System;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class FrenchLang {

	private HashMap<Integer, String> map;

	public FrenchLang() {
		map = new HashMap<Integer,String>(Map.ofEntries(
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.INFO, "Info:"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.VERBOSE, "Verbeaux:"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.WARN, "Avertissement:"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.ERROR, "Erreur:"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.NOTE, "Avis:"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.FINISH_PROGRAM, "Terminé à : %s\n"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.START_PROGRAM, "Commencé à: %s\n"),
			new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.REMOVE_OPTION, "supprimer"),
				new AbstractMap.SimpleEntry<Integer, String>(LanguageMessages.REMOVE_OPTION_MSG,
						"Supprimer les tableaux cibles (delete)"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.REMAKE_OPTION, "refaire"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.REMAKE_OPTION_MSG, 
				"Refaire les tableaux cibles en archivant les anciens et en ajoutant de nouveaux")

		));
	}

	public HashMap<Integer, String> getMap(){
		return map;
	}
}
