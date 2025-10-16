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

            // Parse message type from content (assuming JSON format with "type" field)
            String type = "unknown";
            try {
                // Simple JSON parsing to extract type
                if (content.contains("\"type\"")) {
                    int typeStart = content.indexOf("\"type\"") + 8;
                    int typeEnd = content.indexOf("\"", typeStart);
                    if (typeEnd > typeStart) {
                        type = content.substring(typeStart, typeEnd);
                    }
                }
            } catch (Exception e) {
                // If parsing fails, treat as unknown type
                type = "unknown";
            }

            if ("load_scenario".equals(type)) {
                try {
                    // Extract actual scenario data from content
                    String scenarioData = content;
                    if (content.contains("\"data\"")) {
                        int dataStart = content.indexOf("\"data\"") + 8;
                        int dataEnd = content.lastIndexOf("}");
                        if (dataEnd > dataStart) {
                            scenarioData = content.substring(dataStart, dataEnd);
                        }
                    }

                    FinanceModel model = new FinanceModel();
                    model.loadFromJson(scenarioData);
                    simulationResults.put(namespace, model);

                    return new MessageDto("Scenario loaded successfully", namespace);
                } catch (Exception e) {
                    return new MessageDto("Failed to load scenario: " + e.getMessage(), namespace);
                }

            } else if ("run_simulation".equals(type)) {
                FinanceModel model = simulationResults.get(namespace);
                if (model == null) {
                    return new MessageDto("No scenario loaded for namespace: " + namespace, namespace);
                }

                try {
                    model.runSimulation();
                    return new MessageDto("Simulation completed", namespace);
                } catch (SimulationException e) {
                    return new MessageDto("Simulation failed: " + e.getMessage(), namespace);
                }

            } else if ("get_dump".equals(type)) {
                FinanceModel model = simulationResults.get(namespace);
                if (model == null) {
                    return new MessageDto("No simulation results for namespace: " + namespace, namespace);
                }

                try {
                    // TODO: Implement proper output capture instead of console printing
                    model.dumpToConsole();
                    return new MessageDto("Simulation dump printed to console - see server logs for details", namespace);
                } catch (Exception e) {
                    return new MessageDto("Failed to generate dump: " + e.getMessage(), namespace);
                }

            } else if ("get_sankey".equals(type)) {
                FinanceModel model = simulationResults.get(namespace);
                if (model == null) {
                    return new MessageDto("No simulation results for namespace: " + namespace, namespace);
                }

                try {
                    Map<String, Object> sankeyData = model.buildSankeyData();
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonData = mapper.writeValueAsString(sankeyData);

                    return new MessageDto(jsonData, namespace);
                } catch (Exception e) {
                    return new MessageDto("Failed to generate Sankey data: " + e.getMessage(), namespace);
                }
            }

            return new MessageDto("Unknown message type: " + type, namespace);

        } catch (Exception e) {
            return new MessageDto("Error processing message: " + e.getMessage(), namespace);
        }
    }

    @Override
    public MessageDto getDefaultState(String namespace) {
        return new MessageDto("No scenario loaded", namespace);
    }

    @Override
    protected void storeMessage(String namespace, MessageDto message) {
        // TODO: Integrate with existing persistence layer or AuditLog for message storage
        // Currently using AuditLog as placeholder - may need database integration
        AuditLog.getInstance().log("MVP Message: " + namespace + " - " + message.getContent());
    }
}