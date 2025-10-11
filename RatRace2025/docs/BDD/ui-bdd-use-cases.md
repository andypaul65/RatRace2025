# BDD Use Cases - UI Visualization Requirements

This document outlines the UI-specific capabilities of RatRace2025 using Behavior-Driven Development (BDD) scenarios. Each use case describes a specific user interface behavior and demonstrates how the visualization system handles different data scenarios.

## Entity Height Scaling and Visualization

### UC-001: Entity Height Scaling Based on Maximum Period Balance

**As a** UI developer integrating Sankey diagrams
**I want to** ensure entity heights are consistently scaled across all periods
**So that** the visualization accurately represents relative balances throughout the timeline

**Given** a financial scenario with multiple time periods
**And** entities with varying balances across periods
**When** buildSankeyData() is called
**Then** normalizedHeight for each entity node should be calculated as absolute balance divided by the maximum absolute balance across all periods
**And** entity heights should be visually consistent relative to the period with the highest sum of entity balances

#### Sub-Test: Negative Balance Handling

**Given** an entity with a negative balance of -$5,000 in one period
**And** other entities with positive balances
**When** calculating normalized heights
**Then** the negative balance should be treated as positive ($5,000) for scaling calculations
**And** the entity should still display with appropriate negative balance metadata
**And** the scaling should ensure visibility of negative balance entities

### UC-002: Sankey Node Data Structure Validation

**As a** frontend developer consuming Sankey data
**I want to** receive properly formatted node data
**So that** I can render accurate Sankey diagrams

**Given** a completed simulation with entity balances
**When** buildSankeyData() generates node data
**Then** each node should contain id, name, entityId, balance, normalizedHeight, primaryCategory, detailedCategory, isGroup, and periodIndex
**And** normalizedHeight values should be between 0 and 1
**And** balance values should match the final balances from simulation
**And** category fields should be properly populated

### UC-003: Flow Link Data Structure Validation

**As a** frontend developer consuming Sankey data
**I want to** receive properly formatted link data
**So that** I can render accurate flow connections

**Given** a completed simulation with money flows
**When** buildSankeyData() generates link data
**Then** each link should contain id, source, target, value, flowId, direction, type, isIntraPeriod, sourceName, and targetName
**And** source and target should reference valid node IDs
**And** value should be positive and represent flow amounts
**And** isIntraPeriod should correctly indicate flow timing

### UC-004: Period Data Structure Validation

**As a** frontend developer consuming Sankey data
**I want to** receive properly formatted period metadata
**So that** I can organize the visualization by time periods

**Given** a simulation timeline with multiple periods
**When** buildSankeyData() generates period data
**Then** each period should contain id, index, startDate, endDate, riskFreeRate, and inflation
**And** period indices should be sequential starting from 0
**And** dates should be in ISO format
**And** rates should be valid decimal values

### UC-005: Maximum Balance Calculation Accuracy

**As a** UI system testing the scaling algorithm
**I want to** verify the maximum balance calculation
**So that** I can ensure proper height normalization

**Given** a scenario with entities having balances: $10,000, $25,000, -$15,000, $8,000
**When** calculating the maximum balance for scaling
**Then** the maximum should be $25,000 (highest absolute value)
**And** the -$15,000 should be treated as $15,000 for this calculation
**And** all normalized heights should be calculated relative to $25,000

---

## Implementation Status

All UI visualization use cases listed above are **currently implemented** and tested in the RatRace2025 system. The scenarios demonstrate the complete Sankey diagram data generation capabilities available for frontend integration.

### Key UI Capabilities Demonstrated:
- ✅ Entity height scaling with negative balance handling
- ✅ Sankey node and link data structure generation
- ✅ Period metadata inclusion
- ✅ Maximum balance calculation for normalization
- ✅ Consistent data formatting for frontend consumption

### Future Enhancements (Not Yet Implemented):
- Comparative scenario visualization
- Dynamic filtering and highlighting
- Real-time simulation updates
- Advanced animation sequences
- Customizable color schemes