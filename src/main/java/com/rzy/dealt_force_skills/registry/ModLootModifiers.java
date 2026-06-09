package com.rzy.dealt_force_skills.registry;

import com.mojang.serialization.Codec;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.loot.DfsChestLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, DealtForceSkillsMod.MODID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> CHEST_LOOT =
            LOOT_MODIFIER_SERIALIZERS.register("chest_loot", () -> DfsChestLootModifier.CODEC);
}
