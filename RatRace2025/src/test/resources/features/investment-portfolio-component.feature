Feature: Investment Portfolio Component

  As a financial planner
  I want to define investment portfolios using simple component configurations
  So that I can quickly set up realistic investment scenarios with proper ROI tracking

  Scenario: Basic stock portfolio setup with returns
    Given an investment portfolio component with:
      | id            | stock_portfolio |
      | investmentType | STOCKS          |
      | initialValue   | 100000         |
      | expectedReturn | 0.08           |
      | monthlyContribution | 1000       |
    And a checking account with initial balance of $500,000
    When the scenario is built and run for 24 months
    Then the stock portfolio should show positive growth
    And ROI metrics should be calculated and displayed
    And inflation should be applied to stock returns

  Scenario: Crypto portfolio with inflation immunity
    Given an investment portfolio component with:
      | id            | crypto_portfolio |
      | investmentType | CRYPTO          |
      | initialValue   | 50000          |
      | expectedReturn | 0.15           |
      | volatility     | 0.05           |
    And a checking account with initial balance of $300,000
    When the scenario is built and run for 12 months
    Then the crypto portfolio should show growth without inflation adjustment
    And crypto ROI should exceed traditional investment ROI under high inflation

  Scenario: Mixed portfolio comparison
    Given an investment portfolio component with:
      | id            | stocks_portfolio |
      | investmentType | STOCKS          |
      | initialValue   | 75000          |
      | expectedReturn | 0.07           |
    And an investment portfolio component with:
      | id            | crypto_portfolio |
      | investmentType | CRYPTO          |
      | initialValue   | 25000          |
      | expectedReturn | 0.12           |
    And a checking account with initial balance of $400,000
    When the scenario is built and run for 36 months
    Then both portfolios should show growth
    And ROI comparison should be available in the dump output
    And crypto should outperform stocks in high inflation scenarios

  Scenario: Options portfolio with high volatility
    Given an investment portfolio component with:
      | id            | options_portfolio |
      | investmentType | OPTIONS         |
      | initialValue   | 20000          |
      | expectedReturn | 0.25           |
      | volatility     | 0.30           |
    And a checking account with initial balance of $200,000
    When the scenario is built and run for 12 months
    Then the options portfolio should show volatile returns
    And inflation should be applied to options returns
    And volatility should affect the final balance

  Scenario: Component validation for investment portfolios
    Given an investment portfolio component with invalid configuration:
      | expectedReturn | -0.6 |
    Then component validation should fail with appropriate error message

  Scenario: Bonds portfolio with moderate returns
    Given an investment portfolio component with:
      | id            | bonds_portfolio |
      | investmentType | BONDS          |
      | initialValue   | 100000        |
      | expectedReturn | 0.04          |
      | volatility     | 0.05          |
    And a checking account with initial balance of $300,000
    When the scenario is built and run for 60 months
    Then the bonds portfolio should show stable growth
    And annualized ROI should be calculated correctly
    And inflation should be applied to bond returns