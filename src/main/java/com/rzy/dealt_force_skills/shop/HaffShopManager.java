package com.rzy.dealt_force_skills.shop;

import com.rzy.dealt_force_skills.character.ghroth.GhrothStateManager;
import com.rzy.dealt_force_skills.character.lexninjia.LexNinjiaStateManager;
import com.rzy.dealt_force_skills.character.saeed.SaeedStateManager;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_OpenHaffShop;
import com.rzy.dealt_force_skills.registry.ModGameRules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class HaffShopManager {
    private HaffShopManager() {
    }

    public static boolean shouldOpenShop(ServerPlayer player) {
        return player.serverLevel().getGameRules().getBoolean(ModGameRules.DEALT_FORCE_SHOP)
                && !LexNinjiaStateManager.isLexNinjia(player)
                && !GhrothStateManager.isGhroth(player)
                && !SaeedStateManager.isSaeed(player)
                && HaffCoinManager.canUseShop(player);
    }

    public static void openShop(ServerPlayer player) {
        if (!shouldOpenShop(player)) {
            return;
        }
        NetworkHandler.sendToPlayer(new S2C_OpenHaffShop(HaffCoinManager.get(player)), player);
    }

    public static void buy(ServerPlayer player, String entryId) {
        if (!shouldOpenShop(player)) {
            HaffCoinManager.sync(player);
            return;
        }
        DfsShopCatalog.Entry entry = DfsShopCatalog.find(entryId).orElse(null);
        if (entry == null) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.shop.invalid_item"), true);
            HaffCoinManager.sync(player);
            return;
        }
        if (!HaffCoinManager.trySpend(player, entry.price())) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.shop.not_enough_coins"), true);
            return;
        }

        ItemStack purchased = new ItemStack(entry.item().get());
        boolean inserted = player.getInventory().add(purchased);
        if (!inserted && !purchased.isEmpty()) {
            player.drop(purchased, false);
        }
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.shop.purchased",
                entry.item().get().getDescription(), entry.price()), true);
        HaffCoinManager.sync(player);
    }
}
