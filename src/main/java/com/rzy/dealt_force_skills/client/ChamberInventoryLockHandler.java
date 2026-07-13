package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.chamber.ChamberGunKind;
import com.rzy.dealt_force_skills.character.chamber.ChamberTaczEnhancement;
import com.rzy.dealt_force_skills.client.character.ClientChamberHudState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class ChamberInventoryLockHandler {
    private ChamberInventoryLockHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof AbstractContainerScreen<?> && hasActiveChamberGun()) {
            event.setCanceled(true);
        }
    }

    private static boolean hasActiveChamberGun() {
        if (ClientChamberHudState.hasEquippedGun()) {
            return true;
        }
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && (isChamberGun(minecraft.player.getMainHandItem())
                || isChamberGun(minecraft.player.getOffhandItem()));
    }

    private static boolean isChamberGun(ItemStack stack) {
        return ChamberTaczEnhancement.kind(stack) != ChamberGunKind.NONE;
    }
}
