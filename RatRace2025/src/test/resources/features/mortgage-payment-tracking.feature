Feature: Mortgage Payment Tracking

  As a homeowner
  I want to see how my mortgage balance decreases over time
  So that I can track my progress toward debt payoff

  Scenario: Mortgage payment over 12 months
    Given a mortgage with principal balance of $250,000
    And monthly payments of $1,500 (including interest)
    And interest rate of 3.5%
    When the simulation runs for 12 months
    Then the mortgage balance should decrease each month
    And total interest paid should be calculable
    And the mortgage should appear as a liability node in Sankey diagrams