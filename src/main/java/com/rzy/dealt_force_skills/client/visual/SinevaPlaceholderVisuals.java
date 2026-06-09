package com.rzy.dealt_force_skills.client.visual;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.sineva.SinevaShieldGeometry;
import com.rzy.dealt_force_skills.client.SinevaInputHandler;
import com.rzy.dealt_force_skills.client.character.ClientSinevaRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Locale;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class SinevaPlaceholderVisuals {
    private static final ItemStack BLADE_WIRE_PLACEHOLDER = new ItemStack(Items.COBWEB);
    private static final int OUTER_R = 10;
    private static final int OUTER_G = 12;
    private static final int OUTER_B = 31;
    private static final int FACE_R = 6;
    private static final int FACE_G = 6;
    private static final int FACE_B = 12;
    private static final int TRIM_R = 8;
    private static final int TRIM_G = 34;
    private static final int TRIM_B = 86;
    private static final int GLASS_R = 132;
    private static final int GLASS_G = 178;
    private static final int GLASS_B = 190;
    // Vanilla player layers use humanoid model space, where positive Y points down.
    // The YSM fallback uses world space, where positive Y points up.
    private static final float FIRST_PERSON_VIEWPORT_Y = 0.04f;
    private static final float THIRD_PERSON_WORLD_VIEWPORT_Y = 0.24f;
    private static final float THIRD_PERSON_MODEL_VIEWPORT_Y = -0.39f;
    private static final Map<UUID, ItemStack> SAVED_MAIN_HAND = new HashMap<>();
    private static final Map<UUID, YsmSavedMainHand> YSM_WORLD_SAVED_MAIN_HAND = new HashMap<>();

    private SinevaPlaceholderVisuals() {
    }

    private static boolean isYesSteveModelLoaded() {
        return ModList.get().getMods().stream().anyMatch(modInfo -> {
            String id = modInfo.getModId().toLowerCase(Locale.ROOT);
            return id.equals("yes_steve_model")
                    || id.equals("yesstevemodel")
                    || id.equals("ysm")
                    || id.contains("yes_steve")
                    || id.contains("yessteve");
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderHand(RenderHandEvent event) {
        boolean shieldMode = shouldHideHeldHands(Minecraft.getInstance().player);
        boolean bladeWireMode = SinevaInputHandler.isBladeWireHeld();

        if (!shieldMode && !bladeWireMode) {
            return;
        }

        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Minecraft minecraft = Minecraft.getInstance();

        // Render blade wire in main hand when held (works with or without shield)
        if (bladeWireMode) {
            poseStack.pushPose();
            poseStack.translate(0.56d, -0.42d, -0.72d);
            poseStack.mulPose(Axis.YP.rotationDegrees(-80.0f));
            poseStack.mulPose(Axis.XP.rotationDegrees(-15.0f));
            poseStack.scale(0.65f, 0.65f, 0.65f);
            minecraft.getItemRenderer().renderStatic(
                    minecraft.player,
                    BLADE_WIRE_PLACEHOLDER,
                    ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                    false,
                    poseStack,
                    event.getMultiBufferSource(),
                    minecraft.level,
                    event.getPackedLight(),
                    OverlayTexture.NO_OVERLAY,
                    0
            );
            poseStack.popPose();
        }

        // Render shield in left hand area when shield is deployed
        if (shieldMode) {
            float bash = ClientSinevaVisualState.bashProgress(event.getPartialTick());
            float charge = ClientSinevaVisualState.chargeProgress(event.getPartialTick());
            float thrust = Mth.sin(bash * Mth.PI) * 0.18f + charge * 0.14f;

            poseStack.pushPose();
            poseStack.translate(0.0d, 0.02d, -1.08d - thrust);
            poseStack.mulPose(Axis.XP.rotationDegrees(-2.0f - charge * 6.0f));
            renderSinevaShield(poseStack, event.getMultiBufferSource(), event.getPackedLight(), true,
                    ClientSinevaRenderState.isViewportBroken(minecraft.player));
            poseStack.popPose();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderArm(RenderArmEvent event) {
        if (shouldHideHeldHands(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!shouldHideHeldHands(player) || SAVED_MAIN_HAND.containsKey(player.getUUID())) {
            return;
        }

        int selectedSlot = player.getInventory().selected;
        ItemStack stack = player.getInventory().items.get(selectedSlot);
        if (stack.isEmpty()) {
            return;
        }

        SAVED_MAIN_HAND.put(player.getUUID(), stack);
        player.getInventory().items.set(selectedSlot, ItemStack.EMPTY);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        ItemStack saved = SAVED_MAIN_HAND.remove(player.getUUID());
        if (saved != null) {
            player.getInventory().items.set(player.getInventory().selected, saved);
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!isYesSteveModelLoaded()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            restoreYsmWorldMainHands(minecraft);
            return;
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            // YSM owns its own player renderer / held-item path, so RenderPlayerEvent.Pre is
            // not always enough to hide the main-hand gun. Hide only during world entity
            // rendering and only when YSM is loaded; the vanilla/no-YSM path remains unchanged.
            hideYsmWorldMainHands(minecraft);
            return;
        }

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        for (Player player : minecraft.level.players()) {
            if (!shouldRenderFor(player)) {
                continue;
            }
            if (player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
                continue;
            }
            renderYsmWorldFallbackShield(event, player, buffer);
        }
        buffer.endBatch(RenderType.debugQuads());
        restoreYsmWorldMainHands(minecraft);
    }

    private static void hideYsmWorldMainHands(Minecraft minecraft) {
        if (minecraft.level == null) {
            return;
        }

        for (Player player : minecraft.level.players()) {
            if (!shouldHideHeldHands(player) || YSM_WORLD_SAVED_MAIN_HAND.containsKey(player.getUUID())) {
                continue;
            }

            int selectedSlot = player.getInventory().selected;
            ItemStack stack = player.getInventory().items.get(selectedSlot);
            if (stack.isEmpty()) {
                continue;
            }

            YSM_WORLD_SAVED_MAIN_HAND.put(player.getUUID(), new YsmSavedMainHand(selectedSlot, stack));
            player.getInventory().items.set(selectedSlot, ItemStack.EMPTY);
        }
    }

    private static void restoreYsmWorldMainHands(Minecraft minecraft) {
        if (YSM_WORLD_SAVED_MAIN_HAND.isEmpty()) {
            return;
        }

        if (minecraft.level == null) {
            YSM_WORLD_SAVED_MAIN_HAND.clear();
            return;
        }

        for (Map.Entry<UUID, YsmSavedMainHand> entry : new HashMap<>(YSM_WORLD_SAVED_MAIN_HAND).entrySet()) {
            Player player = minecraft.level.getPlayerByUUID(entry.getKey());
            if (player == null) {
                YSM_WORLD_SAVED_MAIN_HAND.remove(entry.getKey());
                continue;
            }

            YsmSavedMainHand saved = entry.getValue();
            if (saved.slot() >= 0 && saved.slot() < player.getInventory().items.size()) {
                player.getInventory().items.set(saved.slot(), saved.stack());
            }
            YSM_WORLD_SAVED_MAIN_HAND.remove(entry.getKey());
        }
    }

    private static void renderYsmWorldFallbackShield(RenderLevelStageEvent event, Player player, MultiBufferSource buffer) {
        float partialTick = event.getPartialTick();
        double x = Mth.lerp(partialTick, player.xOld, player.getX()) - event.getCamera().getPosition().x;
        double y = Mth.lerp(partialTick, player.yOld, player.getY()) - event.getCamera().getPosition().y;
        double z = Mth.lerp(partialTick, player.zOld, player.getZ()) - event.getCamera().getPosition().z;
        boolean frontShield = ClientSinevaRenderState.isShieldDeployed(player);
        float bash = ClientSinevaVisualState.bashProgress(partialTick);
        float charge = ClientSinevaVisualState.chargeProgress(partialTick);
        float thrust = Mth.sin(bash * Mth.PI) * 0.18f + charge * 0.14f;
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - bodyYaw));
        if (frontShield) {
            poseStack.translate(0.0d, 1.35d, -0.58d - thrust);
            poseStack.mulPose(Axis.XP.rotationDegrees(-3.0f - charge * 7.0f));
        } else {
            poseStack.translate(0.0d, 1.35d, 0.40d);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
            poseStack.mulPose(Axis.XP.rotationDegrees(10.0f));
        }
        renderSinevaShield(poseStack, buffer, LevelRenderer.getLightColor(player.level(), player.blockPosition()), false,
                ClientSinevaRenderState.isViewportBroken(player), THIRD_PERSON_WORLD_VIEWPORT_Y);
        poseStack.popPose();
    }

    private record YsmSavedMainHand(int slot, ItemStack stack) {
    }

    private static boolean shouldRenderFor(Player player) {
        return player != null
                && !player.isInvisible()
                && ClientSinevaRenderState.isBombSuitActive(player);
    }

    private static boolean shouldHideHeldHands(Player player) {
        return shouldRenderFor(player) && ClientSinevaRenderState.isShieldDeployed(player);
    }

    public static void registerLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new SinevaShieldLayer(renderer));
            }
        }
    }

    public static final class SinevaShieldLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        public SinevaShieldLayer(PlayerRenderer renderer) {
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
            if (!shouldRenderFor(player)) {
                return;
            }

            boolean frontShield = ClientSinevaRenderState.isShieldDeployed(player);
            float bash = ClientSinevaVisualState.bashProgress(partialTick);
            float charge = ClientSinevaVisualState.chargeProgress(partialTick);
            float thrust = Mth.sin(bash * Mth.PI) * 0.18f + charge * 0.14f;

            poseStack.pushPose();
            if (frontShield) {
                getParentModel().body.translateAndRotate(poseStack);
                poseStack.mulPose(Axis.YP.rotationDegrees(netHeadYaw));
                poseStack.mulPose(Axis.XP.rotationDegrees(headPitch));
                poseStack.translate(0.0d, 0.30d, -0.56d - thrust);
                poseStack.mulPose(Axis.XP.rotationDegrees(-3.0f - charge * 7.0f));
            } else {
                getParentModel().body.translateAndRotate(poseStack);
                poseStack.translate(0.0d, 0.30d, 0.32d);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                poseStack.mulPose(Axis.XP.rotationDegrees(10.0f));
            }
            renderSinevaShield(poseStack, buffer, packedLight, false,
                    ClientSinevaRenderState.isViewportBroken(player), THIRD_PERSON_MODEL_VIEWPORT_Y);
            poseStack.popPose();
        }
    }

    private static void renderSinevaShield(PoseStack poseStack, MultiBufferSource buffer, int packedLight, boolean firstPerson, boolean viewportBroken) {
        float viewportY = firstPerson ? FIRST_PERSON_VIEWPORT_Y : THIRD_PERSON_WORLD_VIEWPORT_Y;
        renderSinevaShield(poseStack, buffer, packedLight, firstPerson, viewportBroken, viewportY);
    }

    private static void renderSinevaShield(PoseStack poseStack, MultiBufferSource buffer, int packedLight, boolean firstPerson, boolean viewportBroken, float viewportY) {
        float outerWidth = firstPerson ? 2.56f : (float) SinevaShieldGeometry.SHIELD_WORLD_WIDTH;
        float outerHeight = firstPerson ? 1.72f : (float) SinevaShieldGeometry.SHIELD_WORLD_HEIGHT;
        float faceWidth = outerWidth - (firstPerson ? 0.20f : 0.12f);
        float faceHeight = outerHeight - (firstPerson ? 0.22f : 0.16f);
        float viewportX = 0.0f;
        float viewportWidth = firstPerson ? 2.14f : (float) SinevaShieldGeometry.VIEWPORT_WORLD_WIDTH;
        float viewportHeight = firstPerson ? 1.18f : (float) SinevaShieldGeometry.VIEWPORT_WORLD_HEIGHT;
        float frame = firstPerson ? 0.06f : 0.055f;

        VertexConsumer bodyConsumer = buffer.getBuffer(RenderType.debugQuads());
        renderRectAroundHole(poseStack, bodyConsumer, outerWidth, outerHeight,
                faceWidth, faceHeight, 0.000f, OUTER_R, OUTER_G, OUTER_B, 255);
        renderRectAroundHole(poseStack, bodyConsumer, faceWidth, faceHeight,
                viewportX, viewportY, viewportWidth + frame * 2.0f, viewportHeight + frame * 2.0f,
                0.002f, FACE_R, FACE_G, FACE_B, 255);

        float trimX = outerWidth * 0.36f;
        renderRect(poseStack, bodyConsumer, -trimX, -0.10f, firstPerson ? 0.10f : 0.055f,
                faceHeight * 0.76f, 0.004f, TRIM_R, TRIM_G, TRIM_B, 255);
        renderRect(poseStack, bodyConsumer, trimX, -0.10f, firstPerson ? 0.10f : 0.055f,
                faceHeight * 0.76f, 0.004f, TRIM_R, TRIM_G, TRIM_B, 255);
        if (!viewportBroken) {
            renderFrame(poseStack, bodyConsumer, viewportX, viewportY, viewportWidth, viewportHeight,
                    frame, 0.006f, GLASS_R, GLASS_G, GLASS_B, 255);
        }

        VertexConsumer glassConsumer = buffer.getBuffer(RenderType.debugQuads());
        if (!firstPerson && !viewportBroken) {
            renderRect(poseStack, glassConsumer, viewportX, viewportY, viewportWidth, viewportHeight,
                    0.008f, GLASS_R, GLASS_G, GLASS_B, 90);
        }
        if (!viewportBroken) {
            renderRect(poseStack, glassConsumer, viewportX - viewportWidth * 0.30f,
                    viewportY + viewportHeight * 0.24f, viewportWidth * 0.30f, 0.018f,
                    0.010f, 214, 235, 238, firstPerson ? 95 : 130);
        }
    }

    private static void renderRectAroundHole(
            PoseStack poseStack,
            VertexConsumer consumer,
            float outerWidth,
            float outerHeight,
            float holeWidth,
            float holeHeight,
            float centerZ,
            int r,
            int g,
            int b,
            int a
    ) {
        renderRectAroundHole(poseStack, consumer, outerWidth, outerHeight, 0.0f, 0.0f, holeWidth, holeHeight, centerZ, r, g, b, a);
    }

    private static void renderRectAroundHole(
            PoseStack poseStack,
            VertexConsumer consumer,
            float outerWidth,
            float outerHeight,
            float holeCenterX,
            float holeCenterY,
            float holeWidth,
            float holeHeight,
            float centerZ,
            int r,
            int g,
            int b,
            int a
    ) {
        float left = -outerWidth * 0.5f;
        float right = outerWidth * 0.5f;
        float bottom = -outerHeight * 0.5f;
        float top = outerHeight * 0.5f;
        float holeLeft = holeCenterX - holeWidth * 0.5f;
        float holeRight = holeCenterX + holeWidth * 0.5f;
        float holeBottom = holeCenterY - holeHeight * 0.5f;
        float holeTop = holeCenterY + holeHeight * 0.5f;

        renderRectFromBounds(poseStack, consumer, left, right, holeTop, top, centerZ, r, g, b, a);
        renderRectFromBounds(poseStack, consumer, left, right, bottom, holeBottom, centerZ, r, g, b, a);
        renderRectFromBounds(poseStack, consumer, left, holeLeft, holeBottom, holeTop, centerZ, r, g, b, a);
        renderRectFromBounds(poseStack, consumer, holeRight, right, holeBottom, holeTop, centerZ, r, g, b, a);
    }

    private static void renderFrame(
            PoseStack poseStack,
            VertexConsumer consumer,
            float centerX,
            float centerY,
            float width,
            float height,
            float frame,
            float centerZ,
            int r,
            int g,
            int b,
            int a
    ) {
        renderRectAroundHole(poseStack, consumer, width + frame * 2.0f, height + frame * 2.0f,
                centerX, centerY, width, height, centerZ, r, g, b, a);
    }

    private static void renderRect(
            PoseStack poseStack,
            VertexConsumer consumer,
            float centerX,
            float centerY,
            float width,
            float height,
            float centerZ,
            int r,
            int g,
            int b,
            int a
    ) {
        renderRectFromBounds(poseStack, consumer,
                centerX - width * 0.5f, centerX + width * 0.5f,
                centerY - height * 0.5f, centerY + height * 0.5f,
                centerZ, r, g, b, a);
    }

    private static void renderRectFromBounds(
            PoseStack poseStack,
            VertexConsumer consumer,
            float left,
            float right,
            float bottom,
            float top,
            float z,
            int r,
            int g,
            int b,
            int a
    ) {
        if (right <= left || top <= bottom) {
            return;
        }

        Matrix4f matrix = poseStack.last().pose();
        consumer.vertex(matrix, left, bottom, z).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, right, bottom, z).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, right, top, z).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, left, top, z).color(r, g, b, a).endVertex();

        consumer.vertex(matrix, left, top, z).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, right, top, z).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, right, bottom, z).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, left, bottom, z).color(r, g, b, a).endVertex();
    }

    @Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void addLayers(EntityRenderersEvent.AddLayers event) {
            SinevaPlaceholderVisuals.registerLayers(event);
        }
    }
}
