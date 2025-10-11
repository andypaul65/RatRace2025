Feature: Basic Account Balance Tracking

  As a personal finance user
  I want to track balances in my bank accounts over time
  So that I can see how my cash position changes

  Scenario: Monthly salary deposits and expenses
    Given a checking account with initial balance of $5,000
    And monthly salary deposits of $4,000
    And monthly expense payments of $3,500
    When the simulation runs for 6 months
    Then the account balance should increase by $500 per month
    And the final balance should be $8,000
    And Sankey nodes should show balance progression across periods