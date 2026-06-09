package com.rzy.dealt_force_skills.shop;

import com.rzy.dealt_force_skills.character.lexninjia.LexNinjiaArt;
import com.rzy.dealt_force_skills.character.lexninjia.LexNinjiaStateManager;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_OpenLexNinjiaShop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class LexNinjiaShopManager {
    private static final long MIND_EXPANSION_PRICE = 5000L;

    private LexNinjiaShopManager() {
    }

    public static boolean shouldOpen(ServerPlayer player) {
        return LexNinjiaStateManager.isLexNinjia(player) && LexNinjiaCurrencyManager.canUseShop(player);
    }

    public static void open(ServerPlayer player) {
        if (!shouldOpen(player)) {
            return;
        }
        NetworkHandler.sendToPlayer(new S2C_OpenLexNinjiaShop(
                LexNinjiaCurrencyManager.get(player),
                LexNinjiaStateManager.shopData(player)
        ), player);
    }

    public static void handleAction(ServerPlayer player, String artId, LexNinjiaShopAction action) {
        if (!shouldOpen(player)) {
            LexNinjiaCurrencyManager.sync(player);
            return;
        }
        if (action == LexNinjiaShopAction.EXPAND_MIND) {
            syncAndReopen(player);
            return;
        }
        LexNinjiaArt art = LexNinjiaArt.byId(artId).orElse(null);
        if (art == null || !LexNinjiaStateManager.canDisplayInShop(player, art)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.lex_ninjia_shop.invalid_art"), true);
            syncAndReopen(player);
            return;
        }
        if (action == LexNinjiaShopAction.BUY) {
            buy(player, art);
        } else if (action == LexNinjiaShopAction.TOGGLE) {
            toggle(player, art);
        }
    }

    private static void buy(ServerPlayer player, LexNinjiaArt art) {
        if (LexNinjiaStateManager.isKnown(player, art)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.lex_ninjia_shop.known"), true);
            syncAndReopen(player);
            return;
        }
        if (!LexNinjiaCurrencyManager.trySpend(player, art.price())) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.lex_ninjia_shop.not_enough_lotus"), true);
            syncAndReopen(player);
            return;
        }
        LexNinjiaStateManager.learnArt(player, art);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.lex_ninjia_shop.learned",
                Component.translatable(art.nameKey()), art.price()), true);
        syncAndReopen(player);
    }

    private static void toggle(ServerPlayer player, LexNinjiaArt art) {
        if (!LexNinjiaStateManager.isKnown(player, art)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.lex_ninjia_shop.learn_first"), true);
            syncAndReopen(player);
            return;
        }
        boolean equip = !LexNinjiaStateManager.isEquipped(player, art);
        if (!LexNinjiaStateManager.setEquipped(player, art, equip)) {
            player.displayClientMessage(Component.translatable(equip
                    ? "message.dealt_force_skills.lex_ninjia_shop.no_mind"
                    : "message.dealt_force_skills.lex_ninjia_shop.cannot_unequip"), true);
        }
        syncAndReopen(player);
    }

    private static void expandMind(ServerPlayer player) {
        int expansions = LexNinjiaStateManager.forcedMindExpansions(player);
        if (expansions >= LexNinjiaStateManager.MAX_FORCED_MIND_EXPANSIONS) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.lex_ninjia_shop.mind_maxed"), true);
            syncAndReopen(player);
            return;
        }
        long price = MIND_EXPANSION_PRICE * (expansions + 1L);
        if (!LexNinjiaCurrencyManager.trySpend(player, price)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.lex_ninjia_shop.not_enough_lotus"), true);
            syncAndReopen(player);
            return;
        }
        LexNinjiaStateManager.expandMind(player);
        syncAndReopen(player);
    }

    private static void syncAndReopen(ServerPlayer player) {
        LexNinjiaStateManager.syncToClient(player);
        LexNinjiaCurrencyManager.sync(player);
        CompoundTag data = LexNinjiaStateManager.shopData(player);
        NetworkHandler.sendToPlayer(new S2C_OpenLexNinjiaShop(LexNinjiaCurrencyManager.get(player), data), player);
    }
}
