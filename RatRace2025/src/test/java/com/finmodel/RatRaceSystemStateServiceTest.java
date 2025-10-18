package com.finmodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.dto.MessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import com.finmodel.mvp.RatRaceSystemStateService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatRaceSystemStateServiceTest {

    private RatRaceSystemStateService service;

    @Mock
    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        // Use a mock AuditLog for testing
        service = new RatRaceSystemStateService();
        // Note: In real MVP framework, this would be injected or managed by Spring
    }

    @Test
    void testLoadScenarioSuccess() {
        String namespace = "test-namespace";
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

        String requestContent = "{\"type\":\"load_scenario\",\"content\":" + scenarioJson + ",\"namespace\":\"" + namespace + "\"}";
        MessageDto request = new MessageDto(requestContent, namespace);

        MessageDto response = service.processMessage(namespace, request);

        // Parse response content to check type
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(response.getContent(), Map.class);
        assertEquals("load_response", responseMap.get("type"));
        assertEquals("Scenario loaded successfully", responseMap.get("content"));
        assertEquals(namespace, response.getNamespace());
    }

    @Test
    void testLoadScenarioInvalidJson() {
        String namespace = "test-namespace";
        String invalidJson = "invalid json content";

        String requestContent = "{\"type\":\"load_scenario\",\"content\":\"" + invalidJson + "\",\"namespace\":\"" + namespace + "\"}";
        MessageDto request = new MessageDto(requestContent, namespace);

        MessageDto response = service.processMessage(namespace, request);

        // Parse response content to check type
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(response.getContent(), Map.class);
        assertEquals("error", responseMap.get("type"));
        assertTrue(((String) responseMap.get("content")).contains("Failed to load scenario"));
        assertEquals(namespace, response.getNamespace());
    }

    @Test
    void testRunSimulationWithoutScenario() {
        String namespace = "test-namespace";

        MessageDto request = MessageDto.builder()
                .type("run_simulation")
                .content("")
                .namespace(namespace)
                .build();

        MessageDto response = service.processMessage(namespace, request);

        assertEquals("error", response.getType());
        assertTrue(response.getContent().contains("No scenario loaded"));
        assertEquals(namespace, response.getNamespace());
    }

    @Test
    void testRunSimulationWithScenario() {
        // First load a scenario
        String namespace = "test-namespace";
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

        MessageDto loadRequest = MessageDto.builder()
                .type("load_scenario")
                .content(scenarioJson)
                .namespace(namespace)
                .build();

        service.processMessage(namespace, loadRequest);

        // Now run simulation
        MessageDto simRequest = MessageDto.builder()
                .type("run_simulation")
                .content("")
                .namespace(namespace)
                .build();

        MessageDto response = service.processMessage(namespace, simRequest);

        assertEquals("simulation_response", response.getType());
        assertEquals("Simulation completed", response.getContent());
        assertEquals(namespace, response.getNamespace());
    }

    @Test
    void testGetDumpWithoutScenario() {
        String namespace = "test-namespace";

        MessageDto request = MessageDto.builder()
                .type("get_dump")
                .content("")
                .namespace(namespace)
                .build();

        MessageDto response = service.processMessage(namespace, request);

        assertEquals("error", response.getType());
        assertTrue(response.getContent().contains("No simulation results"));
        assertEquals(namespace, response.getNamespace());
    }

    @Test
    void testGetSankeyWithoutScenario() {
        String namespace = "test-namespace";

        MessageDto request = MessageDto.builder()
                .type("get_sankey")
                .content("")
                .namespace(namespace)
                .build();

        MessageDto response = service.processMessage(namespace, request);

        assertEquals("error", response.getType());
        assertTrue(response.getContent().contains("No simulation results"));
        assertEquals(namespace, response.getNamespace());
    }

    @Test
    void testUnknownMessageType() {
        String namespace = "test-namespace";

        MessageDto request = MessageDto.builder()
                .type("unknown_type")
                .content("test")
                .namespace(namespace)
                .build();

        MessageDto response = service.processMessage(namespace, request);

        assertEquals("error", response.getType());
        assertTrue(response.getContent().contains("Unknown message type"));
        assertEquals(namespace, response.getNamespace());
    }

    @Test
    void testGetDefaultState() {
        String namespace = "test-namespace";

        MessageDto response = service.getDefaultState(namespace);

        assertEquals("default_state", response.getType());
        assertEquals("No scenario loaded", response.getContent());
        assertEquals(namespace, response.getNamespace());
    }
}