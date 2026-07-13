package com.rzy.dealt_force_skills.item;

import com.rzy.dealt_force_skills.block.HvkAdvancedTreasureCompassBlockEntity;
import com.rzy.dealt_force_skills.block.WorkBlockStorageAccess;
import com.rzy.dealt_force_skills.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class HvkTreasureChestBoxItem extends Item {
    private static final String TAG_LOOT_TABLE = "HvkTreasureLootTable";
    private static final String TAG_LOOT_NAME = "HvkTreasureLootName";

    public HvkTreasureChestBoxItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(ResourceLocation lootTable, String name) {
        ItemStack stack = new ItemStack(ModItems.HVK_TREASURE_CHEST_BOX.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_LOOT_TABLE, lootTable == null ? "minecraft:chests/simple_dungeon" : lootTable.toString());
        tag.putString(TAG_LOOT_NAME, name == null || name.isBlank() ? tag.getString(TAG_LOOT_TABLE) : name);
        stack.setHoverName(Component.translatable("item.dealt_force_skills.hvk_treasure_chest_box.named",
                tag.getString(TAG_LOOT_NAME)).withStyle(ChatFormatting.GOLD));
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }
        ResourceLocation lootTable = lootTable(stack);
        if (lootTable == null) {
            serverPlayer.displayClientMessage(Component.translatable("message.dealt_force_skills.hvk_treasure.box_invalid"), true);
            return InteractionResultHolder.fail(stack);
        }
        List<ItemStack> loot = HvkAdvancedTreasureCompassBlockEntity.chestBoxLoot(serverPlayer, serverLevel, lootTable);
        if (loot.isEmpty()) {
            serverPlayer.displayClientMessage(Component.translatable("message.dealt_force_skills.hvk_treasure.no_loot"), true);
            return InteractionResultHolder.fail(stack);
        }
        String openedName = displayName(stack);
        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        int produced = 0;
        for (ItemStack output : loot) {
            produced += output.getCount();
            WorkBlockStorageAccess.giveWithBlockLootBonus(serverPlayer, level, serverPlayer.blockPosition(), output.copy());
        }
        serverPlayer.displayClientMessage(Component.translatable("message.dealt_force_skills.hvk_treasure.box_opened",
                openedName, produced), true);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.dealt_force_skills.hvk_treasure_chest_box",
                displayName(stack)).withStyle(ChatFormatting.GRAY));
    }

    private static ResourceLocation lootTable(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return null;
        }
        String value = stack.getOrCreateTag().getString(TAG_LOOT_TABLE);
        if (value.isBlank()) {
            return null;
        }
        try {
            return ResourceLocation.parse(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String displayName(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return "";
        }
        String name = stack.getOrCreateTag().getString(TAG_LOOT_NAME);
        return name.isBlank() ? stack.getOrCreateTag().getString(TAG_LOOT_TABLE) : name;
    }
}
