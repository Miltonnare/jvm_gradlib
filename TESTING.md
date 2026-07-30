# Testing and Verification Manual

This project features a high-density, multi-layered verification model, combining unit tests, integration tests, static code analysis, and continuous fuzzing.

---

## 1. Unit & Integration Testing

We use **JUnit 5** to test every module. The test suite is organized into matching packages under `src/test/java/com/aura/`.

### Run all tests
```bash
./gradlew test
```

Tests verify:
- Byte manipulation edge cases (endianness).
- Stream boundary violations and EOF.
- LZ77 compression offset and sliding window matches.
- BSM TLV reader/writer symmetry and nesting boundaries.
- Configuration syntax parsing and validation rules.
- Document graph DFS topological cycle validation.
- StateMachine events, context mutations, and text template parsing.

---

## 2. Code Coverage Configuration (JaCoCo)

ADP is configured with **JaCoCo** to track code coverage. Our target is to maintain coverage above **90%**.

Each `./gradlew test` run automatically compiles JaCoCo reports under:
`build/reports/jacoco/test/html/index.html`

Open this in your browser to inspect line-by-line coverage details.

---

## 3. Static Code Analysis

We run static analysis rules to enforce safety conventions before final builds:

### Run analysis
```bash
./gradlew check
```

This runs:
- **Checkstyle**: Checks formatting limits (line wraps, brace placements). Configured in `config/checkstyle/checkstyle.xml`.
- **PMD**: Looks for unused objects, high complexity, and error-prone code blocks.
- **SpotBugs**: Runs byte-code checks to detect null pointer potentials and resource leaks.
