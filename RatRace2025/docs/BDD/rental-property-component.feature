Feature: Rental Property Component

  As a financial planner
  I want to define rental properties using simple component configurations
  So that I can quickly set up realistic rental investment scenarios

  Scenario: Basic rental property setup with proper funding
    Given a rental property component with:
      | id          | primary_residence |
      | propertyValue | 300000          |
      | mortgageAmount | 240000         |
      | monthlyRent   | 2500            |
      | ancillaryCosts | 300            |
    And a checking account with initial balance of $500,000
    When the scenario is built and run for 12 months
    Then the rental property should generate positive cash flow
    And the mortgage balance should decrease over time
    And property value should appreciate annually

  Scenario: Rental property with appreciation and funding
    Given a rental property component with:
      | id          | investment_property |
      | propertyValue | 200000            |
      | appreciationRate | 0.04             |
      | mortgageAmount | 150000           |
      | mortgageRate | 0.042             |
      | monthlyRent   | 1800              |
      | vacancyRate   | 0.03              |
    And a checking account with initial balance of $300,000
    When the scenario is built and run for 24 months
    Then the property should show appreciation of approximately 4% per year
    And the effective rent should account for vacancy rate
    And mortgage payments should be calculated correctly

  Scenario: Insufficient funds demonstrates immediate error failure
    Given a rental property component with:
      | id          | unfunded_property |
      | propertyValue | 300000          |
      | mortgageAmount | 240000         |
      | monthlyRent   | 2500            |
      | ancillaryCosts | 300            |
    And a checking account with initial balance of $100
    When attempting to run the simulation for 1 month
    Then the simulation should fail with insufficient funds error

  Scenario: Component validation
    Given a rental property component with invalid configuration:
      | propertyValue | -1000 |
    Then component validation should fail with appropriate error message