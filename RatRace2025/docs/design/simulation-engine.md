# Simulation Engine - How Financial Scenarios Run

## Non-Technical Perspective

Imagine you're running a complex board game where every move affects multiple pieces on the board. The RatRace2025 simulation engine is like the game master who:

1. **Sets up the board** - Creates all your financial accounts and starting balances
2. **Follows the rules** - Applies financial events like paychecks, bills, and investment growth
3. **Tracks everything** - Records how money moves between accounts over time
4. **Shows the results** - Provides a complete picture of your financial evolution

The simulation runs through time periods (like months or years), processing events that change your financial state, just like a game master moves pieces and updates scores.

### Real-World Analogy

Think of a flight simulator:
- **The aircraft** represents your financial portfolio
- **Weather and air traffic** are market conditions and economic events
- **Flight controls** are your financial decisions and automatic events
- **Flight path** shows how your finances evolve over time
- **Instruments and displays** provide visibility into balances and flows

Just as pilots train in simulators to handle different scenarios, you can use RatRace2025 to test different financial strategies before implementing them.

## Simulation Architecture

### Core Components

```plantuml
@startuml Simulation Components
!theme plain
skinparam backgroundColor #FEFEFE

package "Simulation Engine" as SE {
  class FinanceModel {
    +runSimulation()
    +Map<String,Object> buildSankeyData()
  }

  class Simulator {
    +playOut()
    -Scenario scenario
    -EventProcessor processor
  }

  class Timeline {
    +addPeriod(TimePeriod)
    +triggerEvents(Date)
    -List<TimePeriod> periods
  }
}

package "Domain Processing" as DP {
  class TimePeriod {
    +addEvent(Event)
    +getPeriodEntityAggregate(Entity)
    -Map<Entity, List<EntityVersion>> versionChains
  }

  class EventProcessor {
    +process(Event, EntityVersion)
    +handleFlows(Event)
    +handleCreation(Event)
  }
}

FinanceModel --> Simulator : orchestrates
Simulator --> Timeline : uses
Timeline --> TimePeriod : contains
TimePeriod --> EventProcessor : delegates to
@enduml
```

### Simulation Flow Overview

```plantuml
@startuml Simulation Flow
!theme plain
skinparam backgroundColor #FEFEFE
skinparam sequenceParticipant underline

actor User
participant "FinanceModel" as FM
participant "Simulator" as Sim
participant "Timeline" as TL
participant "TimePeriod" as TP
participant "EventProcessor" as EP
participant "EntityVersion" as EV

User -> FM: runSimulation()
FM -> TL: initializePeriods()
FM -> Sim: playOut()

loop for each period
    Sim -> TP: processPeriod()
    TP -> EP: processEvents()
    EP -> EV: updateVersions()
    EP -> TP: recordFlows()
end

Sim -> FM: simulation complete
FM -> User: results available
@enduml
```

## Detailed Simulation Process

### Phase 1: Initialization

```plantuml
@startuml Initialization Phase
!theme plain
skinparam backgroundColor #FEFEFE

start
:Load Scenario
from JSON/configuration;

:Create Timeline
with numPeriods TimePeriods;

:Initialize Entities
clone templates,
set initial balances;

:Setup Event Templates
recurring and conditional events;

:Prepare Simulator
with scenario and processor;

end
@enduml
```

### Phase 2: Time Period Processing

Each time period goes through a structured processing pipeline:

```plantuml
@startuml Time Period Processing
!theme plain
skinparam backgroundColor #FEFEFE

start
:Enter Time Period
(set start/end dates);

:Initialize Version Chains
for all entities in period;

:Process Pending Events
from previous periods;

:Apply Recurring Events
(salary deposits, bill payments);

:Evaluate Conditional Events
(check thresholds and conditions);

:Handle Dynamic Creations
(new entities from events);

:Calculate Aggregates
final balances and flow summaries;

:Prepare for Next Period
carry forward balances;

end
@enduml
```

### Phase 3: Event Processing

The heart of the simulation is event processing:

```plantuml
@startuml Event Processing Detail
!theme plain
skinparam backgroundColor #FEFEFE

start
:Receive Event
(type, parameters, conditions);

if (Event is recurring?) then (yes)
  :Check schedule
  (monthly, quarterly, etc.)
else (no)
  if (Event is conditional?) then (yes)
    :Evaluate conditions
    (balance thresholds, dates)
  endif
endif

if (Should trigger?) then (yes)
  :Calculate amount
  (fixed, percentage, formula);

  :Validate source has funds
  (if applicable);

  :Create Flow object
  (source, target, amount, type);

  :Update entity versions
  (source balance -= amount,
   target balance += amount);

  :Record flow for reporting
  and visualization;
endif

end
@enduml
```

## Event Types and Processing

### Recurring Events

```plantuml
@startuml Recurring Events
!theme plain
skinparam backgroundColor #FEFEFE

class RecurringEvent {
  -String frequency : "MONTHLY"
  -double amount : 5000.00
  -Date startDate
  -Date endDate
  -Entity source
  -Entity target
  --
  +boolean shouldTrigger(Date)
  +double calculateAmount()
}

note right of RecurringEvent
  Examples:
  - Monthly salary deposits
  - Quarterly investment contributions
  - Annual insurance premiums
  - Weekly expense payments
end note
@enduml
```

### Conditional Events

```plantuml
@startuml Conditional Events
!theme plain
skinparam backgroundColor #FEFEFE

class ConditionalEvent {
  -Predicate<EntityVersion> condition
  -Function<EntityVersion, Double> amountCalculator
  -String triggerType : "THRESHOLD"
  -double thresholdValue : 1000.00
  --
  +boolean evaluateCondition(EntityVersion)
  +double calculateAmount(EntityVersion)
}

note right of ConditionalEvent
  Examples:
  - Replenish emergency fund when below threshold
  - Pay extra on debt when surplus cash available
  - Rebalance portfolio when allocations drift
  - Trigger tax loss harvesting when losses available
end note
@enduml
```

### Calculation Events

```plantuml
@startuml Calculation Events
!theme plain
skinparam backgroundColor #FEFEFE

class CalculationEvent {
  -String calculationType : "INTEREST"
  -Function<EntityVersion, Double> calculator
  -boolean affectsBalance : true
  --
  +double performCalculation(EntityVersion)
  +EntityVersion applyResult(EntityVersion, double)
}

note right of CalculationEvent
  Examples:
  - Interest accrual on savings accounts
  - Mortgage interest calculation
  - Investment return calculations
  - Fee assessments and penalties
end note
@enduml
```

## State Management and Versioning

### Entity Version Chain

```plantuml
@startuml Version Chain Management
!theme plain
skinparam backgroundColor #FEFEFE

class EntityVersion {
  -Entity parent
  -Date date
  -int sequence : 0, 1, 2...
  -double balance
  -double rate
  -Map<String,Object> attributes
  -EntityVersion previous
  --
  +EntityVersion applyEvent(Event)
  +EntityVersion createNext(Date)
}

note right of EntityVersion
  Each version represents
  entity state at a point
  in time during simulation
end note

Entity --> EntityVersion : creates initial
EntityVersion --> EntityVersion : chains to next
@enduml
```

### Period State Aggregation

```plantuml
@startuml Period State Aggregation
!theme plain
skinparam backgroundColor #FEFEFE

class TimePeriod {
  -Date start
  -Date end
  -Map<Entity, List<EntityVersion>> versionChains
  -List<Event> events
  -List<Flow> allFlows
  --
  +EntityVersion getFinalVersion(Entity)
  +List<Flow> getIntraFlows(Entity)
  +List<Flow> getInterFlows(Entity)
  +PeriodEntityAggregate getPeriodEntityAggregate(Entity)
}

note right of TimePeriod
  Aggregates all changes
  within a time period
  for reporting and analysis
end note
@enduml
```

## Flow Generation and Tracking

### Flow Creation Process

```plantuml
@startuml Flow Creation
!theme plain
skinparam backgroundColor #FEFEFE

start
:Event Processing
determines need for flow;

:Identify Source Entity
(current version with sufficient balance);

:Identify Target Entity
(where funds should move);

:Calculate Flow Amount
(based on event parameters);

:Create Flow Object
with all required metadata;

:Validate Flow
(business rules and constraints);

:Update Entity Balances
(source -= amount, target += amount);

:Record Flow
in period and global collections;

:Create Audit Trail
for compliance and debugging;

end
@enduml
```

### Flow Classification

```plantuml
@startuml Flow Classification
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Intra-Period Flows" as Intra {
  rectangle "Same Period Transfers" as SamePeriod {
    rectangle "Account transfers"
    rectangle "Rebalancing moves"
    rectangle "Payment allocations"
  }
}

rectangle "Inter-Period Flows" as Inter {
  rectangle "Balance Carryover" as Carryover {
    rectangle "Savings accumulation"
    rectangle "Debt reduction"
    rectangle "Investment growth"
  }
}

rectangle "Flow Types" as Types {
  rectangle "Income" as Income {
    rectangle "Salary deposits"
    rectangle "Investment returns"
    rectangle "Interest earnings"
  }
  rectangle "Expenses" as Expenses {
    rectangle "Bill payments"
    rectangle "Tax payments"
    rectangle "Fee deductions"
  }
  rectangle "Transfers" as Transfers {
    rectangle "Account movements"
    rectangle "Reinvestments"
    rectangle "Consolidations"
  }
}

Intra --> Types
Inter --> Types
@enduml
```

## Error Handling and Recovery

### Simulation Error Handling

```plantuml
@startuml Error Handling
!theme plain
skinparam backgroundColor #FEFEFE
skinparam State {
    BackgroundColor #FFF3E0
    BorderColor #EF6C00
}

[*] --> Processing : Normal operation
Processing --> ValidationError : Invalid event/amount
Processing --> InsufficientFunds : Source has insufficient balance
Processing --> CalculationError : Formula evaluation fails

ValidationError --> SkipEvent : Log and continue
InsufficientFunds --> [*] : **FAIL SCENARIO** - Business rule violation
CalculationError --> UseFallback : Apply default value

SkipEvent --> Processing : Continue simulation
UseFallback --> Processing : Continue with default

Processing --> [*] : Simulation complete
@enduml
```

### Recovery Strategies

```plantuml
@startuml Recovery Strategies
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Validation Failures" as Validation {
  rectangle "Skip Event" as Skip {
    rectangle "Log warning"
    rectangle "Continue simulation"
    rectangle "Mark event as failed"
  }
  rectangle "Use Defaults" as Defaults {
    rectangle "Apply fallback values"
    rectangle "Log substitution"
    rectangle "Continue processing"
  }
}

rectangle "Calculation Errors" as Calculation {
  rectangle "Reduce Amount" as Reduce {
    rectangle "Use available balance"
    rectangle "Partial processing"
    rectangle "Log reduction"
  }
  rectangle "Defer Processing" as Defer {
    rectangle "Move to next period"
    rectangle "Retry with updated state"
    rectangle "Track deferred items"
  }
}

rectangle "System Failures" as System {
  rectangle "Graceful Degradation" as Degrade {
    rectangle "Disable complex features"
    rectangle "Use simplified models"
    rectangle "Maintain core functionality"
  }
  rectangle "Checkpoint Recovery" as Checkpoint {
    rectangle "Save periodic state"
    rectangle "Resume from last good state"
    rectangle "Log recovery actions"
  }
}
@enduml
```

## Performance Optimization

### Caching Strategy

```plantuml
@startuml Simulation Caching
!theme plain
skinparam backgroundColor #FEFEFE

class TimePeriod {
  -Cache<Entity, PeriodEntityAggregate> aggregateCache
  -Cache<String, List<Flow>> flowCache
  --
  +PeriodEntityAggregate getPeriodEntityAggregate(Entity)
  +List<Flow> getEntityFlows(Entity)
}

class Simulator {
  -Cache<String, EntityVersion> versionCache
  --
  +EntityVersion getLatestVersion(Entity, Date)
}

note right of TimePeriod
  Caches expensive aggregations
  to avoid recalculation
end note

note right of Simulator
  Maintains current entity states
  for quick access during simulation
end note
@enduml
```

### Parallel Processing

```plantuml
@startuml Parallel Processing
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Independent Entities" as Independent {
  rectangle "Process in Parallel" as Parallel {
    rectangle "Savings Accounts"
    rectangle "Investment Accounts"
    rectangle "Separate Properties"
  }
}

rectangle "Dependent Entities" as Dependent {
  rectangle "Process Sequentially" as Sequential {
    rectangle "Joint Accounts"
    rectangle "Linked Investments"
    rectangle "Interdependent Debts"
  }
}

rectangle "Event Processing" as Events {
  rectangle "Parallel Evaluation" as Eval {
    rectangle "Condition Checks"
    rectangle "Amount Calculations"
    rectangle "Flow Validations"
  }
}

Independent --> Parallel : can be
Dependent --> Sequential : must be
Events --> Eval : can be
@enduml
```

## Testing and Validation

### Simulation Testing Strategy

```plantuml
@startuml Simulation Testing
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Unit Tests" as Unit {
  rectangle "Event Processing" as EP {
    rectangle "Individual events"
    rectangle "Edge cases"
    rectangle "Error conditions"
  }
  rectangle "Flow Generation" as FG {
    rectangle "Amount calculations"
    rectangle "Entity updates"
    rectangle "Validation rules"
  }
}

rectangle "Integration Tests" as Integration {
  rectangle "Single Period" as Single {
    rectangle "Complete period processing"
    rectangle "State transitions"
    rectangle "Aggregate calculations"
  }
  rectangle "Multi-Period" as Multi {
    rectangle "Timeline progression"
    rectangle "Balance carryover"
    rectangle "Trend analysis"
  }
}

rectangle "Scenario Tests" as Scenario {
  rectangle "Real Scenarios" as Real {
    rectangle "Mortgage payoff"
    rectangle "Retirement planning"
    rectangle "Investment growth"
  }
  rectangle "Stress Tests" as Stress {
    rectangle "High frequency events"
    rectangle "Large entity counts"
    rectangle "Complex interdependencies"
  }
}

Unit --> Integration : supports
Integration --> Scenario : validates
@enduml
```

This simulation engine provides a robust, flexible foundation for modeling complex financial scenarios with accurate event processing, state management, and comprehensive error handling.