package com.rzy.dealt_force_skills.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rzy.dealt_force_skills.item.DfsEquipmentItem;
import com.rzy.dealt_force_skills.item.DfsEquipmentItem.Faction;
import com.rzy.dealt_force_skills.registry.ModItems;
import com.rzy.dealt_force_skills.registry.ModLootModifiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DfsChestLootModifier extends LootModifier {
    public static final Codec<DfsChestLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, DfsChestLootModifier::new));

    private static final float SPECIAL_EQUIPMENT_SUPPLY_CHANCE = 0.03F;
    private static final float PROGRAMMABLE_PROCESSOR_BASE_CHANCE = 0.06F;
    private static final float PROGRAMMABLE_PROCESSOR_LUCK_BONUS = 0.04F;

    public DfsChestLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation lootTableId = context.getQueriedLootTableId();
        if (lootTableId == null || !lootTableId.getPath().startsWith("chests/")) {
            return generatedLoot;
        }

        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity instanceof Player player && hasGtiEquipment(player)) {
            for (ItemStack stack : new ArrayList<>(generatedLoot)) {
                if (!stack.isEmpty()) {
                    generatedLoot.add(stack.copy());
                }
            }
        }

        if (context.getRandom().nextFloat() < SPECIAL_EQUIPMENT_SUPPLY_CHANCE) {
            generatedLoot.add(new ItemStack(ModItems.SPECIAL_EQUIPMENT_SUPPLY.get()));
        }

        float processorChance = Math.min(1.0F, PROGRAMMABLE_PROCESSOR_BASE_CHANCE
                + Math.max(0, Mth.floor(context.getLuck())) * PROGRAMMABLE_PROCESSOR_LUCK_BONUS);
        if (context.getRandom().nextFloat() < processorChance) {
            generatedLoot.add(new ItemStack(ModItems.PROGRAMMABLE_PROCESSOR.get(), 1 + context.getRandom().nextInt(3)));
        }

        return generatedLoot;
    }

    private static boolean hasGtiEquipment(Player player) {
        DfsEquipmentItem.Profile chest = DfsEquipmentItem.profile(player.getItemBySlot(EquipmentSlot.CHEST));
        if (chest != null && chest.faction() == Faction.GTI) {
            return true;
        }
        DfsEquipmentItem.Profile head = DfsEquipmentItem.profile(player.getItemBySlot(EquipmentSlot.HEAD));
        return head != null && head.faction() == Faction.GTI;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.CHEST_LOOT.get();
    }
}
