# RatRace2025-Specific Guidelines

This document distills project-specific details for the RatRace2025 financial modeling backend, supplementing subproject guidelines. It preserves domain omissions during MVP integration, ensuring isolation-first development before UI extension.

## Important Clarification on Guidelines Usage

The subproject-guidelines (e.g., subproject-coding-standards.md, subproject-testing-guidelines.md, subproject-integration-guide.md, etc.) are for **reference only** and must **not be changed or modified** in this project. These are immutable core standards from the parent MVP backplane framework.

If any modifications, updates, or clarifications are needed to these guidelines:
- Add RatRace-specific addendums or customizations to this document (`ratrace2025-guidelines.md`)
- Suggest changes/clarifications to the parent project for consideration in the upstream subproject-guidelines
- Do not alter the original subproject-guidelines files in this repository

This ensures consistency across subprojects while allowing for project-specific adaptations.

## Project Naming and Structure
- **Artifact ID**: `ratrace2025-backend`
- **Base Package**: `com.finmodel`
- **Subpackages**: `domain` (entities like `Scenario`), `service` (e.g., `FinanceModelService`), `controller` (e.g., `/api/simulate`), `repository`, `config`, `exception`.

## Domain Entities and DTOs
- **Scenario**: Entity for simulation inputs (e.g., financial parameters).
- **SimulationRequest**: DTO for API payloads (e.g., `{ scenarioId: string }`).
- **FinanceModel**: Output model with simulation results and dump.

## Error Handling
- **SimulationException**: Thrown for business rule violations (e.g., insufficient funds); propagates to fail requests immediately.
- **AuditLog**: SLF4J-based trail for events; critical errors log and fail simulations.

## Patterns and Hooks
- **EntityFactory**: Builder/Factory for financial entities (e.g., creating `Scenario` instances).
- **SimulationHook**: Callback interface for custom simulation logic (e.g., pre/post-processing).

## Testing Examples
- **Unit (FinanceModelServiceTest)**: Mock `ScenarioRepository`; assert `runSimulation` returns non-null `FinanceModel`.
- **Integration (FinanceModelControllerIntegrationTest)**: `@SpringBootTest`; POST `/api/simulate` expects 200 with dump.
- **E2E (REST-assured)**: Verify `/api/simulate` returns non-null dump.

## Backend Isolation Workflow
- Run independently: `mvn spring-boot:run` for minimal REST testing (e.g., `/api/simulate` without UI).
- Iterative stages: Domain setup → Services → Endpoints; verify via `mvn test` before UI integration.

Update this doc with changes; sync to subproject guidelines as needed.