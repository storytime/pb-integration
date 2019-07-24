package com.github.storytime.model.db.inner;

/**
 * Values is saved in db by name, so names changed not allowed
 */
public enum YnabTagsSyncProperties {
    MATCH_INNER_TAGS, //mach high and inner level tags
    MATCH_PARENT_TAGS, //mach high level tags
    NO_MATCH
}
