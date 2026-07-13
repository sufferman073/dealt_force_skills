package com.rzy.dealt_force_skills.character.saeed;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public final class SaeedTaczEquipment {
    private static final ResourceLocation TACZ_GUN_ITEM = ResourceLocation.fromNamespaceAndPath("tacz", "modern_kinetic_gun");
    private static final String GUN_ID_TAG = "GunId";
    private static final String GUN_FIRE_MODE_TAG = "GunFireMode";
    private static final String GUN_CURRENT_AMMO_COUNT_TAG = "GunCurrentAmmoCount";
    private static final String GUN_HAS_BULLET_IN_BARREL_TAG = "HasBulletInBarrel";
    private static final String GUN_DUMMY_AMMO_TAG = "DummyAmmo";
    private static final String GUN_MAX_DUMMY_AMMO_TAG = "MaxDummyAmmo";

    private SaeedTaczEquipment() {
    }

    public static ItemStack mainHand(SaeedGuardType type) {
        ItemStack gun = taczGun(type);
        return gun.isEmpty() ? type.mainHand() : gun;
    }

    public static ItemStack offHand(SaeedGuardType type) {
        return type.offHand();
    }

    public static boolean isTaczGun(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(GUN_ID_TAG);
    }

    public static void primeForShot(SaeedGuardType type, ItemStack stack) {
        if (stack.isEmpty() || !isTaczGun(stack)) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(GUN_CURRENT_AMMO_COUNT_TAG, Math.max(tag.getInt(GUN_CURRENT_AMMO_COUNT_TAG), magazineSize(type)));
        tag.putBoolean(GUN_HAS_BULLET_IN_BARREL_TAG, true);
        tag.putInt(GUN_DUMMY_AMMO_TAG, Math.max(tag.getInt(GUN_DUMMY_AMMO_TAG), 9999));
        tag.putInt(GUN_MAX_DUMMY_AMMO_TAG, Math.max(tag.getInt(GUN_MAX_DUMMY_AMMO_TAG), 9999));
    }

    public static ResourceLocation gunId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? null : ResourceLocation.tryParse(tag.getString(GUN_ID_TAG));
    }

    private static ItemStack taczGun(SaeedGuardType type) {
        Item item = ForgeRegistries.ITEMS.getValue(TACZ_GUN_ITEM);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        String gunId = gunId(type);
        if (gunId.isBlank()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(GUN_ID_TAG, gunId);
        tag.putInt(GUN_CURRENT_AMMO_COUNT_TAG, magazineSize(type));
        tag.putBoolean(GUN_HAS_BULLET_IN_BARREL_TAG, true);
        tag.putString(GUN_FIRE_MODE_TAG, fireMode(type));
        tag.putInt(GUN_DUMMY_AMMO_TAG, 9999);
        tag.putInt(GUN_MAX_DUMMY_AMMO_TAG, 9999);
        return stack;
    }

    private static String gunId(SaeedGuardType type) {
        return switch (type) {
            case THUNDER -> "tacz:hk_mp5a5";
            case IRON_RAIN -> "tacz:m249";
            case FIREEYE, KARIM -> "tacz:m1911";
            case SHARP_EAGLE -> "tacz:m700";
            case HAKIM -> "tacz:rpg7";
        };
    }

    private static int magazineSize(SaeedGuardType type) {
        return switch (type) {
            case THUNDER -> 30;
            case IRON_RAIN -> 75;
            case FIREEYE, KARIM -> 7;
            case SHARP_EAGLE -> 5;
            case HAKIM -> 1;
        };
    }

    private static String fireMode(SaeedGuardType type) {
        return switch (type) {
            case THUNDER -> "BURST";
            default -> "SEMI";
        };
    }
}
