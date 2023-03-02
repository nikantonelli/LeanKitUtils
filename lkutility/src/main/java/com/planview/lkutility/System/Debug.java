package com.planview.lkutility.System;

public class Debug {
	public final static int ALWAYS = -1;
	public final static int ERROR = 0;
	public final static int INFO = 2;
	public final static int WARN = 1;
	public final static int DEBUG = 3;

	public final static int VERBOSE = 4;

	private Integer debugPrint = 0;
	private Languages msgr;

	public void setMsgr(Languages msgr) {
		this.msgr = msgr;
	}

	public Debug() {
	};

	public Debug(Integer lvl) {
		debugPrint = lvl;
	}

	public void p(Integer level, String fmt, String str) {
		p(level, fmt, (Object) str);
	}

	public void setLevel(Integer lvl) {
		debugPrint = lvl;
	}

	public void p(Integer level, String fmt, Object... parms) {
		String lp = null;
		switch (level) {
			case INFO: {
				lp = (msgr == null) ? "INFO: ": msgr.getMsg(LMS.INFO);
				break;
			}
			case ERROR: {
				lp = (msgr == null) ? "ERROR: ": msgr.getMsg(LMS.ERROR);
				break;
			}
			case WARN: {
				lp = (msgr == null) ? "WARN: ": msgr.getMsg(LMS.WARN);
				break;
			}
			case DEBUG: {
				lp = (msgr == null) ? "DEBUG: ": msgr.getMsg(LMS.DEBUG);
				break;
			}
			case VERBOSE: {
				lp = (msgr == null) ? "VERBOSE: ": msgr.getMsg(LMS.VERBOSE);
				break;
			}
			case ALWAYS:
			default: {
				lp = (msgr == null) ? "NOTE: ": msgr.getMsg(LMS.NOTE);
			}
		}
		if (level <= debugPrint) {
			System.out.printf(lp + fmt, parms);
		}
	}
}
