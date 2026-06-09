package com.rzy.dealt_force_skills.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.luna.LunaTool;
import com.rzy.dealt_force_skills.character.hackclaw.HackclawTool;
import com.rzy.dealt_force_skills.character.morse.MorseTool;
import com.rzy.dealt_force_skills.character.nox.NoxTool;
import com.rzy.dealt_force_skills.character.raptor.RaptorTool;
import com.rzy.dealt_force_skills.character.shepherd.ShepherdTool;
import com.rzy.dealt_force_skills.character.tempest.TempestTool;
import com.rzy.dealt_force_skills.character.uluru.UluruTool;
import com.rzy.dealt_force_skills.character.vyron.VyronTool;
import com.rzy.dealt_force_skills.client.DWolfInputHandler;
import com.rzy.dealt_force_skills.client.SinevaInputHandler;
import com.rzy.dealt_force_skills.client.VyronInputHandler;
import com.rzy.dealt_force_skills.client.character.ClientDWolfHudState;
import com.rzy.dealt_force_skills.client.character.ClientHackclawHudState;
import com.rzy.dealt_force_skills.client.character.ClientLunaHudState;
import com.rzy.dealt_force_skills.client.character.ClientMorseHudState;
import com.rzy.dealt_force_skills.client.character.ClientNoxHudState;
import com.rzy.dealt_force_skills.client.character.ClientRaptorHudState;
import com.rzy.dealt_force_skills.client.character.ClientShepherdHudState;
import com.rzy.dealt_force_skills.client.character.ClientTempestHudState;
import com.rzy.dealt_force_skills.client.character.ClientUluruHudState;
import com.rzy.dealt_force_skills.client.character.ClientVyronHudState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class ThrowTrajectoryPreview {
    private static final int MAX_POINTS = 96;

    private ThrowTrajectoryPreview() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }

        Preview preview = currentPreview(minecraft.player);
        if (preview == null) {
            return;
        }

        Trajectory trajectory = simulate(minecraft.level, minecraft.player, preview);
        if (trajectory.points().size() < 2) {
            return;
        }

        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        PoseStack poseStack = event.getPoseStack();
        PoseStack.Pose pose = poseStack.last();
        Vec3 camera = event.getCamera().getPosition();

        List<Vec3> points = trajectory.points();
        for (int i = 1; i < points.size(); i++) {
            renderLine(consumer, pose, camera, points.get(i - 1), points.get(i),
                    preview.red(), preview.green(), preview.blue(), 210);
        }
        renderLandingMark(consumer, pose, camera, trajectory.end(),
                trajectory.hit() ? 255 : 180, preview.red(), preview.green(), preview.blue());
        buffer.endBatch(RenderType.lines());
    }

    private static Preview currentPreview(Player player) {
        if (ClientShepherdHudState.shouldRender() && ClientShepherdHudState.equippedTool() == ShepherdTool.FRAG_GRENADE) {
            return preview(player, 0.70D, -0.12D, 1.45D, 0.18D, 0.045D, 0.985D, 80, 255, 175, 64);
        }
        if (ClientLunaHudState.shouldRender() && ClientLunaHudState.equippedTool() == LunaTool.COMPOSITE_GRENADE) {
            return preview(player, 0.70D, -0.12D, 1.55D, 0.18D, 0.045D, 0.986D, 80, 112, 210, 255);
        }
        if (ClientHackclawHudState.shouldRender() && ClientHackclawHudState.equippedTool() == HackclawTool.HACKING_KNIFE) {
            return preview(player, 0.62D, -0.08D, 1.55D, 0.30D, 0.040D, 0.988D, 80, 98, 223, 164);
        }
        if (ClientHackclawHudState.shouldRender() && ClientHackclawHudState.equippedTool() == HackclawTool.FLASH_DRONE) {
            return preview(player, 0.68D, -0.04D, 1.45D, 0.10D, 0.012D, 0.995D, 65, 255, 227, 143);
        }
        if (ClientVyronHudState.shouldRender()
                && (ClientVyronHudState.equippedTool() == VyronTool.MAGNETIC_BOMB || VyronInputHandler.isMagneticBombHeldForVisual())) {
            return preview(player, 0.62D, -0.10D, 1.75D, 0.32D, 0.040D, 0.990D, 80, 150, 255, 150);
        }
        if (ClientVyronHudState.shouldRender() && ClientVyronHudState.equippedTool() == VyronTool.TIGER_CANNON) {
            return preview(player, 0.80D, -0.08D, 1.90D, 0.08D, 0.045D, 0.992D, 90, 255, 136, 64);
        }
        if (ClientNoxHudState.shouldRender() && ClientNoxHudState.equippedTool() == NoxTool.FLASH_GRENADE) {
            return preview(player, 0.58D, -0.10D, 1.55D, 0.28D, 0.045D, 0.985D, 80, 245, 245, 180);
        }
        if (ClientMorseHudState.shouldRender() && ClientMorseHudState.equippedTool() == MorseTool.SHOCK_ORB) {
            return preview(player, 0.70D, -0.12D, 1.45D, 0.18D, 0.045D, 0.985D, 80, 242, 200, 75);
        }
        if (ClientMorseHudState.shouldRender() && ClientMorseHudState.equippedTool() == MorseTool.FLASH_GRENADE) {
            return preview(player, 0.58D, -0.10D, 1.55D, 0.28D, 0.045D, 0.985D, 80, 225, 232, 255);
        }
        if (ClientRaptorHudState.shouldRender() && ClientRaptorHudState.equippedTool() == RaptorTool.PULSE_GRENADE) {
            return preview(player, 0.58D, -0.10D, 1.75D, 0.34D, 0.045D, 0.985D, 75, 96, 168, 255);
        }
        if (ClientTempestHudState.shouldRender() && ClientTempestHudState.equippedTool() == TempestTool.WALL_DRILL_STINGER) {
            return preview(player, 0.62D, -0.08D, 1.55D, 0.30D, 0.035D, 0.988D, 80, 124, 255, 91);
        }
        if (ClientDWolfHudState.hasHandCannonEquipped()) {
            return preview(player, 0.75D, -0.12D, 2.05D, 0.05D, 0.045D, 0.985D, 70, 255, 88, 72);
        }
        if (ClientUluruHudState.shouldRender() && ClientUluruHudState.equippedTool() == UluruTool.INCENDIARY) {
            return preview(player, 0.55D, -0.10D, 2.25D, 0.14D, 0.040D, 0.980D, 80, 255, 105, 48);
        }
        if (ClientUluruHudState.shouldRender() && ClientUluruHudState.equippedTool() == UluruTool.COVER) {
            return preview(player, 0.55D, -0.10D, 1.35D, 0.04D, 0.035D, 0.985D, 70, 110, 205, 255);
        }
        if (SinevaInputHandler.isBladeWireHeld()) {
            return preview(player, 0.55D, -0.10D, 2.25D, 0.14D, 0.030D, 0.990D, 80, 190, 190, 210);
        }
        if (DWolfInputHandler.isSmokeHeldForVisual()) {
            boolean high = DWolfInputHandler.isSmokeHighThrowPreview();
            return preview(player, 0.58D, -0.10D, high ? 1.80D : 1.05D, high ? 0.34D : 0.08D,
                    0.045D, 0.985D, 75, 170, 170, 170);
        }
        return null;
    }

    private static Preview preview(Player player, double forwardOffset, double yOffset, double speed, double lift,
                                   double gravity, double drag, int maxTicks, int red, int green, int blue) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition().add(look.scale(forwardOffset)).add(0.0D, yOffset, 0.0D);
        Vec3 velocity = look.scale(speed).add(0.0D, lift, 0.0D);
        return new Preview(start, velocity, gravity, drag, Math.min(maxTicks, MAX_POINTS), red, green, blue);
    }

    private static Trajectory simulate(Level level, Player player, Preview preview) {
        List<Vec3> points = new ArrayList<>();
        Vec3 pos = preview.start();
        Vec3 motion = preview.velocity();
        points.add(pos);

        for (int i = 0; i < preview.maxTicks(); i++) {
            Vec3 next = pos.add(motion);
            HitResult hit = level.clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() != HitResult.Type.MISS) {
                Vec3 end = hit.getLocation();
                points.add(end);
                return new Trajectory(points, end, true);
            }

            points.add(next);
            pos = next;
            motion = motion.add(0.0D, -preview.gravity(), 0.0D).scale(preview.drag());
        }

        return new Trajectory(points, points.get(points.size() - 1), false);
    }

    private static void renderLandingMark(VertexConsumer consumer, PoseStack.Pose pose, Vec3 camera, Vec3 center,
                                          int alpha, int red, int green, int blue) {
        double half = 0.28D;
        renderLine(consumer, pose, camera, center.add(-half, 0.02D, 0.0D), center.add(half, 0.02D, 0.0D), red, green, blue, alpha);
        renderLine(consumer, pose, camera, center.add(0.0D, 0.02D, -half), center.add(0.0D, 0.02D, half), red, green, blue, alpha);
        renderLine(consumer, pose, camera, center.add(-half, 0.02D, -half), center.add(half, 0.02D, half), red, green, blue, alpha / 2);
        renderLine(consumer, pose, camera, center.add(-half, 0.02D, half), center.add(half, 0.02D, -half), red, green, blue, alpha / 2);
    }

    private static void renderLine(VertexConsumer consumer, PoseStack.Pose pose, Vec3 camera,
                                   Vec3 from, Vec3 to, int red, int green, int blue, int alpha) {
        Vec3 a = from.subtract(camera);
        Vec3 b = to.subtract(camera);
        Vec3 normal = b.subtract(a);
        if (normal.lengthSqr() < 1.0E-6D) {
            normal = new Vec3(0.0D, 1.0D, 0.0D);
        } else {
            normal = normal.normalize();
        }
        consumer.vertex(pose.pose(), (float) a.x, (float) a.y, (float) a.z)
                .color(red, green, blue, alpha)
                .normal(pose.normal(), (float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
        consumer.vertex(pose.pose(), (float) b.x, (float) b.y, (float) b.z)
                .color(red, green, blue, alpha)
                .normal(pose.normal(), (float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }

    private record Preview(Vec3 start, Vec3 velocity, double gravity, double drag, int maxTicks, int red, int green, int blue) {
    }

    private record Trajectory(List<Vec3> points, Vec3 end, boolean hit) {
    }
}
