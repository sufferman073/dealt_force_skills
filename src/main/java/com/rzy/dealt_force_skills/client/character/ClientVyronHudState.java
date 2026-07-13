package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.vyron.VyronTool;

public final class ClientVyronHudState {
    private static boolean synced;
    private static int dashCooldownTicks;
    private static int dashTicks;
    private static int poweredTicks;
    private static int bombCharges;
    private static int bombMaxCharges;
    private static int bombRechargeTicks;
    private static int coreCooldownTicks;
    private static VyronTool equippedTool = VyronTool.NONE;

    private ClientVyronHudState() {
    }

    public static void reset() {
        synced = false;
        dashCooldownTicks = 0;
        dashTicks = 0;
        poweredTicks = 0;
        bombCharges = 0;
        bombMaxCharges = 0;
        bombRechargeTicks = 0;
        coreCooldownTicks = 0;
        equippedTool = VyronTool.NONE;
    }

    public static void sync(
            int dashCooldownTicks,
            int dashTicks,
            int poweredTicks,
            int bombCharges,
            int bombMaxCharges,
            int bombRechargeTicks,
            int coreCooldownTicks,
            int equippedToolOrdinal
    ) {
        synced = true;
        ClientVyronHudState.dashCooldownTicks = dashCooldownTicks;
        ClientVyronHudState.dashTicks = dashTicks;
        ClientVyronHudState.poweredTicks = poweredTicks;
        ClientVyronHudState.bombCharges = bombCharges;
        ClientVyronHudState.bombMaxCharges = bombMaxCharges;
        ClientVyronHudState.bombRechargeTicks = bombRechargeTicks;
        ClientVyronHudState.coreCooldownTicks = coreCooldownTicks;
        VyronTool[] tools = VyronTool.values();
        ClientVyronHudState.equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : VyronTool.NONE;
    }

    public static void tick() {
        if (dashCooldownTicks > 0) {
            dashCooldownTicks--;
        }
        if (dashTicks > 0) {
            dashTicks--;
        }
        if (poweredTicks > 0) {
            poweredTicks--;
        }
        if (bombRechargeTicks > 0) {
            bombRechargeTicks--;
        }
        if (coreCooldownTicks > 0) {
            coreCooldownTicks--;
        }
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.VYRON_ID);
    }

    public static boolean shouldDisplay() {
        return synced && ClientCharacterSelectionState.isDisplayedCharacter(ModCharacters.VYRON_ID);
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != VyronTool.NONE;
    }

    public static int dashCooldownTicks() {
        return dashCooldownTicks;
    }

    public static int dashTicks() {
        return dashTicks;
    }

    public static int poweredTicks() {
        return poweredTicks;
    }

    public static int bombCharges() {
        return bombCharges;
    }

    public static int bombMaxCharges() {
        return bombMaxCharges;
    }

    public static int bombRechargeTicks() {
        return bombRechargeTicks;
    }

    public static int coreCooldownTicks() {
        return coreCooldownTicks;
    }

    public static VyronTool equippedTool() {
        return equippedTool;
    }
}
