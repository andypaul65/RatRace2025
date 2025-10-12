# Backend Testing Guidelines for MVP Framework

## Overview
Testing serves as executable documentation and is developed iteratively alongside specifications. Aim for 80%+ code coverage across unit, integration, end-to-end, and BDD tests. Use fixtures for consistent example data.

## BDD Testing with Cucumber (MANDATORY)
**Behavior-Driven Development tests are required for all business functionality:**

- **Framework**: Use Cucumber JVM with JUnit Platform integration
- **Location**: Feature files in both `docs/BDD/` (documentation) and `src/test/resources/features/` (execution)
- **Execution**: Run with `mvn test -Dtest=CucumberTestRunner`
- **Coverage**: All user stories and use cases must have corresponding Cucumber tests
- **Maintenance**: Keep feature files synchronized between `docs/BDD/` and `src/test/resources/features/`

**Business requirements without passing Cucumber tests cannot be committed.**

### BDD Test Structure
- **Feature Files**: Gherkin syntax (.feature files) describing business scenarios
- **Step Definitions**: Java methods implementing Given/When/Then steps
- **Test Runner**: `CucumberTestRunner.java` configured for JUnit Platform
- **Reporting**: HTML and JSON reports generated in `target/cucumber-reports/`

### BDD Development Workflow
1. **Write Feature**: Create/update .feature files based on business requirements
2. **Implement Steps**: Write step definitions that call domain logic
3. **Run Tests**: Execute Cucumber tests to verify business requirements
4. **Refactor**: Update implementation while ensuring tests continue to pass
5. **Document**: Feature files serve as living documentation

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
- **BDD Integration**: Ensure Cucumber tests are updated when business requirements change.
- **Documentation Sync**: Keep design documents and test scenarios synchronized.
- **Pre-Commit Verification**: Never commit without running full test suite including Cucumber tests.
- **Test Coverage**: Maintain 80%+ coverage including BDD scenarios.
- **Living Documentation**: Use Cucumber features as executable specifications that stay current.