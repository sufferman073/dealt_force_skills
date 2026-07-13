package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.chamber.ChamberGunKind;
import com.rzy.dealt_force_skills.character.chamber.ChamberMarker;
import com.rzy.dealt_force_skills.character.chamber.ChamberTool;
import net.minecraft.client.Minecraft;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientChamberHudState {
    private static final EntityDataAccessor<Byte> SHARED_FLAGS = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
    private static final int GLOWING_FLAG = 6;
    private static boolean synced;
    private static int teleportCooldownTicks;
    private static int trapCooldownTicks;
    private static int headhunterCooldownTicks;
    private static int tourDeForceCooldownTicks;
    private static ChamberTool equippedTool = ChamberTool.NONE;
    private static ChamberGunKind equippedGun = ChamberGunKind.NONE;
    private static List<ChamberMarker> markers = List.of();
    private static final Map<Integer, Boolean> originalGlowStates = new HashMap<>();

    private ClientChamberHudState() {
    }

    public static void reset() {
        clearGlow();
        synced = false;
        teleportCooldownTicks = 0;
        trapCooldownTicks = 0;
        headhunterCooldownTicks = 0;
        tourDeForceCooldownTicks = 0;
        equippedTool = ChamberTool.NONE;
        equippedGun = ChamberGunKind.NONE;
        markers = List.of();
    }

    public static void sync(
            int teleportCooldownTicks,
            int trapCooldownTicks,
            int headhunterCooldownTicks,
            int tourDeForceCooldownTicks,
            int equippedToolOrdinal,
            int equippedGunOrdinal,
            List<ChamberMarker> markers
    ) {
        synced = true;
        ClientChamberHudState.teleportCooldownTicks = teleportCooldownTicks;
        ClientChamberHudState.trapCooldownTicks = trapCooldownTicks;
        ClientChamberHudState.headhunterCooldownTicks = headhunterCooldownTicks;
        ClientChamberHudState.tourDeForceCooldownTicks = tourDeForceCooldownTicks;
        ChamberTool[] tools = ChamberTool.values();
        equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : ChamberTool.NONE;
        ChamberGunKind[] guns = ChamberGunKind.values();
        equippedGun = equippedGunOrdinal >= 0 && equippedGunOrdinal < guns.length
                ? guns[equippedGunOrdinal]
                : ChamberGunKind.NONE;
        ClientChamberHudState.markers = List.copyOf(markers);
    }

    public static void tick() {
        if (teleportCooldownTicks > 0) teleportCooldownTicks--;
        if (trapCooldownTicks > 0) trapCooldownTicks--;
        if (headhunterCooldownTicks > 0) headhunterCooldownTicks--;
        if (tourDeForceCooldownTicks > 0) tourDeForceCooldownTicks--;
        tickMarkerGlow();
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.CHAMBER_ID);
    }

    public static boolean shouldDisplay() {
        return synced && ClientCharacterSelectionState.isDisplayedCharacter(ModCharacters.CHAMBER_ID);
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != ChamberTool.NONE;
    }

    public static boolean hasEquippedGun() {
        return shouldRender() && equippedGun != ChamberGunKind.NONE;
    }

    public static int teleportCooldownTicks() {
        return teleportCooldownTicks;
    }

    public static int trapCooldownTicks() {
        return trapCooldownTicks;
    }

    public static int headhunterCooldownTicks() {
        return headhunterCooldownTicks;
    }

    public static int tourDeForceCooldownTicks() {
        return tourDeForceCooldownTicks;
    }

    public static ChamberTool equippedTool() {
        return equippedTool;
    }

    public static ChamberGunKind equippedGun() {
        return equippedGun;
    }

    public static List<ChamberMarker> markers() {
        return markers;
    }

    private static void tickMarkerGlow() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            originalGlowStates.clear();
            return;
        }
        if (!shouldRender()) {
            clearGlow();
            return;
        }
        for (ChamberMarker marker : markers) {
            Entity entity = minecraft.level.getEntity(marker.entityId());
            if (entity != null) {
                forceEntityGlow(entity);
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

    private static void setEntityGlowFlag(Entity entity, boolean glowing) {
        byte flags = entity.getEntityData().get(SHARED_FLAGS);
        int glowMask = 1 << GLOWING_FLAG;
        byte updated = glowing ? (byte) (flags | glowMask) : (byte) (flags & ~glowMask);
        entity.getEntityData().set(SHARED_FLAGS, updated);
    }
}
