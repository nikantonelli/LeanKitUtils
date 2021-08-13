package com.planview.lkutility;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InternalConfig {

    public final static String CHANGES_SHEET_NAME = "Changes";
    public final static Integer MAX_CARDS_PER_BOARD = 10000;    //Maybe add an override on the command line?

    public String xlsxfn = "";

    /**
     * Contains the local stream of info for the program. The importer will need
     * multiple sheets, but the exporter will write to this file if selected on the
     * command line.
     */
    public XSSFWorkbook wb = null;
    public PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    public Integer debugLevel = -1;
    public Configuration source = new Configuration();
    public Configuration destination = new Configuration();
    public Boolean exportArchived = false;
    public Boolean exportTasks = false;
}

