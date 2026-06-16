package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;

public final class ClientLexNinjiaHudState {
    private static boolean synced;
    private static float leicra;
    private static float maxLeicra = 200.0F;
    private static int handStacks;
    private static int bladeStacks;
    private static int harmonyStacks;
    private static String preparedArtId = "";
    private static boolean preparedAffordable;
    private static String comboText = "";
    private static int mindUsed;
    private static int mindCapacity = 25;
    private static int deathFlameTicks;
    private static float deathFlameOverflow;
    private static int hamPowerTicks;
    private static int hamBerserkTicks;
    private static int scientificToolLevel = -1;
    private static long lotusBoxes;

    private ClientLexNinjiaHudState() {
    }

    public static void reset() {
        synced = false;
        leicra = 0.0F;
        maxLeicra = 200.0F;
        handStacks = 0;
        bladeStacks = 0;
        harmonyStacks = 0;
        preparedArtId = "";
        preparedAffordable = false;
        comboText = "";
        mindUsed = 0;
        mindCapacity = 25;
        deathFlameTicks = 0;
        deathFlameOverflow = 0.0F;
        hamPowerTicks = 0;
        hamBerserkTicks = 0;
        scientificToolLevel = -1;
        lotusBoxes = 0L;
    }

    public static void sync(
            float leicra,
            float maxLeicra,
            int handStacks,
            int bladeStacks,
            int harmonyStacks,
            String preparedArtId,
            boolean preparedAffordable,
            String comboText,
            int mindUsed,
            int mindCapacity,
            int deathFlameTicks,
            float deathFlameOverflow,
            int hamPowerTicks,
            int hamBerserkTicks,
            int scientificToolLevel
    ) {
        synced = true;
        ClientLexNinjiaHudState.leicra = Math.max(0.0F, leicra);
        ClientLexNinjiaHudState.maxLeicra = Math.max(1.0F, maxLeicra);
        ClientLexNinjiaHudState.handStacks = Math.max(0, handStacks);
        ClientLexNinjiaHudState.bladeStacks = Math.max(0, bladeStacks);
        ClientLexNinjiaHudState.harmonyStacks = Math.max(0, harmonyStacks);
        ClientLexNinjiaHudState.preparedArtId = preparedArtId == null ? "" : preparedArtId;
        ClientLexNinjiaHudState.preparedAffordable = preparedAffordable;
        ClientLexNinjiaHudState.comboText = comboText == null ? "" : comboText;
        ClientLexNinjiaHudState.mindUsed = Math.max(0, mindUsed);
        ClientLexNinjiaHudState.mindCapacity = Math.max(1, mindCapacity);
        ClientLexNinjiaHudState.deathFlameTicks = Math.max(0, deathFlameTicks);
        ClientLexNinjiaHudState.deathFlameOverflow = Math.max(0.0F, deathFlameOverflow);
        ClientLexNinjiaHudState.hamPowerTicks = Math.max(0, hamPowerTicks);
        ClientLexNinjiaHudState.hamBerserkTicks = Math.max(0, hamBerserkTicks);
        ClientLexNinjiaHudState.scientificToolLevel = Math.max(-1, scientificToolLevel);
    }

    public static void tick() {
        deathFlameTicks = decrement(deathFlameTicks);
        hamPowerTicks = decrement(hamPowerTicks);
        hamBerserkTicks = decrement(hamBerserkTicks);
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.LEX_NINJIA_ID);
    }

    public static float leicra() {
        return leicra;
    }

    public static float maxLeicra() {
        return maxLeicra;
    }

    public static int handStacks() {
        return handStacks;
    }

    public static int bladeStacks() {
        return bladeStacks;
    }

    public static int harmonyStacks() {
        return harmonyStacks;
    }

    public static String preparedArtId() {
        return preparedArtId;
    }

    public static boolean hasPreparedArt() {
        return !preparedArtId.isBlank();
    }

    public static boolean preparedAffordable() {
        return preparedAffordable;
    }

    public static String comboText() {
        return comboText;
    }

    public static int mindUsed() {
        return mindUsed;
    }

    public static int mindCapacity() {
        return mindCapacity;
    }

    public static int deathFlameTicks() {
        return deathFlameTicks;
    }

    public static float deathFlameOverflow() {
        return deathFlameOverflow;
    }

    public static int hamPowerTicks() {
        return hamPowerTicks;
    }

    public static int hamBerserkTicks() {
        return hamBerserkTicks;
    }

    public static int scientificToolLevel() {
        return scientificToolLevel;
    }

    public static boolean hasScientificTool() {
        return scientificToolLevel >= 0;
    }

    public static long lotusBoxes() {
        return lotusBoxes;
    }

    public static void syncLotusBoxes(long lotusBoxes) {
        ClientLexNinjiaHudState.lotusBoxes = Math.max(0L, lotusBoxes);
    }

    private static int decrement(int value) {
        return value > 0 ? value - 1 : 0;
    }
}
