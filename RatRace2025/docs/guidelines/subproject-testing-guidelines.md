# Testing Guidelines for MVP Framework

## Overview
Testing serves as executable documentation and is developed iteratively alongside specifications in subprojects. Aim for 80%+ code coverage across unit, integration, end-to-end, and BDD tests. Use fixtures for consistent example data, incorporate assertions for UX themes (leveraging MVP backplane's cyberpunk styling), and ensure compatibility with the backplane's contracts and hooks.

## BDD Testing with Cucumber (MANDATORY)
Behavior-Driven Development tests are required for all business functionality.

- **Framework**: Cucumber.js for client (with Vitest/Cypress integration); Cucumber JVM for server.
- **Location**: Feature files in `src/test/resources/features/` for execution, step definitions in `src/test/js/steps/` (client) or `src/test/java/steps/` (server).
- **Execution**: Run with `npm run test:bdd` (client) or `mvn test -Dtest=CucumberTestRunner` (server).
- **Coverage**: All user stories and use cases must have corresponding Cucumber tests.
- **Maintenance**: Feature files are located in test directories; documentation copies may be kept in `docs/BDD/` for reference and synchronization.

Business requirements without passing Cucumber tests cannot be committed.

### BDD Test Structure
- **Feature Files**: Gherkin syntax (.feature files) describing business scenarios.
- **Step Definitions**: Methods implementing Given/When/Then steps.
- **Test Runner**: Configured for respective platforms.
- **Reporting**: HTML and JSON reports generated in `target/cucumber-reports/`.

### BDD Development Workflow
1. **Write Feature**: Create/update .feature files based on business requirements.
2. **Implement Steps**: Write step definitions that call domain logic.
3. **Run Tests**: Execute Cucumber tests to verify business requirements.
4. **Refactor**: Update implementation while ensuring tests continue to pass.
5. **Document**: Feature files serve as living documentation.

### BDD Setup Instructions

#### Client-Side Setup (Cucumber.js)
- **Dependencies**: Install via npm: `npm install --save-dev @cucumber/cucumber vitest-cucumber`
- **Scripts**: Add to `package.json`: `"test:bdd": "cucumber-js --config cucumber.mjs"`
- **Configuration**: Create `cucumber.mjs` with paths to features (`src/test/resources/features/**/*.feature`) and step definitions (`src/test/js/steps/**/*.js`).
- **Integration**: Use with Vitest for assertion library compatibility.

#### Server-Side Setup (Cucumber JVM)
- **Dependencies**: Add to `pom.xml`:
  ```xml
  <dependency>
      <groupId>io.cucumber</groupId>
      <artifactId>cucumber-java</artifactId>
      <version>7.14.0</version>
      <scope>test</scope>
  </dependency>
  <dependency>
      <groupId>io.cucumber</groupId>
      <artifactId>cucumber-junit</artifactId>
      <version>7.14.0</version>
      <scope>test</scope>
  </dependency>
  ```
- **Test Runner**: Create `CucumberTestRunner.java` in `src/test/java` with `@RunWith(Cucumber.class)` and `@CucumberOptions(features = "src/test/resources/features", glue = "com.example.steps")`.
- **Execution**: Run via `mvn test -Dtest=CucumberTestRunner`.

## Unit Testing
- **Client-Side**: Use Vitest + React Testing Library for isolated component tests. Assert functionality, state changes, and themed styling.
  - Configuration Tips: In `vite.config.ts`, add `test` section with `environment: 'jsdom'`, `setupFiles: './src/setupTests.ts'`, `globals: true`, `css: false`. Mock CSS if needed.
  - Example Snippet (Vitest for Themed Component):
    ```tsx:disable-run
    import { render, screen } from '@testing-library/react';
    import { describe, it, expect } from 'vitest';
    import TabbedInterface from '../TabbedInterface';

    describe('TabbedInterface', () => {
      it('applies cyberpunk theme to active tab', () => {
        const tabs = [{ namespace: 'test', title: 'Test', component: () => <div>Test Content</div> }];
        render(<TabbedInterface tabs={tabs} />);
        const content = screen.getByText('Test Content').closest('.cyberpunk-content');
        expect(content).toHaveStyle('background-color: rgb(30, 30, 30)');
      });
    });
    ```
  - Example Snippet (Vitest for Hook - useSystemState):
    ```tsx
    import { describe, it, expect, vi } from 'vitest';
    import { renderHook, waitFor } from '@testing-library/react';
    import { useSystemState } from '../useSystemState';
    import { apiService } from '@/services/apiService';

    vi.mock('@/services/apiService');

    describe('useSystemState', () => {
      it('should load state on mount', async () => {
        (apiService.getState as any).mockResolvedValue({ content: 'Loaded', namespace: 'test' });
        const { result } = renderHook(() => useSystemState('test'));
        await waitFor(() => expect(result.current.loading).toBe(false));
        expect(result.current.state).toBe('Loaded');
      });
    });
    ```
  - Example Snippet (Vitest for Service - apiService):
    ```tsx
    import { describe, it, expect } from 'vitest';
    import { apiService } from '../apiService';
    import { server } from '../../setupTests';
    import { http, HttpResponse } from 'msw';

    describe('apiService', () => {
      it('should return MessageDto on successful fetch', async () => {
        server.use(
          http.get('http://localhost:8080/api/state/test', () => HttpResponse.json({ content: 'Test', namespace: 'test' }))
        );
        const result = await apiService.getState('test');
        expect(result.content).toBe('Test');
      });
    });
    ```
- **Server-Side**: Employ JUnit + Mockito for mocking dependencies and testing services/controllers in isolation.
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

## Backend Isolation Testing
Focus on server-side testing independent of the client UI for efficient backend development.

- **Independent Server Runs**: Run `mvn spring-boot:run` to start the backend standalone. Test REST endpoints directly using Postman, curl, or scripts without client-side code.
- **Minimal REST Endpoint Testing**: Validate core APIs (e.g., GET/POST to /api/state/{namespace}) for functionality. Use Spring Boot's test slices (@WebMvcTest) for controller-only tests.
- **Backend-First Iterative Stages**: In workflows, test and iterate on server logic first—e.g., unit test services, then integration test endpoints—before client integration. This ensures backend robustness.

## Continuous Integration
Automate testing in subprojects using CI tools for reliable BDD and overall test runs.

- **Recommended Tools**: GitHub Actions for repository-integrated CI.
- **GitHub Actions Example**:
  ```yaml
  name: CI
  on: [push, pull_request]
  jobs:
    test:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v3
        - uses: actions/setup-node@v3
          with:
            node-version: '18'
        - run: npm install
        - run: npm run test:bdd  # Cucumber BDD tests
        - run: npm test
        - uses: actions/setup-java@v3
          with:
            java-version: '17'
        - run: mvn test  # Server tests
  ```
- **Benefits**: Automated BDD runs ensure business requirements are met on every push. Fails builds if tests don't pass.

## Integration Testing
**Integration Guidance**: Start with polling on client, evolving to WebSockets for real-time. Emphasize BDD integration for real-time features.

- **Client-Side**: Use MSW for API mocking to verify data flow and rendering.
  - Example Snippet (MSW for API Mock):
    ```ts
    import { http } from 'msw';
    import { setupServer } from 'msw/node';

    const server = setupServer(
      http.get('/api/state/:namespace', () => HttpResponse.json({ state: 'active' }))
    );
    ```
- **Server-Side**: Use @SpringBootTest for full context loading and endpoint verification.
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
- Include style assertions for cyberpunk theme consistency.

## End-to-End Testing
Use Cypress integrated with Cucumber for end-to-end testing with Gherkin scenarios, including tab navigation, real-time updates, and visual regression for UX themes.
  - Example Snippet (Cypress for Tab Switch):
    ```js
    describe('Tabbed Interface', () => {
      it('switches tabs and verifies theme', () => {
        cy.visit('/');
        cy.get('.cyberpunk-tabs button').first().click();
        cy.get('.cyberpunk-content').should('have.css', 'background-color', 'rgb(30, 30, 30)');
      });
    });
    ```

## Visual Testing
For UX themes, use snapshot testing or libraries like Percy for regression detection (e.g., dark gray backgrounds, green highlights).

## Best Practices
- Develop tests iteratively with code implementation.
- Run tests frequently to maintain coverage.
- Address common setup errors early to streamline development.
- Prioritize minimal changes; verify with npm run build and server runs.
- **BDD Integration**: Ensure Cucumber tests are updated when business requirements change.
- **Documentation Sync**: Keep design documents and test scenarios synchronized.
- **Pre-Commit Verification**: Never commit without running full test suite including Cucumber tests.
- **Living Documentation**: Use Cucumber features as executable specifications that stay current.

## See Also
- [Testing Guidelines](../guidelines/testing-guidelines.md): Core testing practices.
- [Subproject Coding Standards](subproject-coding-standards.md): Related standards.
- [Subproject Integration Guide](subproject-integration-guide.md): Setup context.
```