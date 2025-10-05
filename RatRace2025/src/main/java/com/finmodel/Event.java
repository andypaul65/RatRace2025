package com.finmodel;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Data
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RecurringEvent.class, name = "recurring"),
        @JsonSubTypes.Type(value = ConditionalEvent.class, name = "conditional"),
        @JsonSubTypes.Type(value = CalculationEvent.class, name = "calculation"),
        @JsonSubTypes.Type(value = CreationEvent.class, name = "creation")
})
public abstract class Event {
    private String id;
    private String type;
    private Map<String, Object> params;
    private boolean isRecurring;
    private Date triggerDate;
    private String conditionScript;

    public abstract EntityVersion apply(EntityVersion from);

    public abstract List<Flow> generateFlows();

    public abstract List<Entity> createEntities();
}