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
            // Initialize versionChains for this period
            if (period.getVersionChains() == null) {
                period.setVersionChains(new java.util.HashMap<>());
            }

            // For each entity, start with initial version if first period, else carry from previous
            List<Entity> entities = new ArrayList<>(scenario.getInitialEntities());
            List<Entity> newEntities = new ArrayList<>();
            for (Entity entity : entities) {
                EntityVersion currentVersion;
                if (timeline.getPeriods().indexOf(period) == 0) {
                    // First period, create initial
                    currentVersion = entity.createInitialVersion(new Date());
                } else {
                    // Carry from previous period
                    TimePeriod prevPeriod = timeline.getPeriods().get(timeline.getPeriods().indexOf(period) - 1);
                    currentVersion = prevPeriod.getFinalVersion(entity);
                    if (currentVersion == null) {
                        currentVersion = entity.createInitialVersion(new Date());
                    }
                }

                // Apply events in this period
                if (period.getEvents() != null) {
                    for (Event event : period.getEvents()) {
                        currentVersion = processor.process(event, currentVersion);
                        double amount = event.getParams() != null ? (double) event.getParams().getOrDefault("amount", 0.0) : 0.0;
                        AuditLog.getInstance().logEvent(event.getType(), entity, amount);
                    }
                }

                // Store in versionChains
                if (!period.getVersionChains().containsKey(entity)) {
                    period.getVersionChains().put(entity, new java.util.ArrayList<>());
                }
                period.getVersionChains().get(entity).add(currentVersion);

                // Handle creations
                if (period.getEvents() != null) {
                    for (Event event : period.getEvents()) {
                        List<Entity> created = processor.handleCreation(event);
                        if (created != null) {
                            for (Entity createdEntity : created) {
                                Entity cloned = createdEntity.cloneAsNew();
                                dynamicEntities.add(cloned);
                                // Collect to add after iteration
                                newEntities.add(cloned);
                            }
                        }
                    }
                }
            }

            // Add new entities after iteration
            scenario.getInitialEntities().addAll(newEntities);

            // Handle externals: stub
            if (scenario.getExternals() != null) {
                for (Object external : scenario.getExternals()) {
                    // Call some method, stub
                }
            }
        }
    }
}