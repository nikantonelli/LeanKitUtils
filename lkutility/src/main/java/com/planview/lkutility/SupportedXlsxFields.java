package com.planview.lkutility;

import java.util.Date;

import com.planview.lkutility.leankit.*;

/**
 * The public ones are the fields that we can modify when doing an import. We can provide
 * this list to the exporter to build the spreadsheet from them.
 */

public class SupportedXlsxFields {
    public String srcID;
    public ItemType type;
    public String title, description, priority;
    
    public Date plannedStart, plannedFinish;    //ISO8601 format
    public Integer size;
    public Lane lane;
    public String[] tags;  
    public CustomId customId; 
    public String color, blockReason;
    public Integer index; 
    public TaskBoardStats taskBoardStats;
    
    /**
     * Unsupported or unsupportable
     * 
    public String customIconId, customIconLabel, iconPath, wipOverrideComment;
 
    public Connections connections;
    public ExternalLink externalLink;
    public String[] assignedUserIds;

    public CustomField[] customFields;
    public CustomIcon customIcon;
    public ParentCard[] parentCards;
    public User[] assignedUsers;
    public Board board;
    public Boolean isBlocked;
    public Attachment[] attachments;
    public Comment[] comments;
    */

}
