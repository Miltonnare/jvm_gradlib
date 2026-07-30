# Aura Document Processor

Aura Document Processor is a library for parsing, validating, and converting structured layout document models.

## Purpose

The library provides components to parse custom configuration formats, compile layout templates, serialize binary structured metadata, and convert document models to HTML or Markdown representations.

## Directory Structure and Organization

- `fuzz/`: Contain Jazzer fuzz targets used to verify parsing robustness.
- `src/main/java/com/aura/`: Main source files of the library.
  - `codec/`: Codecs for run-length and Lz77 compression and basic encryption.
  - `config/`: Parser and writer for the Xon configuration format.
  - `container/`: Reader and writer for Aura Document Containers.
  - `engine/`: Log compactors, template compilers, and state machines.
  - `metadata/`: Schema verification and decoders for binary structured metadata.
  - `model/`: Node graph structures and DOM query expression parsers.
  - `pipeline/`: Formatting stages for CSS parsers, Markdown, and HTML importers.
  - `stream/`: Helper inputs, seeking buffers, and bit streams.
  - `validation/`: Cycle detection, topological sorting, and diff/patch tools.
- `src/test/java/com/aura/`: Unit tests targeting library packages.

## Build and Compilation

To build the project:
```bash
./gradlew build
```
