package com.planview.lkutility;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InternalConfig extends Configuration {
    public String xlsxfn = "";

    /**
     * Contains the local stream of info for the program. The importer will need
     * multiple sheets, but the exporter will write to this file if selected on the
     * command line.
     */
    XSSFWorkbook wb = null;
    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
}
