package com.finmodel;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EntityVersionTest {

    @Test
    void testEntityVersionCreation() {
        Entity parent = Entity.builder().id("parent").build();
        Date date = new Date();
        EntityVersion previous = null; // For initial

        EntityVersion version = new EntityVersion(parent, date, 1, 100.0, 5.0, Map.of("attr", "val"), previous);

        assertEquals(parent, version.getParent());
        assertEquals(date, version.getDate());
        assertEquals(1, version.getSequence());
        assertEquals(100.0, version.getBalance());
        assertEquals(5.0, version.getRate());
        assertEquals(Map.of("attr", "val"), version.getAttributes());
        assertNull(version.getPrevious());
    }

    @Test
    void testApplyEvent() {
        EntityVersion version = EntityVersion.builder()
                .parent(Entity.builder().id("test").build())
                .date(new Date())
                .sequence(0)
                .balance(100.0)
                .rate(0.0)
                .attributes(Map.of())
                .previous(null)
                .build();

        Event event = RecurringEvent.builder()
                .id("event-id")
                .type("deposit")
                .params(Map.of("amount", 50.0))
                .isRecurring(true)
                .build();

        EntityVersion result = version.applyEvent(event);

        assertEquals(150.0, result.getBalance());
        assertEquals(1, result.getSequence());
        assertEquals(version, result.getPrevious());
    }
}