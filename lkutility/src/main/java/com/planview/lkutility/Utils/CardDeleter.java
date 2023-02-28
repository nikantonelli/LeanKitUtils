package com.planview.lkutility.Utils;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

import com.planview.lkutility.Leankit.Card;
import com.planview.lkutility.Leankit.ExternalLink;
import com.planview.lkutility.Leankit.LeanKitAccess;
import com.planview.lkutility.System.Debug;
import com.planview.lkutility.System.InternalConfig;

public class CardDeleter {
	private static final int MAX_ID_ARRAY_SIZE = 200;
	Debug d = new Debug();
	InternalConfig cfg = null;

	public CardDeleter(InternalConfig config) {
		cfg = config;
		d.setLevel(config.debugLevel);
	}

	public void go() {
		ArrayList<Card> cards = LkUtils.getCardIdsFromBoard(cfg, cfg.destination);

		String[] adoDeletes = {};
		String[] jiraDeletes = {};
		String[] apDeletes = {};

		if (cards == null) return;
		
		for (int i = 0; i < cards.size(); i++) {
			apDeletes = (String[]) ArrayUtils.add(apDeletes, cards.get(i).id);
			ExternalLink[] extUrls = cards.get(i).externalLinks;
			for (int j = 0; j < extUrls.length; j++) {
				String url = extUrls[j].url;
				if (url != null) {
					if (url.startsWith("https://")) {
						if (url.contains("atlassian.net")) {
							// Delete from JIRA
							// https://planview1.atlassian.net/browse/AA-1701
							jiraDeletes = (String[]) ArrayUtils.add(jiraDeletes, url);
						} else if (url.contains("visualstudio.com") || url.contains("dev.azure.com")) {
							// Delete from ADO
							// https://leankitdemo.visualstudio.com/Team%20Echo%20Development/_workitems/edit/2605
							adoDeletes = (String[]) ArrayUtils.add(adoDeletes, url);
						}
					}
					else {
						d.p(Debug.INFO, "Ignoring non-https externalLink \"%s\"\n", url);
					}
				}
			}
		}

		if (cfg.tasktop) {
			//Execute the deletes for Jira and ADO
			if (jiraDeletes.length > 0) {
				JiraDeleter jira = new JiraDeleter();
				jira.go(cfg, jiraDeletes);
			}
			if (adoDeletes.length >0) {
				AzureDeleter ado = new AzureDeleter();
				ado.go(cfg, adoDeletes);
			}
		}

		LeanKitAccess lka = new LeanKitAccess(cfg.destination, cfg.debugLevel);
		for (int i = 0; i < apDeletes.length; ) {
			String[] ids = {};
			int j = 0;
			for (; j < MAX_ID_ARRAY_SIZE; j++) {
				if ((i + j) >= apDeletes.length) {
					break;
				}
				ids = (String[]) ArrayUtils.add(ids, apDeletes[i + j]);
				d.p(Debug.INFO, "Deleting card from AgilePlace \"%s\" (%s)\n", cards.get(i+j).title, cards.get(i+j).id);
			}
			i += j;
			lka.deleteCards(ids);
		}
		// lka.deleteCard(cards.get(i).id);
	}
}
