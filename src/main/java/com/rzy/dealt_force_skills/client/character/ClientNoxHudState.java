package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.nox.NoxTool;
import net.minecraft.client.Minecraft;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClientNoxHudState {
    private static final EntityDataAccessor<Byte> SHARED_FLAGS = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
    private static final int GLOWING_FLAG = 6;
    private static boolean synced;
    private static int rotorCooldownTicks;
    private static int flashCharges;
    private static int flashMaxCharges;
    private static int flashRechargeTicks;
    private static int coreCooldownTicks;
    private static int corePrepTicks;
    private static int stealthTicks;
    private static NoxTool equippedTool = NoxTool.NONE;
    private static final Map<Integer, Integer> revealedEntityTicks = new HashMap<>();
    private static final Map<Integer, Vec3> revealedPositions = new HashMap<>();
    private static final Map<Integer, Boolean> originalGlowStates = new HashMap<>();

    private ClientNoxHudState() {
    }

    public static void reset() {
        clearRevealedEntities();
        synced = false;
        rotorCooldownTicks = 0;
        flashCharges = 0;
        flashMaxCharges = 0;
        flashRechargeTicks = 0;
        coreCooldownTicks = 0;
        corePrepTicks = 0;
        stealthTicks = 0;
        equippedTool = NoxTool.NONE;
    }

    public static void sync(
            int rotorCooldownTicks,
            int flashCharges,
            int flashMaxCharges,
            int flashRechargeTicks,
            int coreCooldownTicks,
            int corePrepTicks,
            int stealthTicks,
            int equippedToolOrdinal
    ) {
        synced = true;
        ClientNoxHudState.rotorCooldownTicks = rotorCooldownTicks;
        ClientNoxHudState.flashCharges = flashCharges;
        ClientNoxHudState.flashMaxCharges = flashMaxCharges;
        ClientNoxHudState.flashRechargeTicks = flashRechargeTicks;
        ClientNoxHudState.coreCooldownTicks = coreCooldownTicks;
        ClientNoxHudState.corePrepTicks = corePrepTicks;
        ClientNoxHudState.stealthTicks = stealthTicks;
        NoxTool[] tools = NoxTool.values();
        ClientNoxHudState.equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : NoxTool.NONE;
    }

    public static void tick() {
        if (rotorCooldownTicks > 0) {
            rotorCooldownTicks--;
        }
        if (flashRechargeTicks > 0) {
            flashRechargeTicks--;
        }
        if (coreCooldownTicks > 0) {
            coreCooldownTicks--;
        }
        if (corePrepTicks > 0) {
            corePrepTicks--;
        }
        if (stealthTicks > 0) {
            stealthTicks--;
        }
        tickRevealedEntities();
    }

    public static void revealEntity(int entityId, Vec3 position, int ticks) {
        revealedEntityTicks.put(entityId, Math.max(1, ticks));
        revealedPositions.put(entityId, position);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            Entity entity = minecraft.level.getEntity(entityId);
            if (entity != null) {
                forceEntityGlow(entity);
            }
        }
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.NOX_ID);
    }

    public static boolean shouldDisplay() {
        return synced && ClientCharacterSelectionState.isDisplayedCharacter(ModCharacters.NOX_ID);
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != NoxTool.NONE;
    }

    public static boolean isEntityRevealed(int entityId) {
        return revealedEntityTicks.containsKey(entityId);
    }

    public static Map<Integer, Vec3> revealedPositions() {
        return Map.copyOf(revealedPositions);
    }

    public static int rotorCooldownTicks() {
        return rotorCooldownTicks;
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

    public static int coreCooldownTicks() {
        return coreCooldownTicks;
    }

    public static int corePrepTicks() {
        return corePrepTicks;
    }

    public static int stealthTicks() {
        return stealthTicks;
    }

    public static NoxTool equippedTool() {
        return equippedTool;
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
                revealedPositions.remove(entityId);
                iterator.remove();
            } else {
                entry.setValue(ticks);
                if (entity != null) {
                    revealedPositions.put(entityId, entity.position());
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
        revealedPositions.clear();
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
