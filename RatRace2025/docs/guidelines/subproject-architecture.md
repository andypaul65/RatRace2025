# Subproject Architecture on MVP Backplane
This document outlines the high-level architecture for subprojects building on the MVP backplane. It describes how to extend the modular MVP framework for specific applications, prioritizing clarity, extensibility, and integration with the backplane. Subprojects leverage the MVP's client (React/TypeScript/Vite) and server (Java/Spring Boot) layers, adding custom logic via registries, hooks, and services while maintaining compatibility.
# Core Principles

## Modularity: 
All components are designed as independent modules with well-defined interfaces. This allows for easy swapping, extension, or reuse across applications.
Abstractions and Hooks: Provide abstract classes and interfaces with extension points (e.g., lifecycle hooks) to facilitate customization without altering core logic.
## Clarity and Education: 
Include inline comments explaining key concepts, especially for client-side technologies, to aid learning and debugging.
## Testing Integration: 
Every module must include design specifications, unit tests, and integration tests developed iteratively. Incorporate Cucumber BDD testing for server business logic.
## Debugging Support: 
Bake in hooks for tools like Spring Boot Actuator for server monitoring and React DevTools for client-side inspection.
Incorporate a simple heartbeat between the client and server.

# Design Document Maintenance (MANDATORY)
**Design documents must be kept current and accurate throughout development:**

- **Before Implementation**: Update design documents to reflect planned changes
- **During Development**: Keep class diagrams, sequence diagrams, and architectural descriptions synchronized with code
- **After Implementation**: Update documents to reflect actual implementation details
- **Peer Review**: All design document changes require peer review before committing
- **Version Control**: Design documents are version controlled and committed alongside code changes

**Failure to maintain current design documents will result in development workflow violations.**

# Client-Side Architecture (React, TypeScript, Node.js, Vite)

## Structure: 
Organize into folders such as src/components for reusable UI elements, src/services for API interactions, and src/hooks for custom React hooks. Use Vite for fast builds and hot module replacement to simplify development.

## Key Abstractions:
Define interfaces for props and state (e.g., interface ComponentProps { id: string; onUpdate: () => void; }) to enforce type safety and modularity.
Implement custom hooks (e.g., useApiFetch) for data fetching, with educational comments explaining React's useEffect and useState.
## Extension Hooks: 
Provide overridable methods or callbacks in base components for application-specific behavior.
##  Debugging Hooks: 
Integrate React DevTools by default; include console logging with clear messages and use TypeScript's strict mode for early error detection. For Vite, configure source maps for precise error tracing.
## UX and Styling
Incorporate modular styling mechanisms, such as CSS modules or Tailwind CSS, to 
support thematic customizations. For the MVP, adopt a cyberpunk aesthetic featuring dark gray (#1E1E1E) backgrounds, black (#000000) elements, and green (#00FF00) accents for highlights. This enhances user engagement without introducing complexity, leveraging extension hooks for theme overrides while maintaining accessibility and readability.

## Client-Server Sync Patterns
Treat client fetches as Java method invocations on a proxy object.

# Server-Side Architecture (Java, Spring Boot)

## Structure: 
Use layered architecture with controllers, services, repositories, and entities. Organize into packages like com.example.controllers and com.example.services.
## Key Abstractions:

All services implement interfaces (e.g., public interface UserService { User getUserById(long id); }) to support dependency injection and mocking.
Use Spring Boot's @Configuration for modular setup, allowing overrides via profiles.


## Extension Hooks:
 Define abstract services with protected methods for subclasses to extend core functionality.
## Debugging Hooks:
Enable Spring Boot Actuator endpoints (e.g., /actuator/health, /actuator/metrics) for runtime monitoring. Include logging with SLF4J and advise using IntelliJ's debugger with breakpoints in services.

## Backend Isolation
Subprojects support independent backend development and testing, decoupling server logic from the UI.

- **Independent Server Runs**: Use `mvn spring-boot:run` to start the server standalone for backend-focused development. This allows testing APIs without client dependencies, facilitating iterative backend work.
- **Minimal REST Endpoints**: Implement core endpoints (e.g., /api/state/{namespace}, /api/message/{namespace}) for functional testing. Use tools like Postman or curl for validation without a full UI.
- **Backend-First Workflows**: Prioritize server development in iterative stages—e.g., build services and controllers first, then integrate with client. This ensures robust backend foundations before UI integration.

# Communication Layer

Use RESTful APIs with JSON payloads for client-server interaction. Define shared interfaces or DTOs for data models to ensure consistency.
Include hooks for authentication (e.g., JWT) and error handling, with abstractions for custom middleware.

# Error Handling Patterns

## Client-Side Error Handling
- Wrap all API calls in services (e.g., `apiService.ts`) with try-catch blocks to catch network errors, invalid responses, or server errors.
- Propagate errors to hooks (e.g., `useSystemState`) for state updates, displaying error messages and setting error flags.
- Use consistent error messages and logging for debugging.
- Example: In `apiService.ts`, throw custom errors with descriptive messages; in hooks, catch and update state accordingly.

## Server-Side Error Handling
- Implement global `@ExceptionHandler` in controllers or advisors to catch exceptions and return consistent JSON error responses.
- Use HTTP status codes appropriately (e.g., 400 for bad requests, 500 for server errors).
- Log errors for monitoring and debugging.
- **Critical Business Rule Violations**: Errors that violate core business rules (e.g., invalid input data) immediately fail the request by throwing `BusinessRuleViolationException`.
- **Logging and Audit Trail**: All events are logged via SLF4J for debugging, but critical errors also fail the request.
- **Test Expectations**: BDD scenarios expect immediate failure when business rule violations occur, not continued execution with logged errors.
- **Exception Propagation**: `BusinessRuleViolationException` is thrown from service processing up through the stack to fail requests immediately.
- **Error Recovery**: Non-critical errors may be logged and handled gracefully, but business rule violations always cause request failure.
- Example: `GlobalExceptionHandler.java` with methods for `Exception.class` and specific exceptions like `BusinessRuleViolationException`.

# Cucumber BDD Testing Integration
**BDD tests are a critical part of the development workflow:**

- **Test-Driven Development**: Write or update Cucumber feature files before implementing new functionality
- **Continuous Verification**: Run Cucumber tests after any changes that might affect business requirements
- **Regression Prevention**: Cucumber tests serve as living documentation and prevent feature regressions
- **Business Alignment**: All new features must have corresponding Cucumber tests that pass
- **Documentation Updates**: Update feature files in `docs/BDD/` when business requirements change

**Commits without passing Cucumber tests are not permitted.**

# Development Workflow
Start with design specs in separate Markdown files per module (e.g., module-design-spec.md).
Iteratively develop code, tests(80%+ coverage), verification (including browser inspection, `npm run build` for type checking and optimization, `npm test` for full suite execution, `mvn compile` for compilation, and `mvn test` for suite execution), and documentation, emphasizing clarity through peer-reviewable comments.

## Pre-Commit Verification (MANDATORY)
**ALWAYS run these commands before committing any changes:**
1. `npm run build` - Ensures TypeScript compilation and bundling succeed
2. `npm test` - Runs full test suite with 80%+ coverage requirement
3. `mvn compile` - Ensures Java compilation succeeds
4. `mvn test` - Runs full test suite with 80%+ coverage requirement including Cucumber BDD tests
5. `mvn test -Dtest=CucumberTestRunner` - Specifically runs Cucumber BDD tests to verify business requirements
6. Manual verification in browser at `http://localhost:5173` and server at `http://localhost:8080`

**Failure to run these commands will result in build failures and should be corrected immediately.**

## Development Environment Setup
For client-side development, MSW (Mock Service Worker) automatically intercepts API calls in development mode, allowing full client functionality without requiring the server to be running. This enables independent client development while maintaining the same API contract expectations. 
For server-side development, the backend can be run in isolation via `mvn spring-boot:run` for testing endpoints without the client. This enables independent server development while maintaining the same API contract expectations. When ready for integration testing, start the Spring Boot server on localhost:8080.

## Iterative Committable Stages:
Structure development into discrete, testable stages with a backend-first approach (e.g., backend skeleton, services, endpoints, then client components, integrations). Each stage must include:

1. **Design spec updates** if needed
2. **Code generation/review** with inline comments
3. **Pre-commit verification** (MANDATORY):
   - `npm run build` - TypeScript compilation and bundling
   - `npm test` - Full test suite (80%+ coverage)
   - `mvn compile` - Java compilation
   - `mvn test` - Full test suite (80%+ coverage)
   - Manual verification by running simulations
4. **Manual verification/debugging** including browser inspection
5. **Descriptive commit** linking to updated specs

**Progress only after ALL stage validations pass.** Never commit without running `npm run build`, `npm test`, `mvn compile`, and `mvn test` first.

## Command Execution Guardrails:
- **Timeout Protection**: All commands use 2-minute default timeouts to prevent hangs
- **Single Execution**: Failing commands are investigated before retry, not auto-re-executed
- **Failure Analysis**: Analyze output and fix root causes before retrying failed commands
- **Server Management**: Spring Boot server must be started manually in separate console/IDE
</DOCUMENT>