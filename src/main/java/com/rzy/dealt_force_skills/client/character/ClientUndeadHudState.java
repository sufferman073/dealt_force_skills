package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.undead.UndeadProfession;

public final class ClientUndeadHudState {
    private static boolean synced;
    private static UndeadProfession profession = UndeadProfession.KNIGHT;
    private static float energy;
    private static float maxEnergy = 100.0F;
    private static int disorientedTicks;
    private static int coreCooldownTicks;
    private static int knightShieldTicks;
    private static float knightShield;
    private static boolean warriorMight;
    private static int warriorBloodlustTicks;
    private static int explorerMeditationTicks;
    private static int explorerSpaceTicks;
    private static boolean explorerCanSeeEntities;
    private static boolean explorerExtendedGuidance;
    private static int rogueInvisibleTicks;
    private static int scholarRitualTicks;
    private static boolean hunterScatter;
    private static int hunterExhaustedTicks;
    private static long souls;

    private ClientUndeadHudState() {
    }

    public static void reset() {
        synced = false;
        profession = UndeadProfession.KNIGHT;
        energy = 0.0F;
        maxEnergy = 100.0F;
        disorientedTicks = 0;
        coreCooldownTicks = 0;
        knightShieldTicks = 0;
        knightShield = 0.0F;
        warriorMight = false;
        warriorBloodlustTicks = 0;
        explorerMeditationTicks = 0;
        explorerSpaceTicks = 0;
        explorerCanSeeEntities = false;
        explorerExtendedGuidance = false;
        rogueInvisibleTicks = 0;
        scholarRitualTicks = 0;
        hunterScatter = false;
        hunterExhaustedTicks = 0;
        souls = 0L;
    }

    public static void sync(
            int professionOrdinal,
            float energy,
            float maxEnergy,
            int disorientedTicks,
            int coreCooldownTicks,
            int knightShieldTicks,
            float knightShield,
            boolean warriorMight,
            int warriorBloodlustTicks,
            int explorerMeditationTicks,
            int explorerSpaceTicks,
            boolean explorerCanSeeEntities,
            boolean explorerExtendedGuidance,
            int rogueInvisibleTicks,
            int scholarRitualTicks,
            boolean hunterScatter,
            int hunterExhaustedTicks
    ) {
        synced = true;
        profession = UndeadProfession.byOrdinal(professionOrdinal);
        ClientUndeadHudState.energy = Math.max(0.0F, energy);
        ClientUndeadHudState.maxEnergy = Math.max(1.0F, maxEnergy);
        ClientUndeadHudState.disorientedTicks = Math.max(0, disorientedTicks);
        ClientUndeadHudState.coreCooldownTicks = Math.max(0, coreCooldownTicks);
        ClientUndeadHudState.knightShieldTicks = Math.max(0, knightShieldTicks);
        ClientUndeadHudState.knightShield = Math.max(0.0F, knightShield);
        ClientUndeadHudState.warriorMight = warriorMight;
        ClientUndeadHudState.warriorBloodlustTicks = Math.max(0, warriorBloodlustTicks);
        ClientUndeadHudState.explorerMeditationTicks = Math.max(0, explorerMeditationTicks);
        ClientUndeadHudState.explorerSpaceTicks = Math.max(0, explorerSpaceTicks);
        ClientUndeadHudState.explorerCanSeeEntities = explorerCanSeeEntities;
        ClientUndeadHudState.explorerExtendedGuidance = explorerExtendedGuidance;
        ClientUndeadHudState.rogueInvisibleTicks = Math.max(0, rogueInvisibleTicks);
        ClientUndeadHudState.scholarRitualTicks = Math.max(0, scholarRitualTicks);
        ClientUndeadHudState.hunterScatter = hunterScatter;
        ClientUndeadHudState.hunterExhaustedTicks = Math.max(0, hunterExhaustedTicks);
    }

    public static void tick() {
        disorientedTicks = decrement(disorientedTicks);
        coreCooldownTicks = decrement(coreCooldownTicks);
        knightShieldTicks = decrement(knightShieldTicks);
        warriorBloodlustTicks = decrement(warriorBloodlustTicks);
        explorerMeditationTicks = decrement(explorerMeditationTicks);
        explorerSpaceTicks = decrement(explorerSpaceTicks);
        rogueInvisibleTicks = decrement(rogueInvisibleTicks);
        scholarRitualTicks = decrement(scholarRitualTicks);
        hunterExhaustedTicks = decrement(hunterExhaustedTicks);
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.UNDEAD_ID);
    }

    public static boolean shouldDisplay() {
        return synced && ClientCharacterSelectionState.isDisplayedCharacter(ModCharacters.UNDEAD_ID);
    }

    public static UndeadProfession profession() {
        return profession;
    }

    public static float energy() {
        return energy;
    }

    public static float maxEnergy() {
        return maxEnergy;
    }

    public static int disorientedTicks() {
        return disorientedTicks;
    }

    public static int coreCooldownTicks() {
        return coreCooldownTicks;
    }

    public static int knightShieldTicks() {
        return knightShieldTicks;
    }

    public static float knightShield() {
        return knightShield;
    }

    public static boolean warriorMight() {
        return warriorMight;
    }

    public static int warriorBloodlustTicks() {
        return warriorBloodlustTicks;
    }

    public static int explorerMeditationTicks() {
        return explorerMeditationTicks;
    }

    public static int explorerSpaceTicks() {
        return explorerSpaceTicks;
    }

    public static boolean explorerCanSeeEntities() {
        return explorerCanSeeEntities;
    }

    public static boolean explorerExtendedGuidance() {
        return explorerExtendedGuidance;
    }

    public static int rogueInvisibleTicks() {
        return rogueInvisibleTicks;
    }

    public static int scholarRitualTicks() {
        return scholarRitualTicks;
    }

    public static boolean hunterScatter() {
        return hunterScatter;
    }

    public static int hunterExhaustedTicks() {
        return hunterExhaustedTicks;
    }

    public static long souls() {
        return souls;
    }

    public static void syncSouls(long souls) {
        ClientUndeadHudState.souls = Math.max(0L, souls);
    }

    private static int decrement(int value) {
        return value > 0 ? value - 1 : 0;
    }
}
