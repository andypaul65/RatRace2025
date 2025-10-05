package com.finmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Simulator {
    private Scenario scenario;
    private EventProcessor processor;
    private Timeline timeline;
    private Set<Entity> dynamicEntities;

    public void playOut() {
        if (timeline == null || timeline.getPeriods() == null) {
            return;
        }

        if (dynamicEntities == null) {
            dynamicEntities = new HashSet<>();
        }

        // Loop through periods
        for (TimePeriod period : timeline.getPeriods()) {
            // Check triggers: stub - process pendingEvents
            if (timeline.getPendingEvents() != null) {
                for (Event event : timeline.getPendingEvents()) {
                    // Apply event to entities, but stub
                }
            }

            // Process events in period
            if (period.getEvents() != null) {
                for (Event event : period.getEvents()) {
                    // Process event
                    if (scenario.getInitialEntities() != null) {
                        for (Entity entity : scenario.getInitialEntities()) {
                            EntityVersion version = entity.createInitialVersion(new Date());
                            EntityVersion newVersion = processor.process(event, version);
                            // Add to period versionChains, stub
                        }
                    }

                    // Handle creations
                    List<Entity> created = processor.handleCreation(event);
                    if (created != null) {
                        for (Entity createdEntity : created) {
                            // Clone template if needed, add to dynamic
                            Entity cloned = createdEntity.cloneAsNew();
                            dynamicEntities.add(cloned);
                            // Also add to scenario initials for next periods
                            if (scenario.getInitialEntities() == null) {
                                scenario.setInitialEntities(new ArrayList<>());
                            }
                            scenario.getInitialEntities().add(cloned);
                        }
                    }
                }
            }

            // Handle externals: stub
            if (scenario.getExternals() != null) {
                for (Object external : scenario.getExternals()) {
                    // Call some method, stub
                }
            }

            // Optimizations: stub for parallel processing
        }
    }
}