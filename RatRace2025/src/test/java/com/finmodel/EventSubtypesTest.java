package com.finmodel;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventSubtypesTest {

    @Test
    void testRecurringEventApply() {
        EntityVersion version = EntityVersion.builder()
                .parent(Entity.builder().id("test").build())
                .date(new Date())
                .sequence(0)
                .balance(100.0)
                .rate(0.0)
                .attributes(Map.of())
                .previous(null)
                .build();

        RecurringEvent event = RecurringEvent.builder()
                .id("recurring-id")
                .type("deposit")
                .params(Map.of("amount", 50.0))
                .isRecurring(true)
                .build();

        EntityVersion result = event.apply(version);

        assertEquals(150.0, result.getBalance());
        assertEquals(1, result.getSequence());
    }

    @Test
    void testConditionalEventApplyWhenConditionMet() {
        EntityVersion version = EntityVersion.builder()
                .parent(Entity.builder().id("test").build())
                .date(new Date())
                .sequence(0)
                .balance(100.0)
                .rate(0.0)
                .attributes(Map.of())
                .previous(null)
                .build();

        ConditionalEvent event = ConditionalEvent.builder()
                .id("conditional-id")
                .type("bonus")
                .params(Map.of("amount", 20.0))
                .condition(v -> v.getBalance() > 50.0)
                .build();

        EntityVersion result = event.apply(version);

        assertEquals(120.0, result.getBalance());
    }

    @Test
    void testConditionalEventApplyWhenConditionNotMet() {
        EntityVersion version = EntityVersion.builder()
                .parent(Entity.builder().id("test").build())
                .date(new Date())
                .sequence(0)
                .balance(30.0)
                .rate(0.0)
                .attributes(Map.of())
                .previous(null)
                .build();

        ConditionalEvent event = ConditionalEvent.builder()
                .id("conditional-id")
                .type("bonus")
                .params(Map.of("amount", 20.0))
                .condition(v -> v.getBalance() > 50.0)
                .build();

        EntityVersion result = event.apply(version);

        // No change
        assertEquals(30.0, result.getBalance());
        assertSame(version, result);
    }

    @Test
    void testCalculationEventApply() {
        EntityVersion version = EntityVersion.builder()
                .parent(Entity.builder().id("test").build())
                .date(new Date())
                .sequence(0)
                .balance(1500.0)
                .rate(0.0)
                .attributes(Map.of())
                .previous(null)
                .build();

        CalculationEvent event = CalculationEvent.builder()
                .id("calc-id")
                .type("rate-calc")
                .build();

        EntityVersion result = event.apply(version);

        assertEquals(5.0, result.getRate()); // Balance > 1000, so 5.0
    }
}