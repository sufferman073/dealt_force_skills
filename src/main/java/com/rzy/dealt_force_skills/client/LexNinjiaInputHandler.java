package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.client.character.ClientLexNinjiaHudState;
import com.rzy.dealt_force_skills.character.lexninjia.LexNinjiaInputAction;
import com.rzy.dealt_force_skills.network.C2S_LexNinjiaInput;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class LexNinjiaInputHandler {
    private static boolean sneakWasDown;
    private static boolean useWasDown;
    private static boolean jumpWasDown;

    private LexNinjiaInputHandler() {
    }

    public static void tick(Minecraft minecraft) {
        if (!ClientLexNinjiaHudState.shouldRender() || minecraft.player == null || minecraft.screen != null) {
            reset();
            return;
        }
        boolean sneakDown = minecraft.options.keyShift.isDown();
        if (sneakDown && !sneakWasDown) {
            send(LexNinjiaInputAction.SNEAK_PRESS);
        } else if (!sneakDown && sneakWasDown) {
            send(LexNinjiaInputAction.SNEAK_RELEASE);
        }
        sneakWasDown = sneakDown;

        boolean useDown = minecraft.options.keyUse.isDown();
        if (useDown && !useWasDown) {
            send(LexNinjiaInputAction.RIGHT_PRESS);
        } else if (!useDown && useWasDown) {
            send(LexNinjiaInputAction.RIGHT_RELEASE);
        }
        useWasDown = useDown;

        boolean jumpDown = minecraft.options.keyJump.isDown();
        if (jumpDown && !jumpWasDown) {
            send(LexNinjiaInputAction.JUMP);
        }
        jumpWasDown = jumpDown;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ClientLexNinjiaHudState.shouldRender() || minecraft.screen != null || minecraft.player == null) {
            return;
        }
        if (event.isUseItem() && minecraft.player.isShiftKeyDown()) {
            event.setSwingHand(false);
            event.setCanceled(true);
            send(LexNinjiaInputAction.COOK);
        }
    }

    private static void reset() {
        sneakWasDown = false;
        useWasDown = false;
        jumpWasDown = false;
    }

    private static void send(LexNinjiaInputAction action) {
        NetworkHandler.sendToServer(new C2S_LexNinjiaInput(action));
    }
}
