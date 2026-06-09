package com.rzy.dealt_force_skills.registry;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.block.BladeWireBlock;
import com.rzy.dealt_force_skills.block.QuickCoverBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, DealtForceSkillsMod.MODID);

    public static final RegistryObject<Block> BLADE_WIRE =
            BLOCKS.register("blade_wire", () -> new BladeWireBlock(false));

    public static final RegistryObject<Block> BLADE_WIRE_CORE =
            BLOCKS.register("blade_wire_core", () -> new BladeWireBlock(true));

    public static final RegistryObject<Block> QUICK_COVER =
            BLOCKS.register("quick_cover", QuickCoverBlock::new);
}
