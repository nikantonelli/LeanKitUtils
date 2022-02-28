package com.planview.lkutility;

import com.planview.lkutility.leankit.AccessCache;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InternalConfig {

    public final static String CHANGES_SHEET_NAME = "C_";
    public final static Integer MAX_CARDS_PER_BOARD = 10000;    //Maybe add an override on the command line?

    public String xlsxfn = "";

    /**
     * Contains the local stream of info for the program. The importer will need
     * multiple sheets, but the exporter will write to this file if selected on the
     * command line.
     */
    public XSSFWorkbook wb = null;
    
    public Integer debugLevel = -1;
    public Configuration source = new Configuration();
    public Configuration destination = new Configuration();
    public boolean exportArchived = false;
    public boolean exportTasks = false;
    public boolean exportAttachments = false;
    public boolean exportComments = false;
    public boolean addComment = false;
    public Integer group = 0;
    public AccessCache cache = null;    //Set later.
    public Boolean dualFlow = false;
    public XSSFSheet changesSheet = null;
    public XSSFSheet itemSheet = null;
    public String archive = null;
    public boolean roFieldExport;
    public String diffMode;
    public XSSFSheet replaySheet;
    public boolean replay;
}

