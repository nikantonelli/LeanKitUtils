package com.planview.lkutility.System;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class EnglishLang {

	private HashMap<Integer, String> map;

	public EnglishLang() {
		map = new HashMap<Integer,String>(Map.ofEntries(
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.INFO, "INFO: "),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.VERBOSE, "VERBOSE: "),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.WARN, "WARN: "),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.ERROR, "ERROR: "),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.NOTE, "NOTE: "),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.FINISH_PROGRAM, "Finished at: %s\n"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.START_PROGRAM, "Started at: %s\n"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.REMAKE_OPTION, "remake"),
			new AbstractMap.SimpleEntry<Integer,String>(LanguageMessages.REMAKE_OPTION_MSG, 
				"Remake target boards by archiving old and adding new")
		));
	}

	public HashMap<Integer, String> getMap(){
		return map;
	}
}
