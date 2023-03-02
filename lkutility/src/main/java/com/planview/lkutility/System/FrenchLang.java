package com.planview.lkutility.System;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class FrenchLang {

	private HashMap<Integer, String> map;

	public FrenchLang() {
		map = new HashMap<Integer,String>(Map.ofEntries(
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.INFO, "Info:\n"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.VERBOSE, "Verbeaux:\n"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.WARN, "Avertissement:\n"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.ERROR, "Erreur:\n"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.NOTE, "Avis:\n"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.FINISH_PROGRAM, "Terminé à : %s\n"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.START_PROGRAM, "Commencé à: %s\n"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.REMAKE_OPTION, "refaire"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.REMAKE_OPTION_MSG, 
				"Refaire les tableaux cibles en archivant les anciens et en ajoutant de nouveaux")

		));
	}

	public HashMap<Integer, String> getMap(){
		return map;
	}
}
