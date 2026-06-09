package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class MorseFlashOverlay {
    private static boolean wasFlashed;
    private static boolean wasShocked;
    private static int burstUntilTick;

    private MorseFlashOverlay() {
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!VanillaGuiOverlay.HOTBAR.id().equals(event.getOverlay().id())) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            wasFlashed = false;
            wasShocked = false;
            burstUntilTick = 0;
            return;
        }

        MobEffectInstance flash = minecraft.player.getEffect(ModEffects.MORSE_FLASH_BLIND.get());
        if (flash != null) {
            if (!wasFlashed) {
                wasFlashed = true;
                burstUntilTick = minecraft.player.tickCount + 2;
                minecraft.player.playSound(ModSounds.MORSE_TINNITUS.get(), 0.85f, 1.0f);
            }
            int duration = flash.getDuration();
            int alpha = duration > 100 ? 255 : Math.max(0, Math.min(255, Math.round(duration / 100.0f * 255.0f)));
            if (minecraft.player.tickCount < burstUntilTick) {
                alpha = Math.max(alpha, 220);
            }
            event.getGuiGraphics().fill(0, 0, event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight(), alpha << 24);
        } else {
            wasFlashed = false;
            burstUntilTick = 0;
        }

        MobEffectInstance shock = minecraft.player.getEffect(ModEffects.MORSE_STRONG_SHOCK.get());
        if (shock != null) {
            if (!wasShocked) {
                wasShocked = true;
                minecraft.player.playSound(ModSounds.MORSE_DEAFENED.get(), 0.65f, 1.0f);
            }
            int shockAlpha = shock.getAmplifier() >= 1 ? 0x28 : 0x20;
            int shockColor = (shockAlpha << 24) | 0x00FFFFFF;
            event.getGuiGraphics().fill(0, 0, event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight(), shockColor);
        } else {
            wasShocked = false;
        }
    }
}
