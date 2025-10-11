# BDD Use Cases - Current Functionality

This document outlines the current capabilities of RatRace2025 using Behavior-Driven Development (BDD) scenarios. Each use case describes a specific user journey and demonstrates what the system currently supports.

## Core Financial Modeling

### UC-001: Basic Account Balance Tracking

**As a** personal finance user  
**I want to** track balances in my bank accounts over time  
**So that** I can see how my cash position changes

**Given** a checking account with initial balance of $5,000  
**And** monthly salary deposits of $4,000  
**And** monthly expense payments of $3,500  
**When** the simulation runs for 6 months  
**Then** the account balance should increase by $500 per month  
**And** the final balance should be $8,000  
**And** Sankey nodes should show balance progression across periods

### UC-002: Mortgage Payment Tracking

**As a** homeowner  
**I want to** see how my mortgage balance decreases over time  
**So that** I can track my progress toward debt payoff

**Given** a mortgage with principal balance of $250,000  
**And** monthly payments of $1,500 (including interest)  
**And** interest rate of 3.5%  
**When** the simulation runs for 12 months  
**Then** the mortgage balance should decrease each month  
**And** total interest paid should be calculable  
**And** the mortgage should appear as a liability node in Sankey diagrams

### UC-003: Investment Portfolio Growth

**As an** investor  
**I want to** model how my investment portfolio grows  
**So that** I can project future values

**Given** an investment account with initial balance of $50,000  
**And** monthly contributions of $1,000  
**And** annual return rate of 7%  
**When** the simulation runs for 10 years  
**Then** the final balance should reflect compound growth  
**And** contribution flows should be visible as income streams  
**And** growth should be tracked as intra-period calculations

## Entity Management

### UC-004: Dynamic Entity Creation

**As a** financial planner  
**I want to** automatically create new accounts based on conditions  
**So that** retirement accounts can be established when income thresholds are met

**Given** employment income exceeding $100,000 annually  
**And** a conditional event to create retirement account  
**When** annual income reaches the threshold  
**Then** a new retirement account entity should be created  
**And** initial funding flows should transfer to the new account  
**And** the new entity should appear in subsequent periods

### UC-005: Entity Categorization and Grouping

**As a** portfolio manager  
**I want to** organize accounts by category and group them logically  
**So that** I can analyze assets by type and risk level

**Given** multiple bank accounts, investment accounts, and properties  
**And** asset groups defined for "Cash Reserves", "Growth Investments", "Real Estate"  
**When** viewing the portfolio structure  
**Then** entities should be properly categorized (Asset/Liability/Income/Expense)  
**And** groups should aggregate balances correctly  
**And** hierarchical navigation should allow drill-down to individual entities

## Flow Tracking and Analysis

### UC-006: Income and Expense Flow Visualization

**As a** budget analyst  
**I want to** see all money flows in a visual diagram  
**So that** I can identify spending patterns and income sources

**Given** salary income, rental income, utility expenses, and discretionary spending  
**And** monthly recurring events for each flow type  
**When** generating a Sankey diagram  
**Then** income flows should appear as green inflows  
**And** expense flows should appear as red outflows  
**And** flow thickness should represent relative amounts  
**And** hovering over flows should show amount and description

### UC-007: Transfer Flow Tracking

**As a** cash flow manager  
**I want to** track money transfers between accounts  
**So that** I can optimize my cash positioning

**Given** checking account and high-yield savings account  
**And** automatic transfer events to move excess cash  
**When** monthly transfers occur  
**Then** flows should be classified as intra-period transfers  
**And** both account balances should update correctly  
**And** transfer flows should connect the account nodes in the diagram

### UC-008: Inter-Period Balance Carryover

**As a** financial modeler  
**I want to** see how balances carry forward between periods  
**So that** I can track accumulation and depletion over time

**Given** a savings account with monthly deposits  
**And** no withdrawals during the period  
**When** moving from one month to the next  
**Then** the ending balance should become the starting balance  
**And** inter-period flows should represent balance carryover  
**And** Sankey columns should show balance progression

## Event-Driven Scenarios

### UC-009: Emergency Fund Replenishment

**As a** risk manager  
**I want to** automatically rebuild emergency funds after withdrawals  
**So that** I maintain financial security

**Given** emergency fund with target balance of $10,000  
**And** conditional event to replenish when balance falls below target  
**And** monthly contributions of $500 when condition is met  
**When** emergency withdrawal reduces balance to $5,000  
**Then** the replenishment event should trigger  
**And** monthly contributions should resume until target is reached

### UC-010: Debt Acceleration Payment

**As a** debt optimizer  
**I want to** make extra payments on debt when cash is available  
**So that** I can pay off loans faster

**Given** credit card debt of $5,000  
**And** checking account surplus threshold of $2,000  
**And** conditional event to pay extra when surplus exists  
**When** checking balance exceeds threshold  
**Then** extra payments should be applied to the debt  
**And** debt balance should decrease faster than minimum payments

## Simulation Control

### UC-011: Multi-Period Scenario Planning

**As a** financial strategist  
**I want to** run simulations for different time horizons  
**So that** I can plan for short-term and long-term goals

**Given** a complete financial scenario with all entities and events  
**When** running simulation for 1 year, 5 years, and 10 years  
**Then** each simulation should complete successfully  
**And** results should be consistent across different horizons  
**And** Sankey diagrams should scale appropriately for the time period

### UC-012: Scenario Comparison

**As a** decision maker  
**I want to** compare different financial strategies  
**So that** I can choose the optimal approach

**Given** two scenarios: conservative investing vs. aggressive investing  
**And** identical starting conditions and income  
**When** both simulations run for the same period  
**Then** results should show different outcomes  
**And** comparative analysis should be possible  
**And** key metrics (final balances, growth rates) should be extractable

## UI Interaction Scenarios

### UC-013: Node Detail Exploration

**As a** data explorer  
**I want to** click on Sankey nodes for detailed information  
**So that** I can understand the full context of each account

**Given** a rendered Sankey diagram with multiple account nodes  
**When** clicking on an investment account node  
**Then** a context panel should open  
**And** show account details: balance, rate, category, initial value  
**And** display related flows and events for that account

### UC-014: Flow Analysis

**As a** flow analyst  
**I want to** examine individual money flows in detail  
**So that** I can understand transaction patterns

**Given** a Sankey diagram with visible flows  
**When** hovering over a flow line  
**Then** a tooltip should show flow details  
**And** clicking should open detailed flow information  
**And** show source/target accounts, amount, type, and period

### UC-015: Hierarchical Navigation

**As a** portfolio organizer  
**I want to** navigate between summary and detail views  
**So that** I can analyze at different levels of granularity

**Given** grouped asset categories in the Sankey diagram  
**When** clicking on a "Growth Investments" group node  
**Then** the view should expand to show individual investment accounts  
**And** allow further drill-down to specific holdings  
**And** provide roll-up capability to return to summary view

## Error Handling and Edge Cases

### UC-016: Insufficient Funds Handling

**As a** system user  
**I want to** handle cases where accounts lack sufficient funds  
**So that** simulations don't fail on realistic constraints

**Given** an account with $1,000 balance  
**And** a payment event requiring $1,500  
**When** the event attempts to execute  
**Then** the system should handle the shortfall gracefully  
**And** either reduce the payment amount or skip the transaction  
**And** log the issue for user awareness

### UC-017: Invalid Entity Configuration

**As a** scenario builder
**I want to** receive validation feedback on configuration errors
**So that** I can fix issues before running simulations

**Given** an entity with negative initial balance
**When** attempting to create the entity
**Then** validation should fail with clear error message
**And** suggest correction actions
**And** prevent invalid states from entering the simulation

### UC-018: Insufficient Funds Error Handling

**As a** financial model user
**I want to** see immediate scenario failure when payments exceed available funds
**So that** I can identify and correct unrealistic financial scenarios

**Given** an account with $100 balance
**And** a payment event requiring $200
**When** the simulation attempts to process the payment
**Then** the scenario should fail immediately with SimulationException
**And** the error message should clearly indicate insufficient funds
**And** the simulation should not continue with partial payments or logged warnings

## Reporting and Export

### UC-019: Period Summary Generation

**As a** report consumer
**I want to** see summaries for each time period
**So that** I can track progress and identify trends

**Given** a completed simulation with multiple periods
**When** requesting period summaries
**Then** each period should show starting/ending balances
**And** list all events and flows for that period
**And** calculate net changes and key metrics

### UC-020: Sankey Data Export

**As a** UI developer
**I want to** export Sankey diagram data in standard format
**So that** I can integrate with visualization libraries

**Given** a completed simulation
**When** calling buildSankeyData()
**Then** return JSON with nodes, links, and periods arrays
**And** include all metadata for rich interactions
**And** provide normalized heights for consistent scaling

## Performance and Scalability

### UC-021: Large Scenario Handling

**As a** enterprise user
**I want to** run simulations with many entities and long timeframes
**So that** I can model complex financial situations

**Given** a scenario with 100+ entities and 20-year timeframe
**When** running the simulation
**Then** processing should complete within reasonable time
**And** memory usage should remain bounded
**And** results should remain accurate and consistent

---

## Implementation Status

All use cases listed above are **currently implemented** and tested in the RatRace2025 system. The scenarios demonstrate the breadth of functionality available for financial modeling and visualization.

### Key Capabilities Demonstrated:
- ✅ Entity creation and lifecycle management
- ✅ Event-driven flow generation
- ✅ Multi-period simulation execution
- ✅ Sankey diagram data generation
- ✅ Hierarchical grouping and navigation
- ✅ Rich metadata for UI interactions
- ✅ Error handling and validation
- ✅ **Immediate scenario failure on business rule violations**
- ✅ Insufficient funds detection and scenario termination
- ✅ Performance optimization with caching

### Future Enhancements (Not Yet Implemented):
- Comparative scenario analysis
- Advanced event types (tax calculations, market events)
- External data integration (market rates, economic indicators)
- Advanced reporting and analytics
- Real-time simulation updates