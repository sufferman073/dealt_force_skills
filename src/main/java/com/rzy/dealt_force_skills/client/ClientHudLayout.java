package com.rzy.dealt_force_skills.client;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.client.character.ClientCharacterSelectionState;
import com.rzy.dealt_force_skills.config.ConfigEntryData;
import com.rzy.dealt_force_skills.config.ConfigFileId;
import com.rzy.dealt_force_skills.config.ConfigValueKind;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Local, automatically reloaded offsets for the standard character skill HUD. */
public final class ClientHudLayout {
    public static final String FILE_NAME = "dealtforceskills-hud.toml";

    private static final Logger LOGGER = LoggerFactory.getLogger(DealtForceSkillsMod.MODID + "/hud-config");
    private static final long FILE_CHECK_INTERVAL_NANOS = 1_000_000_000L;
    private static final Set<String> COMMENTED_PATHS = new HashSet<>();
    private static CommentedFileConfig config;
    private static long nextFileCheckNanos;
    private static long observedModifiedMillis = Long.MIN_VALUE;

    private ClientHudLayout() {
    }

    public static synchronized List<ConfigEntryData> snapshotEntries() {
        ensureFresh();
        ensureDefaultsPresent();
        List<ConfigEntryData> entries = new ArrayList<>();
        flattenHud(config, "", entries);
        entries.sort(Comparator.comparing(ConfigEntryData::path));
        return entries;
    }

    public static synchronized boolean setValue(String path, Object value) {
        if (path == null || path.isBlank() || value == null) {
            return false;
        }
        ensureFresh();
        config.set(path, value);
        config.save();
        updateObservedModifiedTime();
        return true;
    }

    public static synchronized void flush() {
        if (config != null) {
            config.save();
            updateObservedModifiedTime();
        }
    }

    public static synchronized void forceReload() {
        if (config == null) {
            loadInitial();
            return;
        }
        reload(modifiedTime(configPath()));
    }

    public static int x(int defaultX) {
        return defaultX + offset("x_offset");
    }

    public static int y(int defaultY) {
        return defaultY + offset("y_offset");
    }

    public static synchronized float buttonScale() {
        ensureFresh();
        float global = floatValue("global.button_scale", 1.0F);
        String characterId = sanitizedCharacterId();
        float character = characterId.isEmpty()
                ? 1.0F
                : floatValue("hud." + characterId + ".button_scale", 1.0F);
        float effective = global * character;
        if (!Float.isFinite(effective)) {
            LOGGER.warn("Ignoring non-finite effective HUD button scale in {}", FILE_NAME);
            return 1.0F;
        }
        return Math.max(0.25F, Math.min(3.0F, effective));
    }

    public static ButtonScale scaleButton(GuiGraphics graphics, int x, int y, int baseSize) {
        float scale = buttonScale();
        if (scale == 1.0F) {
            return ButtonScale.NOOP;
        }
        int anchorX = x(8);
        float visualX = anchorX + (x - anchorX) * scale;
        float visualY = y + baseSize - baseSize * scale;
        graphics.pose().pushPose();
        graphics.pose().translate(visualX, visualY, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.pose().translate(-x, -y, 0.0F);
        return new ButtonScale(graphics);
    }

    private static synchronized int offset(String axis) {
        ensureFresh();
        int global = intValue("global." + axis, 0);
        String characterId = sanitizedCharacterId();
        return characterId.isEmpty() ? global : global + intValue("hud." + characterId + "." + axis, 0);
    }

    private static int intValue(String path, int defaultValue) {
        Object configured = config.get(path);
        boolean changed = false;
        if (!(configured instanceof Number number)) {
            if (configured != null) {
                LOGGER.warn("Ignoring non-numeric HUD setting {} in {}", path, FILE_NAME);
            }
            config.set(path, defaultValue);
            configured = defaultValue;
            changed = true;
        }
        if (COMMENTED_PATHS.add(path)) {
            config.setComment(path, "Pixel offset added to the standard skill HUD. Default: " + defaultValue
                    + ". The file is checked for changes once per second.");
            changed = true;
        }
        if (changed) {
            config.save();
            updateObservedModifiedTime();
        }
        return ((Number) configured).intValue();
    }

    private static float floatValue(String path, float defaultValue) {
        Object configured = config.get(path);
        boolean changed = false;
        if (!(configured instanceof Number number)) {
            if (configured != null) {
                LOGGER.warn("Ignoring non-numeric HUD setting {} in {}", path, FILE_NAME);
            }
            config.set(path, defaultValue);
            configured = defaultValue;
            changed = true;
        }
        if (COMMENTED_PATHS.add(path)) {
            config.setComment(path, "Skill button scale multiplier. Default: " + defaultValue
                    + ". Effective scale is clamped to 0.25-3.0 and the file is checked once per second.");
            changed = true;
        }
        if (changed) {
            config.save();
            updateObservedModifiedTime();
        }
        return ((Number) configured).floatValue();
    }

    private static void ensureFresh() {
        if (config == null) {
            loadInitial();
            return;
        }
        long now = System.nanoTime();
        if (now < nextFileCheckNanos) {
            return;
        }
        nextFileCheckNanos = now + FILE_CHECK_INTERVAL_NANOS;
        long modified = modifiedTime(configPath());
        if (modified != observedModifiedMillis) {
            reload(modified);
        }
    }

    private static void loadInitial() {
        config = openConfig(configPath());
        COMMENTED_PATHS.clear();
        updateObservedModifiedTime();
    }

    private static void reload(long modified) {
        CommentedFileConfig replacement;
        try {
            replacement = openConfig(configPath());
        } catch (RuntimeException error) {
            observedModifiedMillis = modified;
            LOGGER.error("Unable to reload {}; keeping the previous HUD layout", configPath(), error);
            return;
        }
        CommentedFileConfig previous = config;
        config = replacement;
        COMMENTED_PATHS.clear();
        observedModifiedMillis = modified;
        if (previous != null) {
            previous.close();
        }
        LOGGER.info("Reloaded {}", configPath());
    }

    private static CommentedFileConfig openConfig(Path path) {
        CommentedFileConfig opened = CommentedFileConfig.builder(path).sync().build();
        try {
            opened.load();
            return opened;
        } catch (RuntimeException error) {
            opened.close();
            throw error;
        }
    }

    private static void updateObservedModifiedTime() {
        observedModifiedMillis = modifiedTime(configPath());
    }

    private static long modifiedTime(Path path) {
        try {
            return Files.exists(path) ? Files.getLastModifiedTime(path).toMillis() : Long.MIN_VALUE;
        } catch (IOException error) {
            LOGGER.warn("Unable to read modification time for {}", path, error);
            return observedModifiedMillis;
        }
    }

    private static Path configPath() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }

    private static String sanitizedCharacterId() {
        String id = ClientCharacterSelectionState.displayedCharacterId().toLowerCase(Locale.ROOT);
        return id.replaceAll("[^a-z0-9_-]", "_");
    }

    private static void ensureDefaultsPresent() {
        intValue("global.x_offset", 0);
        intValue("global.y_offset", 0);
        floatValue("global.button_scale", 1.0F);
        String characterId = sanitizedCharacterId();
        if (!characterId.isEmpty()) {
            intValue("hud." + characterId + ".x_offset", 0);
            intValue("hud." + characterId + ".y_offset", 0);
            floatValue("hud." + characterId + ".button_scale", 1.0F);
        }
    }

    private static void flattenHud(UnmodifiableConfig source, String prefix, List<ConfigEntryData> out) {
        for (Map.Entry<String, Object> entry : source.valueMap().entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isBlank()) {
                continue;
            }
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            Object value = entry.getValue();
            if (value instanceof UnmodifiableConfig nested) {
                flattenHud(nested, path, out);
                continue;
            }
            if (!(value instanceof Number || value instanceof Boolean || value instanceof String)) {
                continue;
            }
            ConfigValueKind kind = ConfigValueKind.of(value);
            String comment = "";
            try {
                String raw = config.getComment(path);
                if (raw != null) {
                    comment = raw;
                }
            } catch (RuntimeException ignored) {
            }
            out.add(new ConfigEntryData(ConfigFileId.HUD, path, kind, ConfigEntryData.formatValue(value), comment));
        }
    }

    public static final class ButtonScale implements AutoCloseable {
        private static final ButtonScale NOOP = new ButtonScale(null);
        private final GuiGraphics graphics;

        private ButtonScale(GuiGraphics graphics) {
            this.graphics = graphics;
        }

        @Override
        public void close() {
            if (graphics != null) {
                graphics.pose().popPose();
            }
        }
    }
}
