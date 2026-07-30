# Project Directory Structure

Below is a catalog of files and folders in the Aura Document Processor repository:

```
project/
├── .clusterfuzzlite/          # ClusterFuzzLite integrations
│   ├── build.sh               # Offline build compilation script
│   └── project.yaml           # Lists active Jazzer targets
│
├── config/
│   └── checkstyle/
│       └── checkstyle.xml     # Code style layouts configuration
│
├── fuzz/                      # Jazzer fuzz targets
│   ├── corpus/                # Seed corpus directory
│   │   ├── ArchiveDecoderFuzzer/
│   │   ├── BinaryIndexFuzzer/
│   │   ├── ConfigurationParserFuzzer/
│   │   ├── ContainerParserFuzzer/
│   │   ├── EventReplayFuzzer/
│   │   ├── MetadataParserFuzzer/
│   │   ├── ObjectGraphFuzzer/
│   │   └── StreamReaderFuzzer/
│   │
│   ├── ArchiveDecoderFuzzer.java
│   ├── BinaryIndexFuzzer.java
│   ├── ConfigurationParserFuzzer.java
│   ├── ContainerParserFuzzer.java
│   ├── EventReplayFuzzer.java
│   ├── MetadataParserFuzzer.java
│   ├── ObjectGraphFuzzer.java
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
│   │               │   ├── compression/    # LZ77, RLE decompressions
│   │               │   └── encryption/     # XOR key permutations, AES wraps
│   │               │
│   │               ├── config/             # XCF lexer, parser, validator
│   │               ├── container/          # ADC container readers/writers
│   │               ├── engine/             # FSM transitions, templates
│   │               ├── internal/           # Low level CRC32, endian helpers
│   │               ├── metadata/           # BSM metadata reader/writers
│   │               ├── model/              # Document nodes AST, queries, math scripts
│   │               ├── pipeline/           # HTML/Markdown renders, format mappings
│   │               ├── stream/             # Bounded streams, bit-level buffers
│   │               └── validation/         # Topological DFS check engine
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
├── build.gradle               # Gradle build script settings
├── settings.gradle            # Project name setup
├── gradlew                    # Unix shell bootstrap wrapper
├── gradlew.bat                # Windows batch bootstrap wrapper
├── README.md                  # Project overview manual
├── ARCHITECTURE.md            # Packages and pipelines description
├── FORMAT_SPEC.md             # Binary spec layout guides
├── FUZZING.md                 # Fuzz instructions
├── CONTRIBUTING.md            # Developing requirements
├── SECURITY.md                # Sandbox bounds limits policy
├── BUILD.md                   # Build and compile manual
├── TESTING.md                 # Verification manual
└── LICENSE                    # Licensing terms
```
