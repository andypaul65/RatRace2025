package com.finmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Timeline {
    private List<TimePeriod> periods;
    private List<Event> pendingEvents;
    private Observer triggerObserver;
    private Object simulator; // Stub for Simulator

    public void addPeriod(TimePeriod period) {
        if (periods == null) {
            periods = new ArrayList<>();
        }
        periods.add(period);
    }

    public void triggerEvents(Date atDate) {
        // Stub: notify observer if present
        if (triggerObserver != null) {
            // Observable pattern, but since no Observable, just stub
        }
        // In full impl, check pendingEvents and trigger
    }

    public void advancePeriod() {
        // Stub: add a new period or something
        // For now, do nothing
    }
}