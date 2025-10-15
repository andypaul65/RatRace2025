package com.finmodel.steps;

import com.finmodel.RatRaceSystemStateService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.ajp.mvp.server.MessageDto;
import static org.junit.jupiter.api.Assertions.*;

public class MvpIntegrationSteps {
    private RatRaceSystemStateService service;
    private MessageDto lastResponse;
    private String currentNamespace = "test-namespace";

    @Given("the RatRace system state service is initialized")
    public void theRatRaceSystemStateServiceIsInitialized() {
        service = new RatRaceSystemStateService();
    }

    @Given("a scenario has been loaded via MVP message")
    public void aScenarioHasBeenLoadedViaMVPMessage() {
        String scenarioJson = """
            {
                "entities": [
                    {
                        "id": "account1",
                        "name": "Test Account",
                        "primaryCategory": "Asset",
                        "balance": 1000.0,
                        "rate": 0.05
                    }
                ],
                "timeline": {
                    "startDate": "2025-01-01",
                    "endDate": "2025-12-31",
                    "periods": []
                }
            }
            """;

        MessageDto request = MessageDto.builder()
                .type("load_scenario")
                .content(scenarioJson)
                .namespace(currentNamespace)
                .build();

        lastResponse = service.processMessage(currentNamespace, request);
        assertEquals("load_response", lastResponse.getType());
    }

    @Given("a scenario has been loaded and simulated via MVP messages")
    public void aScenarioHasBeenLoadedAndSimulatedViaMVPMessages() {
        aScenarioHasBeenLoadedViaMVPMessage();

        MessageDto simRequest = MessageDto.builder()
                .type("run_simulation")
                .content("")
                .namespace(currentNamespace)
                .build();

        lastResponse = service.processMessage(currentNamespace, simRequest);
        assertEquals("simulation_response", lastResponse.getType());
    }

    @When("I send a {string} message with valid scenario JSON")
    public void iSendAMessageWithValidScenarioJSON(String messageType) {
        String scenarioJson = """
            {
                "entities": [
                    {
                        "id": "account1",
                        "name": "Test Account",
                        "primaryCategory": "Asset",
                        "balance": 1000.0,
                        "rate": 0.05
                    }
                ],
                "timeline": {
                    "startDate": "2025-01-01",
                    "endDate": "2025-12-31",
                    "periods": []
                }
            }
            """;

        MessageDto request = MessageDto.builder()
                .type(messageType)
                .content(scenarioJson)
                .namespace(currentNamespace)
                .build();

        lastResponse = service.processMessage(currentNamespace, request);
    }

    @When("I send a {string} message")
    public void iSendAMessage(String messageType) {
        MessageDto request = MessageDto.builder()
                .type(messageType)
                .content("")
                .namespace(currentNamespace)
                .build();

        lastResponse = service.processMessage(currentNamespace, request);
    }

    @When("I send a {string} message with invalid JSON")
    public void iSendAMessageWithInvalidJSON(String messageType) {
        MessageDto request = MessageDto.builder()
                .type(messageType)
                .content("invalid json content")
                .namespace(currentNamespace)
                .build();

        lastResponse = service.processMessage(currentNamespace, request);
    }

    @When("I send an unknown message type")
    public void iSendAnUnknownMessageType() {
        MessageDto request = MessageDto.builder()
                .type("unknown_message_type")
                .content("test content")
                .namespace(currentNamespace)
                .build();

        lastResponse = service.processMessage(currentNamespace, request);
    }

    @When("I send a {string} message without loading a scenario first")
    public void iSendAMessageWithoutLoadingAScenarioFirst(String messageType) {
        MessageDto request = MessageDto.builder()
                .type(messageType)
                .content("")
                .namespace(currentNamespace)
                .build();

        lastResponse = service.processMessage(currentNamespace, request);
    }

    @Then("I should receive a {string} message with success confirmation")
    public void iShouldReceiveAMessageWithSuccessConfirmation(String expectedType) {
        assertNotNull(lastResponse);
        assertEquals(expectedType, lastResponse.getType());
        assertTrue(lastResponse.getContent().contains("successfully") ||
                  lastResponse.getContent().contains("completed") ||
                  lastResponse.getContent().contains("generated"));
        assertEquals(currentNamespace, lastResponse.getNamespace());
    }

    @Then("I should receive a {string} message with completion confirmation")
    public void iShouldReceiveAMessageWithCompletionConfirmation(String expectedType) {
        assertNotNull(lastResponse);
        assertEquals(expectedType, lastResponse.getType());
        assertTrue(lastResponse.getContent().contains("completed"));
        assertEquals(currentNamespace, lastResponse.getNamespace());
    }

    @Then("I should receive a {string} message with dump information")
    public void iShouldReceiveAMessageWithDumpInformation(String expectedType) {
        assertNotNull(lastResponse);
        assertEquals(expectedType, lastResponse.getType());
        assertTrue(lastResponse.getContent().contains("dump") ||
                  lastResponse.getContent().contains("printed"));
        assertEquals(currentNamespace, lastResponse.getNamespace());
    }

    @Then("I should receive a {string} message with Sankey JSON data")
    public void iShouldReceiveAMessageWithSankeyJSONData(String expectedType) {
        assertNotNull(lastResponse);
        assertEquals(expectedType, lastResponse.getType());
        // Could validate JSON structure here
        assertFalse(lastResponse.getContent().isEmpty());
        assertEquals(currentNamespace, lastResponse.getNamespace());
    }

    @Then("I should receive an {string} message with failure details")
    public void iShouldReceiveAnErrorMessageWithFailureDetails(String expectedType) {
        assertNotNull(lastResponse);
        assertEquals(expectedType, lastResponse.getType());
        assertTrue(lastResponse.getContent().contains("Failed") ||
                  lastResponse.getContent().contains("failed") ||
                  lastResponse.getContent().contains("error"));
        assertEquals(currentNamespace, lastResponse.getNamespace());
    }

    @Then("I should receive an {string} message indicating no scenario loaded")
    public void iShouldReceiveAnErrorMessageIndicatingNoScenarioLoaded(String expectedType) {
        assertNotNull(lastResponse);
        assertEquals(expectedType, lastResponse.getType());
        assertTrue(lastResponse.getContent().contains("No scenario loaded"));
        assertEquals(currentNamespace, lastResponse.getNamespace());
    }

    @Then("I should receive an {string} message about unknown message type")
    public void iShouldReceiveAnErrorMessageAboutUnknownMessageType(String expectedType) {
        assertNotNull(lastResponse);
        assertEquals(expectedType, lastResponse.getType());
        assertTrue(lastResponse.getContent().contains("Unknown message type"));
        assertEquals(currentNamespace, lastResponse.getNamespace());
    }
}