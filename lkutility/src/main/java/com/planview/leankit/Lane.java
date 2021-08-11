package com.planview.leankit;

import java.util.Date;

public class Lane {
    public String activityId, description, id, name, laneClassType, laneType, orientation, parentLaneId, sortBy,
            subscriptionId, title, taskBoard, cardStatus;
    public Boolean active, isDefaultDropLane, isConnectionDoneLane, isCollapsed;
    public Integer wipLimit, columns, cardLimit, cardCount, cardSize, index, archiveCardCount;
    public Date creationDate;
}
