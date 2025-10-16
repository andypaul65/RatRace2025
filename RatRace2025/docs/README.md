# RatRace2025 Documentation Index

**Last Updated**: 2025-10-15

This is the master index for RatRace2025 project documentation. Documents are grouped by category for easy navigation.

## 🎉 MVP Framework Integration Complete

RatRace2025 has been successfully integrated with the MVP backplane framework as a full-stack subproject. This includes:

- **Backend Integration**: Domain-driven financial modeling with event-driven simulation engine
- **Client Integration**: React/TypeScript UI using MVP `TabbedInterface` and message passing
- **API Contracts**: Standardized message schemas with namespace isolation
- **Testing**: Comprehensive unit, integration, and BDD tests
- **Documentation**: Architecture reflecting current domain-driven design

See the [subproject-integration-guide.md](guidelines/subproject-integration-guide.md) for setup instructions.

## Guidelines
Standards and processes for developing on the RatRace2025 platform and MVP backplane.

- **[subproject-integration-guide.md](guidelines/subproject-integration-guide.md)**: Step-by-step guide for integrating the MVP framework into subprojects, including Maven/NPM setup and publishing.
- **[subproject-architecture.md](guidelines/subproject-architecture.md)**: High-level architecture principles for subprojects, including modularity, hooks, and debugging.
- **[ratrace2025-guidelines.md](guidelines/ratrace2025-guidelines.md)**: RatRace-specific project details, structure, and naming conventions.
- **[subproject-testing-guidelines.md](guidelines/subproject-testing-guidelines.md)**: Testing strategies including BDD with Cucumber, unit/integration testing, and backend isolation.
- **[subproject-coding-standards.md](guidelines/subproject-coding-standards.md)**: Coding standards, TypeScript configuration, import conventions, and mandatory pre-commit checks.
- **[subproject-expansion-guide.md](guidelines/subproject-expansion-guide.md)**: How to extend the MVP framework, packaging, registries, and lifecycle hooks.
- **[subproject-framework-patterns.md](guidelines/subproject-framework-patterns.md)**: Design patterns, extension mechanisms, and debugging integration.

## Design
Technical design and architecture of the RatRace2025 system.

- **[overview.md](overview.md)**: High-level introduction to RatRace2025 as a financial modeling platform.
- **[architecture.md](design/architecture.md)**: Technical architecture overview, layers, components, and performance considerations.
- **[entities-and-flows.md](design/entities-and-flows.md)**: Domain modeling of financial entities and money flows.
- **[simulation-engine.md](design/simulation-engine.md)**: How financial scenarios are simulated over time.
- **[scenario-composition.md](design/scenario-composition.md)**: Component-based scenario building with examples.
- **[uk-tax-system.md](design/uk-tax-system.md)**: Implementation of UK tax calculations for 2024/25.
- **[financial-reporting.md](design/financial-reporting.md)**: Income statements and balance sheets generation.
- **[ui-visualization.md](design/ui-visualization.md)**: UI visualization concepts and Sankey diagram data structures.
- **[ui-interactions.md](design/ui-interactions.md)**: User interface interactions and responsive design.
- **[backend-development-guide.md](design/backend-development-guide.md)**: Incremental development phases and best practices for the backend.

## BDD
Behavior-Driven Development use cases demonstrating current functionality.

- **[bdd-use-cases.md](BDD/bdd-use-cases.md)**: Core financial modeling scenarios and capabilities.
- **[ui-bdd-use-cases.md](BDD/ui-bdd-use-cases.md)**: UI-specific visualization and interaction scenarios.

## Backend Model Specification
Detailed technical specifications.

- **[RatRace2025BackendModelSpecification.md](RatRace2025BackendModelSpecification.md)**: Legacy comprehensive specification (consider migrating content to backend-development-guide.md).

---

## Navigation Tips
- Start with [overview.md](overview.md) for a non-technical introduction.
- For developers, begin with [subproject-integration-guide.md](guidelines/subproject-integration-guide.md) and [architecture.md](design/architecture.md).
- All documents include "See Also" sections linking to related files.
- Diagrams are in PlantUML format and render on GitHub or with compatible viewers.