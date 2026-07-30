package com.aura.metadata;

import com.aura.internal.Preconditions;

/**
 * Defines constraint rules for validation of specific BSM Tag-Length-Value fields.
 */
public class BsmFieldConstraint {
    private final String keyName;
    private final MetadataType expectedType;
    private final boolean required;
    private final Long minVal;
    private final Long maxVal;
    private final Integer minLen;
    private final Integer maxLen;

    public static class Builder {
        private final String keyName;
        private final MetadataType expectedType;
        private boolean required = false;
        private Long minVal = null;
        private Long maxVal = null;
        private Integer minLen = null;
        private Integer maxLen = null;

        public Builder(String keyName, MetadataType expectedType) {
            this.keyName = Preconditions.checkNotNull(keyName, "Key name cannot be null");
            this.expectedType = Preconditions.checkNotNull(expectedType, "Expected type cannot be null");
        }

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        public Builder range(long min, long max) {
            this.minVal = min;
            this.maxVal = max;
            return this;
        }

        public Builder lengthBounds(int min, int max) {
            this.minLen = min;
            this.maxLen = max;
            return this;
        }

        public BsmFieldConstraint build() {
            return new BsmFieldConstraint(this);
        }
    }

    private BsmFieldConstraint(Builder builder) {
        this.keyName = builder.keyName;
        this.expectedType = builder.expectedType;
        this.required = builder.required;
        this.minVal = builder.minVal;
        this.maxVal = builder.maxVal;
        this.minLen = builder.minLen;
        this.maxLen = builder.maxLen;
    }

    public String getKeyName() { return keyName; }
    public MetadataType getExpectedType() { return expectedType; }
    public boolean isRequired() { return required; }

    public void validate(MetadataValue record) {
        Preconditions.checkNotNull(record, "MetadataValue cannot be null");
        if (record.getType() != expectedType) {
            throw new IllegalArgumentException(String.format("BSM Type mismatch for key %s: expected %s, got %s",
                keyName, expectedType, record.getType()));
        }

        if (expectedType == MetadataType.INTEGER || expectedType == MetadataType.LONG) {
            long lVal = record.asLong();
            if (minVal != null && lVal < minVal) {
                throw new IllegalArgumentException(String.format("BSM Value below range minimum for key %s: minimum %d, got %d",
                    keyName, minVal, lVal));
            }
            if (maxVal != null && lVal > maxVal) {
                throw new IllegalArgumentException(String.format("BSM Value exceeds range maximum for key %s: maximum %d, got %d",
                    keyName, maxVal, lVal));
            }
        } else if (expectedType == MetadataType.STRING) {
            String str = record.asString();
            int len = str.length();
            if (minLen != null && len < minLen) {
                throw new IllegalArgumentException(String.format("BSM String too short for key %s: min length %d, got %d",
                    keyName, minLen, len));
            }
            if (maxLen != null && len > maxLen) {
                throw new IllegalArgumentException(String.format("BSM String too long for key %s: max length %d, got %d",
                    keyName, maxLen, len));
            }
        } else if (expectedType == MetadataType.BINARY) {
            byte[] bytes = record.asBinary();
            int len = bytes.length;
            if (minLen != null && len < minLen) {
                throw new IllegalArgumentException(String.format("BSM Bytes too short for key %s: min length %d, got %d",
                    keyName, minLen, len));
            }
            if (maxLen != null && len > maxLen) {
                throw new IllegalArgumentException(String.format("BSM Bytes too long for key %s: max length %d, got %d",
                    keyName, maxLen, len));
            }
        }
    }
}
