package com.rzy.dealt_force_skills.item;

import com.rzy.dealt_force_skills.effect.ModItemEffectHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

public class HarmfulCleanerItem extends DfsUseItem {
    private static final String SELECTED_EFFECT = "DfsSelectedHarmfulEffect";

    private final int maxAmplifierInclusive;
    private final boolean strongest;
    private final boolean selectable;

    public HarmfulCleanerItem(Properties properties,
                              DfsItemQuality quality,
                              String tooltipKey,
                              int useTicks,
                              int maxAmplifierInclusive,
                              boolean strongest,
                              Supplier<SoundEvent> startSound,
                              Supplier<SoundEvent> finishSound,
                              String startMessageKey,
                              String finishMessageKey) {
        this(properties, quality, tooltipKey, useTicks, maxAmplifierInclusive, strongest,
                startSound, finishSound, startMessageKey, finishMessageKey, false);
    }

    public HarmfulCleanerItem(Properties properties,
                              DfsItemQuality quality,
                              String tooltipKey,
                              int useTicks,
                              int maxAmplifierInclusive,
                              boolean strongest,
                              Supplier<SoundEvent> startSound,
                              Supplier<SoundEvent> finishSound,
                              String startMessageKey,
                              String finishMessageKey,
                              boolean selectable) {
        super(properties, quality, tooltipKey, useTicks, startSound, finishSound, startMessageKey, finishMessageKey);
        this.maxAmplifierInclusive = maxAmplifierInclusive;
        this.strongest = strongest;
        this.selectable = selectable;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (selectable && player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                cycleSelectedEffect(player, stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return super.use(level, player, hand);
    }

    @Override
    protected boolean canStartUse(Level level, Player player, ItemStack stack) {
        if (!selectable || level.isClientSide) {
            return true;
        }
        return ensureSelectedEffect(player, stack);
    }

    @Override
    protected boolean applyUseEffect(ItemStack stack, Level level, ServerPlayer player) {
        if (selectable) {
            MobEffect selected = selectedEffect(stack);
            boolean removed = ModItemEffectHelper.removeHarmfulEffect(player, selected, maxAmplifierInclusive);
            if (removed) {
                clearSelectedEffect(stack);
            }
            return removed;
        }
        return ModItemEffectHelper.removeHarmfulEffect(player, maxAmplifierInclusive, strongest);
    }

    @Override
    protected void addExtraTooltip(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.addExtraTooltip(stack, level, tooltip, flag);
        if (!selectable) {
            return;
        }
        tooltip.add(Component.translatable("tooltip.dealt_force_skills.select_harmful_effect")
                .withStyle(ChatFormatting.GRAY));
        MobEffect selected = selectedEffect(stack);
        if (selected != null) {
            tooltip.add(Component.translatable("tooltip.dealt_force_skills.selected_harmful_effect",
                    selected.getDisplayName()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private boolean ensureSelectedEffect(Player player, ItemStack stack) {
        List<ModItemEffectHelper.HarmfulEffectChoice> choices =
                ModItemEffectHelper.harmfulEffectChoices(player, maxAmplifierInclusive);
        if (choices.isEmpty()) {
            clearSelectedEffect(stack);
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.item.no_harmful_effect"), true);
            return false;
        }
        MobEffect selected = selectedEffect(stack);
        for (ModItemEffectHelper.HarmfulEffectChoice choice : choices) {
            if (choice.effect() == selected) {
                return true;
            }
        }
        setSelectedEffect(stack, choices.get(0).effect());
        displaySelected(player, choices.get(0), 1, choices.size());
        return true;
    }

    private void cycleSelectedEffect(Player player, ItemStack stack) {
        List<ModItemEffectHelper.HarmfulEffectChoice> choices =
                ModItemEffectHelper.harmfulEffectChoices(player, maxAmplifierInclusive);
        if (choices.isEmpty()) {
            clearSelectedEffect(stack);
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.item.no_harmful_effect"), true);
            return;
        }
        MobEffect selected = selectedEffect(stack);
        int current = -1;
        for (int i = 0; i < choices.size(); i++) {
            if (choices.get(i).effect() == selected) {
                current = i;
                break;
            }
        }
        int next = (current + 1) % choices.size();
        ModItemEffectHelper.HarmfulEffectChoice choice = choices.get(next);
        setSelectedEffect(stack, choice.effect());
        displaySelected(player, choice, next + 1, choices.size());
    }

    private static void displaySelected(Player player, ModItemEffectHelper.HarmfulEffectChoice choice, int index, int total) {
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.item.selected_harmful_effect",
                choice.effect().getDisplayName(), choice.amplifier() + 1,
                Math.max(0, choice.duration() / 20), index, total), true);
    }

    private static MobEffect selectedEffect(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(SELECTED_EFFECT)) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(stack.getTag().getString(SELECTED_EFFECT));
        return id == null ? null : ForgeRegistries.MOB_EFFECTS.getValue(id);
    }

    private static void setSelectedEffect(ItemStack stack, MobEffect effect) {
        ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        if (id == null) {
            clearSelectedEffect(stack);
        } else {
            stack.getOrCreateTag().putString(SELECTED_EFFECT, id.toString());
        }
    }

    private static void clearSelectedEffect(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove(SELECTED_EFFECT);
        }
    }
}
