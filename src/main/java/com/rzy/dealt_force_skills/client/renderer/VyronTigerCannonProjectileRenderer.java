package com.rzy.dealt_force_skills.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.entity.VyronTigerCannonEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public final class VyronTigerCannonProjectileRenderer extends EntityRenderer<VyronTigerCannonEntity> {
    public static final ResourceLocation MODEL =
            new ResourceLocation(DealtForceSkillsMod.MODID, "vyron_tiger_cannon_projectile");

    private final ThrownItemRenderer<VyronTigerCannonEntity> fallback;

    public VyronTigerCannonProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        fallback = new ThrownItemRenderer<>(context);
    }

    @Override
    public void render(
            VyronTigerCannonEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        poseStack.pushPose();
        alignToMotion(poseStack, entity.getDeltaMovement());
        boolean rendered = BlockbenchAnimatedModelRenderer.render(
                MODEL,
                "idle",
                (entity.tickCount + partialTick) / 20.0F,
                poseStack,
                buffer,
                packedLight
        );
        poseStack.popPose();

        if (!rendered) {
            fallback.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
            return;
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(VyronTigerCannonEntity entity) {
        return BlockbenchAnimatedModelRenderer.textureLocation(MODEL);
    }

    private static void alignToMotion(PoseStack poseStack, Vec3 motion) {
        if (motion.lengthSqr() < 0.000001D) {
            return;
        }
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        float yaw = (float) Math.toDegrees(Math.atan2(motion.z, motion.x));
        float pitch = (float) Math.toDegrees(Math.atan2(motion.y, horizontal));
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
    }
}
