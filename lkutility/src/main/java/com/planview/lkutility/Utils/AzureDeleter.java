package com.planview.lkutility.Utils;

import com.planview.lkutility.Azure.AzureAccess;
import com.planview.lkutility.System.Debug;
import com.planview.lkutility.System.InternalConfig;

public class AzureDeleter {
	InternalConfig config;
	AzureAccess aAcc = null;
	Debug d = new Debug();

	public int go(InternalConfig cfg, String[] adoDeletes) {
		config = cfg;
		d.setLevel(config.debugLevel);
		d.setMsgr(cfg.msg);

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
				d.p(Debug.INFO, "Deleted %s\n", url);
			} else {
				d.p(Debug.INFO, "Failed to delete %s\n", url);
			}
		}
		return 0;
	}
}
