# Project Directory Structure

Below is a catalog of files and folders in the Aura Document Processor repository:

```
project/
├── .clusterfuzzlite/          # ClusterFuzzLite build configurations
│   ├── build.sh               # Offline build compilation script
│   └── project.yaml           # Target registration settings
│
├── config/
│   └── checkstyle/
│       └── checkstyle.xml     # Code style settings
│
├── fuzz/                      # Fuzz targets
│   ├── corpus/                # Seed corpus directories
│   │   ├── ArchiveDecoderFuzzer/
│   │   ├── BinaryIndexFuzzer/
│   │   ├── ConfigurationParserFuzzer/
│   │   ├── ContainerParserFuzzer/
│   │   ├── CssStyleParserFuzzer/
│   │   ├── EventReplayFuzzer/
│   │   ├── HtmlImporterFuzzer/
│   │   ├── KeyRingParserFuzzer/
│   │   ├── MarkdownImporterFuzzer/
│   │   ├── MetadataParserFuzzer/
│   │   ├── ObjectGraphFuzzer/
│   │   ├── QueryParserFuzzer/
│   │   └── StreamReaderFuzzer/
│   │
│   ├── ArchiveDecoderFuzzer.java
│   ├── BinaryIndexFuzzer.java
│   ├── ConfigurationParserFuzzer.java
│   ├── ContainerParserFuzzer.java
│   ├── CssStyleParserFuzzer.java
│   ├── EventReplayFuzzer.java
│   ├── HtmlImporterFuzzer.java
│   ├── KeyRingParserFuzzer.java
│   ├── MarkdownImporterFuzzer.java
│   ├── MetadataParserFuzzer.java
│   ├── ObjectGraphFuzzer.java
│   ├── QueryParserFuzzer.java
│   ├── StreamReaderFuzzer.java
│   └── dictionary.txt         # Common fuzz dictionary tokens
│
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── aura/
│   │               ├── codec/
│   │               │   ├── compression/    # LZ77, RLE, word compression
│   │               │   └── encryption/     # XOR key permutations, KeyDerivation, AES wraps
│   │               │
│   │               ├── config/             # XCF lexer, parser, writers, validators
│   │               ├── container/          # ADC container readers/writers, IndexCache
│   │               ├── engine/             # FSM transitions, event log writers/compactors, templates
│   │               ├── internal/           # Low level CRC32, endian, array, math helpers
│   │               ├── metadata/           # BSM metadata schemas, readers/writers
│   │               ├── model/              # Document nodes AST, queries, evaluators
│   │               ├── pipeline/           # HTML/Markdown renders, paginators, StageExecutors
│   │               ├── stream/             # Bounded streams, bit-level buffers
│   │               └── validation/         # Topological DFS checks, patch engine
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── aura/
│                   ├── codec/
│                   ├── config/
│                   ├── engine/
│                   ├── internal/
│                   ├── metadata/
│                   ├── model/
│                   ├── pipeline/
│                   ├── stream/
│                   └── SeedGenerator.java   # Seed corpus compiler tool
│
├── build.gradle               # Gradle build settings
├── settings.gradle            # Gradle project settings
├── gradlew                    # Unix bootstrap wrapper script
├── gradlew.bat                # Windows bootstrap wrapper script
├── README.md                  # Project overview manual
├── Architecture.md            # Architecture and package descriptions
├── FORMAT_SPEC.md             # Format specifications guide
├── CONTRIBUTING.md            # Guidelines for contributions
├── BUILD.md                   # Compilation and build instructions
├── TESTING.md                 # Testing and coverage details
└── LICENSE                    # Project license terms
```
