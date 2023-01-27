package com.planview.lkutility.leankit;

public class CardType {
    String id, name, colorHex;
    Boolean isDefault, isCardType, isTaskType, isDefaultTaskType;

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

	public String getColorHex() {
		return colorHex;
	}

	public void setColorHex(String colorHex) {
		this.colorHex = colorHex;
	}

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public Boolean getIsCardType() {
		return isCardType;
	}

	public void setIsCardType(Boolean isCardType) {
		this.isCardType = isCardType;
	}

	public Boolean getIsTaskType() {
		return isTaskType;
	}

	public void setIsTaskType(Boolean isTaskType) {
		this.isTaskType = isTaskType;
	}

	public Boolean getIsDefaultTaskType() {
		return isDefaultTaskType;
	}

	public void setIsDefaultTaskType(Boolean isDefaultTaskType) {
		this.isDefaultTaskType = isDefaultTaskType;
	}

	public CardType() {

	}

	public CardType( String name ){
		this.name = name;
		isCardType = true;
		isTaskType = false;
		colorHex = "#808080";
	}
}
