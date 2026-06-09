package com.rzy.dealt_force_skills.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

final class HudTextHelper {
    private HudTextHelper() {
    }

    static void drawCenteredFitted(GuiGraphics graphics, Font font, String text,
                                   int centerX, int y, int width, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }
        int textWidth = font.width(text);
        if (textWidth <= width) {
            graphics.drawCenteredString(font, text, centerX, y, color);
            return;
        }
        float scale = width / (float) Math.max(1, textWidth);
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX, y, 0.0D);
        poseStack.scale(scale, scale, 1.0F);
        graphics.drawString(font, text, -textWidth / 2, 0, color, false);
        poseStack.popPose();
    }

    static void drawFitted(GuiGraphics graphics, Font font, String text,
                           int x, int y, int width, int color, boolean shadow) {
        if (text == null || text.isEmpty()) {
            return;
        }
        int textWidth = font.width(text);
        if (textWidth <= width) {
            graphics.drawString(font, text, x, y, color, shadow);
            return;
        }
        float scale = width / (float) Math.max(1, textWidth);
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0.0D);
        poseStack.scale(scale, scale, 1.0F);
        graphics.drawString(font, text, 0, 0, color, shadow);
        poseStack.popPose();
    }
}
