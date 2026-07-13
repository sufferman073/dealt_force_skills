package com.rzy.dealt_force_skills.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Beacon boss M1911 loadout (design NBT) and TACZ magazine priming, same style as {@code SaeedTaczEquipment}.
 */
public final class BeaconTaczEquipment {
    private static final ResourceLocation TACZ_GUN_ITEM =
            ResourceLocation.fromNamespaceAndPath("tacz", "modern_kinetic_gun");
    private static final String GUN_ID_TAG = "GunId";
    private static final String GUN_FIRE_MODE_TAG = "GunFireMode";
    private static final String GUN_CURRENT_AMMO_COUNT_TAG = "GunCurrentAmmoCount";
    private static final String GUN_HAS_BULLET_IN_BARREL_TAG = "HasBulletInBarrel";
    private static final String GUN_DUMMY_AMMO_TAG = "DummyAmmo";
    private static final String GUN_MAX_DUMMY_AMMO_TAG = "MaxDummyAmmo";
    private static final int MAGAZINE_SIZE = 12;
    private static final String DESIGN_SNBT =
            "{Count:1b,id:\"tacz:modern_kinetic_gun\",tag:{AttachmentEXTENDED_MAG:{Count:1b,id:\"tacz:attachment\",tag:{AttachmentId:\"tacz:light_extended_mag_3\"}},AttachmentMUZZLE:{Count:1b,id:\"tacz:attachment\",tag:{AttachmentId:\"tacz:muzzle_silencer_mirage\"}},GunCurrentAmmoCount:12,GunFireMode:\"SEMI\",GunId:\"tacz:m1911\",HasBulletInBarrel:1b}}";

    private BeaconTaczEquipment() {
    }

    public static ItemStack createM1911() {
        try {
            CompoundTag full = TagParser.parseTag(DESIGN_SNBT);
            ItemStack parsed = ItemStack.of(full);
            if (!parsed.isEmpty() && isTaczGun(parsed)) {
                primeForShot(parsed);
                return parsed;
            }
        } catch (Exception ignored) {
            // Fall through to manual build.
        }
        return buildM1911Manually();
    }

    public static boolean isTaczGun(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(GUN_ID_TAG);
    }

    public static ResourceLocation gunId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? null : ResourceLocation.tryParse(tag.getString(GUN_ID_TAG));
    }

    public static void primeForShot(ItemStack stack) {
        if (stack.isEmpty() || !isTaczGun(stack)) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(GUN_CURRENT_AMMO_COUNT_TAG, Math.max(tag.getInt(GUN_CURRENT_AMMO_COUNT_TAG), MAGAZINE_SIZE));
        tag.putBoolean(GUN_HAS_BULLET_IN_BARREL_TAG, true);
        tag.putInt(GUN_DUMMY_AMMO_TAG, Math.max(tag.getInt(GUN_DUMMY_AMMO_TAG), 9999));
        tag.putInt(GUN_MAX_DUMMY_AMMO_TAG, Math.max(tag.getInt(GUN_MAX_DUMMY_AMMO_TAG), 9999));
    }

    private static ItemStack buildM1911Manually() {
        Item item = ForgeRegistries.ITEMS.getValue(TACZ_GUN_ITEM);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(GUN_ID_TAG, "tacz:m1911");
        tag.putInt(GUN_CURRENT_AMMO_COUNT_TAG, MAGAZINE_SIZE);
        tag.putBoolean(GUN_HAS_BULLET_IN_BARREL_TAG, true);
        tag.putString(GUN_FIRE_MODE_TAG, "SEMI");
        tag.putInt(GUN_DUMMY_AMMO_TAG, 9999);
        tag.putInt(GUN_MAX_DUMMY_AMMO_TAG, 9999);
        putAttachment(tag, "AttachmentEXTENDED_MAG", "tacz:light_extended_mag_3");
        putAttachment(tag, "AttachmentMUZZLE", "tacz:muzzle_silencer_mirage");
        return stack;
    }

    private static void putAttachment(CompoundTag gunTag, String slotKey, String attachmentId) {
        CompoundTag attachment = new CompoundTag();
        attachment.putString("id", "tacz:attachment");
        attachment.putByte("Count", (byte) 1);
        CompoundTag attachmentTag = new CompoundTag();
        attachmentTag.putString("AttachmentId", attachmentId);
        attachment.put("tag", attachmentTag);
        gunTag.put(slotKey, attachment);
    }
}
