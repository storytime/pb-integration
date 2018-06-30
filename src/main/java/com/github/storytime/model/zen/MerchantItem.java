package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class MerchantItem {

    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("user")
    private Object user;

    @JsonProperty("changed")
    private int changed;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
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
                "MerchantItem{" +
                        "id = '" + id + '\'' +
                        ",title = '" + title + '\'' +
                        ",user = '" + user + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}