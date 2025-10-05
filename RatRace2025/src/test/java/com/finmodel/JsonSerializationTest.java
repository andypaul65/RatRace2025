package com.finmodel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonSerializationTest {

    @TempDir
    Path tempDir;

    @Test
    void testSaveLoadScenario() {
        // Create scenario with events
        Entity entity = Entity.builder()
                .id("account")
                .name("Savings Account")
                .baseProperties(Map.of("initialBalance", 1000.0))
                .isTemplate(false)
                .build();

        RecurringEvent event = RecurringEvent.builder()
                .id("deposit")
                .type("income")
                .params(Map.of("amount", 100.0))
                .isRecurring(true)
                .conditionScript(null)
                .build();

        ConditionalEvent condEvent = ConditionalEvent.builder()
                .id("bonus")
                .type("bonus")
                .params(Map.of("amount", 50.0))
                .conditionScript("balance > 50")
                .build();

        Scenario originalScenario = Scenario.builder()
                .initialEntities(List.of(entity))
                .eventTemplates(Map.of(entity, List.of(event)))
                .latentEvents(List.of(condEvent))
                .numPeriods(5)
                .externals(List.of())
                .build();

        FinanceModel model = FinanceModel.builder()
                .scenario(originalScenario)
                .build();

        // Save to JSON
        File jsonFile = tempDir.resolve("scenario.json").toFile();
        model.saveToJson(jsonFile.getAbsolutePath());

        // Load into new model
        FinanceModel loadedModel = FinanceModel.builder().build();
        loadedModel.loadFromJson(jsonFile.getAbsolutePath());

        // Assert equality
        Scenario loadedScenario = loadedModel.getScenario();
        assertNotNull(loadedScenario);
        assertEquals(originalScenario.getNumPeriods(), loadedScenario.getNumPeriods());
        assertEquals(originalScenario.getInitialEntities().size(), loadedScenario.getInitialEntities().size());
        assertEquals(originalScenario.getLatentEvents().size(), loadedScenario.getLatentEvents().size());

        // Check entity
        Entity loadedEntity = loadedScenario.getInitialEntities().get(0);
        assertEquals(entity.getId(), loadedEntity.getId());
        assertEquals(entity.getName(), loadedEntity.getName());

        // Check events
        Event loadedEvent = loadedScenario.getLatentEvents().get(0);
        assertEquals(condEvent.getId(), loadedEvent.getId());
        assertEquals(condEvent.getType(), loadedEvent.getType());
        assertEquals(condEvent.getConditionScript(), loadedEvent.getConditionScript());
        assertTrue(loadedEvent instanceof ConditionalEvent);
    }
}