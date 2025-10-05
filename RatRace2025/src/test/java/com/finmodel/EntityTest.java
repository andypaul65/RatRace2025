package com.finmodel;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testEntityCreation() {
        Entity entity = Entity.builder()
                .id("test-id")
                .name("Test Entity")
                .baseProperties(Map.of("key", "value"))
                .isTemplate(true)
                .build();

        assertEquals("test-id", entity.getId());
        assertEquals("Test Entity", entity.getName());
        assertEquals(Map.of("key", "value"), entity.getBaseProperties());
        assertTrue(entity.isTemplate());
    }

    @Test
    void testCloneAsNew() {
        Entity original = Entity.builder()
                .id("original-id")
                .name("Original")
                .baseProperties(Map.of("prop", "val"))
                .isTemplate(true)
                .build();

        Entity cloned = original.cloneAsNew();

        assertEquals("original-id", cloned.getId());
        assertEquals("Original", cloned.getName());
        assertEquals(Map.of("prop", "val"), cloned.getBaseProperties());
        assertFalse(cloned.isTemplate()); // Should be false for cloned
        assertNotSame(original, cloned); // Different instances
        assertNotSame(original.getBaseProperties(), cloned.getBaseProperties()); // Deep copy
    }

    @Test
    void testCreateInitialVersion() {
        Entity entity = Entity.builder()
                .id("entity-id")
                .name("Entity")
                .baseProperties(Map.of("balance", 1000.0))
                .isTemplate(false)
                .build();

        Date date = new Date();
        EntityVersion version = entity.createInitialVersion(date);

        assertEquals(entity, version.getParent());
        assertEquals(date, version.getDate());
        assertEquals(0, version.getSequence());
        assertEquals(0.0, version.getBalance());
        assertEquals(0.0, version.getRate());
        assertEquals(Map.of("balance", 1000.0), version.getAttributes());
        assertNull(version.getPrevious());
    }
}