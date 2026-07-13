package com.rzy.dealt_force_skills.client;

import net.minecraft.world.item.ItemStack;

public final class ClientTeamSpectatorState {
    private static boolean managed;
    private static int targetEntityId = -1;
    private static String targetName = "";
    private static String displayedCharacterId = "";
    private static ItemStack mainHand = ItemStack.EMPTY;
    private static ItemStack offHand = ItemStack.EMPTY;

    private ClientTeamSpectatorState() {
    }

    public static void sync(boolean managed, int targetEntityId, String targetName,
                            String characterId, ItemStack mainHand, ItemStack offHand) {
        ClientTeamSpectatorState.managed = managed;
        ClientTeamSpectatorState.targetEntityId = managed ? targetEntityId : -1;
        ClientTeamSpectatorState.targetName = managed && targetName != null ? targetName : "";
        ClientTeamSpectatorState.displayedCharacterId = managed && characterId != null ? characterId : "";
        ClientTeamSpectatorState.mainHand = managed && mainHand != null ? mainHand.copy() : ItemStack.EMPTY;
        ClientTeamSpectatorState.offHand = managed && offHand != null ? offHand.copy() : ItemStack.EMPTY;
    }

    public static void clear() {
        sync(false, -1, "", "", ItemStack.EMPTY, ItemStack.EMPTY);
    }

    public static boolean isManaged() {
        return managed;
    }

    public static int targetEntityId() {
        return targetEntityId;
    }

    public static boolean isTargetEntity(int entityId) {
        return managed && targetEntityId == entityId;
    }

    public static String targetName() {
        return targetName;
    }

    public static String displayedCharacterId() {
        return managed ? displayedCharacterId : "";
    }

    public static ItemStack mainHand() {
        return mainHand;
    }

    public static ItemStack offHand() {
        return offHand;
    }
}
