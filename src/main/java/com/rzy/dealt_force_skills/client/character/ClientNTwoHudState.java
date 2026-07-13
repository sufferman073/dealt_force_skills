package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.ntwo.NTwoColdMarker;
import com.rzy.dealt_force_skills.character.ntwo.NTwoTool;
import net.minecraft.client.Minecraft;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class ClientNTwoHudState {
    private static final EntityDataAccessor<Byte> SHARED_FLAGS = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
    private static final int GLOWING_FLAG = 6;
    private static boolean synced;
    private static int trackingCooldownTicks;
    private static int dewarCharges;
    private static int dewarMaxCharges;
    private static int dewarRechargeTicks;
    private static int coreCooldownTicks;
    private static int coreActiveTicks;
    private static int coreAmmo;
    private static NTwoTool equippedTool = NTwoTool.NONE;
    private static List<NTwoColdMarker> coldMarkers = List.of();
    private static final Map<Integer, Integer> revealedEntityTicks = new HashMap<>();
    private static final Map<Integer, Boolean> originalGlowStates = new HashMap<>();

    private ClientNTwoHudState() {
    }

    public static void reset() {
        clearGlow();
        synced = false;
        trackingCooldownTicks = 0;
        dewarCharges = 0;
        dewarMaxCharges = 0;
        dewarRechargeTicks = 0;
        coreCooldownTicks = 0;
        coreActiveTicks = 0;
        coreAmmo = 0;
        equippedTool = NTwoTool.NONE;
        coldMarkers = List.of();
        revealedEntityTicks.clear();
    }

    public static void sync(
            int trackingCooldownTicks,
            int dewarCharges,
            int dewarMaxCharges,
            int dewarRechargeTicks,
            int coreCooldownTicks,
            int coreActiveTicks,
            int coreAmmo,
            int equippedToolOrdinal,
            List<NTwoColdMarker> coldMarkers
    ) {
        synced = true;
        ClientNTwoHudState.trackingCooldownTicks = trackingCooldownTicks;
        ClientNTwoHudState.dewarCharges = dewarCharges;
        ClientNTwoHudState.dewarMaxCharges = dewarMaxCharges;
        ClientNTwoHudState.dewarRechargeTicks = dewarRechargeTicks;
        ClientNTwoHudState.coreCooldownTicks = coreCooldownTicks;
        ClientNTwoHudState.coreActiveTicks = coreActiveTicks;
        ClientNTwoHudState.coreAmmo = coreAmmo;
        NTwoTool[] tools = NTwoTool.values();
        equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : NTwoTool.NONE;
        ClientNTwoHudState.coldMarkers = List.copyOf(coldMarkers);
    }

    public static void tick() {
        if (trackingCooldownTicks > 0) trackingCooldownTicks--;
        if (dewarRechargeTicks > 0) dewarRechargeTicks--;
        if (coreCooldownTicks > 0) coreCooldownTicks--;
        if (coreActiveTicks > 0) coreActiveTicks--;
        tickReveals();
    }

    public static void revealEntities(List<Integer> entityIds) {
        Minecraft minecraft = Minecraft.getInstance();
        for (Integer entityId : entityIds) {
            revealedEntityTicks.put(entityId, 40);
            if (minecraft.level != null) {
                Entity entity = minecraft.level.getEntity(entityId);
                if (entity != null) {
                    forceEntityGlow(entity);
                }
            }
        }
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.N_TWO_ID);
    }

    public static boolean shouldDisplay() {
        return synced && ClientCharacterSelectionState.isDisplayedCharacter(ModCharacters.N_TWO_ID);
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != NTwoTool.NONE;
    }

    public static int trackingCooldownTicks() {
        return trackingCooldownTicks;
    }

    public static int dewarCharges() {
        return dewarCharges;
    }

    public static int dewarMaxCharges() {
        return dewarMaxCharges;
    }

    public static int dewarRechargeTicks() {
        return dewarRechargeTicks;
    }

    public static int coreCooldownTicks() {
        return coreCooldownTicks;
    }

    public static int coreActiveTicks() {
        return coreActiveTicks;
    }

    public static int coreAmmo() {
        return coreAmmo;
    }

    public static NTwoTool equippedTool() {
        return equippedTool;
    }

    public static List<NTwoColdMarker> coldMarkers() {
        return coldMarkers;
    }

    private static void tickReveals() {
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
                restoreGlow(entity, entityId);
                iterator.remove();
            } else {
                entry.setValue(ticks);
                if (entity != null) {
                    forceEntityGlow(entity);
                }
            }
        }
    }

    private static void clearGlow() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            for (Map.Entry<Integer, Boolean> entry : originalGlowStates.entrySet()) {
                Entity entity = minecraft.level.getEntity(entry.getKey());
                if (entity != null) {
                    setEntityGlowFlag(entity, entry.getValue());
                }
            }
        }
        originalGlowStates.clear();
    }

    private static void forceEntityGlow(Entity entity) {
        originalGlowStates.putIfAbsent(entity.getId(), entity.isCurrentlyGlowing());
        setEntityGlowFlag(entity, true);
    }

    private static void restoreGlow(Entity entity, int entityId) {
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
