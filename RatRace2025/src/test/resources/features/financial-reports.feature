Feature: Financial Reports - Income Statement and Balance Sheet

  As a financial analyst
  I want to generate simplified Income Statements and Balance Sheets for each period
  So that I can understand the financial health and performance at a high level

  Scenario: Generate Income Statement for a specific period
    Given a financial scenario with multiple time periods
    And entities with income and expense accounts
    When I generate an Income Statement for period 1
    Then the Income Statement should include revenues
    And the Income Statement should include expenses
    And the Income Statement should calculate net income
    And revenues should be categorized appropriately
    And expenses should be categorized appropriately

  Scenario: Generate Balance Sheet for a specific period
    Given a financial scenario with multiple time periods
    And entities with asset and liability accounts
    When I generate a Balance Sheet for period 1
    Then the Balance Sheet should include assets
    And the Balance Sheet should include liabilities
    And the Balance Sheet should calculate net worth
    And assets should be categorized appropriately
    And liabilities should be categorized appropriately

  Scenario: Get formatted financial reports
    Given a financial scenario with multiple time periods
    And entities with various account types
    When I request formatted financial reports for period 1
    Then the formatted reports should include Income Statement
    And the formatted reports should include Balance Sheet
    And the reports should be properly formatted for display

  Scenario: Period details include financial reports
    Given a financial scenario with multiple time periods
    And entities with various account types
    When I request details for period 1
    Then the period details should include Income Statement data
    And the period details should include Balance Sheet data
    And the period details should include formatted reports

  Scenario: Financial reports handle investment income
    Given an investment portfolio component with:
      | id            | stock_portfolio |
      | investmentType | STOCKS          |
      | initialValue   | 100000          |
      | expectedReturn | 0.08            |
    And a checking account with initial balance of $500,000
    When the scenario is built and run for 12 months
    And I generate an Income Statement for period 6
    Then the Income Statement should include investment income

  Scenario: Financial reports show rental property performance
    Given a rental property component with:
      | id          | rental_property |
      | propertyValue | 300000        |
      | monthlyRent   | 2500          |
      | ancillaryCosts | 300          |
    And a checking account with initial balance of $400,000
    When the scenario is built and run for 12 months
    And I generate financial reports for period 6
    Then the Income Statement should show rental income
    And the Income Statement should show property expenses
    And the Balance Sheet should show property as an asset

  Scenario: Invalid period index for financial reports
    Given a financial scenario with multiple time periods
    When I request financial reports for an invalid period index
    Then the reports should indicate an error
    And the error should specify the invalid index