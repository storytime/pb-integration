package com.github.storytime.model.ynab.category;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;
@Deprecated
@Generated("com.robohorse.robopojogenerator")
public class YnabCategoryData {

    @JsonProperty("server_knowledge")
    private int serverKnowledge;

    @JsonProperty("category_groups")
    private List<YnabCategoryGroups> categoryGroups;

    public int getServerKnowledge() {
        return serverKnowledge;
    }

    public void setServerKnowledge(int serverKnowledge) {
        this.serverKnowledge = serverKnowledge;
    }

    public List<YnabCategoryGroups> getCategoryGroups() {
        return categoryGroups;
    }

    public void setCategoryGroups(List<YnabCategoryGroups> categoryGroups) {
        this.categoryGroups = categoryGroups;
    }

    @Override
    public String toString() {
        return
                "YnabAccountData{" +
                        "server_knowledge = '" + serverKnowledge + '\'' +
                        ",category_groups = '" + categoryGroups + '\'' +
                        "}";
    }
}