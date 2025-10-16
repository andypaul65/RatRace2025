# MVP Framework Expansion Guide

This document outlines the contracts and processes for extending the MVP framework as a base layer. It details JAR/NPM naming, lifecycle hooks, registry patterns, and integration examples for building applications on top of the framework.

## Overview

Subprojects extend the MVP backplane as a modular base layer with:
- **Server Side**: Java Spring Boot library (JAR) from MVP backplane for backend services.
- **Client Side**: React TypeScript library (NPM) from MVP backplane for UI components.

Extensions are achieved through:
- **Registries**: Dynamic registration of custom tabs and services in the backplane.
- **Abstract Classes**: Extension points for subproject-specific logic.
- **Lifecycle Hooks**: Callbacks for component/service lifecycles, integrated with backplane.
- **Namespaces**: Scoped isolation for subproject extensions.

## Packaging and Naming

### Server JAR Naming
- **Library JAR**: `org.ajp.mvp:server:0.0.1-SNAPSHOT.jar`
  - Contains core services, controllers, DTOs.
  - Dependency for extending projects.
- **Executable JAR**: `org.ajp.mvp:server:0.0.1-SNAPSHOT-exec.jar`
  - Includes embedded server for standalone apps.
  - Not used when building on top of the base layer.

### Client NPM Naming
- **Package**: `@nednederlander/mvp-client@0.0.1`
- **Dist Files**:
  - `dist/index.js`: ESM bundle (externalizes React/React-DOM).
  - `dist/index.d.ts`: TypeScript definitions.
- **Build Command**: `npm run build` generates the library bundle.

#### Publishing Setup
To enable publishing the client library to NPM:
1. **Create `.npmrc` file** in the client project root:
   ```
   registry=https://registry.npmjs.org/
   //registry.npmjs.org/:_authToken=${NPM_TOKEN}
   ```
   - Replace `${NPM_TOKEN}` with your NPM authentication token (obtain from NPM account settings).
   - For public publishing, ensure the package.json includes `"publishConfig": { "access": "public" }` if using a scoped package.

2. **Update package.json** for publishing:
   ```json
   {
     "name": "@nednederlander/mvp-client",
     "version": "0.0.1",
     "publishConfig": {
       "access": "public"
     },
     "files": ["dist"],
     "main": "dist/index.js",
     "types": "dist/index.d.ts"
   }
   ```

3. **Publish Command**: After building (`npm run build`), run `npm publish` to release the package to NPM registry.

#### Server Publishing Setup
To enable deploying the server JAR to GitHub Packages:

1. **Add `distributionManagement` to pom.xml**:
   ```xml
   <distributionManagement>
       <repository>
           <id>github</id>
           <name>GitHub Packages</name>
           <url>https://maven.pkg.github.com/andypaul65/MVP</url>
       </repository>
   </distributionManagement>
   ```

2. **Configure GitHub Personal Access Token (PAT)**:
   - Create a PAT in GitHub Settings > Developer settings > Personal access tokens > Tokens (classic) with `write:packages` and `read:packages` scopes.
   - Add to `~/.m2/settings.xml`:
     ```xml
     <servers>
         <server>
             <id>github</id>
             <username>andypaul65</username>
             <password>YOUR_GITHUB_PAT</password>
         </server>
     </servers>
     ```
     - Replace `YOUR_GITHUB_PAT` with your GitHub PAT. This authenticates Maven to GitHub Packages.

3. **Deploy Command**: Run `mvn deploy` to upload the JAR. Ensure the repository is public or the PAT has access. For snapshots, append `-SNAPSHOT` to version in pom.xml.

## Core Interfaces and Contracts

### Server Contracts

#### ServiceRegistry Interface
```java
public interface ServiceRegistry {
    void registerService(String serviceName, Object service);
    void unregisterService(String serviceName);
    <T> T getService(String serviceName, Class<T> serviceClass);
    List<String> getRegisteredServiceNames();
}
```

#### AbstractSystemStateService
```java
public abstract class AbstractSystemStateService implements SystemStateService {
    protected MessageDto processMessage(String namespace, MessageDto message) {
        // Override for custom processing
        return message;
    }
    protected MessageDto getDefaultState(String namespace) {
        // Override for custom defaults
        return new MessageDto();
    }
    protected abstract void storeMessage(String namespace, MessageDto message);
}
```

### Client Contracts

#### TabRegistry Interface
```typescript
interface TabRegistry {
  registerTab(tab: TabConfig): void;
  unregisterTab(namespace: string): void;
  getRegisteredTabs(): TabConfig[];
  getTabByNamespace(namespace: string): TabConfig | undefined;
}
```

#### TabLifecycleHooks
```typescript
interface TabLifecycleHooks {
  onTabMount?: (namespace: string) => void;
  onTabUnmount?: (namespace: string) => void;
  onStateUpdate?: (namespace: string, state: any) => void;
}
```

#### TabConfig
```typescript
interface TabConfig {
  namespace: string;
  title: string;
  component: React.ComponentType<{ namespace: string; children?: TabConfig[] }>;
  children?: TabConfig[];
  hooks?: TabLifecycleHooks;
  style?: React.CSSProperties;
}
```

## Lifecycle and Connections

### Server Lifecycle
1. **Startup**: Framework loads with default `SystemStateServiceImpl`.
2. **Extension Registration**: Custom services registered via `ServiceRegistry`.
3. **Request Handling**: Controllers delegate to registered services by namespace.
4. **Message Processing**: Abstract methods allow custom logic per extension.

### Client Lifecycle
1. **Initialization**: `TabbedInterface` renders with initial tabs.
2. **Tab Activation**: `onTabMount` hook called for active tab.
3. **State Updates**: `useSystemState` fetches/polls server state.
4. **Extension Loading**: New tabs registered dynamically.
5. **Unmount**: `onTabUnmount` called when tab deactivated.

### Connections Between Server and Client
- **Namespaces**: Client tabs map to server endpoints (e.g., `/api/state/{namespace}`).
- **State Sync**: Client hooks poll server for namespace-specific state.
- **WebSocket**: Real-time updates via `/ws` endpoint.
- **Authentication**: JWT tokens shared between client/server.

## Extension Examples

### Adding a Custom Tab (Client)
```typescript
import { TabConfig, TabbedInterface } from '@nednederlander/mvp-client';
import React from 'react';

const customTab: TabConfig = {
  namespace: 'my-feature',
  title: 'My Feature',
  component: ({ namespace }) => <div>Custom content for {namespace}</div>,
  hooks: {
    onTabMount: (ns) => console.log(`Mounted ${ns}`),
    onTabUnmount: (ns) => console.log(`Unmounted ${ns}`),
  },
};

// In your app
const tabs = [/* existing tabs */, customTab];
<TabbedInterface tabs={tabs} />;
```

### Custom Service Implementation (Server)
```java
@Service
public class CustomSystemStateService extends AbstractSystemStateService {
    @Override
    protected MessageDto processMessage(String namespace, MessageDto message) {
        // Custom processing logic
        message.setContent("Processed: " + message.getContent());
        return message;
    }

    @Override
    protected void storeMessage(String namespace, MessageDto message) {
        // Custom storage (e.g., database)
        // ...
    }
}
```

### Registry Usage
```typescript
// Client: Register tab dynamically
import { TabRegistry } from '@nednederlander/mvp-client';
const registry: TabRegistry = /* implementation */;
registry.registerTab(customTab);

// Server: Register service
@Autowired
private ServiceRegistry registry;
registry.registerService("customState", new CustomSystemStateService());
```

## Building on Top of the Base Layer

### Maven Project Setup
```xml
<dependency>
    <groupId>org.ajp.mvp</groupId>
    <artifactId>server</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### NPM Project Setup
```bash
npm install @nednederlander/mvp-client react react-dom
```

### Repository Configuration for Subprojects

#### Maven Repository Setup
To use the published JAR from GitHub Packages in a subproject:

1. **Add repository to pom.xml**:
   ```xml
   <repositories>
       <repository>
           <id>github</id>
           <url>https://maven.pkg.github.com/andypaul65/MVP</url>
       </repository>
   </repositories>
   ```

2. **Update dependency version** to the released version (e.g., `0.0.1` instead of `0.0.1-SNAPSHOT`).

#### NPM Package Usage
The `@nednederlander/mvp-client` package is published to NPM registry. Subprojects can install it directly via `npm install @nednederlander/mvp-client`. No additional repository setup needed for public packages.

### Project Structure Example
```
my-extension/
├── pom.xml (includes server JAR)
├── src/main/java/
│   └── com/mycompany/
│       ├── MyController.java
│       └── services/MyService.java (extends AbstractSystemStateService)
├── client/
│   ├── package.json (includes @nednederlander/mvp-client)
│   └── src/
│       ├── App.tsx (uses TabbedInterface)
│       └── components/MyTab.tsx
```

## Best Practices
- **Namespace Isolation**: Use unique namespaces to avoid conflicts.
- **Hook Overrides**: Implement abstract methods thoughtfully.
- **Registry Patterns**: Register extensions early in lifecycle.
- **Testing**: Extend base tests for custom logic.
- **Versioning**: Follow semantic versioning (MAJOR.MINOR.PATCH) for framework releases. Pin dependency versions in extending projects for stability. Increment versions on breaking changes, new features, or bug fixes.

### Versioning and Publishing Process
To maintain a reliable ecosystem:

1. **Development Workflow**:
   - Use `-SNAPSHOT` versions (e.g., `0.0.1-SNAPSHOT` in Maven, `0.0.1-dev` or similar in NPM) for iterative development, commits, and internal deployments. This allows multiple deployments without version changes, supporting ongoing testing and integration.
   - Commits and builds can proceed with SNAPSHOT versions as they do not require version bumps for each change.

2. **Release Preparation**:
   - When ready for official release, remove `-SNAPSHOT` and update to a stable version following SemVer (e.g., `0.0.1` for initial release).
   - Run full pre-release checks: full test suites, builds, and compatibility verification.

3. **Semantic Versioning**: Update versions in `package.json` (client) and `pom.xml` (server) following SemVer conventions (MAJOR.MINOR.PATCH):
   - **MAJOR**: Breaking changes (e.g., 1.0.0 → 2.0.0).
   - **MINOR**: New features (backward-compatible).
   - **PATCH**: Bug fixes.

4. **Publishing Workflow**:
   - **Server**: Use Maven release plugin or manual `mvn deploy` to publish JAR to repository (e.g., GitHub Packages). For releases, deploy stable versions; for development, deploy SNAPSHOTS.
   - **Client**: After build, authenticate with NPM token and run `npm publish` for stable releases.

5. **Changelog**: Maintain a `CHANGELOG.md` documenting version changes and breaking changes.

6. **Dependency Management**: Extending projects should specify version ranges (e.g., `^1.0.0` for NPM, exact versions for Maven) to allow patches but avoid breaking changes. Pin to stable releases for production stability.

## API Reference
- Server Endpoints: See `api-contracts.json`
- Client Components: Exported from `@nednederlander/mvp-client`
- DTOs: Shared between client/server for consistency.

This guide ensures consistent expansion. Refer to it for integrating new features.