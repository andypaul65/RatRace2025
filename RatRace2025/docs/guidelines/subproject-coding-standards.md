# Coding Standards for MVP Subprojects

**Last Updated**: 2025-10-18

This document outlines the coding standards and best practices for subprojects building on the MVP backplane. It ensures consistency, maintainability, and scalability when extending the MVP framework. Adherence to these guidelines is mandatory, with references to the backplane's core standards. Subprojects should integrate seamlessly with the MVP base layer.

**As a living document, review and update this periodically to align with evolving MVP backplane changes, new subproject needs, and main guideline updates.**

## Project Initialization and Dependencies

To establish a robust foundation for a subproject, scaffold the project (e.g., via `npm init vite@latest` for a React + TypeScript setup) and integrate the MVP backplane as per the `subproject-integration-guide.md`.

- **Core Dependencies**: Install and verify the following in `package.json`:
  - Runtime: `react`, `react-dom`, `@nednederlander/mvp-client`.
  - Development: `@types/react`, `@types/react-dom`, `@vitejs/plugin-react`.
  - TypeScript: `typescript`.
- **Server Dependencies**: In `pom.xml`, include the MVP server JAR and configure GitHub Packages repository.

- **Essential Development Dependencies**: Run the following command post-initialization to include Node.js type declarations:
  ```
  npm install --save-dev @types/node
  ```
  This provides type definitions for Node.js built-ins (e.g., `path`, `url`), preventing resolution errors in configuration files like `vite.config.ts`.

- **Verification Checklist**:
  - Confirm `npm install` succeeds without errors.
  - Review `package.json` scripts (e.g., `dev`, `build`, `lint`).

## React
- **Styling Practices**: Use themed CSS imports with inline comments explaining aesthetic choices. For cyberpunk themes, apply dark gray bases for backgrounds, black borders for structure, and green accents for interactive elements. Example:
  ```typescript
  import React from 'react';
  import './cyberpunk.css'; // Applies global cyberpunk theme: dark gray background, green text for cyberpunk aesthetic.

  interface MyComponentProps {
    label: string;
  }

  const MyComponent: React.FC<MyComponentProps> = ({ label }) => {
    const [state, setState] = React.useState('');
    return <div className="cyberpunk-panel">{label}: {state}</div>; // Class applies themed styles for enhanced UX.
  };

  export default MyComponent;

  ```

## TypeScript Configuration

TypeScript enforces type safety and code quality. Configurations must align with Vite's bundler mode for optimal performance.

- **Root `tsconfig.json`**: Use the composite project structure:
  ```json
  {
    "files": [],
    "references": [
      { "path": "./tsconfig.app.json" },
      { "path": "./tsconfig.node.json" }
    ]
  }
  ```

- **`tsconfig.app.json`** (for application code under `src/`):
  ```json
  {
    "compilerOptions": {
      "tsBuildInfoFile": "./node_modules/.tmp/tsconfig.app.tsbuildinfo",
      "target": "ES2022",
      "useDefineForClassFields": true,
      "lib": ["ES2022", "DOM", "DOM.Iterable"],
      "module": "ESNext",
      "skipLibCheck": true,

      /* Bundler mode */
      "moduleResolution": "bundler",
      "allowImportingTsExtensions": true,
      "verbatimModuleSyntax": true,
      "moduleDetection": "force",
      "noEmit": true,
      "jsx": "react-jsx",

      /* Linting */
      "strict": true,
      "noUnusedLocals": true,
      "noUnusedParameters": true,
      "erasableSyntaxOnly": true,
      "noFallthroughCasesInSwitch": true,
      "noUncheckedSideEffectImports": true,

      /* Path mapping for aliases */
      "baseUrl": ".",
      "paths": {
        "@/*": ["./src/*"]
      },
      "esModuleInterop": true
    },
    "include": ["src"]
  }
  ```

- **`tsconfig.node.json`** (for Node.js-specific files like `vite.config.ts`):
  ```json
  {
    "compilerOptions": {
      "tsBuildInfoFile": "./node_modules/.tmp/tsconfig.node.tsbuildinfo",
      "target": "ES2023",
      "lib": ["ES2023"],
      "module": "ESNext",
      "skipLibCheck": true,

      /* Bundler mode */
      "moduleResolution": "bundler",
      "allowImportingTsExtensions": true,
      "verbatimModuleSyntax": true,
      "moduleDetection": "force",
      "noEmit": true,

      /* Linting */
      "strict": true,
      "noUnusedLocals": true,
      "noUnusedParameters": true,
      "erasableSyntaxOnly": true,
      "noFallthroughCasesInSwitch": true,
      "noUncheckedSideEffectImports": true
    },
    "include": ["vite.config.ts"]
  }
  ```
  

- **Handling Strict Options**:
  - Use type-only imports for interfaces/types: `import type { InterfaceName } from '@/path';` to comply with `verbatimModuleSyntax`.
  - For unused variables (e.g., React setters): Prefix with underscore, e.g., `const [_setState] = useState(...)`.

### TypeScript Best Practices
Always import external libraries (e.g., import axios from 'axios';) to avoid TS2304 errors, analogous to Java package imports.

## Import/Export Conventions

Promote modularity and avoid deep relative paths.

- **Preferred Style**: Use named exports for types and components: `export interface TabConfig { ... }`.
- **Avoid**: Default exports for types unless explicitly justified.
- **Path Resolution**: Leverage aliases: `import type { TabConfig } from '@/types/TabConfig';`.
- **Organization**: Group shared types in `src/types/`, components in `src/components/`, hooks in `src/hooks/`, and services in `src/services/`.

## Vite Configuration Best Practices

`vite.config.ts` must support aliases and React out-of-the-box.

- **Standard Template**:
  ```typescript
  import { defineConfig } from 'vite';
  import react from '@vitejs/plugin-react';
  import path from 'path';
  import { fileURLToPath } from 'url';

  const __filename = fileURLToPath(import.meta.url);
  const __dirname = path.dirname(__filename);

  export default defineConfig({
    plugins: [react()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
  });
  ```

- **Testing**: Validate aliases with early imports in components.

## Command Execution Standards

To ensure safe and reliable command execution, follow these guardrails:

### Timeout Protection
- **Default Timeout**: All Maven/shell commands use 2-minute timeouts by default to prevent indefinite hangs
- **Configurable**: Timeout can be adjusted per command when longer execution is expected
- **Purpose**: Prevents hanging processes and maintains responsive development workflow

### Single Execution Policy
- **No Auto-Retry**: Failing commands are not automatically re-executed
- **Investigation First**: Analyze failures through logs, code review, or output analysis before retrying
- **Targeted Fixes**: Propose specific fixes based on failure analysis

### Failure Analysis Approach
- **Exit Code 1 Handling**: Analyze command output thoroughly before any retry attempts
- **Root Cause Focus**: If tests/commands fail repeatedly, prioritize fixing underlying code/logic
- **Diagnostic Tools**: Use logs, debug output, and code inspection for failure diagnosis. For npm commands, inspect npm logs via `npm run <command> --verbose`.

### Command Restrictions
- **Server Startup**: Use `npm run dev` for local development server in a separate terminal, but note potential port conflicts; do not run in chat.
- **Purpose**: Prevents port conflicts and enables independent server management

## Design Document Maintenance (MANDATORY)

**All design documents must be kept current and synchronized with code:**

- **Documentation Structure**: Maintain organized docs in `docs/` with clear folder structure (design/, guidelines/, BDD/)
- **Version Synchronization**: Update component diagrams, sequence diagrams, API specs, and architectural docs when code changes
- **Review Process**: Peer review required for all design document modifications
- **Change Tracking**: Document rationale for design changes and alternatives considered
- **Accessibility**: Ensure docs are clear, well-structured, and discoverable

**Commits that modify code without updating related design documents are not permitted.**

## API Contract Enforcement (MANDATORY)

**API contracts must be explicitly defined, validated, and enforced to ensure client-server consistency:**

- **Contract Definition**: Maintain a single API contract specification file (e.g., `api-contracts.json`) in the server resources, defining all endpoints, schemas, and data models. This serves as the authoritative source for API behavior.
- **Validation Requirements**: Validate the contract against implementation before any commit. Use automated tools (e.g., code generation, schema validation, or linting) to detect mismatches in endpoints, request/response structures, or data types. Manual inspection required if automated tools are unavailable.
- **Update Process**: When modifying APIs (adding/removing endpoints, changing schemas), update the contract spec first, then regenerate/update dependent code (e.g., DTOs, client SDKs). Document changes in design docs and commit spec alongside implementation.
- **Testing Safeguards**: Include contract compliance in tests—e.g., integration tests with mocked endpoints matching the spec, schema validation in unit tests, and E2E tests verifying real API calls. Achieve 100% coverage of spec-defined endpoints in tests.
- **Enforcement Mechanisms**: Implement build-time checks (e.g., via Maven plugins or scripts) to enforce contract adherence. Peer review mandatory for contract changes. No API code changes without passing validation and tests.
- **Versioning and Sync**: Version the contract spec semantically. Ensure client/server sync via generated code or manual alignment. Logs must reflect contract compliance.

**Failures in contract enforcement will result in build/test failures and prevent commits.**

## Verification and Testing Processes

Incorporate checks at milestones to catch issues proactively.

- **Periodic Build Review**: Run `npm run build` after configuration changes, before commits, and at each incremental stage end. Inspect for TypeScript errors (e.g., TS6133, TS1484) and resolve all.
- **Development Workflow**:
  1. `npm install` for dependencies.
  2. `npm run dev` for hot reloading (run in separate terminal, do not run in chat).
  3. `npm test` for unit/integration tests (using Jest/Vitest).
  4. BDD if applicable (e.g., Cypress for E2E): `npm run cypress:run`.
  5. Browser console checks for runtime errors.
- **IDE Tips**: Restart TypeScript server post-config updates. Use VS Code extensions: ESLint, Prettier, TypeScript Importer.

## Common Pitfalls and Resolutions

- **Export Mismatches**: Verify named exports match imports; use type-only for pure types.
- **Alias Discrepancies**: Ensure `tsconfig` paths mirror Vite `resolve.alias`.
- **Unused Declarations**: Use underscores for intentional omissions.
- **Node.js Types**: Always install `@types/node` to resolve built-in module errors.
- **Frontend-Specific**: TS alias mismatches (resolve: `npm run type-check`); React hook rules violations (fix: ESLint --fix).

## Development Environment Setup

### Git Configuration
Always ensure proper `.gitignore` configuration to prevent committing system files and build artifacts:

- **macOS Users**: `.DS_Store` files are automatically ignored
- **IDE Files**: `.vscode/`, `.idea/` directories are ignored
- **Build Artifacts**: `dist/`, `node_modules/`, `*.log` files are ignored
- **Environment and Secret Files**: `.env*`, `.npmrc`, `config/secrets.json`, `config/*.key` files are ignored for security to prevent exposing tokens or credentials

**Before initial commit**: Verify `.gitignore` exists and matches this recommended configuration exactly.

**Version Alignment**: Ensure client and server artifact versions match (e.g., if server is 0.0.2-SNAPSHOT, client should be ^0.0.2) to maintain compatibility.

## Mandatory Pre-Commit Checklist

**NO CODE CHANGES MAY BE COMMITTED WITHOUT COMPLETING THIS CHECKLIST:**

### Design Documentation
- [ ] Design documents updated to reflect planned changes and MVP backplane integration
- [ ] Component diagrams, sequence diagrams, API specs synchronized with code and backplane contracts
- [ ] Peer review completed for design document changes, including backplane compatibility
- [ ] Documentation committed alongside code changes
- [ ] API contract spec validated against code and MVP backplane (e.g., via generation or schema checks)

### Code Quality
- [ ] `npm run lint` and `npm run build` succeed without errors
- [ ] Code follows established coding standards
- [ ] Inline comments explain complex logic
- [ ] No TODO/FIXME comments left unresolved

### Testing Requirements
- [ ] `npm test` passes all unit and integration tests (80%+ coverage)
- [ ] BDD/E2E tests (e.g., Cypress) pass if applicable
- [ ] New business functionality has corresponding tests
- [ ] Existing tests still pass (no regressions)
- [ ] API contract compliance tested (e.g., integration tests, schema validation)

### Verification Steps
- [ ] Manual UX testing of core functionality completed
- [ ] Logs reviewed for errors or warnings
- [ ] Build artifacts cleaned (`npm run clean`)
- [ ] Contract spec updated and synchronized with API changes
- [ ] Run Cucumber tests locally before push
- [ ] Commit message clearly describes changes and links to updated docs

**FAILURE TO COMPLETE ANY CHECKLIST ITEM PREVENTS COMMITTING**

Review this document periodically as the project evolves. Non-compliance requires justification in pull requests.

## See Also
- [Coding Standards](../guidelines/coding-standards.md): Core standards.
- [Subproject Testing Guidelines](subproject-testing-guidelines.md): Testing practices.
- [Subproject Integration Guide](subproject-integration-guide.md): Setup details.