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
        EntityVersion version = new EntityVersion(
                Entity.builder().id("test").build(),
                new Date(),
                0,
                0.0,
                0.0,
                Map.of(),
                null
        );

        Event event = Event.builder().id("event-id").type("test").build();

        EntityVersion result = version.applyEvent(event);

        // Stub implementation returns this
        assertSame(version, result);
    }
}