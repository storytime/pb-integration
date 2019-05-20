package com.github.storytime.model.ynab.category;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class YnabCategories {

    @JsonProperty("note")
    private Object note;

    @JsonProperty("goal_type")
    private Object goalType;

    @JsonProperty("hidden")
    private boolean hidden;

    @JsonProperty("activity")
    private int activity;

    @JsonProperty("goal_target")
    private int goalTarget;

    @JsonProperty("goal_percentage_complete")
    private Object goalPercentageComplete;

    @JsonProperty("original_category_group_id")
    private Object originalCategoryGroupId;

    @JsonProperty("budgeted")
    private int budgeted;

    @JsonProperty("deleted")
    private boolean deleted;

    @JsonProperty("balance")
    private int balance;

    @JsonProperty("goal_target_month")
    private Object goalTargetMonth;

    @JsonProperty("name")
    private String name;

    @JsonProperty("goal_creation_month")
    private Object goalCreationMonth;

    @JsonProperty("id")
    private String id;

    @JsonProperty("category_group_id")
    private String categoryGroupId;

    public Object getNote() {
        return note;
    }

    public void setNote(Object note) {
        this.note = note;
    }

    public Object getGoalType() {
        return goalType;
    }

    public void setGoalType(Object goalType) {
        this.goalType = goalType;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public int getActivity() {
        return activity;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public int getGoalTarget() {
        return goalTarget;
    }

    public void setGoalTarget(int goalTarget) {
        this.goalTarget = goalTarget;
    }

    public Object getGoalPercentageComplete() {
        return goalPercentageComplete;
    }

    public void setGoalPercentageComplete(Object goalPercentageComplete) {
        this.goalPercentageComplete = goalPercentageComplete;
    }

    public Object getOriginalCategoryGroupId() {
        return originalCategoryGroupId;
    }

    public void setOriginalCategoryGroupId(Object originalCategoryGroupId) {
        this.originalCategoryGroupId = originalCategoryGroupId;
    }

    public int getBudgeted() {
        return budgeted;
    }

    public void setBudgeted(int budgeted) {
        this.budgeted = budgeted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public Object getGoalTargetMonth() {
        return goalTargetMonth;
    }

    public void setGoalTargetMonth(Object goalTargetMonth) {
        this.goalTargetMonth = goalTargetMonth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getGoalCreationMonth() {
        return goalCreationMonth;
    }

    public void setGoalCreationMonth(Object goalCreationMonth) {
        this.goalCreationMonth = goalCreationMonth;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryGroupId() {
        return categoryGroupId;
    }

    public void setCategoryGroupId(String categoryGroupId) {
        this.categoryGroupId = categoryGroupId;
    }

    @Override
    public String toString() {
        return
                "YnabCategories{" +
                        "note = '" + note + '\'' +
                        ",goal_type = '" + goalType + '\'' +
                        ",hidden = '" + hidden + '\'' +
                        ",activity = '" + activity + '\'' +
                        ",goal_target = '" + goalTarget + '\'' +
                        ",goal_percentage_complete = '" + goalPercentageComplete + '\'' +
                        ",original_category_group_id = '" + originalCategoryGroupId + '\'' +
                        ",budgeted = '" + budgeted + '\'' +
                        ",deleted = '" + deleted + '\'' +
                        ",balance = '" + balance + '\'' +
                        ",goal_target_month = '" + goalTargetMonth + '\'' +
                        ",name = '" + name + '\'' +
                        ",goal_creation_month = '" + goalCreationMonth + '\'' +
                        ",id = '" + id + '\'' +
                        ",category_group_id = '" + categoryGroupId + '\'' +
                        "}";
    }
}