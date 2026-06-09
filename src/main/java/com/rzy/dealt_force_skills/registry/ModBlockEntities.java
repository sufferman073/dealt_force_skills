package com.rzy.dealt_force_skills.registry;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.block.BladeWireBlockEntity;
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
}
