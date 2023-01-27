package com.planview.lkutility.System;

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
	public final static String WIP_LIMIT_SEPARATOR = "\\";
	public final static String LANE_SEPARATOR = "^";
	public final static String REGEX_WIP_LIMIT_SEPARATOR = "\\\\";
	public final static String REGEX_LANE_SEPARATOR = "\\^";

    public String xlsxfn = "";

    /**
     * Contains the local stream of info for the program. The importer will need
     * multiple sheets, but the exporter will write to this file if selected on the
     * command line.
     */
    public XSSFWorkbook wb = null;
    
	public AccessConfig source = new AccessConfig();
	public AccessConfig destination = new AccessConfig();
    public Integer debugLevel = -1;
    public boolean exportArchived = false;
    public boolean exportTasks = false;
    public boolean exportAttachments = false;
    public boolean exportComments = false;
    public boolean addComment = false;
    public Boolean dualFlow = false;
    public XSSFSheet changesSheet = null;
    public XSSFSheet itemSheet = null;
    public String archive = null;
	public String diffMode = null;
    public XSSFSheet replaySheet = null;
    public boolean replay = false;
	public Boolean roFieldExport = false;
	public Boolean nameResolver = false;
	public Integer group = 0;
	public boolean exporter = false;
	public boolean importer = false;
	public boolean obliterate = false;
	public boolean remakeBoard = false;
	public boolean updateLayout = false;
	public boolean deleteCards = false;
	public boolean eraseBoard = false;
	public boolean ignoreCards = false;
	public String[] ignTypes = null;
	public boolean  nameExtension = false;
	public String   extension = null;
	public String oldExtension = null;
	public boolean tasktop;
}

