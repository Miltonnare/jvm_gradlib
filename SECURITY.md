# Security Policy

This document outlines the validation limits and security contact details for the Aura Document Processor (ADP).

## 1. Input Validation and Limits

ADP implements bounds validation and size limits on input streams:

- **Input Boundaries**: Byte read limits are configured on decompression streams to manage input sizes during decompressions.
- **Nesting Limits**: The XCF config parser, BSM decoder, and template rendering engine enforce recursion depth limits (64 and 16 respectively) to avoid stack recursion failures.
- **Graph Reference Limits**: Topological sort cycle validation is performed on document graphs to prevent infinite loop resolutions.
- **Index Bounds Validation**: Array conversions check index bounds before accessing underlying bytes.

## 2. Reporting Issues

To report security issues or bugs, please contact our maintenance team by sending an email with:
- The class name or trace details.
- A minimal reproducing file or input data.
- The details of the observed failure behavior.
