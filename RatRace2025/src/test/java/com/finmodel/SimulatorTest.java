package com.finmodel;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulatorTest {

    @Test
    void testSimulatorCreation() {
        Simulator simulator = Simulator.builder()
                .scenario(new Scenario())
                .processor(new DefaultEventProcessor())
                .timeline(new Timeline())
                .dynamicEntities(new java.util.HashSet<>())
                .build();

        assertNotNull(simulator.getScenario());
        assertNotNull(simulator.getProcessor());
        assertNotNull(simulator.getTimeline());
        assertNotNull(simulator.getDynamicEntities());
    }

    @Test
    void testPlayOutMultiPeriodSimulation() {
        // Setup scenario
        Scenario scenario = Scenario.builder()
                .numPeriods(2)
                .initialEntities(new ArrayList<>(List.of(Entity.builder().id("initial").build())))
                .externals(List.of())
                .build();

        // Setup timeline
        Timeline timeline = Timeline.builder().build();
        scenario.initialize(timeline); // Adds 2 periods

        // Add creation event to first period
        TimePeriod firstPeriod = timeline.getPeriods().get(0);
        Event creationEvent = CreationEvent.builder()
                .id("create-dynamic")
                .type("creation")
                .build();
        firstPeriod.addEvent(creationEvent);

        // Setup simulator
        Simulator simulator = Simulator.builder()
                .scenario(scenario)
                .processor(new DefaultEventProcessor())
                .timeline(timeline)
                .build();

        // Before playOut
        assertNull(simulator.getDynamicEntities());
        assertEquals(1, scenario.getInitialEntities().size());

        // Run simulation
        simulator.playOut();

        // Assert dynamic entities appeared
        assertFalse(simulator.getDynamicEntities().isEmpty());
        assertEquals(1, simulator.getDynamicEntities().size());

        // Also added to scenario initials
        assertEquals(2, scenario.getInitialEntities().size());
        Entity dynamic = scenario.getInitialEntities().get(1);
        assertTrue(dynamic.getId().startsWith("dynamic-"));
        assertEquals("Dynamic Entity", dynamic.getName());
    }
}