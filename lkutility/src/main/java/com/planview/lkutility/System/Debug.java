package com.planview.lkutility.System;

public class Debug {

	private int debugPrint = 0;
	private Languages msgr;

	public void setMsgr(Languages msgr) {
		this.msgr = msgr;
	}

	public Debug() {
	};

	public Debug(int lvl) {
		debugPrint = lvl;
	}

	public void p(int level, String fmt, String str) {
		p(level, fmt, (Object) str);
	}

	public void setLevel(int lvl) {
		debugPrint = lvl;
	}

	public void p(int level, String fmt, Object... parms) {
		String lp = null;
		switch (level) {
			case LMS.INFO: {
				lp = (msgr == null) ? "INFO: " : msgr.getMsg(LMS.INFO);
				break;
			}
			case LMS.ERROR: {
				lp = (msgr == null) ? "ERROR: " : msgr.getMsg(LMS.ERROR);
				break;
			}
			case LMS.WARN: {
				lp = (msgr == null) ? "WARN: " : msgr.getMsg(LMS.WARN);
				break;
			}
			case LMS.DEBUG: {
				lp = (msgr == null) ? "DEBUG: " : msgr.getMsg(LMS.DEBUG);
				break;
			}
			case LMS.VERBOSE: {
				lp = (msgr == null) ? "VERBOSE: " : msgr.getMsg(LMS.VERBOSE);
				break;
			}
			case LMS.ALWAYS:
			default: {
				lp = (msgr == null) ? "NOTE: " : msgr.getMsg(LMS.ALWAYS);
			}
		}
		if (level <= debugPrint) {
			System.out.printf(lp + fmt, parms);
		}
	}
}
