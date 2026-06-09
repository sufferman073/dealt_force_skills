package com.rzy.dealt_force_skills.client.visual;

import com.mojang.math.Axis;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class UndeadPlayerVisuals {
    private static final Set<Integer> ROTATED_PLAYERS = new HashSet<>();

    private UndeadPlayerVisuals() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (player.hasEffect(ModEffects.UNDEAD_TRUE_INVISIBILITY.get())) {
            event.setCanceled(true);
            return;
        }
        if (player == Minecraft.getInstance().player
                || !player.hasEffect(ModEffects.UNDEAD_HUNTER_SCATTER.get())) {
            return;
        }
        event.getPoseStack().pushPose();
        float angle = (player.tickCount + event.getPartialTick()) * 18.0F;
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(angle));
        ROTATED_PLAYERS.add(player.getId());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (ROTATED_PLAYERS.remove(event.getEntity().getId())) {
            event.getPoseStack().popPose();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.hasEffect(ModEffects.UNDEAD_TRUE_INVISIBILITY.get())) {
            event.setCanceled(true);
        }
    }
}
