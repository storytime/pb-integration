package com.github.storytime.model.ynab;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class YnabZenHolder {

    private final List<YnabZenComplianceObject> storage;

    public YnabZenHolder() {
        this.storage = new ArrayList<>();
    }

    public YnabZenComplianceObject add(YnabZenComplianceObject o) {
        storage.add(o);
        return o;
    }

    public Optional<YnabZenComplianceObject> findByName(final String name) {
        return storage.stream().filter(ynabZenComplianceObject -> ynabZenComplianceObject.getName().equalsIgnoreCase(name)).findFirst();
    }

    public boolean isExistsByName(final String name) {
        return storage.stream().filter(ynabZenComplianceObject -> ynabZenComplianceObject.getName().equalsIgnoreCase(name)).count() > 0;
    }

    public Optional<YnabZenComplianceObject> findByZenId(final String zId) {
        return storage.stream().filter(ynabZenComplianceObject -> ynabZenComplianceObject.getZenId().equalsIgnoreCase(zId)).findFirst();
    }

    public boolean isExistsByZenId(final String zenId) {
        return storage.stream().filter(ynabZenComplianceObject -> ynabZenComplianceObject.getZenId().equalsIgnoreCase(zenId)).count() > 0;
    }

    public Optional<YnabZenComplianceObject> findByYnabId(final String yId) {
        return storage.stream().filter(ynabZenComplianceObject -> ynabZenComplianceObject.getYnabId().equalsIgnoreCase(yId)).findFirst();
    }

    public int size() {
        return storage.size();
    }

    public boolean isEmpty() {
        return storage.isEmpty();
    }
}
