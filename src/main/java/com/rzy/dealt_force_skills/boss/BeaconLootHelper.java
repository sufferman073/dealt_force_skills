package com.rzy.dealt_force_skills.boss;

import com.rzy.dealt_force_skills.config.DealtBossesConfig;
import com.rzy.dealt_force_skills.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class BeaconLootHelper {
    private static final String M1911_SNBT =
            "{Count:1b,id:\"tacz:modern_kinetic_gun\",tag:{AttachmentEXTENDED_MAG:{Count:1b,id:\"tacz:attachment\",tag:{AttachmentId:\"tacz:light_extended_mag_3\"}},AttachmentMUZZLE:{Count:1b,id:\"tacz:attachment\",tag:{AttachmentId:\"tacz:muzzle_silencer_mirage\"}},GunCurrentAmmoCount:12,GunFireMode:\"SEMI\",GunId:\"tacz:m1911\",HasBulletInBarrel:1b}}";

    private BeaconLootHelper() {
    }

    public static int experienceReward() {
        return DealtBossesConfig.intValue("beacon.loot.experience", 5345);
    }

    public static ItemStack createM1911Reward() {
        try {
            CompoundTag tag = TagParser.parseTag(M1911_SNBT);
            ItemStack stack = ItemStack.of(tag);
            return stack.isEmpty() ? ItemStack.EMPTY : stack;
        } catch (Exception ignored) {
            return ItemStack.EMPTY;
        }
    }

    public static void dropAt(ServerLevel level, Vec3 pos, int looting) {
        if (level == null || pos == null) {
            return;
        }
        int coffee = 1 + level.random.nextInt(3) + Math.min(2, Math.max(0, looting));
        coffee = Math.min(3 + Math.min(2, Math.max(0, looting)), coffee);
        spawn(level, pos, new ItemStack(ModItems.PREMIUM_COFFEE_BEANS.get(), coffee));
        spawn(level, pos, new ItemStack(ModItems.BEACON_BRAIN_UNIT.get(), 1));
        ItemStack gun = createM1911Reward();
        if (!gun.isEmpty()) {
            spawn(level, pos, gun);
        }
    }

    private static void spawn(ServerLevel level, Vec3 pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemEntity entity = new ItemEntity(level, pos.x, pos.y, pos.z, stack);
        entity.setDefaultPickUpDelay();
        level.addFreshEntity(entity);
    }
}
