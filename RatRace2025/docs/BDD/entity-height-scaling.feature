Feature: Entity Height Scaling for Sankey Diagrams

  As a UI developer integrating Sankey diagrams
  I want to ensure entity heights are consistently scaled across all periods
  So that the visualization accurately represents relative balances throughout the timeline

  Scenario: Entity height scaling based on maximum period balance
    Given a financial scenario with multiple time periods
    And entities with varying balances across periods
    When buildSankeyData() is called
    Then normalizedHeight for each entity node should be calculated as absolute balance divided by the maximum absolute balance across all periods

  Scenario: Negative balance handling in height calculations
    Given an entity with a balance of -5000.0 in one period
    And other entities with positive balances
    When calculating normalized heights
    Then the negative balance should be treated as positive 5000.0 for scaling calculations
    And the entity should still display with appropriate negative balance metadata
    And the scaling should ensure visibility of negative balance entities

  Scenario: Maximum balance calculation with mixed positive and negative balances
    Given a scenario with entities having balances: 10000.0, 25000.0, -15000.0, 8000.0
    When calculating the maximum balance for scaling
    Then the maximum should be 10000.0 (highest absolute value found)
    And the -15000.0 should be treated as 15000.0 for this calculation
    And all normalized heights should be calculated relative to 10000.0

  Scenario: Insufficient funds causes immediate scenario failure
    Given a checking account with initial balance of $100
    And monthly expense payments of $200
    When attempting to run the simulation for 1 month
    Then the simulation should fail with insufficient funds error