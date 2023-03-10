package com.planview.lkutility.Utils;
import com.planview.lkutility.Jira.JiraAccess;
import com.planview.lkutility.System.Debug;
import com.planview.lkutility.System.InternalConfig;
import com.planview.lkutility.System.LMS;

public class JiraDeleter {
	InternalConfig config;
	JiraAccess jAcc = null;
	Debug d = new Debug();

	public int go(InternalConfig cfg, String[] adoDeletes) {
		config = cfg;
		d.setLevel(config.debugLevel);
		d.setMsgr(cfg.msgr);

		//Check that we have both user and token as ADO is non-standard.
		if (config.ado.getApiKey() != null){
			jAcc = new JiraAccess(config.jira, cfg.debugLevel);
		}
		else {
			return -1;
		}
		for (int i = 0; i < adoDeletes.length; i++) {
			String url = adoDeletes[i];
			jAcc.deleteTicket(url);
			d.p(LMS.INFO, config.msgr.getMsg(LMS.JIRA_DELETE), url);	
		}
		return 0;
	}
}
