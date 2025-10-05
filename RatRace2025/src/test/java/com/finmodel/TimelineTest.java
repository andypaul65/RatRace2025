package com.finmodel;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimelineTest {

    @Test
    void testTimelineCreation() {
        Timeline timeline = Timeline.builder()
                .periods(List.of())
                .pendingEvents(List.of())
                .build();

        assertNotNull(timeline.getPeriods());
        assertNotNull(timeline.getPendingEvents());
    }

    @Test
    void testAddPeriod() {
        Timeline timeline = Timeline.builder().build();

        TimePeriod period = TimePeriod.builder()
                .start(new Date())
                .end(new Date())
                .build();

        timeline.addPeriod(period);

        assertEquals(1, timeline.getPeriods().size());
        assertEquals(period, timeline.getPeriods().get(0));
    }

    @Test
    void testTriggerEvents() {
        Timeline timeline = Timeline.builder().build();

        // Stub test
        assertDoesNotThrow(() -> timeline.triggerEvents(new Date()));
    }

    @Test
    void testAdvancePeriod() {
        Timeline timeline = Timeline.builder().build();

        // Stub test
        assertDoesNotThrow(timeline::advancePeriod);
    }
}