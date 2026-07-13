package com.rzy.dealt_force_skills.character.chamber;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public final class ChamberTaczEquipment {
    private static final ResourceLocation TACZ_GUN_ITEM = new ResourceLocation("tacz", "modern_kinetic_gun");
    private static final String GUN_ID = "GunId";
    private static final String FIRE_MODE = "GunFireMode";
    private static final String AMMO = "GunCurrentAmmoCount";
    private static final String BARREL = "HasBulletInBarrel";

    private ChamberTaczEquipment() {
    }

    public static ItemStack create(ChamberGunKind kind) {
        Item item = ForgeRegistries.ITEMS.getValue(TACZ_GUN_ITEM);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrCreateTag();
        if (kind == ChamberGunKind.HEADHUNTER) {
            tag.putString(GUN_ID, "tacz:deagle_golden");
            tag.putInt(AMMO, 7);
            tag.putBoolean(BARREL, true);
            tag.putString(FIRE_MODE, "SEMI");
            tag.put("AttachmentEXTENDED_MAG", attachment("tacz:ammo_mod_hp"));
            tag.put("AttachmentLASER", attachment("tacz:laser_compact"));
            tag.put("AttachmentMUZZLE", attachment("tacz:deagle_golden_long_barrel"));
            tag.put("AttachmentSCOPE", attachment("tacz:sight_t1"));
        } else if (kind == ChamberGunKind.TOUR_DE_FORCE) {
            tag.putString(GUN_ID, "tacz:m95");
            tag.putInt(AMMO, 4);
            tag.putBoolean(BARREL, true);
            tag.putString(FIRE_MODE, "SEMI");
            tag.put("AttachmentEXTENDED_MAG", attachment("tacz:ammo_mod_fmj"));
            tag.put("AttachmentMUZZLE", attachment("tacz:muzzle_silencer_vulture"));
            tag.put("AttachmentSCOPE", attachment("tacz:scope_mk5hd", 3));
        } else {
            return ItemStack.EMPTY;
        }
        return ChamberTaczEnhancement.mark(stack, kind);
    }

    private static CompoundTag attachment(String id) {
        return attachment(id, 0);
    }

    private static CompoundTag attachment(String id, int zoomNumber) {
        CompoundTag stack = new CompoundTag();
        stack.putByte("Count", (byte) 1);
        stack.putString("id", "tacz:attachment");
        CompoundTag tag = new CompoundTag();
        tag.putString("AttachmentId", id);
        if (zoomNumber > 0) {
            tag.putInt("ZoomNumber", zoomNumber);
        }
        stack.put("tag", tag);
        return stack;
    }
}
