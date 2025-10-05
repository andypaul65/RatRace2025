package com.finmodel;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuditLog {
    @Getter
    private static final AuditLog instance = new AuditLog();
    private List<String> logs = new ArrayList<>();

    private AuditLog() {}

    public void log(String message) {
        logs.add(new Date() + ": " + message);
    }

    public void logEvent(String eventType, Entity entity, double amount) {
        log("Event: " + eventType + " on " + entity.getId() + " amount: " + amount);
    }

    public void clear() {
        logs.clear();
    }

    public List<String> getAllLogs() {
        return new ArrayList<>(logs);
    }
}