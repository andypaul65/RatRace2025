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
                try {
                    FinanceModel model = new FinanceModel();
                    model.loadFromJson(content);
                    simulationResults.put(namespace, model);

                    return MessageDto.builder()
                            .content("Scenario loaded successfully")
                            .namespace(namespace)
                            .type("load_response")
                            .build();
                } catch (Exception e) {
                    return MessageDto.builder()
                            .content("Failed to load scenario: " + e.getMessage())
                            .namespace(namespace)
                            .type("error")
                            .build();
                }

            } else if ("run_simulation".equals(type)) {
                FinanceModel model = simulationResults.get(namespace);
                if (model == null) {
                    return MessageDto.builder()
                            .content("No scenario loaded for namespace: " + namespace)
                            .namespace(namespace)
                            .type("error")
                            .build();
                }

                try {
                    model.runSimulation();

                    return MessageDto.builder()
                            .content("Simulation completed")
                            .namespace(namespace)
                            .type("simulation_response")
                            .build();
                } catch (SimulationException e) {
                    return MessageDto.builder()
                            .content("Simulation failed: " + e.getMessage())
                            .namespace(namespace)
                            .type("error")
                            .build();
                }

            } else if ("get_dump".equals(type)) {
                FinanceModel model = simulationResults.get(namespace);
                if (model == null) {
                    return MessageDto.builder()
                            .content("No simulation results for namespace: " + namespace)
                            .namespace(namespace)
                            .type("error")
                            .build();
                }

                try {
                    // TODO: Implement proper output capture instead of console printing
                    // For now, print to console as per existing behavior, but return summary
                    model.dumpToConsole();

                    return MessageDto.builder()
                            .content("Simulation dump printed to console - see server logs for details")
                            .namespace(namespace)
                            .type("dump_response")
                            .build();
                } catch (Exception e) {
                    return MessageDto.builder()
                            .content("Failed to generate dump: " + e.getMessage())
                            .namespace(namespace)
                            .type("error")
                            .build();
                }

            } else if ("get_sankey".equals(type)) {
                FinanceModel model = simulationResults.get(namespace);
                if (model == null) {
                    return MessageDto.builder()
                            .content("No simulation results for namespace: " + namespace)
                            .namespace(namespace)
                            .type("error")
                            .build();
                }

                try {
                    Map<String, Object> sankeyData = model.buildSankeyData();
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonData = mapper.writeValueAsString(sankeyData);

                    return MessageDto.builder()
                            .content(jsonData)
                            .namespace(namespace)
                            .type("sankey_response")
                            .build();
                } catch (Exception e) {
                    return MessageDto.builder()
                            .content("Failed to generate Sankey data: " + e.getMessage())
                            .namespace(namespace)
                            .type("error")
                            .build();
                }
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