# Entities and Flows - Domain Modeling

## Non-Technical Perspective

Imagine you're tracking different aspects of your personal finances. You have bank accounts, investments, loans, and income sources - these are all **entities** in your financial world. The money moving between these entities - like transferring from checking to savings, or paying your mortgage - are **flows**.

RatRace2025 models your entire financial ecosystem as a network of these entities connected by flows, showing how money moves through your financial life over time.

### Real-World Analogy

Think of a city's water system:
- **Reservoirs, pipes, and treatment plants** are like your entities (savings accounts, investment portfolios, income sources)
- **The water flowing through the pipes** represents money flows
- **Valves and pumps** are like events that control when and how much water (money) moves

Just as a city's water system must balance supply and demand, your financial system must balance income and expenses to achieve your goals.

## Entity Modeling

### Entity Types and Categories

RatRace2025 supports a comprehensive taxonomy of financial entities, organized hierarchically:

```plantuml
@startuml Entity Taxonomy
!theme plain
skinparam backgroundColor #FEFEFE
skinparam rectangle {
    BackgroundColor #E8F5E8
    BorderColor #2E7D32
    FontColor #1B5E20
}

rectangle "Assets\n(Value you own)" as Assets {
  rectangle "Liquid Assets" as Liquid {
    rectangle "Cash & Equivalents"
    rectangle "Short-term Investments"
  }
  rectangle "Investment Assets" as Investments {
    rectangle "Stocks & Bonds"
    rectangle "Real Estate"
    rectangle "Retirement Accounts"
  }
  rectangle "Personal Assets" as Personal {
    rectangle "Vehicles"
    rectangle "Collectibles"
    rectangle "Personal Property"
  }
}

rectangle "Liabilities\n(Value you owe)" as Liabilities {
  rectangle "Secured Debt" as Secured {
    rectangle "Mortgages"
    rectangle "Auto Loans"
    rectangle "Home Equity Loans"
  }
  rectangle "Unsecured Debt" as Unsecured {
    rectangle "Credit Cards"
    rectangle "Personal Loans"
    rectangle "Student Loans"
  }
}

rectangle "Income\n(Value flowing in)" as Income {
  rectangle "Employment Income" as Employment {
    rectangle "Salary"
    rectangle "Bonuses"
    rectangle "Commissions"
  }
  rectangle "Investment Income" as InvIncome {
    rectangle "Dividends"
    rectangle "Interest"
    rectangle "Capital Gains"
  }
  rectangle "Other Income" as OtherInc {
    rectangle "Rental Income"
    rectangle "Royalties"
    rectangle "Gifts"
  }
}

rectangle "Expenses\n(Value flowing out)" as Expenses {
  rectangle "Fixed Expenses" as Fixed {
    rectangle "Housing"
    rectangle "Insurance"
    rectangle "Loan Payments"
  }
  rectangle "Variable Expenses" as Variable {
    rectangle "Groceries"
    rectangle "Entertainment"
    rectangle "Transportation"
  }
  rectangle "Taxes" as Taxes {
    rectangle "Income Tax"
    rectangle "Property Tax"
    rectangle "Capital Gains Tax"
  }
}
@enduml
```

### Entity Lifecycle

Every entity in RatRace2025 has a complete lifecycle from creation to evolution:

```plantuml
@startuml Entity Lifecycle Detailed
!theme plain
skinparam backgroundColor #FEFEFE

start
:Entity Template
Defined in Scenario;

:Clone for Instance
cloneAsNew() called;

:Create Initial Version
createInitialVersion(date)
balance = initialValue;

:Apply Events
Events modify balance
and create flows;

:Evolve Through Time
Each period creates
new EntityVersion;

:Generate Final State
End of simulation
with final balances;

end
@enduml
```

### Entity Properties and Metadata

```plantuml
@startuml Entity Properties
!theme plain
skinparam backgroundColor #FEFEFE

class Entity {
  == Core Identity ==
  -String id : "checking_account_001"
  -String name : "Primary Checking"

  == Categorization ==
  -String primaryCategory : "Asset"
  -String detailedCategory : "Cash Equivalent"

  == Financial Attributes ==
  -double initialValue : 5000.00
  -Map<String,Object> baseProperties : {rate: 0.02}

  == Template Support ==
  -boolean isTemplate : false

  == Methods ==
  +Entity cloneAsNew()
  +EntityVersion createInitialVersion(Date)
}
@enduml
```

## Flow Modeling

### Flow Types and Characteristics

Flows represent money movement between entities. RatRace2025 distinguishes between different types of flows:

```plantuml
@startuml Flow Classification
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "By Direction" as Direction {
  rectangle "Inbound\n(Money entering entity)" as Inbound
  rectangle "Outbound\n(Money leaving entity)" as Outbound
}

rectangle "By Timing" as Timing {
  rectangle "Intra-period\n(Within same time period)" as Intra
  rectangle "Inter-period\n(Between time periods)" as Inter
}

rectangle "By Nature" as Nature {
  rectangle "Transaction\n(Direct transfers)" as Transaction
  rectangle "Interest\n(Earnings on balances)" as Interest
  rectangle "Fees\n(Costs/penalties)" as Fees
  rectangle "Dividends\n(Investment returns)" as Dividends
}

Direction --> Transaction
Timing --> Transaction
Nature --> Transaction
@enduml
```

### Flow Lifecycle

```plantuml
@startuml Flow Lifecycle
!theme plain
skinparam backgroundColor #FEFEFE

start
:Event Triggered
(e.g., "process paycheck");

:Validate Conditions
Check entity states
and parameters;

:Calculate Amount
Based on rules
and entity balances;

:Create Flow Object
source, target, amount, type;

:Update Source Entity
balance -= amount;

:Update Target Entity
balance += amount;

:Record Flow
For reporting
and visualization;

end
@enduml
```

### Flow Properties and Metadata

```plantuml
@startuml Flow Properties
!theme plain
skinparam backgroundColor #FEFEFE

class Flow {
  == Identity ==
  -String id : "flow_2025_Q1_001"
  -EntityVersion source : checking_account
  -EntityVersion target : savings_account

  == Financial Data ==
  -double amount : 1000.00
  -String direction : "outbound"

  == Classification ==
  -String type : "transfer"
  -boolean isIntraPeriod : true

  == Metadata ==
  -Map<String,Object> metadata : {description: "Monthly savings"}

  == Methods ==
  +void validate()
  +Map<String,Object> toSankeyLink(String)
}
@enduml
```

## Entity-Version Relationships

### Version Chain Pattern

Each entity maintains a chain of versions representing its state at different points in time:

```plantuml
@startuml Version Chain
!theme plain
skinparam backgroundColor #FEFEFE

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

Entity --> EntityVersion : creates
EntityVersion --> EntityVersion : links to previous
EntityVersion --> Flow : source/target of
@enduml
```

### Time Period Aggregation

Within each time period, entities have aggregated views showing net changes:

```plantuml
@startuml Period Aggregation
!theme plain
skinparam backgroundColor #FEFEFE

class PeriodEntityAggregate {
  -EntityVersion finalVersion
  -List<Flow> netIntraFlows
  -List<Flow> interFlows
  --
  +double getNetBalance()
  +Map<String,Object> toSankeyNode(String)
}

class TimePeriod {
  -Map<Entity, List<EntityVersion>> versionChains
  -List<Event> events
  --
  +PeriodEntityAggregate getPeriodEntityAggregate(Entity)
}

TimePeriod --> PeriodEntityAggregate : creates
PeriodEntityAggregate --> EntityVersion : contains
PeriodEntityAggregate --> Flow : aggregates
@enduml
```

## Grouping and Hierarchy

### Asset Group Organization

Entities can be organized into hierarchical groups for portfolio management:

```plantuml
@startuml Asset Grouping
!theme plain
skinparam backgroundColor #FEFEFE

class AssetGroup {
  -String name : "Investment Portfolio"
  -List<Entity> assets
  -List<Entity> liabilities
  -double initialFunds : 10000.00
  -List<AssetGroup> subGroups
  -String category : "Investment"
  --
  +double getTotalAssetValue()
  +double getNetWorth()
  +List<Entity> getAllEntities()
  +Map<String,Object> toSankeyNode(double, String)
}

AssetGroup --> Entity : contains
AssetGroup --> AssetGroup : nests
@enduml
```

### Hierarchical Navigation

```plantuml
@startuml Hierarchical Navigation
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Portfolio Level" as Portfolio {
  rectangle "Total Assets: $500K" as TotalAssets
  rectangle "Total Liabilities: $300K" as TotalLiab
  rectangle "Net Worth: $200K" as NetWorth
}

rectangle "Asset Class Level" as AssetClass {
  rectangle "Cash: $50K" as Cash
  rectangle "Stocks: $200K" as Stocks
  rectangle "Bonds: $150K" as Bonds
  rectangle "Real Estate: $100K" as RE
}

rectangle "Individual Asset Level" as Individual {
  rectangle "Brokerage Account #1: $75K" as Account1
  rectangle "Brokerage Account #2: $125K" as Account2
  rectangle "Individual Stocks..." as StocksDetail
}

Portfolio --> AssetClass : drill down
AssetClass --> Individual : drill down
Individual --> AssetClass : roll up
AssetClass --> Portfolio : roll up
@enduml
```

## Event-Driven Flow Generation

### Event Types and Flow Creation

Different types of events generate different types of flows:

```plantuml
@startuml Event Flow Generation
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Recurring Events" as Recurring {
  rectangle "Monthly Events" as Monthly {
    rectangle "Salary Deposit"
    rectangle "Mortgage Payment"
    rectangle "Investment Contribution"
  }
  rectangle "Annual Events" as Annual {
    rectangle "Tax Payment"
    rectangle "Insurance Premium"
    rectangle "Investment Rebalancing"
  }
}

rectangle "Conditional Events" as Conditional {
  rectangle "Threshold Events" as Threshold {
    rectangle "Emergency Fund Replenishment"
    rectangle "Debt Payoff Acceleration"
  }
  rectangle "Market Events" as Market {
    rectangle "Reinvestment of Dividends"
    rectangle "Tax Loss Harvesting"
  }
}

rectangle "Calculation Events" as Calculation {
  rectangle "Interest Accrual"
  rectangle "Fee Assessment"
  rectangle "Performance Calculation"
}

Recurring --> Flow : generates
Conditional --> Flow : generates
Calculation --> Flow : generates
@enduml
```

### Event Processing Pipeline

```plantuml
@startuml Event Processing
!theme plain
skinparam backgroundColor #FEFEFE
skinparam sequenceParticipant underline

participant "Event" as Event
participant "EventProcessor" as Processor
participant "EntityVersion" as Version
participant "Flow" as Flow
participant "TimePeriod" as Period

Event -> Processor: process(event, version)
Processor -> Processor: validateConditions()
Processor -> Processor: calculateAmount()
Processor -> Flow: new Flow(...)
Processor -> Version: applyFlow(flow)
Processor -> Period: recordFlow(flow)
Processor -> Event: return updatedVersion
@enduml
```

## Data Validation and Integrity

### Entity Validation Rules

```plantuml
@startuml Entity Validation
!theme plain
skinparam backgroundColor #FEFEFE

start
:Validate Entity Creation

if (id == null || empty?) then (yes)
  :throw IllegalArgumentException
  stop
endif

if (name == null || empty?) then (yes)
  :throw IllegalArgumentException
  stop
endif

if (initialValue < 0?) then (yes)
  :throw IllegalArgumentException
  stop
endif

if (primaryCategory not in valid list?) then (yes)
  :throw IllegalArgumentException
  stop
endif

:Entity is valid
continue processing;

end
@enduml
```

### Flow Validation Rules

```plantuml
@startuml Flow Validation
!theme plain
skinparam backgroundColor #FEFEFE

start
:Validate Flow Creation

if (id == null || empty?) then (yes)
  :throw IllegalArgumentException
  stop
endif

if (source == null?) then (yes)
  :throw IllegalArgumentException
  stop
endif

if (target == null?) then (yes)
  :throw IllegalArgumentException
  stop
endif

if (amount <= 0?) then (yes)
  :throw IllegalArgumentException
  stop
endif

if (source == target?) then (yes)
  :throw IllegalArgumentException
  note right: Cannot flow to self
  stop
endif

:Flow is valid
continue processing;

end
@enduml
```

## Performance Optimization

### Caching Strategies

```plantuml
@startuml Caching Strategy
!theme plain
skinparam backgroundColor #FEFEFE

class TimePeriod {
  -Cache<Entity, PeriodEntityAggregate> aggregateCache
  --
  +PeriodEntityAggregate getPeriodEntityAggregate(Entity)
}

note right of TimePeriod::aggregateCache
  Guava Cache with:
  - Maximum size: 100
  - LRU eviction
  - Automatic cleanup
end note

TimePeriod -> PeriodEntityAggregate : caches expensive calculations
@enduml
```

### Lazy Evaluation

```plantuml
@startuml Lazy Evaluation
!theme plain
skinparam backgroundColor #FEFEFE

start
:Request PeriodEntityAggregate

if (cache contains entity?) then (yes)
  :return cached result
else (no)
  :calculate final version
  :aggregate flows
  :create PeriodEntityAggregate
  :store in cache
  :return result
endif

end
@enduml
```

This comprehensive entity and flow modeling system provides the foundation for accurate financial simulations and rich visualizations in RatRace2025.