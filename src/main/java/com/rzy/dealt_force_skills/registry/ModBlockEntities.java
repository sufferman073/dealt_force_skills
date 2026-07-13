package com.rzy.dealt_force_skills.registry;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.block.BladeWireBlockEntity;
import com.rzy.dealt_force_skills.block.HvkAdvancedDisassemblyBeamEmitterBlockEntity;
import com.rzy.dealt_force_skills.block.HvkAdvancedStandardTemplateConstructorBlockEntity;
import com.rzy.dealt_force_skills.block.HvkAdvancedTreasureCompassBlockEntity;
import com.rzy.dealt_force_skills.block.HvkClonePrototypeBlockEntity;
import com.rzy.dealt_force_skills.block.InterdimensionalBlockEntity;
import com.rzy.dealt_force_skills.block.QuickCoverBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, DealtForceSkillsMod.MODID);

    public static final RegistryObject<BlockEntityType<BladeWireBlockEntity>> BLADE_WIRE =
            BLOCK_ENTITIES.register("blade_wire",
                    () -> BlockEntityType.Builder.of(
                            BladeWireBlockEntity::new,
                            ModBlocks.BLADE_WIRE.get(),
                            ModBlocks.BLADE_WIRE_CORE.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<QuickCoverBlockEntity>> QUICK_COVER =
            BLOCK_ENTITIES.register("quick_cover",
                    () -> BlockEntityType.Builder.of(
                            QuickCoverBlockEntity::new,
                            ModBlocks.QUICK_COVER.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<HvkAdvancedStandardTemplateConstructorBlockEntity>>
            HVK_ADVANCED_STANDARD_TEMPLATE_CONSTRUCTOR =
            BLOCK_ENTITIES.register("hvk_advanced_standard_template_constructor",
                    () -> BlockEntityType.Builder.of(
                            HvkAdvancedStandardTemplateConstructorBlockEntity::new,
                            ModBlocks.HVK_ADVANCED_STANDARD_TEMPLATE_CONSTRUCTOR.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<HvkAdvancedDisassemblyBeamEmitterBlockEntity>>
            HVK_ADVANCED_DISASSEMBLY_BEAM_EMITTER =
            BLOCK_ENTITIES.register("hvk_advanced_disassembly_beam_emitter",
                    () -> BlockEntityType.Builder.of(
                            HvkAdvancedDisassemblyBeamEmitterBlockEntity::new,
                            ModBlocks.HVK_ADVANCED_DISASSEMBLY_BEAM_EMITTER.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<HvkClonePrototypeBlockEntity>>
            HVK_CLONE_PROTOTYPE =
            BLOCK_ENTITIES.register("hvk_clone_prototype",
                    () -> BlockEntityType.Builder.of(
                            HvkClonePrototypeBlockEntity::new,
                            ModBlocks.HVK_CLONE_PROTOTYPE.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<HvkAdvancedTreasureCompassBlockEntity>>
            HVK_ADVANCED_TREASURE_COMPASS =
            BLOCK_ENTITIES.register("hvk_advanced_treasure_compass",
                    () -> BlockEntityType.Builder.of(
                            HvkAdvancedTreasureCompassBlockEntity::new,
                            ModBlocks.HVK_ADVANCED_TREASURE_COMPASS.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<InterdimensionalBlockEntity>>
            INTERDIMENSIONAL_BLOCK =
            BLOCK_ENTITIES.register("interdimensional_block",
                    () -> BlockEntityType.Builder.of(
                            InterdimensionalBlockEntity::new,
                            ModBlocks.INTERDIMENSIONAL_BLOCK.get()
                    ).build(null));
}
