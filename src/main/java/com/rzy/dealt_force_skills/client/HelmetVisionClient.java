package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.config.DealtForceConfig;
import com.mojang.logging.LogUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.item.DfsEquipmentItem;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class HelmetVisionClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final double THERMAL_HIGHLIGHT_RANGE = DealtForceConfig.doubleValue("client.helmet_vision_client.thermal_highlight_range", 96.0D);
    private static final int HEARING_REVEAL_COLOR = 0xFF7EE8FF;
    private static final String THERMAL_TEAM_NAME = "dfs_thermal";
    private static final Map<Integer, ThermalHighlightState> THERMAL_RESTORE = new HashMap<>();
    private static ClientLevel thermalLevel;

    private HelmetVisionClient() {
    }

    public static boolean isThermalVisionActive() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && DfsEquipmentItem.activeVisionMode(minecraft.player) == DfsEquipmentItem.VISION_THERMAL;
    }

    public static void syncLocalHelmetMode(int mode) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        ItemStack head = minecraft.player.getItemBySlot(EquipmentSlot.HEAD);
        if (DfsEquipmentItem.profile(head) != null) {
            DfsEquipmentItem.setVisionMode(head, mode);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (thermalLevel != minecraft.level) {
            clearThermalHighlightCache();
            thermalLevel = minecraft.level;
        }
        tickThermalHighlights(minecraft);
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clearThermalHighlightCache();
        thermalLevel = null;
    }

    @SubscribeEvent
    public static void onClientLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ClientLevel) {
            clearThermalHighlightCache();
            thermalLevel = null;
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!VanillaGuiOverlay.HOTBAR.id().equals(event.getOverlay().id())) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        int mode = DfsEquipmentItem.activeVisionMode(player);
        int width = event.getGuiGraphics().guiWidth();
        int height = event.getGuiGraphics().guiHeight();
        if (mode == DfsEquipmentItem.VISION_NIGHT) {
            event.getGuiGraphics().fill(0, 0, width, height, 0x244AFF5C);
            event.getGuiGraphics().fill(0, 0, width, height, 0x12F0FFF0);
            renderScanLines(event, player.tickCount, width, height, 0x245CFF66);
        } else if (mode == DfsEquipmentItem.VISION_THERMAL) {
            event.getGuiGraphics().fill(0, 0, width, height, 0x5E101010);
            event.getGuiGraphics().fill(0, 0, width, height, 0x32FFFFFF);
            renderScanLines(event, player.tickCount, width, height, 0x26FFFFFF);
        } else {
            restoreThermalHighlights();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        boolean thermal = DfsEquipmentItem.activeVisionMode(player) == DfsEquipmentItem.VISION_THERMAL;
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        DfsEquipmentItem.Profile profile = DfsEquipmentItem.profile(head);
        double hearingRange = hearingRevealRange(head, profile);
        if (!thermal && hearingRange <= 0.0D) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        Font font = minecraft.font;

        RenderSystem.disableDepthTest();
        var lineBuffer = buffers.getBuffer(RenderType.lines());
        if (thermal) {
            renderThermalEntityBoxes(minecraft, player, poseStack, lineBuffer, cameraPos);
        }
        if (hearingRange > 0.0D) {
            renderHearingRevealMarkers(minecraft, player, profile, hearingRange, poseStack, buffers, font, camera, cameraPos, lineBuffer);
        }
        buffers.endBatch(RenderType.lines());
        buffers.endBatch();
        RenderSystem.enableDepthTest();
    }

    private static void renderScanLines(RenderGuiOverlayEvent.Post event, int tickCount, int width, int height, int color) {
        int offset = Math.floorMod(tickCount, 8);
        for (int y = offset; y < height; y += 8) {
            event.getGuiGraphics().fill(0, y, width, y + 1, color);
        }
    }

    private static void renderThermalEntityBoxes(Minecraft minecraft, LocalPlayer player, PoseStack poseStack,
                                                 VertexConsumer lineBuffer, Vec3 cameraPos) {
        double rangeSqr = THERMAL_HIGHLIGHT_RANGE * THERMAL_HIGHLIGHT_RANGE;
        Vec3 viewer = player.getEyePosition();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity == player || !entity.isAlive() || entity.isSpectator()
                    || entity.distanceToSqr(viewer) > rangeSqr
                    || isThermalHiddenTarget(entity)) {
                continue;
            }
            AABB box = entity.getBoundingBox().inflate(0.08D)
                    .move(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            LevelRenderer.renderLineBox(poseStack, lineBuffer, box, 1.0F, 1.0F, 1.0F, 0.98F);
        }
    }

    private static void renderHearingRevealMarkers(Minecraft minecraft, LocalPlayer player,
                                                   DfsEquipmentItem.Profile profile, double hearingRange,
                                                   PoseStack poseStack, MultiBufferSource.BufferSource buffers,
                                                   Font font, Camera camera, Vec3 cameraPos,
                                                   VertexConsumer lineBuffer) {
        boolean includePlayers = profile != null && profile.ability() == DfsEquipmentItem.SpecialAbility.HEARING_SHARE;
        double rangeSqr = hearingRange * hearingRange;
        for (LivingEntity entity : minecraft.level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(hearingRange),
                entity -> isHearingRevealTarget(player, entity, includePlayers))) {
            if (entity.distanceToSqr(player) > rangeSqr) {
                continue;
            }
            AABB box = entity.getBoundingBox().inflate(0.04D)
                    .move(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            LevelRenderer.renderLineBox(poseStack, lineBuffer, box, 0.49F, 0.91F, 1.0F, 0.82F);
            Vec3 labelPos = entity.position().add(0.0D, entity.getBbHeight() + 0.35D, 0.0D);
            double distance = labelPos.distanceTo(player.getEyePosition());
            String text = Component.translatable("hud.dealt_force_skills.equipment.hearing_revealed").getString()
                    + " " + Math.round(distance) + "m";
            renderWorldLabel(poseStack, buffers, font, camera, cameraPos, labelPos, text, HEARING_REVEAL_COLOR);
        }
    }

    private static boolean isHearingRevealTarget(LocalPlayer player, LivingEntity entity, boolean includePlayers) {
        return entity.isAlive()
                && entity != player
                && !entity.isSpectator()
                && (includePlayers || !(entity instanceof Player));
    }

    private static double hearingRevealRange(ItemStack head, DfsEquipmentItem.Profile profile) {
        if (profile == null || profile.hearingBoost() <= 0.0D) {
            return 0.0D;
        }
        return Math.max(0.0D, profile.hearingBoost() * 100.0D);
    }

    private static void renderWorldLabel(PoseStack poseStack, MultiBufferSource.BufferSource buffers,
                                         Font font, Camera camera, Vec3 cameraPos, Vec3 worldPos,
                                         String text, int color) {
        poseStack.pushPose();
        poseStack.translate(worldPos.x - cameraPos.x, worldPos.y - cameraPos.y, worldPos.z - cameraPos.z);
        poseStack.mulPose(camera.rotation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        font.drawInBatch(text, -font.width(text) / 2.0F, 0.0F, color, false,
                poseStack.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH,
                0x66000000, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    private static void tickThermalHighlights(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null) {
            clearThermalHighlightCache();
            return;
        }
        if (!isThermalVisionActive()) {
            restoreThermalHighlights();
            return;
        }

        Set<Integer> current = new HashSet<>();
        for (LivingEntity entity : minecraft.level.getEntitiesOfClass(
                LivingEntity.class,
                minecraft.player.getBoundingBox().inflate(THERMAL_HIGHLIGHT_RANGE),
                entity -> entity.isAlive()
                        && entity != minecraft.player
                        && !isThermalHiddenTarget(entity))) {
            current.add(entity.getId());
            THERMAL_RESTORE.putIfAbsent(entity.getId(), captureThermalState(entity));
            entity.setGlowingTag(true);
            applyThermalTeam(entity);
        }

        THERMAL_RESTORE.entrySet().removeIf(entry -> {
            if (current.contains(entry.getKey())) {
                return false;
            }
            restoreThermalHighlight(minecraft, entry.getKey(), entry.getValue());
            return true;
        });
    }

    private static boolean isThermalHiddenTarget(Entity entity) {
        return entity instanceof Player player && player.hasEffect(ModEffects.NOX_STEALTH.get());
    }

    private static void restoreThermalHighlights() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level != thermalLevel) {
            clearThermalHighlightCache();
            thermalLevel = minecraft.level;
            return;
        }
        for (Map.Entry<Integer, ThermalHighlightState> entry : THERMAL_RESTORE.entrySet()) {
            restoreThermalHighlight(minecraft, entry.getKey(), entry.getValue());
        }
        clearThermalHighlightCache();
    }

    private static ThermalHighlightState captureThermalState(Entity entity) {
        PlayerTeam team = entity.getTeam() instanceof PlayerTeam playerTeam ? playerTeam : null;
        return new ThermalHighlightState(entity.isCurrentlyGlowing(), entity.getScoreboardName(),
                team != null ? team.getName() : "");
    }

    private static void applyThermalTeam(Entity entity) {
        try {
            Scoreboard scoreboard = entity.level().getScoreboard();
            PlayerTeam thermalTeam = scoreboard.getPlayerTeam(THERMAL_TEAM_NAME);
            if (thermalTeam == null) {
                thermalTeam = scoreboard.addPlayerTeam(THERMAL_TEAM_NAME);
                thermalTeam.setColor(ChatFormatting.WHITE);
            }
            String scoreboardName = entity.getScoreboardName();
            PlayerTeam current = scoreboard.getPlayersTeam(scoreboardName);
            if (current == thermalTeam) {
                return;
            }
            removeFromTeamIfCurrent(scoreboard, scoreboardName, current);
            if (scoreboard.getPlayersTeam(scoreboardName) == null) {
                scoreboard.addPlayerToTeam(scoreboardName, thermalTeam);
            }
        } catch (RuntimeException exception) {
            LOGGER.debug("Failed to apply thermal highlight team to entity {}", entity.getId(), exception);
        }
    }

    private static void restoreThermalHighlight(Minecraft minecraft, int entityId, ThermalHighlightState state) {
        try {
            if (minecraft.level == null || minecraft.level != thermalLevel) {
                return;
            }
            Entity entity = minecraft.level.getEntity(entityId);
            if (entity != null && entity.getScoreboardName().equals(state.scoreboardName())) {
                entity.setGlowingTag(state.glowing());
            }
            Scoreboard scoreboard = minecraft.level.getScoreboard();
            String scoreboardName = state.scoreboardName();
            PlayerTeam thermalTeam = scoreboard.getPlayerTeam(THERMAL_TEAM_NAME);
            removeFromTeamIfCurrent(scoreboard, scoreboardName, thermalTeam);

            PlayerTeam current = scoreboard.getPlayersTeam(scoreboardName);
            if (!state.teamName().isBlank() && (current == null || current == thermalTeam)) {
                PlayerTeam originalTeam = scoreboard.getPlayerTeam(state.teamName());
                if (originalTeam != null && scoreboard.getPlayersTeam(scoreboardName) == current) {
                    scoreboard.addPlayerToTeam(scoreboardName, originalTeam);
                }
            }
        } catch (RuntimeException exception) {
            LOGGER.debug("Failed to restore thermal highlight for entity {} ({})",
                    entityId, state.scoreboardName(), exception);
        }
    }

    private static void removeFromTeamIfCurrent(Scoreboard scoreboard, String entry, PlayerTeam targetTeam) {
        if (targetTeam != null && scoreboard.getPlayersTeam(entry) == targetTeam) {
            scoreboard.removePlayerFromTeam(entry, targetTeam);
        }
    }

    private static void clearThermalHighlightCache() {
        THERMAL_RESTORE.clear();
    }

    private record ThermalHighlightState(boolean glowing, String scoreboardName, String teamName) {
    }
}
