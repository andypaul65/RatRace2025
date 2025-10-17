package com.finmodel.mvp;

import org.springframework.stereotype.Service;
import com.example.services.AbstractSystemStateService;
import com.example.dto.MessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finmodel.FinanceModel;
import com.finmodel.SimulationException;
import com.finmodel.AuditLog;
import java.util.HashMap;
import java.util.Map;

/**
 * RatRace-specific implementation of MVP SystemStateService.
 * Extends AbstractSystemStateService to integrate existing domain logic
 * with MVP backplane framework.
 */
@Service
public class RatRaceSystemStateService extends AbstractSystemStateService {

    // Store simulation results by namespace (scenario id)
    private final Map<String, FinanceModel> simulationResults = new HashMap<>();

    @Override
    public MessageDto processMessage(String namespace, MessageDto message) {
        try {
            String content = message.getContent();

            // Parse message to extract type and data according to API contract
            String messageType = "unknown";
            String messageData = content;

            try {
                // Parse JSON content to extract message type
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> messageMap = mapper.readValue(content, Map.class);
                messageType = (String) messageMap.get("type");
                messageData = (String) messageMap.get("content");
                if (messageData == null) {
                    messageData = content; // fallback if no content field
                }
            } catch (Exception e) {
                // If JSON parsing fails, assume content is the message data
                // and try to infer type from content structure
                if (content.contains("{")) {
                    messageType = "load_scenario"; // JSON content likely means scenario data
                } else if (content.trim().isEmpty()) {
                    messageType = "run_simulation"; // Empty content for simulation
                } else {
                    messageType = "unknown";
                }
                messageData = content;
            }

            if ("load_scenario".equals(messageType)) {
                try {
                    FinanceModel model = new FinanceModel();
                    model.loadFromJson(messageData);
                    simulationResults.put(namespace, model);

                    // Return success response according to API contract
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("type", "load_response");
                    responseMap.put("content", "Scenario loaded successfully");
                    responseMap.put("namespace", namespace);

                    ObjectMapper mapper = new ObjectMapper();
                    return new MessageDto(mapper.writeValueAsString(responseMap), namespace);
                } catch (Exception e) {
                    // Return error response according to API contract
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("type", "error");
                    errorMap.put("content", "Failed to load scenario: " + e.getMessage());
                    errorMap.put("namespace", namespace);

                    ObjectMapper mapper = new ObjectMapper();
                    return new MessageDto(mapper.writeValueAsString(errorMap), namespace);
                }

            } else if ("run_simulation".equals(messageType)) {
                FinanceModel model = simulationResults.get(namespace);
                if (model == null) {
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("type", "error");
                    errorMap.put("content", "No scenario loaded for namespace: " + namespace);
                    errorMap.put("namespace", namespace);

                    ObjectMapper mapper = new ObjectMapper();
                    return new MessageDto(mapper.writeValueAsString(errorMap), namespace);
                }

                try {
                    model.runSimulation();

                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("type", "simulation_response");
                    responseMap.put("content", "Simulation completed");
                    responseMap.put("namespace", namespace);

                    ObjectMapper mapper = new ObjectMapper();
                    return new MessageDto(mapper.writeValueAsString(responseMap), namespace);
                } catch (SimulationException e) {
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("type", "error");
                    errorMap.put("content", "Simulation failed: " + e.getMessage());
                    errorMap.put("namespace", namespace);

                    ObjectMapper mapper = new ObjectMapper();
                    return new MessageDto(mapper.writeValueAsString(errorMap), namespace);
                }

            } else if ("get_dump".equals(messageType)) {
                FinanceModel model = simulationResults.get(namespace);
                if (model == null) {
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("type", "error");
                    errorMap.put("content", "No simulation results for namespace: " + namespace);
                    errorMap.put("namespace", namespace);

                    ObjectMapper mapper = new ObjectMapper();
                    return new MessageDto(mapper.writeValueAsString(errorMap), namespace);
                }

                try {
                    // TODO: Implement proper output capture instead of console printing
                    model.dumpToConsole();

                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("type", "dump_response");
                    responseMap.put("content", "Simulation dump printed to console - see server logs for details");
                    responseMap.put("namespace", namespace);

                    ObjectMapper mapper = new ObjectMapper();
                    return new MessageDto(mapper.writeValueAsString(responseMap), namespace);
                } catch (Exception e) {
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("type", "error");
                    errorMap.put("content", "Failed to generate dump: " + e.getMessage());
                    errorMap.put("namespace", namespace);

                    ObjectMapper mapper = new ObjectMapper();
                    return new MessageDto(mapper.writeValueAsString(errorMap), namespace);
                }

            } else if ("get_sankey".equals(messageType)) {
                FinanceModel model = simulationResults.get(namespace);
                if (model == null) {
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("type", "error");
                    errorMap.put("content", "No simulation results for namespace: " + namespace);
                    errorMap.put("namespace", namespace);

                    ObjectMapper mapper = new ObjectMapper();
                    return new MessageDto(mapper.writeValueAsString(errorMap), namespace);
                }

                try {
                    Map<String, Object> sankeyData = model.buildSankeyData();
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonData = mapper.writeValueAsString(sankeyData);

                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("type", "sankey_response");
                    responseMap.put("content", jsonData);
                    responseMap.put("namespace", namespace);

                    return new MessageDto(mapper.writeValueAsString(responseMap), namespace);
                } catch (Exception e) {
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("type", "error");
                    errorMap.put("content", "Failed to generate Sankey data: " + e.getMessage());
                    errorMap.put("namespace", namespace);

                    ObjectMapper mapper = new ObjectMapper();
                    return new MessageDto(mapper.writeValueAsString(errorMap), namespace);
                }
            }

            // Unknown message type
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("type", "error");
            errorMap.put("content", "Unknown message type: " + messageType);
            errorMap.put("namespace", namespace);

            ObjectMapper mapper = new ObjectMapper();
            return new MessageDto(mapper.writeValueAsString(errorMap), namespace);

        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("type", "error");
            errorMap.put("content", "Error processing message: " + e.getMessage());
            errorMap.put("namespace", namespace);

            try {
                ObjectMapper mapper = new ObjectMapper();
                return new MessageDto(mapper.writeValueAsString(errorMap), namespace);
            } catch (Exception mapperError) {
                return new MessageDto("Critical error in message processing", namespace);
            }
        }
    }

    @Override
    public MessageDto getDefaultState(String namespace) {
        try {
            Map<String, Object> defaultState = new HashMap<>();
            defaultState.put("type", "default_state");
            defaultState.put("content", "No scenario loaded");
            defaultState.put("namespace", namespace);

            ObjectMapper mapper = new ObjectMapper();
            return new MessageDto(mapper.writeValueAsString(defaultState), namespace);
        } catch (Exception e) {
            return new MessageDto("No scenario loaded", namespace);
        }
    }

    @Override
    protected void storeMessage(String namespace, MessageDto message) {
        // TODO: Integrate with existing persistence layer or AuditLog for message storage
        // Currently using AuditLog as placeholder - may need database integration
        AuditLog.getInstance().log("MVP Message: " + namespace + " - " + message.getContent());
    }
}