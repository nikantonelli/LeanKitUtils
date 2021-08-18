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
    /**
     * These are pseudo-fields. When these are seen, some extra
     * activity needs to take place and not be sent to the spreadsheet
     * Compiler gives warning which we will ignore.
     */
    private TaskBoardStats taskBoardStats;
    private ParentCard[] parentCards;
    private Comment[] comments;
    private Attachment[] attachments;
    
    /**
     * Unsupported or unsupportable
     * 
    public String customIconId, customIconLabel, iconPath, wipOverrideComment;
 
    public Connections connections;
    public ExternalLink externalLink;
    public String[] assignedUserIds;

    public CustomField[] customFields;
    public CustomIcon customIcon;
    public ParentCard[] parentCards;    //Seems to be unpopulated in the /io/card?board= call
                                        //so we had to make a call per card to get the parents
    public User[] assignedUsers;
    public Board board;
    public Boolean isBlocked;
    public Attachment[] attachments;
    */
}
