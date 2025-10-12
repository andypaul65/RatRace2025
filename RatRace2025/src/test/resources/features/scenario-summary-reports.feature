Feature: Scenario Summary Financial Reports

  As a financial analyst
  I want to generate comprehensive Income Statements and Balance Sheets for entire scenarios
  So that I can understand the overall financial performance and wealth creation across the full timeframe

  Scenario: Generate scenario-wide Income Statement
    Given a financial scenario with multiple time periods
    And entities with income and expense accounts
    When I generate a scenario Income Statement
    Then the scenario Income Statement should aggregate revenues across all periods
    And the scenario Income Statement should aggregate expenses across all periods
    And the scenario Income Statement should calculate total net income
    And the scenario Income Statement should include average monthly figures

  Scenario: Generate scenario-wide Balance Sheet
    Given a financial scenario with multiple time periods
    And entities with asset and liability accounts
    When I generate a scenario Balance Sheet
    Then the scenario Balance Sheet should show final period assets and liabilities
    And the scenario Balance Sheet should calculate final net worth
    And the scenario Balance Sheet should include net worth change from initial to final
    And the scenario Balance Sheet should include annualized net worth return

  Scenario: Get formatted scenario financial reports
    Given a financial scenario with multiple time periods
    And entities with various account types
    When I request formatted scenario financial reports
    Then the formatted reports should include scenario Income Statement
    And the formatted reports should include scenario Balance Sheet
    And the scenario reports should be properly formatted for display
    And the reports should show total periods and duration

  Scenario: Get comprehensive scenario summary
    Given a financial scenario with multiple time periods
    And entities with various account types
    When I request the comprehensive scenario summary
    Then the scenario summary should include basic scenario information
    And the scenario summary should include investment performance summary
    And the scenario summary should include financial reports
    And the scenario summary should include overall scenario metrics

  Scenario: Scenario summary includes total portfolio performance
    Given an investment portfolio component with:
      | id            | stock_portfolio |
      | investmentType | STOCKS          |
      | initialValue   | 100000          |
      | expectedReturn | 0.08            |
      | monthlyContribution | 1000       |
    And a checking account with initial balance of $500,000
    When the scenario is built and run for 24 months
    And I request the comprehensive scenario summary
    Then the scenario summary should include total portfolio return
    And the scenario summary should include annualized portfolio return
    And the scenario summary should show initial vs final total values

  Scenario: Scenario reports handle empty scenario
    Given an empty financial scenario
    When I request scenario financial reports
    Then the reports should indicate no data available
    And the scenario summary should indicate an error