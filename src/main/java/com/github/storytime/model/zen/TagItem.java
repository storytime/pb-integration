package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class TagItem {

    @JsonProperty("parent")
    private String parent;

    @JsonProperty("color")
    private long color;

    @JsonProperty("budgetOutcome")
    private boolean budgetOutcome;

    @JsonProperty("showIncome")
    private boolean showIncome;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("title")
    private String title;

    @JsonProperty("required")
    private boolean required;

    @JsonProperty("picture")
    private Object picture;

    @JsonProperty("budgetIncome")
    private boolean budgetIncome;

    @JsonProperty("id")
    private String id;

    @JsonProperty("showOutcome")
    private boolean showOutcome;

    @JsonProperty("user")
    private int user;

    @JsonProperty("changed")
    private int changed;

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public long getColor() {
        return color;
    }

    public void setColor(long color) {
        this.color = color;
    }

    public boolean isBudgetOutcome() {
        return budgetOutcome;
    }

    public void setBudgetOutcome(boolean budgetOutcome) {
        this.budgetOutcome = budgetOutcome;
    }

    public boolean isShowIncome() {
        return showIncome;
    }

    public void setShowIncome(boolean showIncome) {
        this.showIncome = showIncome;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Object getPicture() {
        return picture;
    }

    public void setPicture(Object picture) {
        this.picture = picture;
    }

    public boolean isBudgetIncome() {
        return budgetIncome;
    }

    public void setBudgetIncome(boolean budgetIncome) {
        this.budgetIncome = budgetIncome;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isShowOutcome() {
        return showOutcome;
    }

    public void setShowOutcome(boolean showOutcome) {
        this.showOutcome = showOutcome;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getChanged() {
        return changed;
    }

    public void setChanged(int changed) {
        this.changed = changed;
    }

    @Override
    public String toString() {
        return
                "TagItem{" +
                        "parent = '" + parent + '\'' +
                        ",color = '" + color + '\'' +
                        ",budgetOutcome = '" + budgetOutcome + '\'' +
                        ",showIncome = '" + showIncome + '\'' +
                        ",icon = '" + icon + '\'' +
                        ",title = '" + title + '\'' +
                        ",required = '" + required + '\'' +
                        ",picture = '" + picture + '\'' +
                        ",budgetIncome = '" + budgetIncome + '\'' +
                        ",id = '" + id + '\'' +
                        ",showOutcome = '" + showOutcome + '\'' +
                        ",user = '" + user + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}