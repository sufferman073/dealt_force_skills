package com.rzy.dealt_force_skills.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.client.character.ClientCharacterSkinState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class CharacterSkinOverrideVisuals {
    private static final Map<String, CharacterSkinArmRenderer> ARM_RENDERERS = new HashMap<>();

    private CharacterSkinOverrideVisuals() {
    }

    public static void registerLayers(EntityRenderersEvent.AddLayers event) {
        ARM_RENDERERS.clear();
        EntityRendererProvider.Context context = createContext(event);
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer == null) {
                continue;
            }
            renderer.addLayer(new CharacterSkinLayer(renderer));
            ARM_RENDERERS.put(skin, new CharacterSkinArmRenderer(context, "slim".equals(skin)));
        }
    }

    public static void renderRightHand(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player
    ) {
        CharacterSkinArmRenderer override = ClientCharacterSkinState.skinFor(player).isPresent()
                ? ARM_RENDERERS.getOrDefault(player.getModelName(), ARM_RENDERERS.get("default"))
                : null;
        if (override != null) {
            override.renderRightHand(poseStack, buffer, packedLight, player);
            return;
        }
        if (Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer) {
            renderer.renderRightHand(poseStack, buffer, packedLight, player);
        }
    }

    private static EntityRendererProvider.Context createContext(EntityRenderersEvent.AddLayers event) {
        Minecraft minecraft = Minecraft.getInstance();
        return new EntityRendererProvider.Context(
                minecraft.getEntityRenderDispatcher(),
                minecraft.getItemRenderer(),
                minecraft.getBlockRenderer(),
                minecraft.getEntityRenderDispatcher().getItemInHandRenderer(),
                minecraft.getResourceManager(),
                event.getEntityModels(),
                minecraft.font
        );
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderArm(RenderArmEvent event) {
        if (event.isCanceled()) {
            return;
        }
        AbstractClientPlayer player = event.getPlayer();
        if (player == null || player.isInvisible() || ClientCharacterSkinState.skinFor(player).isEmpty()) {
            return;
        }

        CharacterSkinArmRenderer renderer = ARM_RENDERERS.getOrDefault(player.getModelName(), ARM_RENDERERS.get("default"));
        if (renderer == null) {
            return;
        }

        event.setCanceled(true);
        if (event.getArm() == HumanoidArm.RIGHT) {
            renderer.renderRightHand(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), player);
        } else {
            renderer.renderLeftHand(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), player);
        }
    }

    public static final class CharacterSkinLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        public CharacterSkinLayer(PlayerRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(
                PoseStack poseStack,
                MultiBufferSource buffer,
                int packedLight,
                AbstractClientPlayer player,
                float limbSwing,
                float limbSwingAmount,
                float partialTick,
                float ageInTicks,
                float netHeadYaw,
                float headPitch
        ) {
            if (player == null || player.isInvisible() || player.isSpectator()) {
                return;
            }
            Optional<ResourceLocation> skin = ClientCharacterSkinState.skinFor(player);
            if (skin.isEmpty()) {
                return;
            }
            poseStack.pushPose();
            poseStack.scale(1.0015F, 1.0015F, 1.0015F);
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(skin.get()));
            getParentModel().renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }
    }

    private static final class CharacterSkinArmRenderer extends PlayerRenderer {
        private CharacterSkinArmRenderer(EntityRendererProvider.Context context, boolean slim) {
            super(context, slim);
        }

        @Override
        public ResourceLocation getTextureLocation(AbstractClientPlayer player) {
            return ClientCharacterSkinState.skinFor(player).orElseGet(() -> super.getTextureLocation(player));
        }
    }

    @Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void addLayers(EntityRenderersEvent.AddLayers event) {
            CharacterSkinOverrideVisuals.registerLayers(event);
        }
    }
}
