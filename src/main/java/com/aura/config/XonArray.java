package com.aura.config;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An array node containing ordered sequences of values.
 */
public class XonArray extends XonValue {
    private final List<XonValue> elements = new ArrayList<>();

    @Override
    public boolean isArray() { return true; }

    @Override
    public List<XonValue> asArray() {
        return Collections.unmodifiableList(elements);
    }

    public void add(XonValue val) {
        Preconditions.checkNotNull(val, "Value cannot be null");
        elements.add(val);
    }

    public XonValue get(int index) {
        return elements.get(index);
    }

    public int size() {
        return elements.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(elements.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}
