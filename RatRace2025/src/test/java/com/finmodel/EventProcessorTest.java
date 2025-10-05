package com.finmodel;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventProcessorTest {

    private final EventProcessor processor = new DefaultEventProcessor();

    @Test
    void testProcessEvent() {
        EntityVersion version = EntityVersion.builder()
                .parent(Entity.builder().id("test").build())
                .date(new Date())
                .sequence(0)
                .balance(200.0)
                .rate(0.0)
                .attributes(Map.of())
                .previous(null)
                .build();

        Event event = RecurringEvent.builder()
                .id("event-id")
                .type("deposit")
                .params(Map.of("amount", 100.0))
                .isRecurring(true)
                .build();

        EntityVersion result = processor.process(event, version);

        assertEquals(300.0, result.getBalance());
        assertEquals(1, result.getSequence());
    }

    @Test
    void testHandleFlows() {
        Event event = RecurringEvent.builder()
                .id("event-id")
                .type("deposit")
                .params(Map.of("amount", 100.0))
                .build();

        List<Flow> flows = processor.handleFlows(event);

        // Stub returns empty list
        assertTrue(flows.isEmpty());
    }

    @Test
    void testHandleCreation() {
        Event event = RecurringEvent.builder()
                .id("event-id")
                .type("deposit")
                .params(Map.of("amount", 100.0))
                .build();

        List<Entity> entities = processor.handleCreation(event);

        // Stub returns empty list
        assertTrue(entities.isEmpty());
    }
}