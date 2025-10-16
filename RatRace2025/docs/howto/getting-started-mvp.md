# RatRace2025 MVP Integration Guide

## How RatRace Connects to the MVP Framework

RatRace2025 is now fully integrated as a subproject within the MVP (Model-View-Presenter) backplane framework. This modular architecture provides several key benefits:

### Architecture Overview

```plantuml
@startuml MVP Integration Overview
!theme plain
skinparam backgroundColor #FEFEFE

rectangle "MVP Backplane" as MVP {
    rectangle "Service Registry" as SR
    rectangle "Message Bus" as MB
    rectangle "Namespace Manager" as NM
}

rectangle "RatRace2025 Subproject" as RR {
    rectangle "RatRaceSystemStateService" as RSS
    rectangle "MvpConfiguration" as MC
    rectangle "Domain Layer" as DL
}

rectangle "Client Application" as Client {
    rectangle "TabbedInterface" as TI
    rectangle "RatRaceFinanceTab" as RFT
}

MVP --> RR : Service Registration
RR --> MVP : Message Processing
Client --> MVP : WebSocket/Messages
MVP --> Client : State Updates

note right of MVP
    Centralized message routing
    and service discovery
end note

note right of RR
    Financial simulation logic
    with MVP-compliant interface
end note
@enduml
```

### Key Integration Points

1. **Namespace Isolation**: RatRace operates under the `ratrace` namespace for clean separation
2. **Message Contracts**: Uses standardized message types (`load_scenario`, `run_simulation`, `get_dump`, `get_sankey`)
3. **Service Registration**: `RatRaceSystemStateService` registers with MVP `ServiceRegistry`
4. **Client Integration**: React/TypeScript UI using `@nednederlander/mvp-client` library (currently using development stubs)

## Starting the Client and Server

### Prerequisites

- Java 17+ (for server)
- Node.js 18+ and npm (for client)
- GitHub PAT with `read:packages` scope (for Maven dependencies)

### Step 1: Start the Server

The RatRace backend is a Spring Boot application that integrates with the MVP backplane:

```bash
# Navigate to project root
cd /path/to/ratrace2025

# Start the Spring Boot server
mvn spring-boot:run
```

**What happens:**
- Server starts on `http://localhost:8080`
- MVP services are registered in the `ratrace` namespace
- WebSocket endpoint available at `/ws` for real-time updates
- REST API endpoints available under `/mvp/messages`

### Step 2: Start the Client

The RatRace client is a React/TypeScript application built with Vite:

```bash
# Navigate to client directory
cd client

# Install dependencies (if not already done)
npm install

# Start the development server
npm run dev
```

**What happens:**
- Client starts on `http://localhost:5173` (or next available port)
- Connects to server via WebSocket for real-time communication
- Loads MVP client library and initializes tabbed interface

## Running a Scenario in the UI

### Step 1: Access the Application

Open your browser and navigate to `http://localhost:5173`. You should see:

- **MVP Tabbed Interface**: A clean tabbed UI provided by the MVP framework
- **RatRace Finance Tab**: A tab titled "Financial Modeling" (namespace: `ratrace`)
- **Connection Status**: Shows "Connected" when server communication is active

### Step 2: Load a Scenario

1. **Click on the "Financial Modeling" tab**
2. **Scroll to "Load Scenario" section**
3. **Paste scenario JSON** into the textarea. Here's a simple example:

```json
{
  "entities": [
    {
      "id": "checking_account",
      "name": "Primary Checking",
      "primaryCategory": "Asset",
      "detailedCategory": "Cash Equivalent",
      "balance": 5000.0,
      "rate": 0.02
    },
    {
      "id": "salary_income",
      "name": "Monthly Salary",
      "primaryCategory": "Income",
      "detailedCategory": "Employment Income",
      "balance": 0.0,
      "rate": 0.0
    }
  ],
  "timeline": {
    "startDate": "2025-01-01",
    "endDate": "2025-12-31",
    "periods": []
  }
}
```

4. **Click "Load Scenario"**
5. **Status should show**: "Scenario loaded successfully"

### Step 3: Run the Simulation

1. **Click "Run Simulation"** button
2. **Wait for completion** (status shows "Simulation completed successfully")
3. **Use additional controls**:
   - **"Get Dump"**: Shows console output of simulation results
   - **"Get Sankey Data"**: Generates data for flow visualization

## What You Should See Initially

### Main Interface
- **Clean tabbed layout** with MVP framework styling
- **Connection indicator** showing real-time server status
- **Financial Modeling tab** with organized sections

### Scenario Loading Section
- **Large textarea** for JSON scenario input
- **Load Scenario button** (enabled when connected)
- **Status messages** providing feedback on operations

### Simulation Controls Section
- **Run Simulation button** (processes loaded scenario)
- **Get Dump button** (shows text-based results)
- **Get Sankey Data button** (prepares visualization data)
- **Status area** showing operation progress and results

### Expected Behavior
- **Real-time updates**: Status changes as operations complete
- **Error handling**: Clear messages for invalid scenarios or connection issues
- **Console logging**: Detailed output visible in browser developer tools
- **Responsive design**: Works on desktop and mobile devices

## Troubleshooting

### Server Won't Start
- Check Java version: `java -version` (should be 17+)
- Verify Maven is installed: `mvn -version`
- Check for port conflicts on 8080

### Client Won't Connect
- Ensure server is running on port 8080
- Check browser console for connection errors
- Verify WebSocket endpoint is accessible

### Scenario Loading Fails
- Validate JSON syntax
- Check that required fields are present
- Review server logs for detailed error messages

## Next Steps

Once you have the basic setup working:

1. **Explore advanced scenarios** with multiple entities and complex flows
2. **Try the Sankey visualization** data (currently logs to console)
3. **Extend the UI** with custom components using MVP hooks
4. **Add new message types** following the established patterns

The MVP integration provides a solid foundation for building rich financial modeling experiences while maintaining clean separation between UI and business logic.