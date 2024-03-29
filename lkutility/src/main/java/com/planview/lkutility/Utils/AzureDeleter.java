package com.planview.lkutility.Utils;

import com.planview.lkutility.Azure.AzureAccess;
import com.planview.lkutility.System.Debug;
import com.planview.lkutility.System.InternalConfig;
import com.planview.lkutility.System.LMS;

public class AzureDeleter {
	InternalConfig config;
	AzureAccess aAcc = null;
	Debug d = new Debug();

	public int go(InternalConfig cfg, String[] adoDeletes) {
		config = cfg;
		d.setLevel(config.debugLevel);
		d.setMsgr(cfg.msgr);

		//Check that we have both user and token as ADO is non-standard.
		if ((config.ado.getUser() != null) && (config.ado.getApiKey() != null)){
			aAcc = new AzureAccess(config.ado, cfg.debugLevel);
		}
		else {
			return -1;
		}
		for (int i = 0; i < adoDeletes.length; i++) {
			String url = adoDeletes[i];
			if ( null != aAcc.deleteTicket(url)) {
				d.p(LMS.INFO, config.msgr.getMsg(LMS.AZURE_DELETE), url);
			} else {
				d.p(LMS.INFO, config.msgr.getMsg(LMS.AZURE_DELETE_FAIL), url);
			}
		}
		return 0;
	}
}
