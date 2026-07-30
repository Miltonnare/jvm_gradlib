package com.aura.config;

import com.aura.internal.Preconditions;

/**
 * Defines constraint rules comparing fields within a XonObject hierarchy.
 */
public class XonCustomRule {
    private final String targetField;
    private final String compareField;
    private final String relation; // "gt", "lt", "eq"

    public XonCustomRule(String targetField, String compareField, String relation) {
        this.targetField = Preconditions.checkNotNull(targetField, "Target field cannot be null");
        this.compareField = Preconditions.checkNotNull(compareField, "Compare field cannot be null");
        this.relation = Preconditions.checkNotNull(relation, "Relation cannot be null");
    }

    /**
     * Checks if the XonObject complies with this relation constraint.
     */
    public void check(XonObject obj) {
        Preconditions.checkNotNull(obj, "XonObject cannot be null");
        XonValue val1 = obj.get(targetField);
        XonValue val2 = obj.get(compareField);

        if (val1 == null || val2 == null) {
            return; // Fields are optional or missing, constraint holds vacuously
        }

        if ((val1.isInt() || val1.isDouble()) && (val2.isInt() || val2.isDouble())) {
            double d1 = val1.isInt() ? val1.asInt() : val1.asDouble();
            double d2 = val2.isInt() ? val2.asInt() : val2.asDouble();

            boolean passed = switch (relation.toLowerCase()) {
                case "gt" -> d1 > d2;
                case "lt" -> d1 < d2;
                case "eq" -> d1 == d2;
                default -> throw new IllegalArgumentException("Unsupported relation type in custom rule: " + relation);
            };

            if (!passed) {
                throw new IllegalArgumentException(String.format("Xon Custom Rule failed: field '%s' (value %f) is not %s field '%s' (value %f)",
                    targetField, d1, relation, compareField, d2));
            }
        }
    }
}
