package com.finmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;
import java.util.function.Predicate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private String id;
    private String type;
    private Map<String, Object> params;
    private boolean isRecurring;
    private Date triggerDate;
    private Predicate<EntityVersion> condition;
}