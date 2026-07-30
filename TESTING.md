# Testing and Verification Manual

This document describes the unit testing setup, static analysis checks, and coverage reports configured for the project.

## 1. Unit & Integration Testing

We use **JUnit 5** to test every module. The test suite is organized into matching packages under `src/test/java/com/aura/`.

### Run all tests
```bash
./gradlew test
```

The tests verify:
- Byte manipulation conversions and endianness.
- Stream boundary parameters and EOF conditions.
- LZ77 compression matching, RLE codecs, and word-level dictionary compressors.
- BSM TLV reader/writer symmetry and schema validations.
- Configuration syntax parsing and validation rules.
- Document graph cycle checking and topological sorting.
- FSM events, log compaction, context mutations, and template processing.

## 2. Code Coverage (JaCoCo)

JaCoCo is configured to generate coverage reports.

After running the test suite, reports are compiled under:
`build/reports/jacoco/test/html/index.html`

Open this file in a browser to view coverage metrics by package and class.

## 3. Static Code Analysis

We use Checkstyle, PMD, and SpotBugs to verify coding conventions and detect bugs.

### Run all analysis checks
```bash
./gradlew check
```

This runs:
- **Checkstyle**: Checks formatting limits (line wraps, brace placements). Configured in `config/checkstyle/checkstyle.xml`.
- **PMD**: Scans for unused variables, complexity, and other code style issues.
- **SpotBugs**: Runs byte-code checks to identify null pointer potentials and resource leaks.
