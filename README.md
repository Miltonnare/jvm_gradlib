# Aura Document Processor (ADP)

Aura Document Processor (ADP) is a high-performance, enterprise-grade JVM library designed for processing, transforming, validating, and replaying complex structured binary and textual data. 

Built from the ground up to have zero runtime external dependencies, ADP operates with a strong security boundary, robust bounds checking, and stateful verification, making it ideal for sandboxed environments and fuzzing-driven development (OSS-Fuzz/ClusterFuzzLite).

## Key Features

- **Binary Container Format (`.adc`)**: An archive container that packs structured binary metadata, layouts, assets, and replaying event streams with CRC32 integrity checks.
- **Custom Configuration Language (Xon Config - `.xcf`)**: A clean JSON/YAML-like syntax parser with optional commas, line comments, type validation, and schema definitions.
- **Binary Structured Metadata (BSM)**: A highly-efficient Tag-Length-Value (TLV) key-value serialization model supporting nested maps and lists.
- **Sliding-Window Decompressions**: Custom, fuzzer-targeted implementations of sliding-window LZ77 and Run-Length Encoding (RLE) algorithms.
- **Transformation Pipeline**: Modular, type-safe transform pipelines that run textual extraction and converts layout models to semantic HTML or Markdown.
- **Document Graph Engine**: Computes nodes layout relationships, resolves inline calculation expressions, and performs topological cycle detection.
- **State Machine Event Replays**: Interprets event logs in binary formats, replaying state transitions with guard conditions and context mutations.

## Quick Start

### Build Project
Compile the library and run JUnit unit tests locally:
```bash
./gradlew build
```

### Direct Javac Compile (Offline Fallback)
For offline environments without Gradle wrapper distributions:
```bash
# Generate file list
find src/main/java -name "*.java" > sources.txt
# Compile classes
mkdir -p build/classes/main
javac -d build/classes/main @sources.txt
```

### Run Fuzzing Harnesses
ADP contains 8 pre-configured Jazzer fuzzing targets inside the `fuzz/` directory.

To run compilations and register targets in a ClusterFuzzLite environment:
```bash
export OUT=/path/to/output
export SRC=$(pwd)
.clusterfuzzlite/build.sh
```

## Documentation Sitemap
For deeper engineering details, please consult:
1. [ARCHITECTURE.md](Architecture.md) - Design patterns, packages, and data flows.
2. [FORMAT_SPEC.md](FORMAT_SPEC.md) - Binary specs for ADC, BSM, and XCF syntax specs.
3. [FUZZING.md](FUZZING.md) - Jazzer targets and corpus guide.
4. [BUILD.md](BUILD.md) - Developer compilation and offline environment settings.
5. [TESTING.md](TESTING.md) - JUnit test execution and coverage reports.
6. [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Catalog of folders and source responsibilities.
7. [SECURITY.md](SECURITY.md) - Bounds validation, stack protection, and cryptography interfaces.
8. [CONTRIBUTING.md](CONTRIBUTING.md) - Developer onboarding, formatting, checkstyle, and PMD.
9. [LICENSE](LICENSE) - Project licensing terms.
