package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.morse.MorseMarkerType;
import com.rzy.dealt_force_skills.character.morse.MorseTool;
import com.rzy.dealt_force_skills.character.morse.MorseWorldMarker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class ClientMorseHudState {
    private static final EntityDataAccessor<Byte> SHARED_FLAGS = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
    private static final int GLOWING_FLAG = 6;

    private static boolean synced;
    private static int shockCharges;
    private static int shockMaxCharges;
    private static int shockRechargeTicks;
    private static int flashCharges;
    private static int flashMaxCharges;
    private static int flashRechargeTicks;
    private static int sonarCooldownTicks;
    private static MorseTool equippedTool = MorseTool.NONE;
    private static int deployTicks;
    private static int deployRequiredTicks;
    private static boolean sonarActive;
    private static boolean sonarScanning;
    private static int sonarRemainingTicks;
    private static int sonarTargetCount;
    private static final List<TimedMarker> worldMarkers = new ArrayList<>();
    private static final Map<Integer, Boolean> originalGlowStates = new HashMap<>();

    private ClientMorseHudState() {
    }

    public static void reset() {
        clearGlow();
        synced = false;
        shockCharges = 0;
        shockMaxCharges = 0;
        shockRechargeTicks = 0;
        flashCharges = 0;
        flashMaxCharges = 0;
        flashRechargeTicks = 0;
        sonarCooldownTicks = 0;
        equippedTool = MorseTool.NONE;
        deployTicks = 0;
        deployRequiredTicks = 0;
        sonarActive = false;
        sonarScanning = false;
        sonarRemainingTicks = 0;
        sonarTargetCount = 0;
        worldMarkers.clear();
    }

    public static void sync(
            int shockCharges,
            int shockMaxCharges,
            int shockRechargeTicks,
            int flashCharges,
            int flashMaxCharges,
            int flashRechargeTicks,
            int sonarCooldownTicks,
            int equippedToolOrdinal,
            int deployTicks,
            int deployRequiredTicks,
            boolean sonarActive,
            boolean sonarScanning,
            int sonarRemainingTicks,
            int sonarTargetCount
    ) {
        synced = true;
        ClientMorseHudState.shockCharges = shockCharges;
        ClientMorseHudState.shockMaxCharges = shockMaxCharges;
        ClientMorseHudState.shockRechargeTicks = shockRechargeTicks;
        ClientMorseHudState.flashCharges = flashCharges;
        ClientMorseHudState.flashMaxCharges = flashMaxCharges;
        ClientMorseHudState.flashRechargeTicks = flashRechargeTicks;
        ClientMorseHudState.sonarCooldownTicks = sonarCooldownTicks;
        MorseTool[] tools = MorseTool.values();
        ClientMorseHudState.equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : MorseTool.NONE;
        ClientMorseHudState.deployTicks = deployTicks;
        ClientMorseHudState.deployRequiredTicks = deployRequiredTicks;
        ClientMorseHudState.sonarActive = sonarActive;
        ClientMorseHudState.sonarScanning = sonarScanning;
        ClientMorseHudState.sonarRemainingTicks = sonarRemainingTicks;
        ClientMorseHudState.sonarTargetCount = sonarTargetCount;
    }

    public static void addMarkers(List<MorseWorldMarker> markers) {
        for (MorseWorldMarker marker : markers) {
            worldMarkers.add(new TimedMarker(marker, Math.max(1, marker.ticks())));
            if (marker.type() == MorseMarkerType.SONAR_REVEAL && marker.entityId() >= 0) {
                Minecraft minecraft = Minecraft.getInstance();
                Entity entity = minecraft.level == null ? null : minecraft.level.getEntity(marker.entityId());
                if (entity != null) {
                    forceEntityGlow(entity);
                }
            }
        }
    }

    public static void tick() {
        if (shockRechargeTicks > 0) {
            shockRechargeTicks--;
        }
        if (flashRechargeTicks > 0) {
            flashRechargeTicks--;
        }
        if (sonarCooldownTicks > 0) {
            sonarCooldownTicks--;
        }
        if (sonarRemainingTicks > 0) {
            sonarRemainingTicks--;
        }
        tickMarkers();
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.MORSE_ID);
    }

    public static boolean shouldDisplay() {
        return synced && ClientCharacterSelectionState.isDisplayedCharacter(ModCharacters.MORSE_ID);
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != MorseTool.NONE;
    }

    public static int shockCharges() {
        return shockCharges;
    }

    public static int shockMaxCharges() {
        return shockMaxCharges;
    }

    public static int shockRechargeTicks() {
        return shockRechargeTicks;
    }

    public static int flashCharges() {
        return flashCharges;
    }

    public static int flashMaxCharges() {
        return flashMaxCharges;
    }

    public static int flashRechargeTicks() {
        return flashRechargeTicks;
    }

    public static int sonarCooldownTicks() {
        return sonarCooldownTicks;
    }

    public static MorseTool equippedTool() {
        return equippedTool;
    }

    public static int deployTicks() {
        return deployTicks;
    }

    public static int deployRequiredTicks() {
        return deployRequiredTicks;
    }

    public static boolean sonarActive() {
        return sonarActive;
    }

    public static boolean sonarScanning() {
        return sonarScanning;
    }

    public static int sonarRemainingTicks() {
        return sonarRemainingTicks;
    }

    public static int sonarTargetCount() {
        return sonarTargetCount;
    }

    public static List<MorseWorldMarker> worldMarkers() {
        return worldMarkers.stream().map(TimedMarker::marker).toList();
    }

    private static void tickMarkers() {
        if (worldMarkers.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Iterator<TimedMarker> iterator = worldMarkers.iterator();
        while (iterator.hasNext()) {
            TimedMarker timed = iterator.next();
            int nextTicks = timed.ticks - 1;
            if (nextTicks <= 0) {
                if (timed.marker.entityId() >= 0 && minecraft.level != null) {
                    restoreEntityGlow(minecraft.level.getEntity(timed.marker.entityId()), timed.marker.entityId());
                }
                iterator.remove();
            } else {
                timed.ticks = nextTicks;
                if (timed.marker.entityId() >= 0 && minecraft.level != null) {
                    Entity entity = minecraft.level.getEntity(timed.marker.entityId());
                    if (entity != null) {
                        forceEntityGlow(entity);
                    }
                }
            }
        }
    }

    private static void clearGlow() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            for (Integer entityId : originalGlowStates.keySet()) {
                restoreEntityGlow(minecraft.level.getEntity(entityId), entityId);
            }
        }
        originalGlowStates.clear();
    }

    private static void forceEntityGlow(Entity entity) {
        originalGlowStates.putIfAbsent(entity.getId(), entity.isCurrentlyGlowing());
        setEntityGlowFlag(entity, true);
    }

    private static void restoreEntityGlow(Entity entity, int entityId) {
        Boolean original = originalGlowStates.remove(entityId);
        if (entity != null && original != null) {
            setEntityGlowFlag(entity, original);
        }
    }

    private static void setEntityGlowFlag(Entity entity, boolean glowing) {
        byte flags = entity.getEntityData().get(SHARED_FLAGS);
        int glowMask = 1 << GLOWING_FLAG;
        byte updated = glowing ? (byte) (flags | glowMask) : (byte) (flags & ~glowMask);
        entity.getEntityData().set(SHARED_FLAGS, updated);
    }

    private static final class TimedMarker {
        private final MorseWorldMarker marker;
        private int ticks;

        private TimedMarker(MorseWorldMarker marker, int ticks) {
            this.marker = marker;
            this.ticks = ticks;
        }

        private MorseWorldMarker marker() {
            return marker;
        }
    }
}
