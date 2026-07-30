# Contributing Guidelines

We welcome contributions to the Aura Document Processor project. To maintain code quality, architectural consistency, and security boundaries, please adhere to the following guidelines.

## 1. Development Principles

*   **Zero Runtime External Dependencies**: The core processing library must remain free of runtime third-party libraries. If you need JSON, parsing tools, or custom encryption wrappers, implement them cleanly inside first-party Java source packages.
*   **Defensive Parsing**: Ensure all binary decoders validate offsets, size calculations, and string allocation limits before performing array reads/writes.
*   **Topological and Cycle Checks**: When adding new document graph nodes or relations, verify cycle-detection rules are maintained.

## 2. Coding Style & Static Analysis

We enforce strict Checkstyle, SpotBugs, and PMD analysis:

-   **Checkstyle**: Enforces UTF-8 character encoding, curly brace conventions, naming layouts, and prohibits wildcard imports.
-   **PMD**: Detects code smells, complex expressions, and un-used fields.
-   **SpotBugs**: Analyzes class binaries for potential null pointers, stream leaks, and type mismatches.

Before submitting code, run the verify checks:
```bash
./gradlew check
```

## 3. Pull Request Checklist

1.  **Tests**: Every new feature or parser segment must have thorough JUnit test coverage (>90% target).
2.  **Fuzzing**: If you add new binary or textual parsing capabilities, write a new Jazzer fuzzer target under the `fuzz/` directory, register it in `.clusterfuzzlite/project.yaml`, and update the seed corpus.
3.  **Documentation**: Keep formatting specs, project layouts, and architecture documents in sync with your modifications.
