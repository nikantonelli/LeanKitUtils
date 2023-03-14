package com.planview.lkutility.System;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class EnglishLang {

	private HashMap<Integer, String> map;

	public EnglishLang() {
		try {
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
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ALWAYS,
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

					// Main.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.FINISH_PROGRAM,
							"Finished at: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.START_PROGRAM,
							"Started at: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SETTING_LANGUAGE,
							"Setting language to: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.COMMANDLINE_ERROR,
							"%s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SHEET_NOTFOUND_ERROR,
							"Did not detect required sheet in the spreadsheet: \"Config\""),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SHT_HDR_ERROR,
							"Did not detect any header info on Config sheet (first row!)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SHT_COL_ERROR,
							"Did not detect correct columns on Config sheet"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SHT_ITR_ERROR,
							"Did not detect any potential transfer info on Config sheet (first cell must be non-blank, e.g. url to a real host)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_SHEET_NOT_FOUND,
							"Replay sheet not found. Run with -c before (or with) -a\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SET_COMPARE_MODE,
							"Setting to \"compare\" mode.\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.INVALID_OPTIONS,
							"Invalid options specified (-r with another). Defaulting to Replay mode.\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SET_REPLAY_MODE,
							"Setting to Replay mode.\n"),

					// LeanKitAccess.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.NO_HTTP,
							"http access to AgilePlace not supported. Switching to https\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.STATUSCODE_ERROR,
							"\"%s\" gave response: \"%s\"\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.UNKNOWN_API_TYPE_ERROR,
							"Unsupported item type requested from server API: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.RECEIVED_DATA,
							"Received %d %s (out of %d)\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.UNRECOGNISED_TYPE,
							"oops! don't recognise requested item type:"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.CANNOT_ENCODE_BOARD,
							"Cannot encode board name"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DOWNLOAD_ATT_TYPE,
							"Downloaded attachment type: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.AUTO_FROM_SCRIPT,
							"Auto-generated from Script"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SETTING_PARENT,
							"Trying to set parent of %s to value \"%s\"\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.EXTLINK_ERROR,
							"Could not extract externalLink from %s (possible ',' in label?)"),

					// NetworkAccess.java output messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.NEED_SECURE_MODE,
							"http access not supported. Switching to https"),

					new AbstractMap.SimpleEntry<Integer, String>(LMS.APIKEY_ERROR,
							"No valid apiKey provided"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.CREDS_ERROR,
							"Unauthorised. Check Credentials in spreadsheet:"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.NETWORK_FAULT_ERROR,
							"Network fault"),

					// BoardCreator.java output messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.BOARD_COPY_ERROR,
							"Cannot duplicate locally from \"%s\" to \"%s\"\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.BOARD_NOT_FOUND_ERROR,
							"Cannot locate board with title: \"%s\"\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.BOARD_CREATE_ERROR,
							"Could not create/locate destination board "),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.LAYOUT_CONV_ERROR,
							"Layout conversion failed: "),

					// Importer.java output messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.IMP_NO_CHG_SHT,
							"Cannot find required Changes sheet in file: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.IMP_CREATE_FAIL,
							"Could not create card on board \"%s\" with details: \"%s\"\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.IMP_MOD_FAIL,
							"Could not modify card \"%s\" on board %s with details: %s"),

					// Diff.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DIFF_NOT_FOUND,
							"sheets not found for src board:"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DIFF_FETCH_ERROR,
							"Oops! fetch of new data for board: %s failed\n"),

					// Exporter.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.EXP_INVALID_TYPE,
							"Invalid card type - check \"Task\" setting on \"%s\". Opting to use lane \"%s\"\n"),

					// XlUtils.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.XLUTILS_COLS_ERROR,
							"Could not find all required columns in sheet:"),

					// AzureDeleter.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.AZURE_DELETE,
							"Deleted %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.AZURE_DELETE_FAIL,
							"Failed to Delete %s\n"),

					// JiraDeleter.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.JIRA_DELETE,
							"Delete attempted %s\n")

			));
		} catch (Exception e) {
			System.out.printf("English Language Map Error: %s\n", e.getMessage());
		}
	}

	public HashMap<Integer, String> getMap() {
		return map;
	}
}
