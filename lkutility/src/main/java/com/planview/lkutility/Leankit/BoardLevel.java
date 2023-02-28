package com.planview.lkutility.Leankit;

import org.json.JSONObject;

public class BoardLevel {
	public String id, label, color;
	public Integer depth, maxDepth, unarchivedBoardCount, archivedBoardCount;

	public BoardLevel(){
		
	}
	public  BoardLevel(Integer depth, String label, String color) {
		this.depth = depth;
		this.label = label;
		this.color = color;
	}

	@Override
	public String toString() {
		JSONObject retval = new JSONObject();
		retval.put("id", id);
		retval.put("label", label);
		retval.put("color", color);
		retval.put("depth", depth);
		retval.put("maxDepth", maxDepth);
		retval.put("unarchivedBoardCount", unarchivedBoardCount);
		retval.put("archivedBoardCount", archivedBoardCount);
		return retval.toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Integer getDepth() {
		return depth;
	}

	public void setDepth(Integer depth) {
		this.depth = depth;
	}

	public Integer getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(Integer maxDepth) {
		this.maxDepth = maxDepth;
	}

	public Integer getUnarchivedBoardCount() {
		return unarchivedBoardCount;
	}

	public void setUnarchivedBoardCount(Integer unarchivedBoardCount) {
		this.unarchivedBoardCount = unarchivedBoardCount;
	}

	public Integer getArchivedBoardCount() {
		return archivedBoardCount;
	}

	public void setArchivedBoardCount(Integer archivedBoardCount) {
		this.archivedBoardCount = archivedBoardCount;
	}

}
