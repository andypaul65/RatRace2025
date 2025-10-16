# RatRace2025 - Technical Architecture

**Last Updated**: 2025-10-15

## Non-Technical Perspective

Think of RatRace2025's architecture like a well-designed factory. The factory has different departments (layers) that each handle specific tasks, with clear interfaces between them. Raw materials (financial data) enter one end, get processed through various stages, and emerge as finished products (simulation results and visualizations).

The architecture emphasizes:
- **Modularity**: Each part can be understood and modified independently
- **Extensibility**: New features can be added without breaking existing functionality
- **Testability**: Each component can be tested in isolation
- **Maintainability**: Clear separation of concerns makes the codebase easier to understand and modify

## MVP Framework Integration

RatRace2025 is implemented as a subproject within the MVP (Model-View-Presenter) backplane framework. This provides standardized message passing, namespace isolation, and modular integration capabilities.

### MVP Architecture Pattern

```plantuml
@startuml MVP Integration
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "MVP Backplane" as MVP {
    rectangle "ServiceRegistry" as SR
    rectangle "Message Bus" as MB
    rectangle "Namespace Manager" as NM
}

rectangle "RatRace2025 Subproject" as RR {
    rectangle "FinanceModel" as FM
    rectangle "Simulator" as SIM
    rectangle "Domain Layer" as DL
}

rectangle "Client Application" as Client {
    rectangle "TabbedInterface" as TI
    rectangle "RatRaceFinanceTab" as RFT
}

Client --> MVP : WebSocket/Message API
MVP --> RR : Service Calls
RR --> DL : Domain Operations
FM --> SR : Registration

note right of MVP
    Centralized message routing
    and service discovery
end note

note right of RR
    MVP-compliant service
    with domain logic integration
end note
@enduml
```

### MVP Message Contracts

The system uses standardized message contracts defined in `api-contracts.json`:

- **load_scenario**: Load financial scenario configuration
- **run_simulation**: Execute simulation with loaded scenario
- **get_dump**: Generate console dump of simulation state
- **get_sankey**: Produce Sankey diagram data for visualization

All messages include namespace isolation and proper error handling.

## Technical Architecture Overview

RatRace2025 follows Domain-Driven Design (DDD) principles with a layered architecture, implemented in Java using Spring Boot patterns and integrated with the MVP backplane framework.

### System Layers

```plantuml
@startuml Architecture Layers
!theme plain
skinparam backgroundColor #FEFEFE
skinparam rectangle {
    BackgroundColor #E3F2FD
    BorderColor #1976D2
    BorderThickness 2
}

rectangle "MVP Backplane\n(Message Routing)" as MVP
rectangle "Client Layer\n(React/TypeScript)" as CL
rectangle "Presentation Layer\n(MVP Services)" as PL
rectangle "Application Layer\n(Use Cases/Services)" as AL
rectangle "Domain Layer\n(Business Logic)" as DL
rectangle "Infrastructure Layer\n(Persistence/External)" as IL

CL --> MVP : WebSocket/Messages
MVP --> PL : Service Calls
PL --> AL : Domain Objects
AL --> DL : Business Logic
DL --> IL : Repository Interfaces
IL --> DL : Data Access
@enduml
```

The MVP backplane provides the communication layer between client and server, enabling modular subproject architecture with standardized message contracts and namespace isolation.

### Core Components

```plantuml
@startuml Core Components
!theme plain
skinparam backgroundColor #FEFEFE
skinparam packageStyle rect

  package "com.finmodel" {
  package "service" as Service {
    class FinanceModel
    class Simulator
    class EventProcessor
  }

  package "domain" as Domain {
    class Entity
    class EntityVersion
    class Flow
    class Event
    class TimePeriod
  }

  package "service" as Service {
    class FinanceModel
    class Simulator
    class EventProcessor
  }

  package "config" as Config {
    class Scenario
    class Timeline
    class AssetGroup
  }
}

package "client" {
  package "src" as ClientSrc {
    class App
    class RatRaceFinanceTab
    class TabbedInterface
  }
}

MVP --> Service : integrates with
Domain --> Service : used by
Service --> Config : configured by
ClientSrc --> MVP : communicates via
@enduml
```

The service layer (`FinanceModel`, `Simulator`, `EventProcessor`) implements the core financial modeling logic, while the client layer provides the user interface through standardized MVP components.

## Domain Layer Deep Dive

The domain layer contains the core business logic and rules that govern financial modeling.

### Entity Lifecycle

```plantuml
@startuml Entity Lifecycle
!theme plain
skinparam backgroundColor #FEFEFE
skinparam State {
    BackgroundColor #E3F2FD
    BorderColor #1976D2
}

[*] --> Template : createTemplate()
Template --> Instance : cloneAsNew()
Instance --> Version1 : createInitialVersion()
Version1 --> Version2 : applyEvent()
Version2 --> VersionN : applyEvent()

Version1 : balance = initialValue
Version2 : balance += flows
VersionN : final balance
@enduml
```

### Flow Generation Process

```plantuml
@startuml Flow Generation
!theme plain
skinparam backgroundColor #FEFEFE

start
:Event Triggered
(e.g., "pay mortgage");

if (Event Type?) then (Recurring)
  :Calculate amount
  based on schedule
else (Conditional)
  :Evaluate condition
  on entity state
endif

:Generate Flow object
with source/target/amount;

:Validate Flow
(amount > 0, valid entities);

:Update Entity Versions
with new balances;

end
@enduml
```

## Service Layer Architecture

### Simulation Engine

```plantuml
@startuml Simulation Engine
!theme plain
skinparam backgroundColor #FEFEFE
skinparam sequenceParticipant underline

participant "FinanceModel" as FM
participant "Simulator" as Sim
participant "Timeline" as TL
participant "TimePeriod" as TP
participant "EventProcessor" as EP

FM -> Sim: playOut()
Sim -> TL: getPeriods()
loop for each period
    Sim -> TP: processPeriod()
    TP -> EP: processEvents()
    EP -> TP: updateVersions()
    EP -> TP: generateFlows()
end
Sim -> FM: return results
@enduml
```

### Key Design Patterns

```plantuml
@startuml Design Patterns
!theme plain
skinparam backgroundColor #FEFEFE

class "Factory Pattern" as FP {
  +createEntity()
  +createEvent()
  +createFlow()
}

class "Strategy Pattern" as SP {
  +EventProcessor
  +process(Event, EntityVersion)
}

class "Observer Pattern" as OP {
  +Timeline
  +triggerEvents(Date)
}

class "Builder Pattern" as BP {
  +Scenario.Builder
  +AssetGroup.Builder
}

class "Aggregate Pattern" as AP {
  +TimePeriod
  +PeriodEntityAggregate
}

FP --> Entity : creates
SP --> EventProcessor : implements
OP --> Timeline : implements
BP --> Scenario : uses
AP --> TimePeriod : contains
@enduml
```

## Data Structures and Relationships

### Core Domain Objects

```plantuml
@startuml Domain Objects
!theme plain
skinparam backgroundColor #FEFEFE

class Entity {
  -String id
  -String name
  -String primaryCategory
  -String detailedCategory
  -double initialValue
  -Map<String,Object> baseProperties
  -boolean isTemplate
  --
  +Entity cloneAsNew()
  +EntityVersion createInitialVersion(Date)
}

class EntityVersion {
  -Entity parent
  -Date date
  -int sequence
  -double balance
  -double rate
  -Map<String,Object> attributes
  -EntityVersion previous
  --
  +EntityVersion applyEvent(Event)
}

class Flow {
  -String id
  -EntityVersion source
  -EntityVersion target
  -double amount
  -String direction
  -String type
  -Map<String,Object> metadata
  -boolean isIntraPeriod
  --
  +void validate()
  +Map<String,Object> toSankeyLink(String)
}

Entity ||--o{ EntityVersion : evolves to
EntityVersion ||--o{ Flow : participates in
Flow --> EntityVersion : connects
@enduml
```

### Aggregation Structures

```plantuml
@startuml Aggregation Structures
!theme plain
skinparam backgroundColor #FEFEFE

class TimePeriod {
  -Date start
  -Date end
  -double riskFreeRate
  -double inflation
  -Map<Entity, List<EntityVersion>> versionChains
  -List<Event> events
  --
  +void addEvent(Event)
  +EntityVersion getFinalVersion(Entity)
  +List<Flow> getAggregatedFlows(Entity)
  +PeriodEntityAggregate getPeriodEntityAggregate(Entity)
}

class PeriodEntityAggregate {
  -EntityVersion finalVersion
  -List<Flow> netIntraFlows
  -List<Flow> interFlows
  --
  +double getNetBalance()
  +Map<String,Object> toSankeyNode(String)
}

class AssetGroup {
  -String name
  -List<Entity> assets
  -List<Entity> liabilities
  -double initialFunds
  -List<AssetGroup> subGroups
  --
  +double getTotalAssetValue()
  +double getNetWorth()
  +Map<String,Object> toSankeyNode(double, String)
}

TimePeriod --> PeriodEntityAggregate : creates
AssetGroup --> Entity : groups
AssetGroup --> AssetGroup : contains
@enduml
```

## Configuration and Setup

### Scenario Configuration

```plantuml
@startuml Scenario Configuration
!theme plain
skinparam backgroundColor #FEFEFE

class Scenario {
  -List<Entity> initialEntities
  -Map<Entity, List<Event>> eventTemplates
  -Map<String, Entity> entityTemplates
  -List<Event> latentEvents
  -int numPeriods
  -List<Object> externals
  -List<AssetGroup> assetGroups
  --
  +void initialize(Timeline)
  +void registerLatentEvent(Event)
  +Entity getTemplate(String)
}

class Timeline {
  -List<TimePeriod> periods
  -List<Event> pendingEvents
  -Simulator simulator
  --
  +void addPeriod(TimePeriod)
  +void triggerEvents(Date)
  +void advancePeriod()
}

Scenario --> Entity : contains
Scenario --> Timeline : initializes
Timeline --> TimePeriod : manages
@enduml
```

## Error Handling and Validation

### Validation Strategy

```plantuml
@startuml Validation Strategy
!theme plain
skinparam backgroundColor #FEFEFE

class ValidationException
class IllegalArgumentException

class Flow {
  +void validate()
}

class Entity {
  +void validateState()
}

class Event {
  +void validateParameters()
}

Flow --> ValidationException : throws
Entity --> IllegalArgumentException : throws
Event --> IllegalArgumentException : throws
@enduml
```

### Error Recovery Patterns

```plantuml
@startuml Error Recovery
!theme plain
skinparam backgroundColor #FEFEFE
skinparam State {
    BackgroundColor #FFF3E0
    BorderColor #EF6C00
}

[*] --> Processing : Event triggered
Processing --> ValidationError : Invalid data
Processing --> ProcessingError : Runtime failure
Processing --> Success : Valid processing

ValidationError --> [*] : Skip event
ProcessingError --> Fallback : Use defaults
Fallback --> Success : Continue simulation
@enduml
```

## Testing Strategy

### Test Pyramid

```plantuml
@startuml Test Pyramid
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Unit Tests\n(80% coverage)" as Unit {
  rectangle "Entity Tests" as ET
  rectangle "Flow Tests" as FT
  rectangle "Event Tests" as EVT
}

rectangle "Integration Tests\n(Service layer)" as Integration {
  rectangle "Simulator Tests" as ST
  rectangle "FinanceModel Tests" as FMT
}

rectangle "End-to-End Tests\n(Full scenarios)" as E2E {
  rectangle "Scenario Tests" as SNT
  rectangle "Sankey Export Tests" as SET
}

Unit --> Integration : supports
Integration --> E2E : supports
@enduml
```

## Performance Considerations

### Caching Strategy

```plantuml
@startuml Caching Strategy
!theme plain
skinparam backgroundColor #FEFEFE

class TimePeriod {
  -Cache<Entity, PeriodEntityAggregate> aggregateCache
  --
  +PeriodEntityAggregate getPeriodEntityAggregate(Entity)
}

class Guava {
  +Cache<Entity, Object> newBuilder()
}

TimePeriod --> Guava : uses
@enduml
```

### Optimization Techniques

- **Lazy Evaluation**: PeriodEntityAggregate created on demand
- **Caching**: Expensive calculations cached per period
- **Streaming**: Large datasets processed with Java streams
- **Immutable Objects**: EntityVersion prevents accidental mutation

## Client Architecture

The client is built with React and TypeScript, integrated with the MVP backplane framework for modular UI development.

### Client Component Structure

```plantuml
@startuml Client Architecture
!theme plain
skinparam backgroundColor #FEFEFE

package "client/src" {
  class App {
    +render()
  }

  class RatRaceFinanceTab {
    -namespace: string
    -scenarioJson: string
    -status: string
    +loadScenario()
    +runSimulation()
    +getDump()
    +getSankey()
  }

  interface TabbedInterface {
    +tabs: TabConfig[]
  }
}

package "@ajp/mvp-client" {
  class TabbedInterface
  class useMvpClient
  interface TabConfig {
    +namespace: string
    +title: string
    +component: React.Component
  }
}

App --> TabbedInterface : uses
RatRaceFinanceTab --> useMvpClient : communicates
TabbedInterface --> RatRaceFinanceTab : renders
@enduml
```

### Client-Server Communication

The client communicates with the server exclusively through the MVP backplane:

1. **Message Sending**: Uses `useMvpClient` hook to send typed messages
2. **Namespace Isolation**: Each tab operates in its own namespace
3. **Real-time Updates**: WebSocket-based communication for live status
4. **Error Handling**: Standardized error responses with user feedback

### Build and Development

- **Vite**: Fast development server and optimized production builds
- **TypeScript**: Type-safe development with MVP client library integration
- **Proxy Configuration**: Development server proxies API calls to backend

## Extensibility Points

### Plugin Architecture

```plantuml
@startuml Extensibility
!theme plain
skinparam backgroundColor #FEFEFE

interface EventProcessor {
  +EntityVersion process(Event, EntityVersion)
  +List<Flow> handleFlows(Event)
  +List<Entity> handleCreation(Event)
}

interface IExternalSource {
  +Map<String, Object> fetchData(String key)
}

class DefaultEventProcessor
class CustomEventProcessor
class MarketDataSource
class EconomicIndicatorSource

DefaultEventProcessor ..|> EventProcessor
CustomEventProcessor ..|> EventProcessor
MarketDataSource ..|> IExternalSource
EconomicIndicatorSource ..|> IExternalSource
@enduml
```

## Deployment and Packaging

### Build and Dependencies

```plantuml
@startuml Build Dependencies
!theme plain
skinparam backgroundColor #FEFEFE

package "Maven Dependencies" {
  rectangle "Spring Boot" as SB
  rectangle "Jackson" as J
  rectangle "Lombok" as L
  rectangle "JUnit 5" as J5
  rectangle "Guava" as G
}

package "Core Framework" {
  rectangle "Domain Layer" as DL
  rectangle "Service Layer" as SL
  rectangle "Infrastructure" as INF
}

SB --> DL : runtime
J --> DL : serialization
L --> DL : boilerplate
J5 --> DL : testing
G --> DL : caching
@enduml
```

This architecture provides a solid foundation for financial modeling while maintaining flexibility for future enhancements and UI integrations.

## Performance Considerations

### Caching Strategy
- **Guava Cache**: Used for expensive `PeriodEntityAggregate` calculations to avoid redundant computations.
- **Cache Configuration**: Maximum size limits and LRU eviction for memory efficiency.

### Parallel Processing
- **ForkJoinPool**: Employed for processing independent entities/events in large simulations.
- **Thread Safety**: Immutable objects and synchronized critical sections prevent race conditions.

### Scalability
- **Streaming APIs**: Java streams for efficient processing of large datasets.
- **Lazy Evaluation**: Components created on-demand to reduce memory footprint.

## See Also
- **[backend-development-guide.md](backend-development-guide.md)**: Incremental development phases
- **[entities-and-flows.md](entities-and-flows.md)**: Domain object relationships
- **[simulation-engine.md](simulation-engine.md)**: Runtime processing details
- **[ui-visualization.md](../ui-visualization.md)**: Data structure integration