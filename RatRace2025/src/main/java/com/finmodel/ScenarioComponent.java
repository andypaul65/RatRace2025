package com.finmodel;

import java.util.List;

/**
 * Interface for scenario components that encapsulate related financial entities and events.
 * Components provide high-level abstractions for common financial scenarios like rental properties,
 * investment portfolios, and personal finances.
 */
public interface ScenarioComponent {
    String getId();
    String getName();
    List<Entity> getEntities();
    List<Event> getEvents();
    void validate() throws ValidationException;
    String describe();
}