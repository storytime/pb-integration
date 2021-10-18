package com.github.storytime.model.ynab.category;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;
@Deprecated
@Generated("com.robohorse.robopojogenerator")
public class YnabCategoryGroups {

    @JsonProperty("deleted")
    private boolean deleted;

    @JsonProperty("hidden")
    private boolean hidden;

    @JsonProperty("name")
    private String name;

    @JsonProperty("id")
    private String id;

    @JsonProperty("categories")
    private List<YnabCategories> categories;

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<YnabCategories> getCategories() {
        return categories;
    }

    public void setCategories(List<YnabCategories> categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        return
                "YnabCategoryGroups{" +
                        "deleted = '" + deleted + '\'' +
                        ",hidden = '" + hidden + '\'' +
                        ",name = '" + name + '\'' +
                        ",id = '" + id + '\'' +
                        ",categories = '" + categories + '\'' +
                        "}";
    }
}