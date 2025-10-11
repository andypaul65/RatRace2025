package com.finmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        for (int periodIndex = 0; periodIndex < timeline.getPeriods().size(); periodIndex++) {
            TimePeriod period = timeline.getPeriods().get(periodIndex);

            // Initialize versionChains for this period
            if (period.getVersionChains() == null) {
                period.setVersionChains(new java.util.HashMap<>());
            }

            // Populate period with events from scenario templates
            populatePeriodWithEvents(period, periodIndex);

            // For each entity, start with initial version if first period, else carry from previous
            List<Entity> entities = new ArrayList<>(scenario.getInitialEntities());
            List<Entity> newEntities = new ArrayList<>();
            for (Entity entity : entities) {
                EntityVersion currentVersion;
                if (periodIndex == 0) {
                    // First period, create initial
                    currentVersion = entity.createInitialVersion(new Date());
                } else {
                    // Carry from previous period
                    TimePeriod prevPeriod = timeline.getPeriods().get(periodIndex - 1);
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

    private void populatePeriodWithEvents(TimePeriod period, int periodIndex) {
        if (scenario.getEventTemplates() == null) {
            return;
        }

        List<Event> periodEvents = new ArrayList<>();

        // Add events from templates based on entity and timing
        for (Map.Entry<Entity, List<Event>> entry : scenario.getEventTemplates().entrySet()) {
            Entity entity = entry.getKey();
            List<Event> templateEvents = entry.getValue();

            for (Event templateEvent : templateEvents) {
                // For recurring events, add to every period
                if (templateEvent.isRecurring()) {
                    // Clone the event for this period
                    Event periodEvent = createEventForPeriod(templateEvent, entity, periodIndex);
                    periodEvents.add(periodEvent);
                }
                // For one-time events, could add logic here based on periodIndex
            }
        }

        // Also add latent events from scenario
        if (scenario.getLatentEvents() != null) {
            periodEvents.addAll(scenario.getLatentEvents());
        }

        if (!periodEvents.isEmpty()) {
            period.setEvents(periodEvents);
        }
    }

    private Event createEventForPeriod(Event template, Entity entity, int periodIndex) {
        // Create a copy of the template event for this specific period
        // For recurring events, we can reuse the same event instance since it's stateless
        return template;
    }
}