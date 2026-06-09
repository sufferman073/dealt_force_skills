package com.rzy.dealt_force_skills;

import com.rzy.dealt_force_skills.entity.NoxDecoyEntity;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.registry.ModBlockEntities;
import com.rzy.dealt_force_skills.registry.ModBlocks;
import com.rzy.dealt_force_skills.registry.ModBrewingRecipes;
import com.rzy.dealt_force_skills.registry.ModCreativeTabs;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModEntities;
import com.rzy.dealt_force_skills.registry.ModGameRules;
import com.rzy.dealt_force_skills.registry.ModItems;
import com.rzy.dealt_force_skills.registry.ModLootModifiers;
import com.rzy.dealt_force_skills.registry.ModParticles;
import com.rzy.dealt_force_skills.registry.ModSounds;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DealtForceSkillsMod.MODID)
public class DealtForceSkillsMod {
    public static final String MODID = "dealt_force_skills";

    public DealtForceSkillsMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModSounds.SOUNDS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModLootModifiers.LOOT_MODIFIER_SERIALIZERS.register(modBus);
        ModCreativeTabs.CREATIVE_TABS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEffects.EFFECTS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModParticles.PARTICLES.register(modBus);
        ModGameRules.register();

        modBus.addListener(NetworkHandler::onCommonSetup);
        modBus.addListener(this::onCommonSetup);
        modBus.addListener(this::registerEntityAttributes);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModBrewingRecipes::register);
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.NOX_DECOY.get(), NoxDecoyEntity.createAttributes().build());
    }
}
