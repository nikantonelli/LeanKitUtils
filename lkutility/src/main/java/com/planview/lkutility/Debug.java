package com.planview.lkutility;

public class Debug {
    public final static Integer ERROR = 0;
    public final static Integer INFO = 1;
    public final static Integer WARN = 2;
    public final static Integer DEBUG = 3;
    public final static Integer VERBOSE = 4;
    
    private Integer debugPrint = 0;
    
    public Debug() {};
    
    public  Debug(Integer lvl) {
        debugPrint = lvl;
    }

    public void p(Integer level, String fmt, String str) {
        p(level, fmt, (Object) str);
    }

    public void setLevel(Integer lvl){
        debugPrint = lvl;
    }

    public void p(Integer level, String fmt, Object... parms) {
        String lp = null;
        switch (level) {
            case 1: {
                lp = "INFO: ";
                break;
            }
            case 0: {
                lp = "ERROR: ";
                break;
            }
            case 2: {
                lp = "WARN: ";
                break;
            }
            case 3: {
                lp = "DEBUG: ";
                break;
            }
            case 4: {
                lp = "VERBOSE: ";
                break;
            }
        }
        if (level <= debugPrint) {
            System.out.printf(lp+fmt, parms);
        }
    }
}
