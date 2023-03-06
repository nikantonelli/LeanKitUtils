package com.planview.lkutility.System;

public class LMS {

	/*
	 * Could use enums, but thats adds a bit of faff with no real gain (unless you
	 * are careless) and I like the different 'sections' here
	 */

	// Debugger numbers have to be int as they are in a switch statement (constant
	// expression)
	public final static int ALWAYS = -1;
	public final static int ERROR = 0;
	public final static int INFO = 1;
	public final static int WARN = 2;
	public final static int DEBUG = 3;
	public final static int VERBOSE = 4;

	// System
	public final static Integer NOTE = 101;
	public static final Integer COMMANDLINE_ERROR = 102;
	public static final Integer SHEET_NOTFOUND_ERROR = 103;

	// Command line options
	public static final Integer REMAKE_OPTION = 1001;
	public static final Integer REMAKE_OPTION_MSG = 1002;
	public static final Integer REMOVE_OPTION = 1003;
	public static final Integer REMOVE_OPTION_MSG = 1004;
	public static final Integer REPLAY_OPTION = 1005;
	public static final Integer REPLAY_OPTION_MSG = 1006;
	public static final Integer COMPARE_OPTION = 1007;
	public static final Integer COMPARE_OPTION_MSG = 1008;
	public static final Integer IMPORT_OPTION = 1009;
	public static final Integer IMPORT_OPTION_MSG = 1010;
	public static final Integer EXPORT_OPTION = 1011;
	public static final Integer EXPORT_OPTION_MSG = 1012;
	public static final Integer FILE_OPTION = 1013;
	public static final Integer FILE_OPTION_MSG = 1014;
	public static final Integer DELETE_X_OPTION = 1015;
	public static final Integer DELETE_X_OPTION_MSG = 1016;
	public static final Integer DELETE_OPTION = 1017;
	public static final Integer DELETE_OPTION_MSG = 1018;
	public static final Integer LANGUAGE_OPTION = 1019;
	public static final Integer LANGUAGE_OPTION_MSG = 1020;
	public static final Integer TASKTOP_OPTION = 1021;
	public static final Integer TASKTOP_OPTION_MSG = 1022;
	public static final Integer GROUP_OPTION = 1023;
	public static final Integer GROUP_OPTION_MSG = 1024;
	public static final Integer MOVE_OPTION = 1025;
	public static final Integer MOVE_OPTION_MSG = 1026;
	public static final Integer DEBUG_OPTION = 1027;
	public static final Integer DEBUG_OPTION_MSG = 1028;
	public static final Integer ARCHIVED_OPTION = 1029;
	public static final Integer ARCHIVED_OPTION_MSG = 1030;
	public static final Integer TASKS_OPTION = 1031;
	public static final Integer TASKS_OPTION_MSG = 1032;
	public static final Integer ATTACHMENTS_OPTION = 1033;
	public static final Integer ATTACHMENTS_OPTION_MSG = 1034;
	public static final Integer COMMENTS_OPTION = 1035;
	public static final Integer COMMENTS_OPTION_MSG = 1036;
	public static final Integer ORIGIN_OPTION = 1037;
	public static final Integer ORIGIN_OPTION_MSG = 1038;
	public static final Integer RO_OPTION = 1039;
	public static final Integer RO_OPTION_MSG = 1040;

	// Output Messages
	public final static Integer START_PROGRAM = 2001;
	public final static Integer FINISH_PROGRAM = 2002;
	public static final Integer SETTING_LANGUAGE = 2003;
	public static final Integer REPLAY_SHEET_NOT_FOUND = 2004;
	public static final Integer SET_COMPARE_MODE = 2005;
	public static final Integer INVALID_OPTIONS = 2006;
	public static final Integer SET_REPLAY_MODE = 2007;

}
