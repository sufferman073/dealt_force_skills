package com.rzy.dealt_force_skills.client.renderer;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.saeed.SaeedGuardType;
import com.rzy.dealt_force_skills.entity.SaeedGuardEntity;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

public class SaeedGuardRenderer extends LivingEntityRenderer<SaeedGuardEntity, PlayerModel<SaeedGuardEntity>> {
    private static final ResourceLocation THUNDER =
            texture("thunder");
    private static final ResourceLocation IRON_RAIN =
            texture("iron_rain");
    private static final ResourceLocation FIREEYE =
            texture("fireeye");
    private static final ResourceLocation SHARP_EAGLE =
            texture("sharp_eagle");
    private static final ResourceLocation HAKIM =
            texture("hakim");
    private static final ResourceLocation KARIM =
            texture("karim");

    public SaeedGuardRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.45F);
        addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ));
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(SaeedGuardEntity entity) {
        return switch (entity.guardType()) {
            case THUNDER -> THUNDER;
            case IRON_RAIN -> IRON_RAIN;
            case FIREEYE -> FIREEYE;
            case SHARP_EAGLE -> SHARP_EAGLE;
            case HAKIM -> HAKIM;
            case KARIM -> KARIM;
        };
    }

    @Override
    protected boolean shouldShowName(SaeedGuardEntity entity) {
        return false;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(DealtForceSkillsMod.MODID, "textures/entity/saeed/" + name + ".png");
    }
}
