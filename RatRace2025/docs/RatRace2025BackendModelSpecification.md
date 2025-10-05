# RatRace2025 Backend Model Design Specification

### Requirements Analysis

To incorporate the request for improvements, command-line output, detailed design, and phased development, we'll refine requirements using an agile-inspired elicitation process: (1) Elicit new elements as user stories from the query (e.g., text-based model visualization), (2) Prioritize based on dependencies (e.g., core model before outputs), (3) Integrate with prior stories like dynamic creation, ensuring the "clockwork" scenario determinism. Analogous to Java's Factory pattern, the detailed design acts as a central creator producing blueprints, while phased steps are like Builder pattern increments for step-by-step assembly.

#### Updated Key User Stories (Prioritized)
1. **High Priority: Detailed Design Compilation**
   - As a developer, I want a comprehensive design document compiling all elements (classes, diagrams, practices) to guide implementation.

2. **High Priority: Incremental Development Phases**
   - As a developer, I want phased steps for building the system incrementally, suitable for AI-assisted coding (e.g., Cursor/Grok), starting with core model and layering features.

3. **Medium Priority: Pre-UI Command Line Dump**
   - As a modeler, I want a text-based output (e.g., ASCII art or structured print) of the model state, flows, and Sankey-like representation for debugging before UI integration.

4. **Low Priority: Suggested Improvements**
   - As a modeler, I want enhancements like error resilience and performance optimizations to evolve the design.

**Prioritization Rationale**: Design and phases are foundational for coding; dump builds on the model; improvements enhance post-core.

#### Updated Use Cases
- **Primary Use Case: Generate and Dump Model**
  1. Run simulation from scenario.
  2. Output command-line dump (e.g., periods, entities, flows in text form).
- **Edge Case: Dynamic Dump**
  - Post-creation event: Dump shows new entities appearing mid-timeline.

### Suggested Improvements and Changes

- **Error Resilience**: Add try-catch in Simulator.playOut() for trigger failures (e.g., insufficient funds), logging via SLF4J and continuing with fallback states—like Java's Optional for graceful degradation in enterprise apps.
- **Performance Optimization**: For large timelines (e.g., 100+ periods), use caching in PeriodEntityAggregate (e.g., Memoization pattern via Guava Cache) for repeated queries; parallelize non-dependent event processing with Java's ForkJoinPool.
- **Auditability**: Introduce an AuditLog class (Observer pattern: subscribes to events/creations) to record all mutations/flows as a list, exportable to JSON/CSV for compliance—analogous to Java's logging interceptors in microservices.
- **Modularity**: Refactor Scenario into microservices-like modules (e.g., EntityModule, EventModule) using interfaces, enabling future swaps (e.g., DB-backed Scenario via Spring).
- **Testing Enhancements**: Add integration tests for dumps (e.g., assert dump contains "Mortgage created at 2028"); use JUnit's @ParameterizedTest for scenario variants.
- **Potential Changes**: Simplify latentEvents to a Rule-based system (e.g., integrate JBoss Drools for complex conditions) if lambdas insufficient; add versioning to FinanceModel (e.g., schemaVersion field) for evolving JSON schemas.

### Pre-UI Command Line Dump

To enable quick prototyping without a UI, implement a text-based dump in FinanceModel via a dumpToConsole() method. This outputs a structured representation mimicking Sankey: periods as "columns," entities/aggregates as nodes, flows as arrows with amounts. Use ASCII art for visualization, or tabular format for parsability. Analogous to Java's toString() overrides in POJOs for debugging.

#### Implementation Outline
- **Method**: void dumpToConsole() in FinanceModel.
- **Logic**:
  1. Print header: "Finance Model Dump - Timeline from [start] to [end]".
  2. For each TimePeriod (sorted):
     - Print "Period: [start]-[end] | RiskFreeRate: [value] | Inflation: [value]".
     - List entities: For each, print PeriodEntityAggregate: "Entity [id]: Final Balance [balance], Rate [rate]".
     - List aggregated flows: "Intra Flows: [net in/out sums] | Inter Flows: [source -> target: amount]".
  3. Sankey-like ASCII: Columns per period, nodes as boxes, flows as --> with labels (e.g., using StringBuilder for alignment).
- **Example Output** (for a simple debt-pension scenario):
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
- **Best Practices**: Use System.out.printf for formatting; make configurable (e.g., verbose mode shows full chains). Serialize to file via dumpToFile(String path) for logs.

### Detailed Design

Compiling from prior iterations, this is a comprehensive DDD-based design for an enterprise Java app. Architectural blueprint: MVC with Model as domain (Scenario, Timeline, etc.), Controller as Simulator/FinanceModel orchestrator, View as dump/Sankey exporter (future UI via REST/JSON). Microservices analogy: Scenario as config service, Simulator as execution engine. Use patterns: Factory for entities, Prototype for templates, Observer for triggers, Strategy for processors, Builder for scenarios.

#### Class Diagram (UML-like Text)
- **Entity** (Core domain object; Factory-created).
  - Attributes: String id, String name, Map<String, Object> baseProperties, boolean isTemplate.
  - Methods: Entity cloneAsNew(), createInitialVersion(Date date) → EntityVersion.

- **EntityVersion** (Immutable snapshot).
  - Attributes: Entity parent, Date date, int sequence, double balance, double rate, Map<String, Object> attributes, EntityVersion previous.
  - Methods: EntityVersion applyEvent(Event).

- **Flow** (Connector).
  - Attributes: String id, EntityVersion source, EntityVersion target, double amount, String direction, String type, Map<String, Object> metadata, boolean isIntraPeriod.
  - Methods: void validate().

- **Event** (Command for changes; subtypes: RecurringEvent, ConditionalEvent, CalculationEvent).
  - Attributes: String id, String type, Map<String, Object> params, boolean isRecurring, Date triggerDate, Predicate<EntityVersion> condition.
  - Methods: EntityVersion apply(EntityVersion from), List<Flow> generateFlows(), List<Entity> createEntities().

- **EventProcessor** (Strategy interface).
  - Methods: EntityVersion process(Event, EntityVersion), List<Flow> handleFlows(Event), List<Entity> handleCreation(Event).

- **TimePeriod** (Aggregate).
  - Attributes: Date start, Date end, double riskFreeRate, double inflation, Map<Entity, List<EntityVersion>> versionChains, List<Event> events.
  - Methods: void addEvent(Event), EntityVersion getFinalVersion(Entity), List<Flow> getAggregatedFlows(Entity), PeriodEntityAggregate getPeriodEntityAggregate(Entity).

- **PeriodEntityAggregate** (DTO record).
  - Attributes: EntityVersion finalVersion, List<Flow> netIntraFlows, List<Flow> interFlows.
  - Methods: double getNetBalance(), Map<String, Object> toSankeyNode().

- **Timeline** (Sequence manager).
  - Attributes: List<TimePeriod> periods, List<Event> pendingEvents, Observer triggerObserver, Simulator simulator.
  - Methods: void addPeriod(TimePeriod), void triggerEvents(Date atDate), void advancePeriod().

- **Scenario** (Config aggregate).
  - Attributes: List<Entity> initialEntities, Map<Entity, List<Event>> eventTemplates, Map<String, Entity> entityTemplates, List<Event> latentEvents, int numPeriods, ExternalSource[] externals.
  - Methods: void initialize(Timeline), void registerLatentEvent(Event), Entity getTemplate(String).

- **Simulator** (Runtime engine).
  - Attributes: Scenario scenario, EventProcessor processor.
  - Methods: void playOut() (loops periods, checks triggers, processes events/creations, generates aggregates).

- **FinanceModel** (Top-level facade).
  - Attributes: Scenario scenario, Timeline timeline, Set<Entity> dynamicEntities.
  - Methods: void loadFromJson(String file), void saveToJson(String file), void runSimulation(), Map<String, Object> buildSankeyData(), void dumpToConsole(), void addDynamicEntity(Entity).

#### Implementation Best Practices
1. **Tech Stack**: Java 21+, Jackson for JSON, Lombok for boilers (e.g., @Data, @Builder), SLF4J for logging, JUnit 5 for tests.
2. **JSON Handling**: Use ObjectMapper; custom serializers for Predicates (e.g., as JSON strings, reconstruct via ScriptEngine).
3. **Invariants**: Use javax.validation for constraints (e.g., @NotNull on ids).
4. **Thread-Safety**: Synchronize Timeline advances; use immutable records where possible.
5. **Extensibility**: Interfaces for all (e.g., IEventProcessor); Spring-ready (e.g., @Component for DI).
6. **Error Handling**: Custom exceptions (e.g., TriggerFailedException); transactional rolls in playOut().

### Incremental Development Steps

These phases are designed for AI-assisted coding (e.g., feed into Cursor/Grok one-by-one). Each builds a compilable increment, starting with core POJOs. Assume Maven project setup (pom.xml with dependencies: jackson-databind, lombok, slf4j, junit-jupiter). Use package com.finmodel; for organization.

1. **Phase 1: Core Domain Classes (Entities, Versions, Flows)**
   - Create Entity, EntityVersion, Flow classes with attributes/methods.
   - Add Lombok annotations (@Data, @Builder).
   - Implement basic methods (e.g., cloneAsNew() using Builder).
   - Test: JUnit for creation/validation.
   - Output: Compilable POJOs; console test print.

2. **Phase 2: Event and Processor (Base Dynamics)**
   - Add Event class and subtypes (RecurringEvent etc.) with lambdas/Predicates.
   - Create EventProcessor interface and a DefaultEventProcessor impl.
   - Implement apply(), generateFlows(), createEntities().
   - Test: Simulate simple event on version, assert new version/flows.

3. **Phase 3: Aggregates and Periods (Grouping)**
   - Add TimePeriod with maps/lists.
   - Implement addEvent(), getFinalVersion(), getAggregatedFlows().
   - Add PeriodEntityAggregate as record.
   - Test: Chain events in period, assert aggregate nets.

4. **Phase 4: Timeline and Scenario (Config and Sequence)**
   - Add Timeline with periods, observer (use java.util.Observable).
   - Add Scenario with initials/templates/latents.
   - Implement initialize(), registerLatentEvent().
   - Test: Setup simple scenario, assert initial periods.

5. **Phase 5: Simulator and Playback (Dynamics)**
   - Add Simulator with playOut().
   - Integrate triggers, creations (clone templates, add dynamics).
   - Handle externals, caps, optimizations.
   - Test: Run multi-period sim, assert dynamic entities appear.

6. **Phase 6: FinanceModel Facade (Orchestration)**
   - Add FinanceModel with scenario/timeline.
   - Implement runSimulation(), addDynamicEntity().
   - Test: End-to-end sim from scenario.

7. **Phase 7: JSON Serialization (Prototyping)**
   - Add Jackson annotations (@JsonProperty, @JsonTypeInfo for subtypes).
   - Implement loadFromJson(), saveToJson() using ObjectMapper.
   - Handle Predicates (serialize as strings, eval via javax.script).
   - Test: Save/load scenario, assert equality.

8. **Phase 8: Outputs (Dump and Sankey)**
   - Implement dumpToConsole() with ASCII/formatting.
   - Add buildSankeyData() returning Map (e.g., {"nodes": [...], "links": [...]}).
   - Test: Run sim, assert dump matches expected text.

9. **Phase 9: Improvements and Polish**
   - Add error handling, caching (Guava), audit log.
   - Refactor for modularity (interfaces).
   - Add main() entry: Load JSON, run sim, dump.
   - Full tests: Coverage >80%.

10. **Phase 10: Extensibility and Refinements**
    - Integrate Drools if needed; add Builder for Scenario.
    - Optimize for large data; document API.