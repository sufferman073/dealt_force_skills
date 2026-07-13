package com.rzy.dealt_force_skills.registry;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.block.BladeWireBlock;
import com.rzy.dealt_force_skills.block.HvkAdvancedDisassemblyBeamEmitterBlock;
import com.rzy.dealt_force_skills.block.HvkAdvancedStandardTemplateConstructorBlock;
import com.rzy.dealt_force_skills.block.HvkAdvancedTreasureCompassBlock;
import com.rzy.dealt_force_skills.block.HvkClonePrototypeBlock;
import com.rzy.dealt_force_skills.block.InterdimensionalBlock;
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

    public static final RegistryObject<Block> HVK_ADVANCED_STANDARD_TEMPLATE_CONSTRUCTOR =
            BLOCKS.register("hvk_advanced_standard_template_constructor",
                    HvkAdvancedStandardTemplateConstructorBlock::new);

    public static final RegistryObject<Block> HVK_ADVANCED_DISASSEMBLY_BEAM_EMITTER =
            BLOCKS.register("hvk_advanced_disassembly_beam_emitter",
                    HvkAdvancedDisassemblyBeamEmitterBlock::new);

    public static final RegistryObject<Block> HVK_CLONE_PROTOTYPE =
            BLOCKS.register("hvk_clone_prototype", HvkClonePrototypeBlock::new);

    public static final RegistryObject<Block> HVK_ADVANCED_TREASURE_COMPASS =
            BLOCKS.register("hvk_advanced_treasure_compass", HvkAdvancedTreasureCompassBlock::new);

    public static final RegistryObject<Block> INTERDIMENSIONAL_BLOCK =
            BLOCKS.register("interdimensional_block", InterdimensionalBlock::new);
}
