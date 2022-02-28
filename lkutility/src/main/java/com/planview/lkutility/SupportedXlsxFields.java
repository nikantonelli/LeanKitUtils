package com.planview.lkutility;

import java.util.Date;

import com.planview.lkutility.leankit.*;

/**
 * The public ones are the fields that we can modify when doing an import. We
 * can provide
 * this list to the exporter to build the spreadsheet from them.
 */

public class SupportedXlsxFields {

    /**
     * The order of these fields determine the order they are processed in
     * This is importent, for example, when a Lane needs to be changed and 
     * then the index set. If you set an index that is too high, LK just
     * puts the item last
     */
    public class Modifiable {
        public String srcID;
        public String title, description;
        public ItemType type;
        public Date plannedStart, plannedFinish; // ISO8601 format
        public Integer size;
        public Lane lane;
        public String priority;
        public String[] tags;
        public CustomId customId;
        public String color, blockReason;
        public Integer index;
        public User[] assignedUsers;
        public CustomIcon customIcon;
        public ExternalLink externalLink; // Should really be in the plural, but I have never seen
                                          // support for more than one.
        /*
         * public String wipOverrideComment; //This is part of the 'lane' column,
         * //comma separated from lane and we can't get at it
         * // on export. It's available for import only
         */

        /*
         * public CustomField[] customFields; //Custom fields are found and listed
         * //as if they were normal fields
         */
        /*
         * public Boolean isBlocked; //Gets translated into blockReason in spreadsheet
         */

        /*
         * public ParentCard[] parentCards; //Seems to be unpopulated in the
         * /io/card?board= call
         * //so we had to make a special call per card to get the parents
         */

    }

    public class ReadOnly {
        public Date createdOn, updatedOn, movedOn, archivedOn, actualStart, actualFinish;
        public Integer commentsCount, childCommentsCount, version;
        public User createdBy, updatedBy, archivedBy;
    }

    /**
     * These are pseudo-fields. When these are seen, some extra
     * activity needs to take place and not be sent to the spreadsheet
     * Compiler gives 'unused' warning, which we will ignore. We just use the names
     * of the fields in a big switch statement.
     */
    public class Pseudo {
        public ParentCard[] parentCards;
        public Comment[] comments;
        public Attachment[] attachments;
        public TaskBoardStats taskBoardStats;
    }

    /**
     * All the following are
     * unsupported or unsupportable
     * 
     * public String customIconId, customIconLabel, iconPath;
     * public Connections connections;
     * 
     * public Board board;
     * 
     * 
     */
}
