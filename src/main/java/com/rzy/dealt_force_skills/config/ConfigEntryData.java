package com.rzy.dealt_force_skills.config;

import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

/** One editable scalar leaf from a NightConfig TOML file. */
public final class ConfigEntryData {
    private final ConfigFileId file;
    private final String path;
    private final ConfigValueKind kind;
    private final String valueText;
    private final String comment;

    public ConfigEntryData(ConfigFileId file, String path, ConfigValueKind kind, String valueText, String comment) {
        this.file = Objects.requireNonNull(file, "file");
        this.path = Objects.requireNonNull(path, "path");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.valueText = valueText == null ? "" : valueText;
        this.comment = comment == null ? "" : comment;
    }

    public ConfigFileId file() {
        return file;
    }

    public String path() {
        return path;
    }

    public ConfigValueKind kind() {
        return kind;
    }

    public String valueText() {
        return valueText;
    }

    public String comment() {
        return comment;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(file);
        buf.writeUtf(path, 512);
        buf.writeEnum(kind);
        buf.writeUtf(valueText, 1024);
        buf.writeUtf(comment, 1024);
    }

    public static ConfigEntryData decode(FriendlyByteBuf buf) {
        return new ConfigEntryData(
                buf.readEnum(ConfigFileId.class),
                buf.readUtf(512),
                buf.readEnum(ConfigValueKind.class),
                buf.readUtf(1024),
                buf.readUtf(1024));
    }

    public Object parsedValue() {
        return parse(kind, valueText);
    }

    public static Object parse(ConfigValueKind kind, String text) {
        String trimmed = text == null ? "" : text.trim();
        return switch (kind) {
            case BOOLEAN -> {
                if ("true".equalsIgnoreCase(trimmed) || "1".equals(trimmed) || "yes".equalsIgnoreCase(trimmed)) {
                    yield true;
                }
                if ("false".equalsIgnoreCase(trimmed) || "0".equals(trimmed) || "no".equalsIgnoreCase(trimmed)) {
                    yield false;
                }
                throw new IllegalArgumentException("Expected boolean: " + text);
            }
            case INTEGER -> Integer.parseInt(trimmed);
            case LONG -> Long.parseLong(trimmed);
            case FLOAT -> {
                float value = Float.parseFloat(trimmed);
                if (!Float.isFinite(value)) {
                    throw new IllegalArgumentException("Non-finite float: " + text);
                }
                yield value;
            }
            case DOUBLE -> {
                double value = Double.parseDouble(trimmed);
                if (!Double.isFinite(value)) {
                    throw new IllegalArgumentException("Non-finite double: " + text);
                }
                yield value;
            }
            case STRING -> text == null ? "" : text;
        };
    }

    public static String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Float floatValue) {
            return Float.toString(floatValue);
        }
        if (value instanceof Double doubleValue) {
            return Double.toString(doubleValue);
        }
        return String.valueOf(value);
    }
}
