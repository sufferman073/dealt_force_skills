package com.rzy.dealt_force_skills.config;

public enum ConfigValueKind {
    BOOLEAN,
    INTEGER,
    LONG,
    FLOAT,
    DOUBLE,
    STRING;

    public static ConfigValueKind of(Object value) {
        if (value instanceof Boolean) {
            return BOOLEAN;
        }
        if (value instanceof Integer) {
            return INTEGER;
        }
        if (value instanceof Long) {
            return LONG;
        }
        if (value instanceof Float) {
            return FLOAT;
        }
        if (value instanceof Double) {
            return DOUBLE;
        }
        if (value instanceof Number number) {
            double d = number.doubleValue();
            if (d == Math.rint(d) && Math.abs(d) <= Integer.MAX_VALUE) {
                return INTEGER;
            }
            return DOUBLE;
        }
        return STRING;
    }
}
