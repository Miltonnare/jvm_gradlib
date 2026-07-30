# Format Specifications

This document defines the binary layout of the Aura Document Container (`.adc`), the Binary Structured Metadata (`.bsm`), the FSM Event Log streams, and the grammar of the Xon Config Format (`.xcf`).

---

## 1. Aura Document Container (`.adc`) Layout

An `.adc` file is a packed archive containing assets, layout structures, and event logs. It is laid out sequentially:

```
┌──────────────────────────────────────┐
│  Header (28 bytes)                   │
├──────────────────────────────────────┤
│  Payload Data Block (Variable)       │
├──────────────────────────────────────┤
│  Metadata Block (Variable)           │
├──────────────────────────────────────┤
│  Index Block (Variable)              │
└──────────────────────────────────────┘
```

### 1.1 Header block (28 bytes)
- **Offset 0 (4 bytes)**: Magic bytes `0x41 0x55 0x52 0x41` ("AURA").
- **Offset 4 (1 byte)**: Major Version (must be `0x01`).
- **Offset 5 (1 byte)**: Minor Version.
- **Offset 6 (2 bytes)**: Flags (short, e.g., Bit 0 = Encryption Enabled).
- **Offset 8 (8 bytes)**: Metadata Block Absolute Offset (long).
- **Offset 16 (8 bytes)**: Index Block Absolute Offset (long).
- **Offset 24 (4 bytes)**: Header Checksum (CRC32 of the first 24 header bytes).

### 1.2 Index entry structure
- **0-15 (16 bytes)**: UUID (Long MostSig, Long LeastSig).
- **16 (1 byte)**: File Type tag (0x01 = text, 0x02 = image, 0x03 = config, 0x04 = binary, 0x05 = event log).
- **17 (1 byte)**: Compression algorithm (0 = None, 1 = RLE, 2 = LZ77).
- **18-25 (8 bytes)**: Absolute offset in file (long).
- **26-33 (8 bytes)**: Uncompressed payload size in bytes (long).
- **34-41 (8 bytes)**: Compressed payload size in bytes (long).
- **42-45 (4 bytes)**: Payload Checksum (CRC32 of the raw payload block as written).
- **46-47 (2 bytes)**: Name string length (unsigned short).
- **48+ (variable)**: UTF-8 Resource Name.

---

## 2. Binary Structured Metadata (`.bsm`) Layout

A `.bsm` block represents serialized key-value maps. It contains a sequence of entries terminated by EOF:

- **Offset 0 (1 byte)**: Value type tag:
  * `0x01`: Integer (4 bytes)
  * `0x02`: Long (8 bytes)
  * `0x03`: Double (8 bytes)
  * `0x04`: Boolean (1 byte, 0x00=false, 0x01=true)
  * `0x05`: UTF-8 String (length = value length)
  * `0x06`: Raw Binary (length = value length)
  * `0x07`: Nested Map (value is serialized Map bytes)
  * `0x08`: Array (first 4 bytes = element count, then entries containing type, value length, and value bytes)
- **Offset 1 (2 bytes)**: Key string length (unsigned short).
- **Offset 3 (variable)**: UTF-8 Key Name.
- **Offset 3+KeyLength (4 bytes)**: Value size in bytes (int).
- **Offset 7+KeyLength (variable)**: Serialized Value payload.

---

## 3. FSM Event Log Streams

Replay event logs are stored in a binary stream starting with a 4-byte count of total events:

- **0-3 (4 bytes)**: Total Event Count (int).
- **For each Event**:
  * **0-7 (8 bytes)**: Timestamp offset in milliseconds (long).
  * **8-9 (2 bytes)**: Trigger name length (unsigned short).
  * **10+ (variable)**: UTF-8 Trigger Name.
  * **(variable) (2 bytes)**: Count of Context Mutations (unsigned short).
  * **For each Context Mutation**:
    - **(2 bytes)**: Variable key name length (unsigned short).
    - **(variable)**: UTF-8 Variable key.
    - **(1 byte)**: Type tag (1=int, 2=long, 3=boolean, 4=string).
    - **(4 bytes)**: Value length (int).
    - **(variable)**: Mutation value bytes.

---

## 4. Xon Config Format (`.xcf`) Specification

XCF is a human-readable text configuration format.

### 4.1 Syntax Rules
- **Comments**: Start with `#` or `//` and extend to the end of the line.
- **Root**: Must be an object `{}` or array `[]`.
- **Objects**: Mappings of `key : value` separated by optional or required commas `,`.
- **Arrays**: Sequence of values separated by optional or required commas `,` enclosed in `[]`.
- **Keys**: String literals enclosed in `"` or `'`, or unquoted identifiers.
- **Values**: String literals, numbers (integers, floating decimals, or exponents), booleans (`true`/`false`), `null` keywords, or nested objects/arrays.
- **Escape Characters**: Standard backslash escapes (`\n`, `\t`, `\r`, `\"`, `\\`) are supported inside quoted string literals.
