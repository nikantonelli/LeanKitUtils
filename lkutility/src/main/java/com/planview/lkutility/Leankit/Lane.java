package com.planview.lkutility.Leankit;

import java.util.Arrays;
import java.util.Date;

public class Lane {
	public String activityId, description, id, name, laneClassType, laneType, orientation, parentLaneId, sortBy,
			subscriptionId, title, cardStatus, boardId, type, classType;
	public TaskBoard taskBoard;
	public Boolean active, isDefaultDropLane, isConnectionDoneLane, isCollapsed;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((orientation == null) ? 0 : orientation.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((boardId == null) ? 0 : boardId.hashCode());
		result = prime * result + ((isDefaultDropLane == null) ? 0 : isDefaultDropLane.hashCode());
		result = prime * result + ((wipLimit == null) ? 0 : wipLimit.hashCode());
		result = prime * result + ((columns == null) ? 0 : columns.hashCode());
		result = prime * result + ((index == null) ? 0 : index.hashCode());
		result = prime * result + Arrays.hashCode(children);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Lane other = (Lane) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (orientation == null) {
			if (other.orientation != null)
				return false;
		} else if (!orientation.equals(other.orientation))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (boardId == null) {
			if (other.boardId != null)
				return false;
		} else if (!boardId.equals(other.boardId))
			return false;
		if (isDefaultDropLane == null) {
			if (other.isDefaultDropLane != null)
				return false;
		} else if (!isDefaultDropLane.equals(other.isDefaultDropLane))
			return false;
		if (wipLimit == null) {
			if (other.wipLimit != null)
				return false;
		} else if (!wipLimit.equals(other.wipLimit))
			return false;
		if (columns == null) {
			if (other.columns != null)
				return false;
		} else if (!columns.equals(other.columns))
			return false;
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
		if (!Arrays.equals(children, other.children))
			return false;
		return true;
	}

	public Integer wipLimit, columns, cardLimit, cardCount, cardSize, index, archiveCardCount;
	public Date creationDate;
	public Lane[] children;

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLaneClassType() {
		return laneClassType;
	}

	public void setLaneClassType(String laneClassType) {
		this.laneClassType = laneClassType;
	}

	public String getLaneType() {
		return laneType;
	}

	public void setLaneType(String laneType) {
		this.laneType = laneType;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public String getParentLaneId() {
		return parentLaneId;
	}

	public void setParentLaneId(String parentLaneId) {
		this.parentLaneId = parentLaneId;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCardStatus() {
		return cardStatus;
	}

	public void setCardStatus(String cardStatus) {
		this.cardStatus = cardStatus;
	}

	public String getBoardId() {
		return boardId;
	}

	public void setBoardId(String boardId) {
		this.boardId = boardId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getClassType() {
		return classType;
	}

	public void setClassType(String classType) {
		this.classType = classType;
	}

	public TaskBoard getTaskBoard() {
		return taskBoard;
	}

	public void setTaskBoard(TaskBoard taskBoard) {
		this.taskBoard = taskBoard;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getIsDefaultDropLane() {
		return isDefaultDropLane;
	}

	public void setIsDefaultDropLane(Boolean isDefaultDropLane) {
		this.isDefaultDropLane = isDefaultDropLane;
	}

	public Boolean getIsConnectionDoneLane() {
		return isConnectionDoneLane;
	}

	public void setIsConnectionDoneLane(Boolean isConnectionDoneLane) {
		this.isConnectionDoneLane = isConnectionDoneLane;
	}

	public Boolean getIsCollapsed() {
		return isCollapsed;
	}

	public void setIsCollapsed(Boolean isCollapsed) {
		this.isCollapsed = isCollapsed;
	}

	public Integer getWipLimit() {
		return wipLimit;
	}

	public void setWipLimit(Integer wipLimit) {
		this.wipLimit = wipLimit;
	}

	public Integer getColumns() {
		return columns;
	}

	public void setColumns(Integer columns) {
		this.columns = columns;
	}

	public Integer getCardLimit() {
		return cardLimit;
	}

	public void setCardLimit(Integer cardLimit) {
		this.cardLimit = cardLimit;
	}

	public Integer getCardCount() {
		return cardCount;
	}

	public void setCardCount(Integer cardCount) {
		this.cardCount = cardCount;
	}

	public Integer getCardSize() {
		return cardSize;
	}

	public void setCardSize(Integer cardSize) {
		this.cardSize = cardSize;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Integer getArchiveCardCount() {
		return archiveCardCount;
	}

	public void setArchiveCardCount(Integer archiveCardCount) {
		this.archiveCardCount = archiveCardCount;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Lane[] getChildren() {
		return children;
	}

	public void setChildren(Lane[] children) {
		this.children = children;
	}

	public Lane copy() {
		Lane newLane = new Lane();
		newLane.columns = columns;
		newLane.description = description;
		newLane.wipLimit = wipLimit;
		newLane.title = name;
		newLane.type = laneType;
		newLane.classType = laneClassType;
		newLane.index = index;
		newLane.orientation = orientation;
		newLane.isConnectionDoneLane = isConnectionDoneLane;
		newLane.isDefaultDropLane = isDefaultDropLane;
		newLane.id = id;
		return newLane;
	}
}
