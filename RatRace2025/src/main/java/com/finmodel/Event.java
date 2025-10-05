package com.finmodel;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Data
@SuperBuilder
public abstract class Event {
    private String id;
    private String type;
    private Map<String, Object> params;
    private boolean isRecurring;
    private Date triggerDate;
    private Predicate<EntityVersion> condition;

    public abstract EntityVersion apply(EntityVersion from);

    public abstract List<Flow> generateFlows();

    public abstract List<Entity> createEntities();
}