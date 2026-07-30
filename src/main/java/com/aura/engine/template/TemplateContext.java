package com.aura.engine.template;

import com.aura.internal.Preconditions;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context frame manager that resolves variable paths recursively.
 */
public class TemplateContext {
    private final List<Map<String, Object>> frames = new ArrayList<>();

    public TemplateContext(Map<String, Object> globalVariables) {
        Preconditions.checkNotNull(globalVariables, "Globals cannot be null");
        frames.add(new HashMap<>(globalVariables));
    }

    public void pushFrame() {
        frames.add(new HashMap<>());
    }

    public void popFrame() {
        Preconditions.checkState(frames.size() > 1, "Cannot pop the global frame");
        frames.remove(frames.size() - 1);
    }

    public void setLocal(String key, Object value) {
        Preconditions.checkNotNull(key, "Key cannot be null");
        frames.get(frames.size() - 1).put(key, value);
    }

    public Object resolve(List<String> path) {
        Preconditions.checkNotNull(path, "Path cannot be null");
        if (path.isEmpty()) return null;

        String rootKey = path.get(0);
        Object current = lookup(rootKey);
        if (current == null) return null;

        for (int i = 1; i < path.size(); i++) {
            current = resolveProperty(current, path.get(i));
            if (current == null) break;
        }

        return current;
    }

    private Object lookup(String key) {
        for (int i = frames.size() - 1; i >= 0; i--) {
            Map<String, Object> frame = frames.get(i);
            if (frame.containsKey(key)) {
                return frame.get(key);
            }
        }
        return null;
    }

    private Object resolveProperty(Object obj, String prop) {
        if (obj == null) return null;

        if (obj instanceof Map<?, ?> map) {
            return map.get(prop);
        }

        // Try reflection: bean properties or public getter methods
        try {
            String getterName = "get" + Character.toUpperCase(prop.charAt(0)) + prop.substring(1);
            Method m = obj.getClass().getMethod(getterName);
            return m.invoke(obj);
        } catch (Exception e1) {
            try {
                // Try direct getter or field check
                Method m = obj.getClass().getMethod(prop);
                return m.invoke(obj);
            } catch (Exception e2) {
                // Property not resolvable
                return null;
            }
        }
    }
}
