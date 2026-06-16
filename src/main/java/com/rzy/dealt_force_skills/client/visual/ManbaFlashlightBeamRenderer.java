package com.rzy.dealt_force_skills.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.client.renderer.DfsRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class ManbaFlashlightBeamRenderer {
    private static final int RING_SEGMENTS = 32;
    private static final int RED = 255;
    private static final int GREEN = 235;
    private static final int BLUE = 155;
    private static final Map<Integer, Beam> BEAMS = new HashMap<>();

    private ManbaFlashlightBeamRenderer() {
    }

    public static void syncBeam(int entityId, boolean active, float range, float halfAngleDegrees, int ticks) {
        if (!active || ticks <= 0 || range <= 0.0F) {
            BEAMS.remove(entityId);
            return;
        }
        BEAMS.put(entityId, new Beam(range, halfAngleDegrees, ticks));
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null) {
            BEAMS.clear();
            return;
        }
        Iterator<Map.Entry<Integer, Beam>> iterator = BEAMS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Beam> entry = iterator.next();
            entry.getValue().ticks--;
            if (entry.getValue().ticks <= 0 || minecraft.level.getEntity(entry.getKey()) == null) {
                iterator.remove();
            }
        }
    }

    public static boolean isActive(Player player) {
        return player != null && BEAMS.containsKey(player.getId());
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || BEAMS.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        VertexConsumer sheetConsumer = buffer.getBuffer(DfsRenderTypes.untexturedQuads());
        VertexConsumer lineConsumer = buffer.getBuffer(RenderType.lines());
        PoseStack.Pose pose = event.getPoseStack().last();
        Matrix4f matrix = pose.pose();
        Vec3 camera = event.getCamera().getPosition();

        for (Map.Entry<Integer, Beam> entry : BEAMS.entrySet()) {
            Entity entity = minecraft.level.getEntity(entry.getKey());
            if (entity instanceof Player player) {
                renderBeam(player, entry.getValue(), event.getPartialTick(), pose, matrix, camera,
                        sheetConsumer, lineConsumer);
            }
        }
        buffer.endBatch(DfsRenderTypes.untexturedQuads());
        buffer.endBatch(RenderType.lines());
    }

    private static void renderBeam(Player player, Beam beam, float partialTick, PoseStack.Pose pose,
                                   Matrix4f matrix, Vec3 camera, VertexConsumer sheetConsumer,
                                   VertexConsumer lineConsumer) {
        Vec3 look = player.getViewVector(partialTick).normalize();
        if (look.lengthSqr() < 1.0E-6D) {
            return;
        }

        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = rightVector(look, up);
        Vec3 vertical = look.cross(right).normalize();
        Vec3 start = muzzle(player, partialTick);
        Vec3 end = start.add(look.scale(beam.range));
        double radius = Math.tan(Math.toRadians(beam.halfAngleDegrees)) * beam.range;
        double nearRadius = Math.max(0.08D, radius * 0.035D);

        renderSheet(sheetConsumer, matrix, camera, start, end, right, nearRadius, radius, 42);
        renderSheet(sheetConsumer, matrix, camera, start, end, vertical, nearRadius, radius, 36);

        renderLine(lineConsumer, pose, camera, start, end, 205);
        Vec3 previous = ringPoint(end, right, vertical, radius, 0.0D);
        for (int i = 1; i <= RING_SEGMENTS; i++) {
            double angle = Math.PI * 2.0D * i / RING_SEGMENTS;
            Vec3 next = ringPoint(end, right, vertical, radius, angle);
            renderLine(lineConsumer, pose, camera, previous, next, 125);
            if (i % 4 == 0) {
                renderLine(lineConsumer, pose, camera, start, next, 95);
            }
            previous = next;
        }
        renderLine(lineConsumer, pose, camera, start, end.add(right.scale(radius)), 120);
        renderLine(lineConsumer, pose, camera, start, end.add(right.scale(-radius)), 120);
        renderLine(lineConsumer, pose, camera, start, end.add(vertical.scale(radius)), 120);
        renderLine(lineConsumer, pose, camera, start, end.add(vertical.scale(-radius)), 120);
    }

    public static Vec3 muzzle(Player player, float partialTick) {
        Vec3 look = player.getViewVector(partialTick).normalize();
        if (look.lengthSqr() < 1.0E-6D) {
            look = new Vec3(0.0D, 0.0D, 1.0D);
        }
        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = rightVector(look, up);
        Vec3 vertical = look.cross(right).normalize();
        return player.getEyePosition(partialTick)
                .add(look.scale(0.66D))
                .add(right.scale(0.74D))
                .add(vertical.scale(-0.34D));
    }

    private static Vec3 rightVector(Vec3 look, Vec3 up) {
        Vec3 right = up.cross(look);
        if (right.lengthSqr() < 1.0E-6D) {
            return new Vec3(1.0D, 0.0D, 0.0D);
        }
        return right.normalize();
    }

    private static Vec3 ringPoint(Vec3 center, Vec3 right, Vec3 vertical, double radius, double angle) {
        return center.add(right.scale(Math.cos(angle) * radius)).add(vertical.scale(Math.sin(angle) * radius));
    }

    private static void renderSheet(VertexConsumer consumer, Matrix4f matrix, Vec3 camera,
                                    Vec3 start, Vec3 end, Vec3 axis, double nearRadius,
                                    double farRadius, int alpha) {
        Vec3 startA = start.add(axis.scale(nearRadius)).subtract(camera);
        Vec3 startB = start.add(axis.scale(-nearRadius)).subtract(camera);
        Vec3 endA = end.add(axis.scale(farRadius)).subtract(camera);
        Vec3 endB = end.add(axis.scale(-farRadius)).subtract(camera);
        renderQuad(consumer, matrix, startA, endA, endB, startB, alpha);
        renderQuad(consumer, matrix, startB, endB, endA, startA, alpha);
    }

    private static void renderQuad(VertexConsumer consumer, Matrix4f matrix,
                                   Vec3 a, Vec3 b, Vec3 c, Vec3 d, int alpha) {
        consumer.vertex(matrix, (float) a.x, (float) a.y, (float) a.z).color(RED, GREEN, BLUE, alpha).endVertex();
        consumer.vertex(matrix, (float) b.x, (float) b.y, (float) b.z).color(RED, GREEN, BLUE, alpha).endVertex();
        consumer.vertex(matrix, (float) c.x, (float) c.y, (float) c.z).color(RED, GREEN, BLUE, alpha).endVertex();
        consumer.vertex(matrix, (float) d.x, (float) d.y, (float) d.z).color(RED, GREEN, BLUE, alpha).endVertex();
    }

    private static void renderLine(VertexConsumer consumer, PoseStack.Pose pose, Vec3 camera,
                                   Vec3 from, Vec3 to, int alpha) {
        Vec3 a = from.subtract(camera);
        Vec3 b = to.subtract(camera);
        Vec3 normal = b.subtract(a);
        if (normal.lengthSqr() < 1.0E-6D) {
            normal = new Vec3(0.0D, 1.0D, 0.0D);
        } else {
            normal = normal.normalize();
        }
        consumer.vertex(pose.pose(), (float) a.x, (float) a.y, (float) a.z)
                .color(RED, GREEN, BLUE, alpha)
                .normal(pose.normal(), (float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
        consumer.vertex(pose.pose(), (float) b.x, (float) b.y, (float) b.z)
                .color(RED, GREEN, BLUE, alpha)
                .normal(pose.normal(), (float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }

    private static final class Beam {
        private final float range;
        private final float halfAngleDegrees;
        private int ticks;

        private Beam(float range, float halfAngleDegrees, int ticks) {
            this.range = range;
            this.halfAngleDegrees = halfAngleDegrees;
            this.ticks = ticks;
        }
    }
}
