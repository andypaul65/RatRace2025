# UI Visualization - Data Structures and Rendering

**Last Updated**: 2025-10-14

## Non-Technical Perspective

Imagine you're looking at a complex subway map showing how trains move through a city. The RatRace2025 UI turns your financial data into a similar visual experience - a Sankey diagram that shows how money flows through your financial life.

Instead of subway lines and stations, you see:
- **"Stations"** (nodes) representing your bank accounts, investments, and debts
- **"Train tracks"** (flows) showing money moving between accounts
- **Time progression** with columns representing different months or years
- **Interactive features** letting you click on elements for more details

The UI makes complex financial relationships intuitive and explorable, just like zooming in on a subway map to see transfer details.

### Real-World Analogy

Think of financial planning software as a GPS navigation system:
- **The map** shows your current financial position and destination goals
- **Route options** display different financial strategies
- **Traffic updates** represent market conditions and economic events
- **Detour suggestions** show how to avoid financial pitfalls
- **Progress tracking** monitors how well you're following your plan

RatRace2025's UI provides this GPS-like experience for your financial journey.

## Sankey Diagram Data Structure

### Core Data Format

The `buildSankeyData()` method provides a comprehensive JSON structure for UI rendering:

```plantuml
@startuml Sankey Data Structure
!theme plain
skinparam backgroundColor #FEFEFE
skinparam json {
    BackgroundColor #E3F2FD
    BorderColor #1976D2
    FontColor #0D47A1
}

json "SankeyData" as data {
    "nodes": [
        {
            "id": "checking_period_0",
            "name": "Primary Checking",
            "entityId": "checking_001",
            "balance": 2500.00,
            "normalizedHeight": 0.25,
            "primaryCategory": "Asset",
            "detailedCategory": "Cash Equivalent",
            "isGroup": false,
            "periodIndex": 0
        }
    ],
    "links": [
        {
            "id": "flow_123_period_0",
            "source": "checking_period_0",
            "target": "savings_period_0",
            "value": 500.00,
            "flowId": "flow_123",
            "direction": "outbound",
            "type": "transfer",
            "isIntraPeriod": true,
            "sourceName": "Primary Checking",
            "targetName": "Emergency Savings"
        }
    ],
    "periods": [
        {
            "id": "period_0",
            "index": 0,
            "startDate": "2025-01-01T00:00:00Z",
            "endDate": "2025-01-31T23:59:59Z",
            "riskFreeRate": 3.5,
            "inflation": 2.0
        }
    ],
    "maxBalance": 10000.00,
    "totalPeriods": 12
}
@enduml
```

### Node Types and Properties

```plantuml
@startuml Node Types
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Entity Nodes" as Entity {
  rectangle "Individual Assets" as Individual {
    rectangle "Bank Accounts"
    rectangle "Investment Accounts"
    rectangle "Real Estate Holdings"
  }
  rectangle "Individual Liabilities" as Liability {
    rectangle "Mortgages"
    rectangle "Credit Cards"
    rectangle "Personal Loans"
  }
}

rectangle "Group Nodes" as Group {
  rectangle "Asset Portfolios" as Portfolio {
    rectangle "Investment Portfolio"
    rectangle "Real Estate Portfolio"
    rectangle "Cash Reserves"
  }
  rectangle "Liability Groups" as LiabGroup {
    rectangle "Secured Debt"
    rectangle "Unsecured Debt"
    rectangle "Tax Liabilities"
  }
}

rectangle "Node Properties" as Properties {
  rectangle "Core Data" as Core {
    rectangle "id, name, balance"
    rectangle "normalizedHeight"
    rectangle "periodIndex"
  }
  rectangle "Categorization" as Category {
    rectangle "primaryCategory"
    rectangle "detailedCategory"
    rectangle "isGroup, hasSubGroups"
  }
  rectangle "Navigation" as Navigation {
    rectangle "level, entityId"
    rectangle "groupName"
  }
}

Entity --> Properties
Group --> Properties
@enduml
```

## Visual Design Principles

### Column-Based Time Layout

```plantuml
@startuml Time Column Layout
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Period 0\nJan 2025" as P0 {
  rectangle "Node Heights" as N0 {
    rectangle "Entity A\n$5K" as A0
    rectangle "Entity B\n$3K" as B0
    rectangle "Entity C\n$2K" as C0
  }
}

rectangle "Period 1\nFeb 2025" as P1 {
  rectangle "Scaled Heights" as N1 {
    rectangle "Entity A\n$5.2K" as A1
    rectangle "Entity B\n$2.8K" as B1
    rectangle "Entity C\n$2.2K" as C1
  }
}

rectangle "Period 2\nMar 2025" as P2 {
  rectangle "Scaled Heights" as N2 {
    rectangle "Entity A\n$5.4K" as A2
    rectangle "Entity B\n$2.6K" as B2
    rectangle "Entity C\n$2.4K" as C2
  }
}

P0 --> P1 : Time progression
P1 --> P2 : Time progression
A0 --> A1 : Balance evolution
A1 --> A2 : Balance evolution
B0 --> B1 : Balance evolution
B1 --> B2 : Balance evolution
C0 --> C1 : Balance evolution
C1 --> C2 : Balance evolution
@enduml
```

### Common Scale Calculation

```plantuml
@startuml Common Scale
!theme plain
skinparam backgroundColor #FEFEFE

start
:Collect all entity balances
across all periods;

:Find maximum absolute balance
maxBalance = MAX(|balance|);

:Calculate normalized heights
for each node;

if (maxBalance > 0) then (yes)
  :normalizedHeight = |balance| / maxBalance
else (no)
  :normalizedHeight = 0
endif

:Apply to node rendering
(minimum height for visibility);

end
@enduml
```

## Visual Styling and Theming

### Node Styling Rules

```plantuml
@startuml Node Styling
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Category-Based Colors" as Colors {
  rectangle "Assets" as Assets {
    rectangle "Cash: Green (#4CAF50)" as Cash
    rectangle "Investments: Blue (#2196F3)" as Invest
    rectangle "Real Estate: Orange (#FF9800)" as RE
  }
  rectangle "Liabilities" as Liabilities {
    rectangle "Mortgages: Red (#F44336)" as Mort
    rectangle "Loans: Pink (#E91E63)" as Loans
    rectangle "Credit: Purple (#9C27B0)" as Credit
  }
  rectangle "Income/Expense" as Flows {
    rectangle "Income: Light Green" as Inc
    rectangle "Expenses: Light Red" as Exp
  }
}

rectangle "State-Based Styling" as States {
  rectangle "Normal: Solid colors" as Normal
  rectangle "Hover: Brightened + border" as Hover
  rectangle "Selected: Highlighted + shadow" as Selected
  rectangle "Zero Balance: Grayed out" as Zero
}

rectangle "Size-Based Properties" as Size {
  rectangle "Height: normalizedHeight * maxHeight" as Height
  rectangle "Minimum height: 20px" as MinHeight
  rectangle "Maximum height: container height" as MaxHeight
}
@enduml
```

### Flow Styling Rules

```plantuml
@startuml Flow Styling
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Flow Types" as Types {
  rectangle "Income Flows" as Income {
    rectangle "Color: Green gradient" as IncColor
    rectangle "Style: Solid lines" as IncStyle
  }
  rectangle "Expense Flows" as Expense {
    rectangle "Color: Red gradient" as ExpColor
    rectangle "Style: Solid lines" as ExpStyle
  }
  rectangle "Transfer Flows" as Transfer {
    rectangle "Color: Blue gradient" as TransColor
    rectangle "Style: Dashed lines" as TransStyle
  }
}

rectangle "Flow States" as States {
  rectangle "Normal: Base colors" as Normal
  rectangle "Hover: Thickened + highlighted" as Hover
  rectangle "Intra-period: Dashed" as Intra
  rectangle "Inter-period: Solid" as Inter
}

rectangle "Animation" as Animation {
  rectangle "Flow pulses on hover" as Pulse
  rectangle "Progressive reveal by period" as Progressive
  rectangle "Smooth transitions" as Smooth
}
@enduml
```

## Responsive Design

### Multi-Device Support

```plantuml
@startuml Responsive Design
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Desktop (>1200px)" as Desktop {
  rectangle "Full Sankey view" as Full
  rectangle "Side context panel" as SidePanel
  rectangle "Detailed tooltips" as Tooltips
  rectangle "Multi-level navigation" as MultiNav
}

rectangle "Tablet (768-1200px)" as Tablet {
  rectangle "Condensed Sankey" as Condensed
  rectangle "Collapsible context" as Collapsible
  rectangle "Touch-friendly interactions" as Touch
  rectangle "Simplified navigation" as SimpleNav
}

rectangle "Mobile (<768px)" as Mobile {
  rectangle "Single period view" as SinglePeriod
  rectangle "Modal context dialogs" as Modal
  rectangle "Swipe navigation" as Swipe
  rectangle "Essential info only" as Essential
}

Desktop --> Tablet : scales down
Tablet --> Mobile : adapts
@enduml
```

### Progressive Enhancement

```plantuml
@startuml Progressive Enhancement
!theme plain
skinparam backgroundColor #FEFEFE

start
:Detect device capabilities;

if (High-performance device?) then (yes)
  :Enable full animations
  :Complex interactions
  :Real-time updates
else (no)
  if (Medium-performance?) then (yes)
    :Basic animations
    :Core interactions
    :Periodic updates
  else (no)
    :Static rendering
    :Essential interactions
    :Manual refresh
  endif
endif

:Apply appropriate features;

end
@enduml
```

## Performance Optimization

### Rendering Optimization

```plantuml
@startuml Rendering Optimization
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Data Processing" as Data {
  rectangle "Pre-calculate positions" as PreCalc
  rectangle "Cache node/link data" as Cache
  rectangle "Lazy load periods" as Lazy
}

rectangle "Visual Rendering" as Visual {
  rectangle "Use Canvas/WebGL" as Canvas
  rectangle "Virtual scrolling for large datasets" as Virtual
  rectangle "Level-of-detail rendering" as LOD
}

rectangle "Interaction Handling" as Interaction {
  rectangle "Debounce hover events" as Debounce
  rectangle "Spatial indexing for hits" as Spatial
  rectangle "Async context loading" as Async
}

Data --> Visual : feeds
Visual --> Interaction : supports
@enduml
```

### Memory Management

```plantuml
@startuml Memory Management
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Data Chunking" as Chunking {
  rectangle "Load periods on demand" as OnDemand
  rectangle "Unload distant periods" as Unload
  rectangle "Compress historical data" as Compress
}

rectangle "Object Pooling" as Pooling {
  rectangle "Reuse node elements" as ReuseNodes
  rectangle "Recycle flow paths" as RecycleFlows
  rectangle "Pool tooltip instances" as PoolTooltips
}

rectangle "Garbage Collection" as GC {
  rectangle "Clean up event listeners" as CleanEvents
  rectangle "Remove detached DOM elements" as RemoveDOM
  rectangle "Clear cached calculations" as ClearCache
}

Chunking --> Pooling : reduces
Pooling --> GC : minimizes
@enduml
```

## Accessibility Features

### Keyboard Navigation

```plantuml
@startuml Keyboard Navigation
!theme plain
skinparam backgroundColor #FEFEFE
skinparam sequenceParticipant underline

actor "Keyboard User" as User
participant "UI Component" as UI
participant "Focus Manager" as FM
participant "Context Pane" as CP

User -> UI: Tab key
UI -> FM: move focus to next node
FM -> UI: highlight focused node
FM -> CP: update context for focused item

User -> UI: Enter key
UI -> CP: open detailed context
CP -> User: show full information

User -> UI: Arrow keys
UI -> FM: navigate between periods
FM -> UI: scroll to period
FM -> UI: update visible nodes
@enduml
```

### Screen Reader Support

```plantuml
@startuml Screen Reader Support
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Semantic HTML" as Semantic {
  rectangle "Proper ARIA labels" as ARIA
  rectangle "Semantic roles" as Roles
  rectangle "Descriptive alt text" as AltText
}

rectangle "Dynamic Updates" as Dynamic {
  rectangle "ARIA live regions" as Live
  rectangle "Status announcements" as Status
  rectangle "Focus management" as Focus
}

rectangle "Content Descriptions" as Content {
  rectangle "Balance descriptions" as Balance
  rectangle "Flow explanations" as Flow
  rectangle "Period context" as Period
}

Semantic --> Dynamic : supports
Dynamic --> Content : enhances
@enduml
```

This visualization design provides a comprehensive framework for rendering complex financial data in an intuitive and accessible manner.

## See Also
- **[ui-interactions.md](ui-interactions.md)**: User interaction patterns and navigation
- **[architecture.md](../design/architecture.md)**: Technical architecture and performance considerations
- **[subproject-coding-standards.md](../guidelines/subproject-coding-standards.md)**: UI theming and styling guidelines