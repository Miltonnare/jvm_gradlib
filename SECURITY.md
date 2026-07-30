# Security Policy

Aura Document Processor (ADP) is designed for security-sensitive sandboxed document parsing environments. 

## 1. Security Architecture & Controls

ADP implements strict defensive coding boundaries to block memory exhaustion (Denial of Service), infinite loops, and out-of-bounds array reads:

-   **Zip Bomb Mitigation**: Binary streams are wrapped in `BoundedInputStream` to restrict raw read limits and prevent expansion attacks during LZ77 or RLE decompression.
-   **Nesting Limits**: The XCF text config parser, BSM decoder, and template rendering engines enforce a maximum recursion depth limit (64 and 16 respectively) to avoid stack overflows.
-   **Topological Cycle Checks**: Topological reference validation is executed on the document graph, preventing recursive loop resolving during markdown and HTML rendering.
-   **Strict Bounds Checks**: Byte array conversions (Big/Little Endian) validate offset range limits before reading primitives.

---

## 2. Reporting Vulnerabilities

If you discover a security vulnerability (such as a parsing bypass, out-of-bounds read, or potential resource exhaustion crash), please report it to our security team immediately.

Do **NOT** open public GitHub issues for security-critical bugs. Instead, email a detailed bug report containing:
- Code location or fuzzer trace.
- A minimal reproducing file/sample.
- An impact description.

We aim to evaluate and respond to all reports within 48 hours.
