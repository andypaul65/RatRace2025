# RatRace2025 Financial Modeling Platform - Overview

**Last Updated**: 2025-10-16

## Non-Technical Introduction

Imagine you're trying to understand your financial future - how your savings grow, how your mortgage balance changes over time, or how different investment decisions might play out over the next 30 years. RatRace2025 is like a financial crystal ball that helps you visualize and understand complex money flows through time.

### What It Does

**RatRace2025** is a sophisticated financial simulation platform that models how money moves between different parts of your financial life. Think of it as a financial ecosystem simulator that shows:

- How your salary flows into savings and investments
- How mortgage payments reduce your debt over time
- How investments grow and generate returns
- How UK taxes (Income Tax, National Insurance, Capital Gains Tax) impact your take-home pay
- How tax-efficient strategies can optimize your financial outcomes
- How unexpected events (like bonuses, tax changes, or emergencies) affect your overall financial picture

### Why It Matters

Traditional financial planning often shows you static snapshots - "You'll have $X in 30 years." RatRace2025 shows you the **journey**, not just the destination. You can see:

- **The big picture**: How all your financial pieces work together
- **The details**: Exactly how much flows where and when
- **The what-ifs**: What happens if you change your strategy

### Real-World Analogy

Imagine you're watching a busy highway interchange at rush hour. Cars (representing money) are constantly entering and exiting different lanes (representing different financial accounts). Some cars merge onto highways (investments), others exit to neighborhoods (expenses), and traffic patterns change based on time of day and road conditions.

RatRace2025 gives you a bird's-eye view of this traffic flow, showing you not just where cars end up, but the entire journey they take.

## Technical Overview

RatRace2025 is built as a modular Java-based financial modeling framework using Domain-Driven Design principles. It provides both programmatic simulation capabilities and rich data structures for visualization.

### Core Concepts

- **Entities**: Financial accounts/assets/liabilities with properties like balance, category, rate
- **Flows**: Money movements between entities with amount, type, direction, and timing
- **Events**: Triggers for flows (recurring, conditional, calculation)
- **Scenarios**: Component-based configurations using RentalProperty, Person, InvestmentPortfolio components
- **Simulation Engine**: Event-driven processing with time-based state management

### Key Capabilities

- **Dynamic Entity Management**: Assets, liabilities, income streams, and expense categories
- **Time-Based Simulation**: Configurable periods with event-driven changes
- **Flow Visualization**: Money movement tracking with Sankey diagram data structures
- **UK Tax Modeling**: Comprehensive 2024/25 tax rules implementation
- **Financial Reporting**: Income statements and balance sheets generation

## Getting Started

### For Business Users
Start with the [BDD Use Cases](../BDD/bdd-use-cases.md) to understand what scenarios the platform currently supports.

### For Developers
Continue with the [Architecture](design/architecture.md) document for technical implementation details.

### For UI Developers
See the [UI Integration](design/ui-integration.md) guide for visualization capabilities.

## Development Guidelines

For developers contributing to the project, follow these mandatory guidelines:

- [Backend Architecture](design/backend-development-guide.md) - Development workflow and pre-commit requirements
- [Coding Standards](../guidelines/subproject-coding-standards.md) - Code quality and documentation maintenance
- [Testing Guidelines](../guidelines/subproject-testing-guidelines.md) - Unit, integration, and BDD testing requirements
- [Framework Patterns](../guidelines/subproject-framework-patterns.md) - Design patterns and extension mechanisms

**All development must follow the [Mandatory Pre-Commit Checklist](../guidelines/subproject-coding-standards.md#mandatory-pre-commit-checklist) before any commits.**

## See Also

- **[Architecture](design/architecture.md)**: Technical implementation details
- **[Entities and Flows](design/entities-and-flows.md)**: Domain modeling concepts
- **[Simulation Engine](design/simulation-engine.md)**: How simulations work
- **[UI Visualization](design/ui-visualization.md)**: Visualization capabilities
- **[UI Interactions](design/ui-interactions.md)**: User interaction patterns
- **[Getting Started Guide](howto/getting-started-mvp.md)**: Step-by-step setup instructions