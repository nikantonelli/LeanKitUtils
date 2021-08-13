package com.planview.lkutility;

import java.util.Date;

import com.planview.lkutility.leankit.*;

/**
 * These are the fields that we can modify when doing an import. We can provide
 * this list to the exporter to build the spreadsheet from them.
 */

public class SupportedXlsxFields {
    public String title, description, priority, color;
    public Date plannedStart, plannedFinish;    //ISO8601 format
    public ItemType type;
    

    /** Unchecked Fields
        
    public String blockReason, customIconId, customIconLabel, iconPath, wipOverrideComment;
    public CustomId customId;
    public Connections connections;
    public ExternalLink externalLink;
    public String[] tags, assignedUserIds;
    public Integer size, index;
    public CustomField[] customFields;
    public CustomIcon customIcon;
    public ParentCard[] parentCards;
    public User[] assignedUsers;
    public Board board;
    public Lane lane;
    public Boolean isBlocked;
    public Attachment[] attachments;
    public Comment[] comments;

    */

}
