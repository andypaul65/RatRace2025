Feature: MVP Framework Integration

  As a RatRace system integrator
  I want to interact with the financial modeling service through the MVP backplane
  So that I can use standardized message passing for scenario management

  Scenario: Load scenario via MVP messages
    Given the RatRace system state service is initialized
    When I send a "load_scenario" message with valid scenario JSON
    Then I should receive a "load_response" message with success confirmation

  Scenario: Run simulation via MVP messages
    Given the RatRace system state service is initialized
    And a scenario has been loaded via MVP message
    When I send a "run_simulation" message
    Then I should receive a "simulation_response" message with completion confirmation

  Scenario: Get dump via MVP messages
    Given the RatRace system state service is initialized
    And a scenario has been loaded and simulated via MVP messages
    When I send a "get_dump" message
    Then I should receive a "dump_response" message with dump information

  Scenario: Get Sankey data via MVP messages
    Given the RatRace system state service is initialized
    And a scenario has been loaded and simulated via MVP messages
    When I send a "get_sankey" message
    Then I should receive a "sankey_response" message with Sankey JSON data

  Scenario: Handle invalid scenario JSON
    Given the RatRace system state service is initialized
    When I send a "load_scenario" message with invalid JSON
    Then I should receive an "error" message with failure details

  Scenario: Handle simulation without loaded scenario
    Given the RatRace system state service is initialized
    When I send a "run_simulation" message without loading a scenario first
    Then I should receive an "error" message indicating no scenario loaded

  Scenario: Handle unknown message types
    Given the RatRace system state service is initialized
    When I send an unknown message type
    Then I should receive an "error" message about unknown message type