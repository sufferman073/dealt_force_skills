package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.client.character.ClientSinevaHudState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class SinevaHudOverlay {
    private static final int SLOT = 46;
    private static final int GAP = 4;

    private SinevaHudOverlay() {
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!VanillaGuiOverlay.HOTBAR.id().equals(event.getOverlay().id())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || !ClientSinevaHudState.shouldRender()) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        Font font = minecraft.font;
        int x = 8;
        int y = Math.max(8, graphics.guiHeight() - 68);

        int coreCooldown = ClientSinevaHudState.bombSuitEquipTicks() > 0
                ? ClientSinevaHudState.bombSuitEquipTicks()
                : (ClientSinevaHudState.bombSuitActive() ? 0 : ClientSinevaHudState.bombSuitCooldownTicks());
        Component coreStatus = ClientSinevaHudState.bombSuitEquipTicks() > 0
                ? Component.translatable("hud.dealt_force_skills.sineva.core.equip")
                : ClientSinevaHudState.bombSuitActive()
                ? Component.translatable(ClientSinevaHudState.shieldDeployed()
                ? "hud.dealt_force_skills.sineva.core.shield"
                : "hud.dealt_force_skills.sineva.core.stowed")
                : Component.translatable("hud.dealt_force_skills.sineva.core.ready");
        drawSlot(graphics, font, x, y, 0xFF4AC4FF, KeybindRegister.CORE_SKILL,
                Component.translatable("character.dealt_force_skills.sineva.skill.bomb_suit"),
                coreCooldown,
                coreStatus.getString());

        int active2X = x + SLOT + GAP;
        drawSlot(graphics, font, active2X, y, 0xFFFFD166, KeybindRegister.ACTIVE_SKILL_2,
                Component.translatable("character.dealt_force_skills.sineva.skill.grapple"),
                ClientSinevaHudState.grappleCooldownTicks(),
                Component.translatable("hud.dealt_force_skills.sineva.active_2").getString());

        int active1Cooldown = ClientSinevaHudState.bladeWireCharges() > 0
                ? 0
                : ClientSinevaHudState.bladeWireRechargeTicks();
        int active1X = active2X + SLOT + GAP;
        drawSlot(graphics, font, active1X, y, 0xFF6EE7A8, KeybindRegister.ACTIVE_SKILL_1,
                Component.translatable("character.dealt_force_skills.sineva.skill.blade_wire"),
                active1Cooldown,
                ClientSinevaHudState.bladeWireCharges() + "/" + ClientSinevaHudState.bladeWireMaxCharges());
    }

    private static void drawSlot(
            GuiGraphics graphics,
            Font font,
            int x,
            int y,
            int accentColor,
            KeyMapping key,
            Component icon,
            int cooldownTicks,
            String detail
    ) {
        graphics.fill(x, y, x + SLOT, y + SLOT, 0xAA071018);
        graphics.fill(x, y, x + SLOT, y + 1, accentColor);
        graphics.fill(x, y + SLOT - 1, x + SLOT, y + SLOT, accentColor);
        graphics.fill(x, y, x + 1, y + SLOT, accentColor);
        graphics.fill(x + SLOT - 1, y, x + SLOT, y + SLOT, accentColor);

        graphics.fill(x + 6, y + 5, x + 40, y + 31, 0xDD0C1720);
        drawCenteredClipped(graphics, font, icon.getString(), x + SLOT / 2, y + 12, 32, 0xFFFFFFFF);

        if (cooldownTicks > 0) {
            graphics.fill(x + 6, y + 5, x + 40, y + 31, 0xCC000000);
            drawCenteredClipped(graphics, font, cooldownText(cooldownTicks), x + SLOT / 2, y + 14, 34, 0xFFFFE6A6);
        } else {
            drawCenteredClipped(graphics, font, detail, x + SLOT / 2, y + 22, 34, 0xFFB7C3D0);
        }

        String keyName = key == null ? "?" : key.getTranslatedKeyMessage().getString();
        drawCenteredClipped(graphics, font, keyName, x + SLOT / 2, y + 35, 38, 0xFFFFFFFF);
    }

    private static void drawCenteredClipped(GuiGraphics graphics, Font font, String text, int centerX, int y, int width, int color) {
        HudTextHelper.drawCenteredFitted(graphics, font, text, centerX, y, width, color);
    }

    private static String cooldownText(int ticks) {
        return Math.max(1, (ticks + 19) / 20) + "s";
    }
}
