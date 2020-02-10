package com.github.storytime.model.ynab;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class YnabZenHolder {

    private final List<YnabZenSyncObject> storage;

    public YnabZenHolder() {
        this.storage = new ArrayList<>();
    }

    public YnabZenSyncObject add(YnabZenSyncObject o) {
        storage.add(o);
        return o;
    }

    public Optional<YnabZenSyncObject> findByName(final String name) {
        return storage.stream().filter(ynabZenSyncObject -> ynabZenSyncObject.getName().equalsIgnoreCase(name)).findFirst();
    }

    public boolean isExistsByName(final String name) {
        return storage.stream().filter(ynabZenSyncObject -> ynabZenSyncObject.getName().equalsIgnoreCase(name)).count() > 0;
    }

    public Optional<YnabZenSyncObject> findByZenId(final String zId) {
        return storage.stream().filter(ynabZenSyncObject -> ynabZenSyncObject.getZenId().equalsIgnoreCase(zId)).findFirst();
    }

    public boolean isExistsByZenId(final String zenId) {
        return storage.stream().filter(ynabZenSyncObject -> ynabZenSyncObject.getZenId().equalsIgnoreCase(zenId)).count() > 0;
    }

    public Optional<YnabZenSyncObject> findByYnabId(final String yId) {
        return storage.stream().filter(ynabZenSyncObject -> ynabZenSyncObject.getYnabId().equalsIgnoreCase(yId)).findFirst();
    }

    public int size() {
        return storage.size();
    }

    public boolean isEmpty() {
        return storage.isEmpty();
    }
}
