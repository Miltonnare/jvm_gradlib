# Fuzzing Guide (Jazzer & ClusterFuzzLite)

This project is engineered to support thorough, continuous security fuzzing. It defines 8 distinct fuzzer targets in the `fuzz/` directory, testing every parsing boundary, state transition, and compression codec.

## Fuzzing Targets Catalog

1.  **`ContainerParserFuzzer`**: Fuzzes the `AuraContainerReader`. Evaluates container headers, index block allocations, alignment, and checks for CRC32 validation bypasses.
2.  **`ArchiveDecoderFuzzer`**: Fuzzes sliding window LZ77 and Run-Length Encoding (RLE) decompressors. This is highly sensitive to index out of bounds, infinite loops, and stack exhaustion.
3.  **`MetadataParserFuzzer`**: Fuzzes the BSM (Binary Structured Metadata) reader, targeting TLV deserialization depth, array bounds, and negative length limits.
4.  **`ObjectGraphFuzzer`**: Fuzzes document structure generation. Integrates parsing, format conversion, topological DFS cycle checks, markdown, and HTML rendering in a single deep execution path.
5.  **`StreamReaderFuzzer`**: Fuzzes bit-level input buffers, bounded streams, and checksum computations.
6.  **`BinaryIndexFuzzer`**: Fuzzes the deserialization of container resource indexes.
7.  **`ConfigurationParserFuzzer`**: Fuzzes the XCF syntax lexer and recursive descent parser.
8.  **`EventReplayFuzzer`**: Fuzzes state machine event log replaying, verifying that context updates and transition triggers do not cause unexpected exceptions.

---

## Running Fuzzers Locally

To run the fuzzers locally on your machine, you must have the Jazzer agent installed.

### 1. Compile project and fuzz targets
```bash
# Compile library classes
./gradlew compileJava
# Compile fuzz targets (pointing to local Jazzer jar)
javac -cp "path/to/jazzer_api.jar:build/classes/java/main" -d build/classes/fuzz fuzz/*.java
```

### 2. Execute fuzzer target
Run Jazzer pointing to the output classpath:
```bash
jazzer \
  --cp="build/classes/java/main:build/classes/fuzz" \
  --target_class=ContainerParserFuzzer \
  -dict=fuzz/dictionary.txt \
  fuzz/corpus/ContainerParserFuzzer
```

---

## ClusterFuzzLite Integration

When submitted to a private repository for ClusterFuzzLite continuous integration:

1.  **`.clusterfuzzlite/project.yaml`** declares all active fuzzing executables.
2.  **`.clusterfuzzlite/build.sh`** compiles all targets completely offline inside the builder container using standard `javac` and the pre-installed Jazzer API.
3.  The build script automatically packages all classes into `aura-fuzzers.jar` and writes out wrapper scripts and zipped seed corpuses (`[fuzzer_name]_seed_corpus.zip`) into `$OUT`.
4.  ClusterFuzzLite automatically schedules coverage-guided fuzz runs using the provided seed corpus and the `fuzz/dictionary.txt` token definitions.
