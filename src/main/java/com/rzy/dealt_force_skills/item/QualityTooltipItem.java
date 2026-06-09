package com.rzy.dealt_force_skills.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class QualityTooltipItem extends Item {
    private final DfsItemQuality quality;
    private final String tooltipKey;

    public QualityTooltipItem(Properties properties, DfsItemQuality quality, String tooltipKey) {
        super(properties);
        this.quality = quality;
        this.tooltipKey = tooltipKey;
    }

    public DfsItemQuality quality() {
        return quality;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack)).withStyle(style -> style.withColor(quality.color()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.dealt_force_skills.quality",
                Component.translatable("quality.dealt_force_skills." + quality.key())).withStyle(ChatFormatting.GRAY));
        if (stack.isDamageableItem() && stack.isDamaged()) {
            tooltip.add(Component.translatable("tooltip.dealt_force_skills.durability",
                    stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage()).withStyle(ChatFormatting.GRAY));
        }
        if (tooltipKey != null && !tooltipKey.isBlank()) {
            tooltip.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.DARK_GRAY));
        }
        addExtraTooltip(stack, level, tooltip, flag);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return super.isBarVisible(stack);
    }

    protected void addExtraTooltip(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
    }
}
