#!/bin/bash -eu
# Clean, offline compilation script for ClusterFuzzLite fuzz targets.
# Never downloads external dependencies, compiles directly using local JDK and Jazzer API.

# 1. Locate Jazzer API jar
JAZZER_API=""

if [ -n "${JAZZER_API_PATH:-}" ] && [ -f "$JAZZER_API_PATH" ]; then
  JAZZER_API="$JAZZER_API_PATH"
fi

if [ -z "$JAZZER_API" ]; then
  # Try search standard directories
  found_jar=$(find /usr /usr/local /lib /lib64 /opt -name "*jazzer_api*.jar" 2>/dev/null | head -n 1)
  if [ -n "$found_jar" ] && [ -f "$found_jar" ]; then
    JAZZER_API="$found_jar"
  fi
fi

if [ -z "$JAZZER_API" ]; then
  # Fallback to hardcoded list
  for path in \
      /usr/local/lib/jazzer_api_deploy.jar \
      /usr/local/share/jazzer/jazzer_api_deploy.jar \
      /usr/lib/jazzer/jazzer_api_deploy.jar \
      /usr/lib/jazzer/jazzer_api.jar \
      /usr/local/share/jazzer/jazzer_api.jar; do
    if [ -f "$path" ]; then
      JAZZER_API="$path"
      break
    fi
  done
fi

if [ -z "$JAZZER_API" ]; then
  echo "Error: jazzer_api.jar not found in standard installation directories."
  exit 1
fi
echo "Using Jazzer API: $JAZZER_API"

# 2. Setup compilation directories
mkdir -p build/classes/main
mkdir -p build/classes/fuzz

# 3. Compile main project sources
find src/main/java -name "*.java" > sources_main.txt
javac -d build/classes/main @sources_main.txt

# 4. Compile fuzz targets
find fuzz -maxdepth 1 -name "*.java" > sources_fuzz.txt
javac -cp "$JAZZER_API:build/classes/main" -d build/classes/fuzz @sources_fuzz.txt

# 5. Build combined JAR file in $OUT
jar cvf "$OUT/aura-fuzzers.jar" -C build/classes/main . -C build/classes/fuzz .

# 6. Generate wrapper scripts for each fuzzer target
FUZZERS=(
  "ContainerParserFuzzer"
  "ArchiveDecoderFuzzer"
  "MetadataParserFuzzer"
  "ObjectGraphFuzzer"
  "StreamReaderFuzzer"
  "BinaryIndexFuzzer"
  "ConfigurationParserFuzzer"
  "EventReplayFuzzer"
  "QueryParserFuzzer"
  "KeyRingParserFuzzer"
  "HtmlImporterFuzzer"
  "MarkdownImporterFuzzer"
  "CssStyleParserFuzzer"
)

for fuzzer in "${FUZZERS[@]}"; do
  # Create wrapper executable
  cat << EOF > "$OUT/$fuzzer"
#!/bin/sh
# Wrapper script to execute $fuzzer
this_dir=\$(dirname "\$0")
exec jazzer \\
  --cp="\$this_dir/aura-fuzzers.jar" \\
  --target_class="$fuzzer" \\
  "\$@"
EOF
  chmod +x "$OUT/$fuzzer"

  # Copy dictionary
  cp fuzz/dictionary.txt "$OUT/$fuzzer.dict"
done

# 7. Zip seed corpuses into $OUT
if [ -d fuzz/corpus ]; then
  cd fuzz/corpus
  for fuzzer_dir in *; do
    if [ -d "$fuzzer_dir" ]; then
      zip -j -r "$OUT/${fuzzer_dir}_seed_corpus.zip" "$fuzzer_dir"
    fi
  done
  cd -
fi

echo "Offline fuzzer compilation completed successfully."
