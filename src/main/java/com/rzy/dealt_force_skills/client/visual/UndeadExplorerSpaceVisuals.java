package com.rzy.dealt_force_skills.client.visual;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.client.character.ClientUndeadHudState;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class UndeadExplorerSpaceVisuals {
    private UndeadExplorerSpaceVisuals() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (hidesOtherEntities() && event.getEntity() != Minecraft.getInstance().player) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (hidesOtherEntities() && event.getEntity() != Minecraft.getInstance().player) {
            event.setResult(Event.Result.DENY);
        }
    }

    private static boolean hidesOtherEntities() {
        return ClientUndeadHudState.shouldRender()
                && ClientUndeadHudState.explorerSpaceTicks() > 0
                && !ClientUndeadHudState.explorerCanSeeEntities();
    }
}
