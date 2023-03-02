package com.planview.lkutility.System;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class EnglishLang {

	private HashMap<Integer, String> map;

	public EnglishLang() {
		map = new HashMap<Integer, String>(Map.ofEntries(
				// Main message beginnings
				new AbstractMap.SimpleEntry<Integer, String>(LMS.INFO, "INFO: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.VERBOSE, "VERBOSE: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.WARN, "WARN: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.ERROR, "ERROR: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.NOTE, "NOTE: "),

				// Command line options
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMOVE_OPTION, "remove"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMOVE_OPTION_MSG,
						"Remove target boards (delete)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMAKE_OPTION, "remake"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMAKE_OPTION_MSG,
						"Remake target boards by archiving old and adding new"),

				// Output Messages
				new AbstractMap.SimpleEntry<Integer, String>(LMS.FINISH_PROGRAM, "Finished at: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.START_PROGRAM, "Started at: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.SETTING_LANGUAGE, "Setting language to: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.COMMANDLINE_ERROR, "%s\n"), //Error comes from the exception in English
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_SHEET_NOT_FOUND, "Replay sheet not found. Run with -c before (or with) -a\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.SET_COMPARE_MODE, "Setting to \"compare\" mode.\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.INVALID_OPTIONS, "Invalid options specified (-r with another). Defaulting to Replay mode.\n")

		));
	}

	public HashMap<Integer, String> getMap() {
		return map;
	}
}
