package com.rzy.dealt_force_skills.shop;

import com.rzy.dealt_force_skills.character.lexninjia.LexNinjiaArt;
import com.rzy.dealt_force_skills.character.lexninjia.LexNinjiaStateManager;
import com.rzy.dealt_force_skills.network.C2S_LexNinjiaPresetAction;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_OpenLexNinjiaShop;
import com.rzy.dealt_force_skills.network.S2C_OpenLexNinjiaPresets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class LexNinjiaShopManager {
    private static final long MIND_EXPANSION_BASE_PRICE = com.rzy.dealt_force_skills.config.DealtForceConfig.longValue("shop.lexninjiashopmanager.mind_expansion_base_price", 1500L);
    private static final long MIND_EXPANSION_PRICE_STEP = com.rzy.dealt_force_skills.config.DealtForceConfig.longValue("shop.lexninjiashopmanager.mind_expansion_price_step", 650L);
    private static final long SCIENTIFIC_TOOL_PURCHASE_PRICE = com.rzy.dealt_force_skills.config.DealtForceConfig.longValue("shop.lexninjiashopmanager.scientific_tool_purchase_price", 6000L);
    private static final long SCIENTIFIC_TOOL_UPGRADE_BASE_PRICE = com.rzy.dealt_force_skills.config.DealtForceConfig.longValue("shop.lexninjiashopmanager.scientific_tool_upgrade_base_price", 3000L);
    private static final long SCIENTIFIC_TOOL_UPGRADE_PRICE_STEP = com.rzy.dealt_force_skills.config.DealtForceConfig.longValue("shop.lexninjiashopmanager.scientific_tool_upgrade_price_step", 1500L);

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
            expandMind(player);
            return;
        }
        if (action == LexNinjiaShopAction.BUY_SCIENTIFIC_TOOL) {
            buyScientificTool(player);
            return;
        }
        if (action == LexNinjiaShopAction.UPGRADE_SCIENTIFIC_TOOL) {
            upgradeScientificTool(player);
            return;
        }
        if (artId == null || artId.isBlank()) {
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
        long price = mindExpansionPrice(expansions);
        if (!LexNinjiaCurrencyManager.trySpend(player, price)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.lex_ninjia_shop.not_enough_lotus"), true);
            syncAndReopen(player);
            return;
        }
        LexNinjiaStateManager.expandMind(player);
        syncAndReopen(player);
    }

    private static void buyScientificTool(ServerPlayer player) {
        if (LexNinjiaStateManager.hasScientificTool(player)) {
            syncAndReopen(player);
            return;
        }
        if (!LexNinjiaCurrencyManager.trySpend(player, SCIENTIFIC_TOOL_PURCHASE_PRICE)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.lex_ninjia_shop.not_enough_lotus"), true);
            syncAndReopen(player);
            return;
        }
        LexNinjiaStateManager.buyScientificTool(player);
        syncAndReopen(player);
    }

    private static void upgradeScientificTool(ServerPlayer player) {
        int level = LexNinjiaStateManager.scientificToolLevel(player);
        if (level < 0 || level >= LexNinjiaStateManager.SCIENTIFIC_TOOL_MAX_LEVEL) {
            syncAndReopen(player);
            return;
        }
        long price = scientificToolUpgradePrice(level);
        if (!LexNinjiaCurrencyManager.trySpend(player, price)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.lex_ninjia_shop.not_enough_lotus"), true);
            syncAndReopen(player);
            return;
        }
        LexNinjiaStateManager.upgradeScientificTool(player);
        syncAndReopen(player);
    }

    public static long mindExpansionPrice(int currentLevel) {
        return MIND_EXPANSION_BASE_PRICE + Math.max(0, currentLevel) * MIND_EXPANSION_PRICE_STEP;
    }

    public static long scientificToolPurchasePrice() {
        return SCIENTIFIC_TOOL_PURCHASE_PRICE;
    }

    public static long scientificToolUpgradePrice(int currentLevel) {
        return SCIENTIFIC_TOOL_UPGRADE_BASE_PRICE
                + Math.max(0, currentLevel) * SCIENTIFIC_TOOL_UPGRADE_PRICE_STEP;
    }

    public static void handlePresetAction(ServerPlayer player, C2S_LexNinjiaPresetAction action) {
        if (!shouldOpen(player)) {
            return;
        }
        switch (action.action()) {
            case OPEN_MENU -> openPresets(player, false);
            case OPEN_CONFIG -> openPresets(player, true);
            case SAVE -> {
                LexNinjiaStateManager.saveScientificPreset(player, action.slot(), action.name(), action.inputs());
                openPresets(player, true);
            }
            case EXECUTE -> {
                LexNinjiaStateManager.executeScientificPreset(player, action.slot());
                LexNinjiaCurrencyManager.sync(player);
            }
        }
    }

    private static void openPresets(ServerPlayer player, boolean configure) {
        if (!LexNinjiaStateManager.hasScientificTool(player)
                || (!configure && player.isCreative())) {
            return;
        }
        NetworkHandler.sendToPlayer(new S2C_OpenLexNinjiaPresets(
                configure,
                LexNinjiaStateManager.shopData(player)
        ), player);
    }

    private static void syncAndReopen(ServerPlayer player) {
        LexNinjiaStateManager.syncToClient(player);
        LexNinjiaCurrencyManager.sync(player);
        CompoundTag data = LexNinjiaStateManager.shopData(player);
        NetworkHandler.sendToPlayer(new S2C_OpenLexNinjiaShop(LexNinjiaCurrencyManager.get(player), data), player);
    }
}
