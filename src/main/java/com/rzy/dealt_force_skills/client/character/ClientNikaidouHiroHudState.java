package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.nikaidou.NikaidouHiroTool;

public final class ClientNikaidouHiroHudState {
    private static boolean synced;
    private static int riftStacks;
    private static int riftTicks;
    private static int active1CooldownTicks;
    private static int active1Ticks;
    private static NikaidouHiroTool equippedTool = NikaidouHiroTool.NONE;
    private static int coreCooldownTicks;
    private static boolean coreActive;
    private static int coreActiveTicks;
    private static int doomedTicks;
    private static int attackCooldownTicks;

    private ClientNikaidouHiroHudState() {
    }

    public static void reset() {
        synced = false;
        riftStacks = 0;
        riftTicks = 0;
        active1CooldownTicks = 0;
        active1Ticks = 0;
        equippedTool = NikaidouHiroTool.NONE;
        coreCooldownTicks = 0;
        coreActive = false;
        coreActiveTicks = 0;
        doomedTicks = 0;
        attackCooldownTicks = 0;
    }

    public static void sync(int riftStacks, int riftTicks, int active1CooldownTicks, int active1Ticks,
                            int equippedToolOrdinal, int coreCooldownTicks, boolean coreActive,
                            int coreActiveTicks, int doomedTicks, int attackCooldownTicks) {
        synced = true;
        ClientNikaidouHiroHudState.riftStacks = riftStacks;
        ClientNikaidouHiroHudState.riftTicks = riftTicks;
        ClientNikaidouHiroHudState.active1CooldownTicks = active1CooldownTicks;
        ClientNikaidouHiroHudState.active1Ticks = active1Ticks;
        NikaidouHiroTool[] tools = NikaidouHiroTool.values();
        ClientNikaidouHiroHudState.equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : NikaidouHiroTool.NONE;
        ClientNikaidouHiroHudState.coreCooldownTicks = coreCooldownTicks;
        ClientNikaidouHiroHudState.coreActive = coreActive;
        ClientNikaidouHiroHudState.coreActiveTicks = coreActiveTicks;
        ClientNikaidouHiroHudState.doomedTicks = doomedTicks;
        ClientNikaidouHiroHudState.attackCooldownTicks = attackCooldownTicks;
    }

    public static void tick() {
        if (riftTicks > 0) riftTicks--;
        if (active1CooldownTicks > 0) active1CooldownTicks--;
        if (active1Ticks > 0) active1Ticks--;
        if (coreCooldownTicks > 0) coreCooldownTicks--;
        if (coreActive) coreActiveTicks++;
        if (doomedTicks > 0) doomedTicks--;
        if (attackCooldownTicks > 0) attackCooldownTicks--;
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.NIKAIDOU_HIRO_ID);
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != NikaidouHiroTool.NONE;
    }

    public static int riftStacks() { return riftStacks; }
    public static int riftTicks() { return riftTicks; }
    public static int active1CooldownTicks() { return active1CooldownTicks; }
    public static int active1Ticks() { return active1Ticks; }
    public static NikaidouHiroTool equippedTool() { return equippedTool; }
    public static int coreCooldownTicks() { return coreCooldownTicks; }
    public static boolean coreActive() { return coreActive; }
    public static int coreActiveTicks() { return coreActiveTicks; }
    public static int doomedTicks() { return doomedTicks; }
    public static int attackCooldownTicks() { return attackCooldownTicks; }
}
