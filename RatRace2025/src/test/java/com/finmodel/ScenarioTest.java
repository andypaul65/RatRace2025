package com.finmodel;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ScenarioTest {

    @Test
    void testScenarioCreation() {
        Scenario scenario = Scenario.builder()
                .initialEntities(List.of())
                .eventTemplates(Map.of())
                .entityTemplates(Map.of())
                .latentEvents(List.of())
                .numPeriods(5)
                .externals(List.of())
                .build();

        assertNotNull(scenario.getInitialEntities());
        assertNotNull(scenario.getEventTemplates());
        assertNotNull(scenario.getEntityTemplates());
        assertNotNull(scenario.getLatentEvents());
        assertEquals(5, scenario.getNumPeriods());
        assertNotNull(scenario.getExternals());
    }

    @Test
    void testInitialize() {
        Scenario scenario = Scenario.builder()
                .numPeriods(3)
                .build();

        Timeline timeline = Timeline.builder().build();

        scenario.initialize(timeline);

        assertEquals(3, timeline.getPeriods().size());
        for (TimePeriod period : timeline.getPeriods()) {
            assertNotNull(period.getStart());
            assertNotNull(period.getEnd());
            assertEquals(3.5, period.getRiskFreeRate());
            assertEquals(2.0, period.getInflation());
        }
    }

    @Test
    void testRegisterLatentEvent() {
        Scenario scenario = Scenario.builder().build();

        Event event = RecurringEvent.builder().id("latent").build();

        scenario.registerLatentEvent(event);

        assertEquals(1, scenario.getLatentEvents().size());
        assertEquals(event, scenario.getLatentEvents().get(0));
    }

    @Test
    void testGetTemplate() {
        Entity template = Entity.builder().id("template").build();

        Scenario scenario = Scenario.builder()
                .entityTemplates(Map.of("key", template))
                .build();

        Entity result = scenario.getTemplate("key");

        assertEquals(template, result);
    }

    @Test
    void testGetTemplateNotFound() {
        Scenario scenario = Scenario.builder()
                .entityTemplates(Map.of())
                .build();

        Entity result = scenario.getTemplate("missing");

        assertNull(result);
    }
}