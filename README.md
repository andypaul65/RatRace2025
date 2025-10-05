# RatRace2025
this has started

@startuml
actor User
participant Controller
participant Service
User -> Controller: POST /simulate
Controller -> Service: runSimulation(scenarioId)
Service --> Controller: FinanceModel
Controller --> User: JSON Response
@enduml