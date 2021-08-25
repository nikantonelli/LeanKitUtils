package com.planview.lkutility.leankit;

import java.util.Date;

public class Card {
    public String id, title, typeId, description, laneId, mirrorSourceCardId, copiedFromCardId, 
            blockReason, priority, subscriptionId, version, containingCardId,
            customIconId, customIconLabel, iconPath, color, wipOverrideComment;
    public CustomId customId;
    public Connections connections;
    public ExternalLink externalLink;
    public String[] tags, assignedUserIds;
    public Integer commentsCount, childCommentsCount, size, index;
    public Date updatedOn, movedOn, createdOn, archivedOn, plannedStart, plannedFinish, actualStart, actualFinish;
    public CustomField[] customFields;
    public CustomIcon customIcon;
    public ConnectedCardStats connectedCardStats;
    public ParentCard[] parentCards;
    public User[] assignedUsers;
    public User createdBy, updatedBy, movedBy, archivedBy;
    public ItemType type;
    public BlockedStatus blockedStatus;
    public Board board;
    public Lane lane;
    public TaskBoard taskBoard;
    public Boolean isBlocked, canView;
    public ExternalLink[] externalLinks;
    public TaskBoardStats taskBoardStats;
    public Attachment[] attachments;
    public Comment[] comments;
}
