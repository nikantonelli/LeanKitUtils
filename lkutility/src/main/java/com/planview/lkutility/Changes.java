package com.planview.lkutility;

public class Changes {
    public Integer currChgRow;
    public Integer currItmRow;

    public Changes() {

    }

    public Changes(Integer startChg, Integer startItm) {
        currChgRow = startChg;
        currItmRow = startItm;
    }
}
