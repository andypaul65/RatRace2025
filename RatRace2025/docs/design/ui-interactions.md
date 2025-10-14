# UI Interactions - User Experience and Navigation

**Last Updated**: 2025-10-14

## Interactive Features

### Hover Interactions

```plantuml
@startuml Hover Interactions
!theme plain
skinparam backgroundColor #FEFEFE
skinparam sequenceParticipant underline

actor User
participant "UI Component" as UI
participant "SankeyData" as Data
participant "Tooltip" as Tooltip

User -> UI: mouseover node
UI -> Data: get node properties
Data -> UI: return node data
UI -> Tooltip: display info

alt Node is Entity
    Tooltip -> User: Show entity details
    note right: Name, Balance, Category, Rate
else Node is Group
    Tooltip -> User: Show group summary
    note right: Group name, Total value, Sub-group count
end

User -> UI: mouseout
UI -> Tooltip: hide tooltip
@enduml
```

### Click Interactions

```plantuml
@startuml Click Interactions
!theme plain
skinparam backgroundColor #FEFEFE
skinparam sequenceParticipant underline

actor User
participant "UI Component" as UI
participant "Context Pane" as Pane
participant "SankeyData" as Data

User -> UI: click node
UI -> Data: get full node data
Data -> UI: return complete properties
UI -> Pane: populate context

alt Entity Node
    Pane -> User: Show entity details
    note right: Full history, Events, Flows
else Group Node
    Pane -> User: Show group overview
    note right: Constituents, Aggregations
    User -> Pane: drill down
    Pane -> UI: expand group
    UI -> UI: show sub-nodes
end

User -> UI: close context
UI -> Pane: hide pane
@enduml
```

### Flow Interactions

```plantuml
@startuml Flow Interactions
!theme plain
skinparam backgroundColor #FEFEFE
skinparam sequenceParticipant underline

actor User
participant "UI Component" as UI
participant "FlowTooltip" as FT
participant "FlowDetails" as FD

User -> UI: hover flow
UI -> UI: highlight flow path
UI -> FT: show flow summary
FT -> User: display amount, type, direction

User -> UI: click flow
UI -> FD: open flow details
FD -> User: show complete flow info
note right: Source/Target details,\nEvent that created it,\nPeriod information

User -> UI: mouseout flow
UI -> UI: remove highlights
UI -> FT: hide tooltip
@enduml
```

## Hierarchical Navigation

### Drill-Down Capabilities

```plantuml
@startuml Hierarchical Navigation
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "Portfolio Level" as Level0 {
  rectangle "Investment Portfolio" as IP {
    rectangle "Balance: $100K" as IPBal
    rectangle "Has 3 sub-groups" as IPSub
  }
  rectangle "Cash Reserves" as CR {
    rectangle "Balance: $25K" as CRBal
    rectangle "No sub-groups" as CRSub
  }
}

rectangle "Asset Class Level" as Level1 {
  rectangle "Stocks" as Stocks {
    rectangle "Balance: $60K" as SBal
    rectangle "Has 5 accounts" as SSub
  }
  rectangle "Bonds" as Bonds {
    rectangle "Balance: $30K" as BBal
    rectangle "Has 2 accounts" as BSub
  }
  rectangle "Cash" as Cash {
    rectangle "Balance: $10K" as CBal
    rectangle "No sub-groups" as CSub
  }
}

rectangle "Account Level" as Level2 {
  rectangle "Brokerage #1" as B1 {
    rectangle "Balance: $25K" as B1Bal
  }
  rectangle "Brokerage #2" as B2 {
    rectangle "Balance: $35K" as B2Bal
  }
  rectangle "Individual Stocks..." as IS
}

Level0 --> Level1 : drill down
Level1 --> Level2 : drill down
Level2 --> Level1 : roll up
Level1 --> Level0 : roll up
@enduml
```

### Navigation State Management

```plantuml
@startuml Navigation State
!theme plain
skinparam backgroundColor #FEFEFE

class NavigationState {
  -int currentLevel : 0
  -String selectedGroupId
  -List<String> expandedGroups
  -Map<String, Boolean> nodeVisibility
  --
  +void drillDown(String groupId)
  +void rollUp()
  +boolean isNodeVisible(String nodeId)
  +List<String> getVisibleNodes()
}

class UIManager {
  -NavigationState navState
  -SankeyRenderer renderer
  --
  +void handleGroupClick(String groupId)
  +void updateDisplay()
  +void animateTransition()
}

NavigationState --> UIManager : manages
UIManager --> NavigationState : updates
@enduml
```

This interactions design ensures users can effectively explore and understand complex financial data through intuitive navigation and responsive feedback.

## See Also
- **[ui-visualization.md](ui-visualization.md)**: Data structures and visual rendering
- **[architecture.md](../design/architecture.md)**: Technical architecture and performance considerations
- **[subproject-coding-standards.md](../guidelines/subproject-coding-standards.md)**: UI theming and styling guidelines