# Overview
This document outlines the high-level architecture for the backend model framework, serving as a modular "backend plane" layer. The design prioritizes clarity, extensibility, and ease of debugging over rapid development. It establishes a bedrock structure that can be extended for various applications through interfaces, abstractions, and hooks. The backend employs Java with Spring Boot for robust services, designed to run and test the model in isolation, integrable into an existing backplane client-server system.
# Core Principles

## Modularity: 
All components are designed as independent modules with well-defined interfaces. This allows for easy swapping, extension, or reuse across applications.
Abstractions and Hooks: Provide abstract classes and interfaces with extension points (e.g., lifecycle hooks) to facilitate customization without altering core logic.
## Clarity and Education:
Include inline comments explaining key concepts to aid learning and debugging.
## Testing Integration: 
Every module must include design specifications, unit tests, and integration tests developed iteratively.
## Debugging Support:
Bake in hooks for tools like Spring Boot Actuator for monitoring.
Incorporate a simple heartbeat for health checks.


# Backend Architecture (Java, Spring Boot)

## Structure:
Use layered architecture with controllers, services, repositories, and entities. For the model, focus on services and domain objects in packages like com.finmodel.services and com.finmodel.domain. Designed to run in isolation for testing, with REST endpoints for future integration into the existing backplane system.
## Key Abstractions:

All services implement interfaces (e.g., public interface FinanceModelService { FinanceModel runSimulation(String scenarioId); }) to support dependency injection and mocking.
Use Spring Boot's @Configuration for modular setup, allowing overrides via profiles.


## Extension Hooks:
 Define abstract services with protected methods for subclasses to extend core functionality.
## Debugging Hooks: 
Enable Spring Boot Actuator endpoints (e.g., /actuator/health, /actuator/metrics) for runtime monitoring. Include logging with SLF4J and advise using IntelliJ's debugger with breakpoints in services.

# Communication Layer

For isolation, minimal REST APIs with JSON payloads for model input/output. Define DTOs for scenarios, results, and dumps to ensure consistency with future backplane integration.
Include hooks for error handling, with abstractions for custom middleware.

# Error Handling Patterns

## Backend Error Handling
- **Critical Business Rule Violations**: Errors that violate core business rules (e.g., insufficient funds for payments) immediately fail the scenario by throwing `SimulationException`.
- **Logging and Audit Trail**: All events are logged via `AuditLog` for debugging, but critical errors also fail the simulation.
- **Test Expectations**: BDD scenarios expect immediate failure when business rule violations occur, not continued execution with logged errors.
- **Exception Propagation**: `SimulationException` is thrown from event processing up through the simulation stack to fail scenarios immediately.
- **Error Recovery**: Non-critical errors may be logged and handled gracefully, but business rule violations always cause scenario failure.

# Development Workflow
Start with design specs in separate Markdown files per module (e.g., module-design-spec.md).
Iteratively develop code, tests(80%+ coverage), verification (including `mvn compile` for compilation and `mvn test` for suite execution), and documentation, emphasizing clarity through peer-reviewable comments.

## Design Document Maintenance (MANDATORY)
**Design documents must be kept current and accurate throughout development:**

- **Before Implementation**: Update design documents to reflect planned changes
- **During Development**: Keep class diagrams, sequence diagrams, and architectural descriptions synchronized with code
- **After Implementation**: Update documents to reflect actual implementation details
- **Peer Review**: All design document changes require peer review before committing
- **Version Control**: Design documents are version controlled and committed alongside code changes

**Failure to maintain current design documents will result in development workflow violations.**

## Pre-Commit Verification (MANDATORY)
**ALWAYS run these commands before committing any changes:**
1. `mvn compile` - Ensures Java compilation succeeds
2. `mvn test` - Runs full test suite with 80%+ coverage requirement including Cucumber BDD tests
3. `mvn test -Dtest=CucumberTestRunner` - Specifically runs Cucumber BDD tests to verify business requirements
4. Manual verification by running the model in isolation

**Failure to run these commands will result in build failures and should be corrected immediately.**

## Cucumber BDD Testing Integration
**BDD tests are a critical part of the development workflow:**

- **Test-Driven Development**: Write or update Cucumber feature files before implementing new functionality
- **Continuous Verification**: Run Cucumber tests after any changes that might affect business requirements
- **Regression Prevention**: Cucumber tests serve as living documentation and prevent feature regressions
- **Business Alignment**: All new features must have corresponding Cucumber tests that pass
- **Documentation Updates**: Update feature files in `docs/BDD/` when business requirements change

**Commits without passing Cucumber tests are not permitted.**

## Development Environment Setup
The backend is designed for isolation testing. Use IntelliJ or Eclipse for development. Spring Boot can be run via `mvn spring-boot:run` in a separate console for testing endpoints.
 

## Iterative Committable Stages:
Structure development into discrete, testable stages (e.g., core domain, services, endpoints). Each stage must include:

1. **Design spec updates** if needed
2. **Code generation/review** with inline comments
3. **Pre-commit verification** (MANDATORY):
   - `mvn compile` - Java compilation
   - `mvn test` - Full test suite (80%+ coverage)
   - Manual verification by running simulations
4. **Manual verification/debugging** including logs and breakpoints
5. **Descriptive commit** linking to updated specs

**Progress only after ALL stage validations pass.** Never commit without running `mvn compile` and `mvn test` first.

## Command Execution Guardrails:
- **Timeout Protection**: All commands use 2-minute default timeouts to prevent hangs
- **Single Execution**: Failing commands are investigated before retry, not auto-re-executed
- **Failure Analysis**: Analyze output and fix root causes before retrying failed commands
- **Server Management**: Spring Boot server can be started manually in separate console/IDE for testing