package com.planview.lkutility.Utils;

import com.planview.lkutility.System.Debug;
import com.planview.lkutility.System.InternalConfig;
import com.planview.lkutility.System.LMS;
import com.planview.lkutility.Leankit.Board;

public class BoardDeleter {
	Debug d = new Debug();
	InternalConfig cfg = null;

	public BoardDeleter(InternalConfig config) {
		cfg = config;
		d.setLevel(config.debugLevel);
		d.setMsgr(cfg.msgr);
	}

	public void go() {
		Board brd = LkUtils.getBoardByTitle(cfg, cfg.destination);
		if (brd != null) {
			if (LkUtils.deleteBoard(cfg, cfg.destination)) {
				d.p(LMS.INFO, "Deleted board \"%s\" from \"%s\"\n", cfg.destination.getBoardName(), cfg.destination.getUrl());
			} else {
				d.p(LMS.WARN, "Delete of board \"%s\" from \"%s\" unsuccessful\n", cfg.destination.getBoardName(),
						cfg.destination.getUrl());
			}
		} else {
			d.p(LMS.INFO, "Board \"%s\" not present on \"%s\" (for deletion)\n", cfg.destination.getBoardName(),
					cfg.destination.getUrl());
		}
	}
}