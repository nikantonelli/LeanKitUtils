package com.planview.lkutility.Leankit;

import java.util.Date;

public class CustomField {
    public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String fieldId, id, type,label,helpText, path;
    public Object value;
    public Integer index;
    public ChoiceConfig choiceConfiguration;
    public String getFieldId() {
		return fieldId;
	}
	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getHelpText() {
		return helpText;
	}
	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	public ChoiceConfig getChoiceConfiguration() {
		return choiceConfiguration;
	}
	public void setChoiceConfiguration(ChoiceConfig choiceConfiguration) {
		this.choiceConfiguration = choiceConfiguration;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public Date getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(Date createdBy) {
		this.createdBy = createdBy;
	}
	public Date createdOn, createdBy;
}
