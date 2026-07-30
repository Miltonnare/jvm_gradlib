package com.aura.config;

import com.aura.internal.Preconditions;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An object node containing key-value mappings.
 */
public class XonObject extends XonValue {
    private final Map<String, XonValue> fields = new LinkedHashMap<>();

    @Override
    public boolean isObject() { return true; }

    @Override
    public Map<String, XonValue> asObject() {
        return Collections.unmodifiableMap(fields);
    }

    public void put(String key, XonValue val) {
        Preconditions.checkNotNull(key, "Key cannot be null");
        Preconditions.checkNotNull(val, "Value cannot be null");
        fields.put(key, val);
    }

    public XonValue get(String key) {
        return fields.get(key);
    }

    public boolean has(String key) {
        return fields.containsKey(key);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, XonValue> entry : fields.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append('"').append(entry.getKey()).append("\": ").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
