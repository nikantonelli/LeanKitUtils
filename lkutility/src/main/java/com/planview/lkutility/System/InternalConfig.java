package com.planview.lkutility.System;

import java.util.HashMap;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InternalConfig {

    public final static String CHANGES_SHEET_NAME = "C_";
    public final static Integer MAX_CARDS_PER_BOARD = 10000;    //Maybe add an override on the command line?

	public final static String SOURCE_URL_COLUMN = "srcUrl";
	public final static String SOURCE_BOARDNAME_COLUMN = "srcBoardName";
	public final static String SOURCE_APIKEY_COLUMN = "srcApiKey";
	public final static String DESTINATION_URL_COLUMN = "dstUrl";
	public final static String DESTINATION_BOARDNAME_COLUMN = "dstBoardName";
	public final static String DESTINATION_APIKEY_COLUMN = "dstApiKey";
	public final static String DESTINATION_TID_COLUMN = "Target Id";
	public final static String DESTINATION_ADO_USER = "ADO User";
	public final static String DESTINATION_ADO_TOKEN = "ADO Token";
	public final static String DESTINATION_JIRA_USER = "JIRA User";
	public final static String DESTINATION_JIRA_KEY = "JIRA Key";
	public final static String IGNORE_LIST = "Import Ignore";

    public String xlsxfn = "";


	public static String LANE_DIVIDER_CHAR = "^";
	public static String WIP_DIVIDER_CHAR = "`";
	public static String SPLIT_LANE_REGEX_CHAR = "\\" + LANE_DIVIDER_CHAR;
	public static String SPLIT_WIP_REGEX_CHAR = "\\" + WIP_DIVIDER_CHAR;

    /**
     * Contains the local stream of info for the program. The importer will need
     * multiple sheets, but the exporter will write to this file if selected on the
     * command line.
     */
    public XSSFWorkbook wb = null;
    
	public AccessConfig source = new AccessConfig();
	public AccessConfig destination = new AccessConfig();

	public AccessConfig jira = new AccessConfig();
	public AccessConfig ado = new AccessConfig();

    public Integer   debugLevel = -1;
    public Boolean   exportArchived = false;
    public Boolean   exportTasks = false;
    public Boolean   exportAttachments = false;
    public Boolean   exportComments = false;
    public Boolean   addComment = false;
    public Boolean   dualFlow = false;
    public XSSFSheet changesSheet = null;
    public XSSFSheet itemSheet = null;
    public String    archive = null;
    public XSSFSheet replaySheet;

	public Integer  group = 0;
	public Boolean  exporter = false;
	public Boolean  importer = false;
	public Boolean  obliterate = false;
	public Boolean  remakeBoard = false;
	public Boolean  updateLayout = false;
	public Boolean  deleteCards = false;
	public Boolean  eraseBoard = false;
	public Boolean  ignoreCards = false;
	public String[] ignTypes;
	public Boolean  nameExtension = false;
	public String   extension;
	public String oldExtension;
	public Boolean tasktop = false;
	public Boolean updateLevels = false;
	public Boolean replay = false;
	public Boolean roFieldExport = false;
	public Boolean nameResolver = false;
	public String diffMode;
	public Boolean deleteXlsx = false;
	public Messages msg;

}

