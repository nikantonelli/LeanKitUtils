package com.planview.lkutility.System;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class EnglishLang {

	private HashMap<Integer, String> map;

	public EnglishLang() {
		map = new HashMap<Integer, String>(Map.ofEntries(
				// Main message beginnings
				new AbstractMap.SimpleEntry<Integer, String>(LMS.INFO,
						"INFO: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.VERBOSE,
						"VERBOSE: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.WARN,
						"WARN: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.ERROR,
						"ERROR: "),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.NOTE,
						"NOTE: "),

				// Command line options
				new AbstractMap.SimpleEntry<Integer, String>(LMS.FILE_OPTION,
						"filename"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.FILE_OPTION_MSG,
						"XLSX Spreadsheet (must contain API config!)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMOVE_OPTION,
						"remove"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMOVE_OPTION_MSG,
						"Remove target boards (delete)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMAKE_OPTION,
						"remake"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REMAKE_OPTION_MSG,
						"Remake target boards by archiving old and adding new"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_OPTION,
						"replay"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_OPTION_MSG,
						"Remake target boards by archiving old and adding new"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.COMPARE_OPTION,
						"compare"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.COMPARE_OPTION_MSG,
						"compare dst URL to a previous transfer"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.IMPORT_OPTION,
						"import"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.IMPORT_OPTION_MSG,
						"run importer"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.EXPORT_OPTION,
						"export"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.EXPORT_OPTION_MSG,
						"run exporter"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.DELETE_X_OPTION,
						"xlsx"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.DELETE_X_OPTION_MSG,
						"Delete cards on target boards (from spreadsheet)"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.LANGUAGE_OPTION,
						"language"),
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
						"Finished at: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.START_PROGRAM,
						"Started at: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.SETTING_LANGUAGE,
						"Setting language to: %s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.COMMANDLINE_ERROR,
						"%s\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.SHEET_NOTFOUND_ERROR,
						"(-5) Did not detect required sheet in the spreadsheet: \"Config\""),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_SHEET_NOT_FOUND,
						"Replay sheet not found. Run with -c before (or with) -a\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.SET_COMPARE_MODE,
						"Setting to \"compare\" mode.\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.INVALID_OPTIONS,
						"Invalid options specified (-r with another). Defaulting to Replay mode.\n"),
				new AbstractMap.SimpleEntry<Integer, String>(LMS.SET_REPLAY_MODE,
						"Setting to Replay mode.\n")

		));
	}

	public HashMap<Integer, String> getMap() {
		return map;
	}
}
