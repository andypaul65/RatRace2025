Feature: Period Details and Context Pane Information

  As a financial analyst
  I want to select specific time periods and view detailed information
  So that I can analyze financial performance and trends over time

  Scenario: Get detailed information for a specific period
    Given a financial scenario with multiple time periods
    And entities with varying balances across periods
    When I request details for period 1
    Then the period details should include economic factors
    And the period details should include entity balances
    And the period details should include investment performance
    And the period details should include period flows

  Scenario: Compare two different periods
    Given a financial scenario with multiple time periods
    And entities with varying balances across periods
    When I compare period 0 and period 1
    Then the comparison should show differences in assets, liabilities, and net worth
    And the comparison should include investment performance differences
    And the comparison should highlight key changes

  Scenario: Get list of available periods
    Given a financial scenario with multiple time periods
    When I request the list of available periods
    Then each period should have basic information
    And periods should include economic factors
    And periods should include summary metrics

  Scenario: Period details include investment ROI metrics
    Given an investment portfolio component with:
      | id            | stock_portfolio |
      | investmentType | STOCKS          |
      | initialValue   | 100000          |
      | expectedReturn | 0.08            |
    And a checking account with initial balance of $500,000
    When the scenario is built and run for 12 months
    And I request details for period 6
    Then the period details should include investment-specific ROI calculations
    And the period ROI should reflect period-over-period performance

  Scenario: Sankey data includes enhanced period information
    Given a financial scenario with multiple time periods
    And entities with varying balances across periods
    When buildSankeyData() is called
    Then the period data should include detailed summaries
    And the period data should include investment performance
    And the period data should include key metrics for quick access

  Scenario: Invalid period index handling
    Given a financial scenario with multiple time periods
    When I request details for an invalid period index
    Then the response should indicate an error
    And the error should specify the invalid index

  Scenario: Period comparison with investment growth analysis
    Given an investment portfolio component with:
      | id            | growth_portfolio |
      | investmentType | STOCKS          |
      | initialValue   | 50000          |
      | expectedReturn | 0.10           |
    And a checking account with initial balance of $300,000
    When the scenario is built and run for 24 months
    And I compare period 0 and period 12
    Then the investment comparison should show growth percentages
    And the comparison should highlight significant changes