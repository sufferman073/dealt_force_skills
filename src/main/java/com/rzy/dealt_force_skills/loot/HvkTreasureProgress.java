package com.rzy.dealt_force_skills.loot;

import com.rzy.dealt_force_skills.config.DealtForceConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class HvkTreasureProgress {
    private static final String PLAYER_PERSISTED = "PlayerPersisted";
    private static final String ROOT = "dealt_force_skills.hvk_treasure_progress";
    private static final String OPENED_CHEST_LOOT = "OpenedChestLootCount";

    private HvkTreasureProgress() {
    }

    public static int requiredOpenedChestLootForCompass() {
        return Math.max(0, DealtForceConfig.intValue(
                "loot.hvk_treasure_progress.required_opened_chest_loot_for_compass", 50));
    }

    public static int openedChestLootCount(Player player) {
        if (player == null) {
            return 0;
        }
        return root(player, false).getInt(OPENED_CHEST_LOOT);
    }

    public static int recordOpenedChestLoot(Player player) {
        if (player == null) {
            return 0;
        }
        CompoundTag root = root(player, true);
        int next = Math.max(0, root.getInt(OPENED_CHEST_LOOT)) + 1;
        root.putInt(OPENED_CHEST_LOOT, next);
        return next;
    }

    public static boolean canCraftCompass(Player player) {
        return openedChestLootCount(player) >= requiredOpenedChestLootForCompass();
    }

    private static CompoundTag root(Player player, boolean create) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag persisted = persistent.getCompound(PLAYER_PERSISTED);
        if (create && !persistent.contains(PLAYER_PERSISTED, 10)) {
            persistent.put(PLAYER_PERSISTED, persisted);
        }
        CompoundTag root = persisted.getCompound(ROOT);
        if (create && !persisted.contains(ROOT, 10)) {
            persisted.put(ROOT, root);
        }
        return root;
    }
}
