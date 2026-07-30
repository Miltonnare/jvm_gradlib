# Aura Document Processor (ADP)

Aura Document Processor (ADP) is a Java library for processing, transforming, validating, and replaying structured binary and textual formats. ADP operates without runtime external dependencies.

## Key Features

- **Binary Container Format (`.adc`)**: Pack structured binary metadata, layouts, assets, and event streams.
- **Custom Configuration Language (Xon Config - `.xcf`)**: Text parser with support for comments, type validation, and schema definitions.
- **Binary Structured Metadata (BSM)**: Tag-Length-Value (TLV) key-value serialization for maps and lists.
- **Compression Codecs**: Implementation of sliding-window LZ77 and Run-Length Encoding (RLE) algorithms.
- **Transformation Pipeline**: Convert document layouts into HTML or Markdown formats.
- **Document Graph Engine**: Resolves layout node relationships and expressions.
- **Event Replay Engine**: Replays context mutations from binary event logs.

## Building the Project

Compile the library and run tests using the Gradle wrapper:
```bash
./gradlew build
```

### Direct Javac Compile (Alternative)
To compile without Gradle:
```bash
find src/main/java -name "*.java" > sources.txt
mkdir -p build/classes/main
javac -d build/classes/main @sources.txt
```

## Running Fuzzing Targets
The fuzz targets are located under the `fuzz/` directory.

To build and package them offline:
```bash
export OUT=/path/to/output
export SRC=$(pwd)
.clusterfuzzlite/build.sh
```

## Documentation
1. [Architecture Design](Architecture.md) - Project package structure and data flow.
2. [Format Specifications](FORMAT_SPEC.md) - Specifications for ADC, BSM, and XCF formats.
3. [Build Guide](BUILD.md) - Compilation instructions.
4. [Testing Manual](TESTING.md) - Unit test and coverage guides.
5. [Project Directory Structure](PROJECT_STRUCTURE.md) - File and folder layouts.
6. [Contributing Guidelines](CONTRIBUTING.md) - Guidelines for code style and tests.
