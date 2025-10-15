# MVP Backplane Integration Guide

This guide provides step-by-step instructions for integrating the MVP framework as a backplane into a client-server subproject. It allows building simpler applications on top of the modular MVP base layer, leveraging its registries, hooks, and shared contracts.

## Overview

The MVP framework consists of:
- **Server Side**: Java Spring Boot JAR (published to GitHub Packages).
- **Client Side**: React TypeScript NPM package (published to NPM registry).

Subprojects can extend the framework by registering custom services, tabs, and hooks without modifying core code.

## Prerequisites

- Java 17+ and Maven for server-side.
- Node.js and NPM for client-side.
- GitHub account for accessing private packages (if applicable).
- NPM account for package access.

## Step 1: Set Up Server-Side Integration

### Add GitHub Packages Repository
In your subproject's `pom.xml`, add the repository to access the MVP server JAR:

```xml
<reproject ...>
    <!-- Other configurations -->

    <repositories>
        <repository>
            <id>github</id>
            <url>https://maven.pkg.github.com/andypaul65/MVP</url>
        </repository>
    </repositories>

    <!-- Dependencies -->
    <dependencies>
        <dependency>
            <groupId>org.ajp.mvp</groupId>
            <artifactId>server</artifactId>
            <version>0.0.1</version> <!-- Use latest stable release -->
        </dependency>
        <!-- Add your custom dependencies -->
    </dependencies>
</project>
```

### Configure GitHub PAT for Maven
Create a Personal Access Token (PAT) in GitHub with `read:packages` scope. Add to `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>andypaul65</username>
            <password>YOUR_GITHUB_PAT</password>
        </server>
    </servers>
</settings>
```

### Extend the Framework
Create a custom service by extending `AbstractSystemStateService`:

```java
import org.ajp.mvp.server.AbstractSystemStateService;
import org.springframework.stereotype.Service;

@Service
public class MyCustomService extends AbstractSystemStateService {
    @Override
    protected MessageDto processMessage(String namespace, MessageDto message) {
        // Custom logic
        message.setContent("Processed: " + message.getContent());
        return message;
    }

    @Override
    protected void storeMessage(String namespace, MessageDto message) {
        // Custom storage
    }
}
```

Register services via `ServiceRegistry` in your configuration.

## Step 2: Set Up Client-Side Integration

### Install NPM Package
In your subproject's client directory, install the MVP client package:

```bash
npm install @nednederlander/mvp-client react react-dom
```

### Configure Build and Scripts
Update `package.json` for the library build:

```json
{
  "name": "my-subproject-client",
  "version": "1.0.0",
  "scripts": {
    "build": "tsc -b && vite build",
    "dev": "vite"
  },
  "dependencies": {
    "@ajp/mvp-client": "^0.0.1",
    "react": "^19.1.1",
    "react-dom": "^19.1.1"
  },
  "devDependencies": {
    "@vitejs/plugin-react": "^5.0.2",
    "typescript": "~5.8.3",
    "vite": "^7.1.6"
  }
}
```

### Extend the Framework
Create custom tabs and register them:

```typescript
import React from 'react';
import { TabConfig, TabbedInterface } from '@nednederlander/mvp-client';

const myTab: TabConfig = {
  namespace: 'my-feature',
  title: 'My Feature',
  component: () => <div>My Custom Content</div>,
  hooks: {
    onTabMount: (ns) => console.log(`Mounted ${ns}`),
  },
};

const tabs = [/* other tabs */, myTab];

function App() {
  return <TabbedInterface tabs={tabs} />;
}

export default App;
```

Use `useSystemState` for state management and API calls.

## Step 3: Configuration and Deployment

### API Contracts
The framework uses OpenAPI contracts in `api-contracts.json`. Ensure your subproject aligns with the defined endpoints, schemas, and namespaces.

### Testing
- **Server**: Use JUnit + Mockito for unit tests, extending base test suites.
- **Client**: Use Vitest + React Testing Library for component tests.
- **Integration**: Test with MSW for API mocking and Cypress for E2E.

### Building and Running
- **Server**: `mvn clean install` then `mvn spring-boot:run`.
- **Client**: `npm run build` then `npm run dev`.

### Versioning
Pin to stable versions for production (e.g., `0.0.1`). Use version ranges for development (e.g., `^0.0.1`).

## Troubleshooting

- **Repository Access**: Ensure PAT is correctly configured in `settings.xml`.
- **Dependency Resolution**: Run `mvn clean` if JAR issues occur.
- **Build Errors**: Check TypeScript and Java versions match requirements.
- **Namespace Conflicts**: Use unique namespaces for extensions.

## Next Steps

- Review the full `expansion-guide.md` for advanced extension patterns.
- Contribute back to the MVP framework via pull requests.
- Maintain your subproject's changelog for version tracking.

This guide ensures seamless integration of the MVP backplane into your subproject.

**As a living document, review and update this periodically to align with evolving MVP backplane releases, new integration patterns, and subproject needs.**