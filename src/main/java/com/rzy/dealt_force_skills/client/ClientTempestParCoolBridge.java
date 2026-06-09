package com.rzy.dealt_force_skills.client;

import com.alrex.parcool.common.action.impl.Roll;
import com.alrex.parcool.common.capability.Parkourability;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class ClientTempestParCoolBridge {
    private static final int CAMERA_DIP_TICKS = 9;
    private static int cameraDipTicks;

    private ClientTempestParCoolBridge() {
    }

    public static void startRoll() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        cameraDipTicks = CAMERA_DIP_TICKS;
        try {
            Parkourability parkourability = Parkourability.get(player);
            if (parkourability != null) {
                float previousForward = player.input.forwardImpulse;
                float previousLeft = player.input.leftImpulse;
                try {
                    player.input.forwardImpulse = 1.0F;
                    player.input.leftImpulse = 0.0F;
                    parkourability.get(Roll.class).startRoll(player);
                } finally {
                    player.input.forwardImpulse = previousForward;
                    player.input.leftImpulse = previousLeft;
                }
            }
        } catch (LinkageError | RuntimeException ignored) {
            // ParCool is an integration dependency; if it is missing at runtime, Tempest still keeps server state valid.
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (cameraDipTicks <= 0) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            cameraDipTicks = 0;
            return;
        }

        float partialTick = (float) event.getPartialTick();
        float elapsed = Mth.clamp(CAMERA_DIP_TICKS - cameraDipTicks + partialTick, 0.0F, (float) CAMERA_DIP_TICKS);
        float progress = elapsed / (float) CAMERA_DIP_TICKS;
        float dip = Mth.sin(progress * (float) Math.PI) * 10.0F;
        event.setYaw(player.getViewYRot(partialTick));
        event.setPitch(Mth.clamp(player.getViewXRot(partialTick) + dip, -89.9F, 89.9F));
        event.setRoll(0.0F);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || cameraDipTicks <= 0) {
            return;
        }
        if (Minecraft.getInstance().player == null) {
            cameraDipTicks = 0;
            return;
        }
        cameraDipTicks--;
    }
}
