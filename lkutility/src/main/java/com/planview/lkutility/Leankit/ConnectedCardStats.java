package com.planview.lkutility.Leankit;

import java.util.Date;

public class ConnectedCardStats {
    public Date plannedStart, plannedFinish, actualStart, actualFinish;
    public int startedCount, startedSize, notStartedCount, notStartedSize, completedCount, completedSize, blockedCount,
            totalCount, totalSize, pastDueCount, projectedLateCount;
}
