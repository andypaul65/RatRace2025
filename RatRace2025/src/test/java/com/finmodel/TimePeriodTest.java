package com.finmodel;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TimePeriodTest {

    @Test
    void testTimePeriodCreation() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 86400000); // +1 day

        TimePeriod period = TimePeriod.builder()
                .start(start)
                .end(end)
                .riskFreeRate(3.5)
                .inflation(2.0)
                .versionChains(Map.of())
                .events(List.of())
                .build();

        assertEquals(start, period.getStart());
        assertEquals(end, period.getEnd());
        assertEquals(3.5, period.getRiskFreeRate());
        assertEquals(2.0, period.getInflation());
        assertNotNull(period.getVersionChains());
        assertNotNull(period.getEvents());
    }

    @Test
    void testAddEvent() {
        TimePeriod period = TimePeriod.builder().build();

        Event event = RecurringEvent.builder().id("test-event").build();

        period.addEvent(event);

        assertEquals(1, period.getEvents().size());
        assertEquals(event, period.getEvents().get(0));
    }

    @Test
    void testGetFinalVersion() {
        Entity entity = Entity.builder().id("entity1").build();
        EntityVersion v1 = EntityVersion.builder().parent(entity).sequence(0).balance(100.0).build();
        EntityVersion v2 = EntityVersion.builder().parent(entity).sequence(1).balance(150.0).build();

        TimePeriod period = TimePeriod.builder()
                .versionChains(Map.of(entity, List.of(v1, v2)))
                .build();

        EntityVersion finalVersion = period.getFinalVersion(entity);

        assertEquals(v2, finalVersion);
        assertEquals(150.0, finalVersion.getBalance());
    }

    @Test
    void testGetFinalVersionNoVersions() {
        Entity entity = Entity.builder().id("entity1").build();

        TimePeriod period = TimePeriod.builder()
                .versionChains(Map.of())
                .build();

        EntityVersion finalVersion = period.getFinalVersion(entity);

        assertNull(finalVersion);
    }

    @Test
    void testGetAggregatedFlows() {
        Entity entity = Entity.builder().id("entity1").build();

        TimePeriod period = TimePeriod.builder().build();

        List<Flow> flows = period.getAggregatedFlows(entity);

        // Stub returns empty list
        assertTrue(flows.isEmpty());
    }

    @Test
    void testGetPeriodEntityAggregate() {
        Entity entity = Entity.builder().id("entity1").name("Test Entity").build();
        EntityVersion finalVersion = EntityVersion.builder()
                .parent(entity)
                .sequence(1)
                .balance(200.0)
                .rate(4.0)
                .build();

        TimePeriod period = TimePeriod.builder()
                .versionChains(Map.of(entity, List.of(finalVersion)))
                .build();

        PeriodEntityAggregate aggregate = period.getPeriodEntityAggregate(entity);

        assertNotNull(aggregate);
        assertEquals(finalVersion, aggregate.finalVersion());
        assertEquals(200.0, aggregate.getNetBalance());
        Map<String, Object> sankey = aggregate.toSankeyNode();
        assertEquals("entity1", sankey.get("id"));
        assertEquals("Test Entity", sankey.get("name"));
        assertEquals(200.0, sankey.get("balance"));
        assertEquals(4.0, sankey.get("rate"));
    }

    @Test
    void testChainEvents() {
        Entity entity = Entity.builder().id("account").name("Savings Account").build();
        EntityVersion initial = entity.createInitialVersion(new Date());

        // Simulate applying event
        RecurringEvent event = RecurringEvent.builder()
                .id("deposit")
                .params(Map.of("amount", 50.0))
                .build();
        EntityVersion afterEvent = event.apply(initial);

        List<EntityVersion> versions = List.of(initial, afterEvent);

        TimePeriod period = TimePeriod.builder()
                .start(new Date())
                .end(new Date())
                .versionChains(Map.of(entity, versions))
                .build();

        // Add event to period
        period.addEvent(event);

        // Assert final version
        EntityVersion finalVersion = period.getFinalVersion(entity);
        assertEquals(afterEvent, finalVersion);
        assertEquals(50.0, finalVersion.getBalance()); // Initial 0 + 50

        // Assert aggregate
        PeriodEntityAggregate aggregate = period.getPeriodEntityAggregate(entity);
        assertEquals(50.0, aggregate.getNetBalance());
    }
}