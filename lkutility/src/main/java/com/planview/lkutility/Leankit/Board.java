package com.planview.lkutility.leankit;

import java.util.Date;

public class Board {
    public String id;
    public String title, description, boardRole, customBoardMoniker, customIconFieldLabel, version, subscriptionId, sharedBoardRole, effectiveBoardRole, format, prefix, organizationId, layoutChecksum, defaultCardTypeId, defaultTaskTypeId ;
    public int boardRoleId, currentExternalCardId, cardColorField, templateId;
    public Boolean isDuplicateCardIdAllowed, isAutoIncrementCardIdEnabled, isWelcome, isArchived, isShared, isPermalinkEnabled, isExternalUrlEnabled, allowUsersToDeleteCards,
            allowPlanviewIntegration, classOfServiceEnabled, isCardIdEnabled, isHeaderEnabled, isHyperlinkEnabled, isPrefixIncludedInHyperlink,
            isPrefixEnabled, includeCards, includeExistingUsers, excludeCompletedAndArchiveViolations, baseWipOnCardSize;
    public ClassOfService[] classesOfService;
    public String[] tags;
    public CustomField[] customFields;
    public Date creationDate;
    public Level level;
    public Lane[] lanes;
    public LaneClassType[] laneClassTypes, laneTypes;   //What stupidity is this????
    public User[] users;
    public CardType[] cardTypes;
    public Priority[] priorities;
    public IncrementSeries[] planningSeries;
}