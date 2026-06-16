package com.rzy.dealt_force_skills.character.lexninjia;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public record LexNinjiaPreset(String name, List<LexNinjiaComboInput> inputs) {
    public static final int MAX_NAME_LENGTH = 32;

    public LexNinjiaPreset {
        name = sanitizeName(name);
        inputs = inputs == null ? List.of() : List.copyOf(inputs);
    }

    public static LexNinjiaPreset read(CompoundTag presets, int slot, int maxInputs) {
        if (presets == null || slot < 0 || !presets.contains(Integer.toString(slot))) {
            return new LexNinjiaPreset("", List.of());
        }
        CompoundTag tag = presets.getCompound(Integer.toString(slot));
        List<LexNinjiaComboInput> inputs = new ArrayList<>();
        int[] ordinals = tag.getIntArray("Inputs");
        LexNinjiaComboInput[] values = LexNinjiaComboInput.values();
        for (int ordinal : ordinals) {
            if (inputs.size() >= Math.max(0, maxInputs)) {
                break;
            }
            if (ordinal >= 0 && ordinal < values.length) {
                LexNinjiaComboInput input = values[ordinal];
                inputs.add(input);
                if (isReleaseInput(input)) {
                    break;
                }
            }
        }
        return new LexNinjiaPreset(tag.getString("Name"), inputs);
    }

    public static void write(CompoundTag presets, int slot, LexNinjiaPreset preset, int maxInputs) {
        if (presets == null || slot < 0) {
            return;
        }
        LexNinjiaPreset safe = preset == null ? new LexNinjiaPreset("", List.of()) : preset;
        List<LexNinjiaComboInput> inputs = new ArrayList<>();
        for (LexNinjiaComboInput input : safe.inputs()) {
            if (input == null || inputs.size() >= Math.max(0, maxInputs)) {
                break;
            }
            inputs.add(input);
            if (isReleaseInput(input)) {
                break;
            }
        }
        String key = Integer.toString(slot);
        if (safe.name().isBlank() && inputs.isEmpty()) {
            presets.remove(key);
            return;
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("Name", safe.name());
        tag.putIntArray("Inputs", inputs.stream().mapToInt(Enum::ordinal).toArray());
        presets.put(key, tag);
    }

    private static boolean isReleaseInput(LexNinjiaComboInput input) {
        return input == LexNinjiaComboInput.LEFT_CLICK || input == LexNinjiaComboInput.RIGHT_RELEASE;
    }

    private static String sanitizeName(String value) {
        String safe = value == null ? "" : value.strip();
        if (safe.length() > MAX_NAME_LENGTH) {
            safe = safe.substring(0, MAX_NAME_LENGTH);
        }
        return safe;
    }
}
