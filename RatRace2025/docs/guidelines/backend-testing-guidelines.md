# Backend Testing Guidelines for MVP Framework

## Overview
Testing serves as executable documentation and is developed iteratively alongside specifications. Aim for 80%+ code coverage across unit, integration, and end-to-end tests. Use fixtures for consistent example data.

## Unit Testing
- **Backend**: Employ JUnit + Mockito for mocking dependencies and testing services/controllers in isolation.
  - Example Snippet (JUnit for Service):
    ```java
    @ExtendWith(MockitoExtension.class)
    class FinanceModelServiceTest {
      @Mock private ScenarioRepository repository;
      @InjectMocks private FinanceModelService service;

      @Test
      void runSimulationReturnsModel() {
        Scenario scenario = new Scenario();
        when(repository.findById("test")).thenReturn(Optional.of(scenario));
        FinanceModel result = service.runSimulation("test");
        assertNotNull(result);
      }
    }
    ```

## Integration Testing
- **Backend**: Use @SpringBootTest for full context loading and endpoint verification.
  - Example Snippet (@SpringBootTest):
    ```java
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    class FinanceModelControllerIntegrationTest {
      @Autowired private TestRestTemplate restTemplate;

      @Test
      void runSimulationReturns200() {
        ResponseEntity<FinanceModel> response = restTemplate.postForEntity("/api/simulate", new SimulationRequest("test"), FinanceModel.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
      }
    }
    ```

## End-to-End Testing
Use REST-assured or Spring's TestRestTemplate for full-stack scenarios, mirroring Java's integration tests.

- Example Snippet (REST-assured for Simulation):
  ```java
  @Test
  void simulateScenarioReturnsDump() {
    given()
      .contentType(ContentType.JSON)
      .body(new SimulationRequest("test"))
    .when()
      .post("/api/simulate")
    .then()
      .statusCode(200)
      .body("dump", notNullValue());
  }
  ```

## Best Practices
- Develop tests iteratively with code implementation.
- Run tests frequently to maintain coverage.
- Address common setup errors early, such as incompatible testing environments, to streamline development.
- Prioritize minimal changes; verify with mvn compile and server runs.