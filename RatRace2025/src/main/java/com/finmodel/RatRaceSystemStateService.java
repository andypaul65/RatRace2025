package com.finmodel;

import org.springframework.stereotype.Service;
import org.ajp.mvp.server.AbstractSystemStateService;
import org.ajp.mvp.server.MessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    protected MessageDto processMessage(String namespace, MessageDto message) {
        try {
            String content = message.getContent();
            String type = message.getType();

            if ("load_scenario".equals(type)) {
                // TODO: Integrate with FinanceModel.loadFromJson() - ensure proper error handling
                FinanceModel model = new FinanceModel();
                model.loadFromJson(content);
                simulationResults.put(namespace, model);

                return MessageDto.builder()
                        .content("Scenario loaded successfully")
                        .namespace(namespace)
                        .type("load_response")
                        .build();

            } else if ("run_simulation".equals(type)) {
                // TODO: Integrate with FinanceModel.runSimulation() - handle simulation exceptions
                FinanceModel model = simulationResults.get(namespace);
                if (model == null) {
                    return MessageDto.builder()
                            .content("No scenario loaded for namespace: " + namespace)
                            .namespace(namespace)
                            .type("error")
                            .build();
                }

                model.runSimulation();

                return MessageDto.builder()
                        .content("Simulation completed")
                        .namespace(namespace)
                        .type("simulation_response")
                        .build();

            } else if ("get_dump".equals(type)) {
                // TODO: Integrate with FinanceModel.dumpToConsole() - capture output properly
                FinanceModel model = simulationResults.get(namespace);
                if (model == null) {
                    return MessageDto.builder()
                            .content("No simulation results for namespace: " + namespace)
                            .namespace(namespace)
                            .type("error")
                            .build();
                }

                // Capture dump output (in real implementation, would redirect System.out)
                model.dumpToConsole();

                return MessageDto.builder()
                        .content("Dump printed to console")
                        .namespace(namespace)
                        .type("dump_response")
                        .build();

            } else if ("get_sankey".equals(type)) {
                // TODO: Integrate with FinanceModel.buildSankeyData() - ensure data format matches MVP expectations
                FinanceModel model = simulationResults.get(namespace);
                if (model == null) {
                    return MessageDto.builder()
                            .content("No simulation results for namespace: " + namespace)
                            .namespace(namespace)
                            .type("error")
                            .build();
                }

                Map<String, Object> sankeyData = model.buildSankeyData();
                // Convert to JSON string for message content
                ObjectMapper mapper = new ObjectMapper();
                String jsonData = mapper.writeValueAsString(sankeyData);

                return MessageDto.builder()
                        .content(jsonData)
                        .namespace(namespace)
                        .type("sankey_response")
                        .build();
            }

            return MessageDto.builder()
                    .content("Unknown message type: " + type)
                    .namespace(namespace)
                    .type("error")
                    .build();

        } catch (Exception e) {
            return MessageDto.builder()
                    .content("Error processing message: " + e.getMessage())
                    .namespace(namespace)
                    .type("error")
                    .build();
        }
    }

    @Override
    protected MessageDto getDefaultState(String namespace) {
        return MessageDto.builder()
                .content("No scenario loaded")
                .namespace(namespace)
                .type("default_state")
                .build();
    }

    @Override
    protected void storeMessage(String namespace, MessageDto message) {
        // TODO: Integrate with existing persistence layer or AuditLog for message storage
        // Currently using AuditLog as placeholder - may need database integration
        AuditLog.getInstance().log("MVP Message: " + namespace + " - " + message.getType() + " - " + message.getContent());
    }
}