# Build Guide

This document describes how to compile the Aura Document Processor (ADP) in local developer environments and offline container environments.

## 1. Prerequisites

- **Java Development Kit (JDK)**: Version 17 or higher is required.
- **Gradle**: Local builds use the self-bootstrapping Gradle wrapper (version 8.5).

---

## 2. Standard Local Compilation

To compile and package the library jar file locally:
```bash
# Clean previous builds and compile
./gradlew clean build
```
This downloads project testing dependencies and packages the core library under:
`build/libs/aura-document-processor-1.0.0-SNAPSHOT.jar`

---

## 3. Direct Javac Compilation (Zero Network Dependencies)

In air-gapped or restricted offline environments, you can compile the library without invoking Gradle or fetching external assets:

```bash
# 1. Compile main library files
mkdir -p build/classes/main
find src/main/java -name "*.java" > sources_main.txt
javac -d build/classes/main @sources_main.txt

# 2. Package main library jar
jar cvf build/libs/aura-document-processor.jar -C build/classes/main .
```

---

## 4. ClusterFuzzLite Build Compilation

ClusterFuzzLite compiles the fuzz targets inside an offline Docker builder container using the script:
`.clusterfuzzlite/build.sh`

This script:
1. Locates the pre-installed Jazzer API jar from standard directories.
2. Compiles all main project source files.
3. Compiles the Jazzer fuzzing targets from `fuzz/`.
4. Creates a consolidated jar file `aura-fuzzers.jar` and wrapper runner scripts inside `$OUT/`.
5. Packs the seed corpus folders into zip files inside `$OUT/`.
