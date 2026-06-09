package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.item.DfsEquipmentItem;
import com.rzy.dealt_force_skills.network.C2S_ToggleHelmetVision;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class HelmetVisionInputHandler {
    private HelmetVisionInputHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null) {
            return;
        }

        ItemStack held = minecraft.player.getMainHandItem();
        if (!DfsEquipmentItem.hasUsableVision(held)) {
            return;
        }

        boolean cycle = minecraft.player.isShiftKeyDown();
        event.setSwingHand(false);
        event.setCanceled(true);
        NetworkHandler.sendToServer(new C2S_ToggleHelmetVision(cycle));
        minecraft.options.keyAttack.setDown(false);
        while (minecraft.options.keyAttack.consumeClick()) {
            // Drain this left-click so the vision toggle does not also attack or mine.
        }
    }
}
