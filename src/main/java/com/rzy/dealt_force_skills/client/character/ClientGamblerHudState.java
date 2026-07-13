package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.gambler.GamblerSuperpower;

import java.util.Arrays;

public final class ClientGamblerHudState {
    private static boolean synced;
    private static int chips;
    private static int shield;
    private static int active1CooldownTicks;
    private static int active2CooldownTicks;
    private static int active2UsesLeft;
    private static int active2ExpiresTicks;
    private static boolean coreActive;
    private static int jackpotTicks;
    private static int invulnerableTicks;
    private static int[] powers = new int[0];

    private ClientGamblerHudState() {
    }

    public static void reset() {
        synced = false;
        chips = 0;
        shield = 0;
        active1CooldownTicks = 0;
        active2CooldownTicks = 0;
        active2UsesLeft = 0;
        active2ExpiresTicks = 0;
        coreActive = false;
        jackpotTicks = 0;
        invulnerableTicks = 0;
        powers = new int[0];
    }

    public static void sync(int chips, int shield, int active1CooldownTicks, int active2CooldownTicks,
                            int active2UsesLeft, int active2ExpiresTicks, boolean coreActive,
                            int jackpotTicks, int invulnerableTicks, int[] powers) {
        synced = true;
        ClientGamblerHudState.chips = Math.max(0, chips);
        ClientGamblerHudState.shield = Math.max(0, shield);
        ClientGamblerHudState.active1CooldownTicks = Math.max(0, active1CooldownTicks);
        ClientGamblerHudState.active2CooldownTicks = Math.max(0, active2CooldownTicks);
        ClientGamblerHudState.active2UsesLeft = Math.max(0, active2UsesLeft);
        ClientGamblerHudState.active2ExpiresTicks = Math.max(0, active2ExpiresTicks);
        ClientGamblerHudState.coreActive = coreActive;
        ClientGamblerHudState.jackpotTicks = Math.max(0, jackpotTicks);
        ClientGamblerHudState.invulnerableTicks = Math.max(0, invulnerableTicks);
        ClientGamblerHudState.powers = powers == null ? new int[0] : Arrays.copyOf(powers, powers.length);
    }

    public static void tick() {
        if (active1CooldownTicks > 0) active1CooldownTicks--;
        if (active2CooldownTicks > 0) active2CooldownTicks--;
        if (active2ExpiresTicks > 0) active2ExpiresTicks--;
        if (jackpotTicks > 0) jackpotTicks--;
        if (invulnerableTicks > 0) invulnerableTicks--;
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.GAMBLER_ID);
    }

    public static boolean shouldDisplay() {
        return synced && ClientCharacterSelectionState.isDisplayedCharacter(ModCharacters.GAMBLER_ID);
    }

    public static int chips() {
        return chips;
    }

    public static int shield() {
        return shield;
    }

    public static int active1CooldownTicks() {
        return active1CooldownTicks;
    }

    public static int active2CooldownTicks() {
        return active2CooldownTicks;
    }

    public static int active2UsesLeft() {
        return active2UsesLeft;
    }

    public static int active2ExpiresTicks() {
        return active2ExpiresTicks;
    }

    public static boolean coreActive() {
        return coreActive;
    }

    public static int jackpotTicks() {
        return jackpotTicks;
    }

    public static int invulnerableTicks() {
        return invulnerableTicks;
    }

    public static int[] powers() {
        return Arrays.copyOf(powers, powers.length);
    }

    public static int count(GamblerSuperpower power) {
        if (power == null || power.ordinal() >= powers.length) {
            return 0;
        }
        return Math.max(0, powers[power.ordinal()]);
    }

    public static int totalPowers() {
        int total = 0;
        for (int count : powers) {
            total += Math.max(0, count);
        }
        return total;
    }
}
