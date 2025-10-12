# Backend Coding Standards

This document outlines the coding standards and best practices for the backend model project. It ensures consistency, maintainability, and scalability across all contributions. Adherence to these guidelines is mandatory during code generation (e.g., via tools like grok-code-fast) and manual implementations, with a post-generation review required to verify compliance.

## Project Initialization and Dependencies

To establish a robust foundation, follow these steps immediately after scaffolding the project (e.g., via Maven archetype for Spring Boot):

- **Core Dependencies**: Include and verify the following in `pom.xml`:
  - Spring Boot Starter Web, Data JPA (if needed), Actuator.
  - Jackson for JSON.
  - Lombok for boilerplate reduction.
  - SLF4J for logging.
  - JUnit 5, Mockito for testing.

- **Essential Development Dependencies**: Run the following command post-initialization to include Lombok support:
  ```
  mvn clean compile
  ```
  This ensures annotation processing works; verify in IDE settings for Lombok plugin.

- **Verification Checklist**:
  - Confirm `mvn clean compile` succeeds without errors.
  - Review `pom.xml` for correct versions and profiles.

## Java Coding Standards

- **Package Structure**: Use `com.finmodel` as base. Subpackages: `domain`, `service`, `controller`, `repository`, `config`, `exception`.
- **Class Naming**: PascalCase for classes, camelCase for methods/variables.
- **Annotations**: Use Lombok (@Data, @Builder, @AllArgsConstructor, etc.) to reduce boilerplate. Ensure IDE is configured.
- **Immutability**: Prefer records for DTOs and immutable objects.
- **Logging**: Use SLF4J with parameterized messages: `logger.info("Processing scenario: {}", scenarioId);`

## TypeScript Configuration (Removed - Backend Only)

[Removed as frontend is not in scope.]

## Import/Export Conventions (Adapted for Java)

Promote modularity and avoid deep relative paths.

- **Preferred Style**: Use fully qualified imports or wildcards sparingly; prefer explicit imports.
- **Path Resolution**: Leverage package structure; use `import com.finmodel.domain.Entity;` explicitly.
- **Organization**: Shared types/DTOs in `dto` package, services in `service`, etc.

## Maven Configuration Best Practices

`pom.xml` must support Spring Boot and testing.

- **Standard Template**:
  ```xml
  <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <modelVersion>4.0.0</modelVersion>
      <parent>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-parent</artifactId>
          <version>3.2.0</version>
      </parent>
      <groupId>com.finmodel</groupId>
      <artifactId>ratrace2025-backend</artifactId>
      <version>1.0.0</version>
      <dependencies>
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-web</artifactId>
          </dependency>
          <!-- Other deps -->
      </dependencies>
  </project>
  ```

- **Testing**: Use Surefire plugin for JUnit; configure for coverage with Jacoco.

## Command Execution Standards

To ensure safe and reliable command execution, follow these guardrails:

### Timeout Protection
- **Default Timeout**: All Maven/shell commands use 2-minute timeouts by default to prevent indefinite hangs
- **Configurable**: Timeout can be adjusted per command when longer execution is expected
- **Purpose**: Prevents hanging processes and maintains responsive development workflow

### Single Execution Policy
- **No Auto-Retry**: Failing commands are not automatically re-executed
- **Investigation First**: Analyze failures through logs, code review, or output analysis before retrying
- **Targeted Fixes**: Propose specific fixes based on failure analysis

### Failure Analysis Approach
- **Exit Code 1 Handling**: Analyze command output thoroughly before any retry attempts
- **Root Cause Focus**: If tests/commands fail repeatedly, prioritize fixing underlying code/logic
- **Diagnostic Tools**: Use logs, debug output, and code inspection for failure diagnosis

### Command Restrictions
- **Server Startup**: Spring Boot can be run via `mvn spring-boot:run` for testing, but not required for core development

## Design Document Maintenance (MANDATORY)

**All design documents must be kept current and synchronized with code:**

- **Documentation Structure**: Maintain organized docs in `docs/` with clear folder structure (design/, guidelines/, BDD/)
- **Version Synchronization**: Update class diagrams, sequence diagrams, and architectural docs when code changes
- **Review Process**: Peer review required for all design document modifications
- **Change Tracking**: Document rationale for design changes and alternatives considered
- **Accessibility**: Ensure docs are clear, well-structured, and discoverable

**Commits that modify code without updating related design documents are not permitted.**

## Verification and Testing Processes

Incorporate checks at milestones to catch issues proactively.

- **Periodic Build Review**: Run `mvn compile` after configuration changes, before commits, and at each incremental stage end. Inspect for compilation errors and resolve all.
- **Development Workflow**:
  1. Update design documents for planned changes
  2. `mvn clean compile` for dependencies.
  3. `mvn test` for unit/integration tests including Cucumber BDD tests.
  4. `mvn test -Dtest=CucumberTestRunner` to specifically verify business requirements.
  5. Update design documents to reflect actual implementation.
  6. Logs/console checks for runtime errors.
- **IDE Tips**: Use IntelliJ's Maven tool window; enable annotation processing.
- **Pre-Commit Requirements**: All changes must pass: compilation, unit tests, integration tests, and Cucumber BDD tests.

## Common Pitfalls and Resolutions

- **Dependency Conflicts**: Check `mvn dependency:tree` for conflicts; use `<exclusions>` if needed.
- **Lombok Issues**: Ensure plugin is installed; restart IDE.
- **Annotation Processing**: Verify in Maven settings.
- **Logging Levels**: Set to DEBUG in dev profiles.

## Development Environment Setup

### Git Configuration
Always ensure proper `.gitignore` configuration to prevent committing system files and build artifacts:

- **macOS Users**: `.DS_Store` files are automatically ignored
- **IDE Files**: `.idea/` directories are ignored
- **Build Artifacts**: `target/`, `*.log` files are ignored
- **Environment and Secret Files**: `.env*`, `.npmrc`, `config/secrets.json`, `config/*.key` files are ignored for security to prevent exposing tokens or credentials    
**Before initial commit**: Verify `.gitignore` exists and contains appropriate exclusions.

## Mandatory Pre-Commit Checklist

**NO CODE CHANGES MAY BE COMMITTED WITHOUT COMPLETING THIS CHECKLIST:**

### Design Documentation
- [ ] Design documents updated to reflect planned changes
- [ ] Class diagrams, sequence diagrams synchronized with code
- [ ] Peer review completed for design document changes
- [ ] Documentation committed alongside code changes

### Code Quality
- [ ] `mvn clean compile` succeeds without errors
- [ ] Code follows established coding standards
- [ ] Inline comments explain complex logic
- [ ] No TODO/FIXME comments left unresolved

### Testing Requirements
- [ ] `mvn test` passes all unit and integration tests (80%+ coverage)
- [ ] `mvn test -Dtest=CucumberTestRunner` passes all Cucumber BDD tests
- [ ] New business functionality has corresponding Cucumber tests
- [ ] Existing tests still pass (no regressions)

### Verification Steps
- [ ] Manual testing of core functionality completed
- [ ] Logs reviewed for errors or warnings
- [ ] Build artifacts cleaned (`mvn clean`)
- [ ] Commit message clearly describes changes and links to updated docs

**FAILURE TO COMPLETE ANY CHECKLIST ITEM PREVENTS COMMITTING**

Review this document periodically as the project evolves. Non-compliance requires justification in pull requests.