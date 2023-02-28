package com.planview.lkutility.Leankit;

public class BoardBulkAccessId {
	public String[] boardIds,
					userIds;
	public String	boardRole;
	public String[] getBoardIds() {
		return boardIds;
	}
	public void setBoardIds(String[] boardIds) {
		this.boardIds = boardIds;
	}
	public String[] getUserIds() {
		return userIds;
	}
	public void setUserIds(String[] userIds) {
		this.userIds = userIds;
	}
	public String getBoardRole() {
		return boardRole;
	}
	public void setBoardRole(String boardRole) {
		this.boardRole = boardRole;
	}
}
