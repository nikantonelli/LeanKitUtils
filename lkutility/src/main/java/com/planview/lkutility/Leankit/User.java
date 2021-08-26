package com.planview.lkutility.leankit;

import java.util.Date;

public class User {
    public String id, username, firstName, lastName, fullName, emailAddress, organizationId, dateFormat, timeZone, licenseType, externalUserName, avatar, boardId, wip, roleTypeId;
    public Boolean administrator, accountOwner, enabled, deleted, boardCreator;
    public BoardCount boardCount;
    public Date createdOn, lastAccess;
    public String gravatarLink;
    public BoardRole[] boardRoles;
    public UserSetting settings;
}
