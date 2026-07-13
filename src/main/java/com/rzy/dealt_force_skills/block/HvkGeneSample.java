package com.rzy.dealt_force_skills.block;

import com.rzy.dealt_force_skills.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class HvkGeneSample {
    public static final String TAG_ENTITY = "HvkGeneSampleEntity";

    private HvkGeneSample() {
    }

    public static ItemStack create(ResourceLocation entityId, String entityName) {
        ItemStack stack = new ItemStack(ModItems.GLASS_SYRINGE.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_ENTITY, entityId == null ? "minecraft:pig" : entityId.toString());
        stack.setHoverName(Component.translatable("item.dealt_force_skills.hvk_gene_sample",
                entityName == null || entityName.isBlank() ? tag.getString(TAG_ENTITY) : entityName)
                .withStyle(ChatFormatting.AQUA));
        return stack;
    }

    public static boolean isFor(ItemStack stack, ResourceLocation entityId) {
        ResourceLocation stored = entityId(stack);
        return stored != null && stored.equals(entityId);
    }

    public static ResourceLocation entityId(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() != ModItems.GLASS_SYRINGE.get() || !stack.hasTag()) {
            return null;
        }
        String value = stack.getOrCreateTag().getString(TAG_ENTITY);
        if (value.isBlank()) {
            return null;
        }
        try {
            return ResourceLocation.parse(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
