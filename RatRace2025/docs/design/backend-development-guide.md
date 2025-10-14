# Backend Development Guide - Incremental Implementation

**Last Updated**: 2025-10-14

This guide provides incremental development phases for building the RatRace2025 backend, designed for AI-assisted coding. Each phase builds a compilable increment, starting with core domain objects and layering features.

## Requirements Analysis

To incorporate improvements like command-line output and detailed design, we've refined requirements using agile principles:

### Key User Stories (Prioritized)
1. **High Priority: Detailed Design Compilation**
   - As a developer, I want comprehensive design documents to guide implementation.

2. **High Priority: Incremental Development Phases**
   - As a developer, I want phased steps for building incrementally.

3. **Medium Priority: Pre-UI Command Line Dump**
   - As a modeler, I want text-based output for debugging before UI integration.

4. **Low Priority: Suggested Improvements**
   - As a modeler, I want enhancements like error resilience and performance optimizations.

### Use Cases
- **Primary**: Generate and dump model state via command line.
- **Edge Case**: Dynamic entity creation mid-simulation.

## Suggested Improvements

- **Error Resilience**: Add try-catch in `Simulator.playOut()` for trigger failures, with SLF4J logging.
- **Performance**: Use Guava Cache for `PeriodEntityAggregate`, parallelize event processing.
- **Auditability**: Introduce `AuditLog` class (Observer pattern) for recording mutations.
- **Modularity**: Refactor `Scenario` into modules with interfaces.
- **Testing**: Add integration tests for dumps, use parameterized tests.
- **Changes**: Simplify `latentEvents` if needed, add versioning to `FinanceModel`.

## Pre-UI Command Line Dump

Implement `dumpToConsole()` in `FinanceModel` for text-based debugging:

### Implementation Outline
- **Method**: `void dumpToConsole()` in `FinanceModel`.
- **Logic**:
  1. Print header: "Finance Model Dump - Timeline from [start] to [end]".
  2. For each `TimePeriod`: Print period info, entities, aggregated flows.
  3. ASCII Sankey view with columns and arrows.

### Example Output
```
Finance Model Dump - Timeline: 2025-01-01 to 2028-12-31

Period: 2025-Q1 | RiskFree: 3.5% | Inflation: 2%
Entities:
  - AccountB: Balance $100,000 | Rate 2%
  - Debt: Balance $50,000 | Rate 5%
Aggregated Flows:
  - Intra: Net In $500 (Pension) | Net Out $100 (Payment)
  - Inter: (from prev) AccountB --> Debt: $100

Sankey ASCII View:
[2025-Q1]          [2025-Q2]
AccountB ($100k) --> Pension In ($500) --> Payment Out ($100) --> Debt ($49,900)
                   \--> Carryover ($100,400)
```

**Best Practices**: Use `System.out.printf`, add `dumpToFile(String path)` for serialization.

## Detailed Design

This follows Domain-Driven Design with MVC architecture. See [architecture.md](architecture.md) for full technical details.

### Class Diagram Summary
- **Entity**: Core domain object (Factory pattern).
- **EntityVersion**: Immutable snapshot.
- **Flow**: Money movement connector.
- **Event**: Change trigger (Strategy pattern).
- **EventProcessor**: Processing interface.
- **TimePeriod**: Aggregate with version chains.
- **PeriodEntityAggregate**: DTO for reporting.
- **Timeline**: Sequence manager (Observer pattern).
- **Scenario**: Configuration aggregate (Builder pattern).
- **Simulator**: Runtime engine.
- **FinanceModel**: Top-level facade.

### Implementation Best Practices
1. **Tech Stack**: Java 21+, Jackson for JSON, Lombok, SLF4J, JUnit 5.
2. **JSON Handling**: Custom serializers for complex objects.
3. **Validation**: Use javax.validation annotations.
4. **Thread-Safety**: Synchronize critical sections.
5. **Extensibility**: Interfaces for all components.
6. **Error Handling**: Custom exceptions, transactional boundaries.

## Incremental Development Phases

These phases build compilable increments. Assume Maven setup with required dependencies.

1. **Phase 1: Core Domain Classes**
   - Create `Entity`, `EntityVersion`, `Flow` with Lombok.
   - Implement basic methods (`cloneAsNew()`).
   - Test: JUnit for validation.

2. **Phase 2: Event and Processor**
   - Add `Event` hierarchy with lambdas.
   - Create `EventProcessor` interface and default impl.
   - Implement processing logic.
   - Test: Event application and flow generation.

3. **Phase 3: Aggregates and Periods**
   - Add `TimePeriod` with version chains.
   - Implement aggregation methods.
   - Add `PeriodEntityAggregate`.
   - Test: Flow aggregation accuracy.

4. **Phase 4: Timeline and Scenario**
   - Add `Timeline` with observer pattern.
   - Implement `Scenario` with templates.
   - Test: Period initialization.

5. **Phase 5: Simulator and Playback**
   - Add `Simulator` with `playOut()`.
   - Integrate dynamic creation.
   - Test: Multi-period simulation.

6. **Phase 6: FinanceModel Facade**
   - Add top-level orchestration.
   - Implement simulation control.
   - Test: End-to-end scenarios.

7. **Phase 7: JSON Serialization**
   - Add Jackson annotations.
   - Handle complex types.
   - Test: Save/load functionality.

8. **Phase 8: Outputs**
   - Implement dump and Sankey methods.
   - Test: Output validation.

9. **Phase 9: Improvements**
   - Add caching, audit logging.
   - Enhance error handling.
   - Test: Performance and edge cases.

10. **Phase 10: Extensibility**
    - Add builders, optimize for scale.
    - Document APIs.

## See Also
- **[architecture.md](architecture.md)**: Full technical architecture
- **[entities-and-flows.md](entities-and-flows.md)**: Domain modeling details
- **[simulation-engine.md](simulation-engine.md)**: Simulation mechanics
- **[subproject-coding-standards.md](../guidelines/subproject-coding-standards.md)**: Coding guidelines