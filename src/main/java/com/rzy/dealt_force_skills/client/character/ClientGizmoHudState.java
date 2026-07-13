package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.gizmo.GizmoTool;
import com.rzy.dealt_force_skills.character.gizmo.GizmoTrapMarker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class ClientGizmoHudState {
    private static final EntityDataAccessor<Byte> SHARED_FLAGS = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
    private static final int GLOWING_FLAG = 6;
    private static boolean synced;
    private static int smokeCharges;
    private static int smokeMaxCharges;
    private static int smokeRechargeTicks;
    private static int spiderCharges;
    private static int spiderMaxCharges;
    private static int spiderRechargeTicks;
    private static int coreCooldownTicks;
    private static GizmoTool equippedTool = GizmoTool.NONE;
    private static boolean passiveBoost;
    private static int webEscapeTicks;
    private static List<GizmoTrapMarker> trapMarkers = List.of();
    private static final Map<Integer, Integer> revealedEntityTicks = new HashMap<>();
    private static final Map<Integer, Boolean> originalGlowStates = new HashMap<>();

    private ClientGizmoHudState() {
    }

    public static void reset() {
        clearRevealedEntities();
        synced = false;
        smokeCharges = 0;
        smokeMaxCharges = 0;
        smokeRechargeTicks = 0;
        spiderCharges = 0;
        spiderMaxCharges = 0;
        spiderRechargeTicks = 0;
        coreCooldownTicks = 0;
        equippedTool = GizmoTool.NONE;
        passiveBoost = false;
        webEscapeTicks = 0;
        trapMarkers = List.of();
    }

    public static void sync(
            int smokeCharges,
            int smokeMaxCharges,
            int smokeRechargeTicks,
            int spiderCharges,
            int spiderMaxCharges,
            int spiderRechargeTicks,
            int coreCooldownTicks,
            int equippedToolOrdinal,
            boolean passiveBoost,
            int webEscapeTicks,
            List<GizmoTrapMarker> trapMarkers
    ) {
        synced = true;
        ClientGizmoHudState.smokeCharges = smokeCharges;
        ClientGizmoHudState.smokeMaxCharges = smokeMaxCharges;
        ClientGizmoHudState.smokeRechargeTicks = smokeRechargeTicks;
        ClientGizmoHudState.spiderCharges = spiderCharges;
        ClientGizmoHudState.spiderMaxCharges = spiderMaxCharges;
        ClientGizmoHudState.spiderRechargeTicks = spiderRechargeTicks;
        ClientGizmoHudState.coreCooldownTicks = coreCooldownTicks;
        GizmoTool[] tools = GizmoTool.values();
        ClientGizmoHudState.equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : GizmoTool.NONE;
        ClientGizmoHudState.passiveBoost = passiveBoost;
        ClientGizmoHudState.webEscapeTicks = webEscapeTicks;
        ClientGizmoHudState.trapMarkers = List.copyOf(trapMarkers);
    }

    public static void tick() {
        if (smokeRechargeTicks > 0) {
            smokeRechargeTicks--;
        }
        if (spiderRechargeTicks > 0) {
            spiderRechargeTicks--;
        }
        if (coreCooldownTicks > 0) {
            coreCooldownTicks--;
        }
        tickRevealedEntities();
    }

    public static void revealEntities(List<Integer> entityIds) {
        Minecraft minecraft = Minecraft.getInstance();
        for (Integer entityId : entityIds) {
            revealedEntityTicks.put(entityId, 30);
            if (minecraft.level != null) {
                Entity entity = minecraft.level.getEntity(entityId);
                if (entity != null) {
                    forceEntityGlow(entity);
                }
            }
        }
    }

    public static boolean isEntityRevealed(int entityId) {
        return shouldRender() && revealedEntityTicks.getOrDefault(entityId, 0) > 0;
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.GIZMO_ID);
    }

    public static boolean shouldDisplay() {
        return synced && ClientCharacterSelectionState.isDisplayedCharacter(ModCharacters.GIZMO_ID);
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != GizmoTool.NONE;
    }

    public static int smokeCharges() {
        return smokeCharges;
    }

    public static int smokeMaxCharges() {
        return smokeMaxCharges;
    }

    public static int smokeRechargeTicks() {
        return smokeRechargeTicks;
    }

    public static int spiderCharges() {
        return spiderCharges;
    }

    public static int spiderMaxCharges() {
        return spiderMaxCharges;
    }

    public static int spiderRechargeTicks() {
        return spiderRechargeTicks;
    }

    public static int coreCooldownTicks() {
        return coreCooldownTicks;
    }

    public static GizmoTool equippedTool() {
        return equippedTool;
    }

    public static boolean passiveBoost() {
        return passiveBoost;
    }

    public static int webEscapeTicks() {
        return webEscapeTicks;
    }

    public static List<GizmoTrapMarker> trapMarkers() {
        return trapMarkers;
    }

    private static void tickRevealedEntities() {
        if (revealedEntityTicks.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Iterator<Map.Entry<Integer, Integer>> iterator = revealedEntityTicks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int entityId = entry.getKey();
            int ticks = entry.getValue() - 1;
            Entity entity = minecraft.level == null ? null : minecraft.level.getEntity(entityId);
            if (ticks <= 0) {
                restoreEntityGlow(entity, entityId);
                iterator.remove();
            } else {
                entry.setValue(ticks);
                if (entity != null) {
                    forceEntityGlow(entity);
                }
            }
        }
    }

    private static void clearRevealedEntities() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            for (Integer entityId : revealedEntityTicks.keySet()) {
                restoreEntityGlow(minecraft.level.getEntity(entityId), entityId);
            }
        }
        revealedEntityTicks.clear();
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
}
