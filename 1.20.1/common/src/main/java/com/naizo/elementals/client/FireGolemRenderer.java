package com.naizo.elementals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.naizo.elementals.Elementals;
import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.entity.FireGolemEntity;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** The Fire Golem: iron golem geometry with its own magma texture. Minecraft 1.20.1 edition. */
public class FireGolemRenderer extends MobRenderer<FireGolemEntity, IronGolemModel<FireGolemEntity>> {
    private static final ResourceLocation TEXTURE = Compat.id(Elementals.MOD_ID, "textures/entity/fire_golem.png");

    public FireGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new IronGolemModel<>(context.bakeLayer(ModelLayers.IRON_GOLEM)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(FireGolemEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(FireGolemEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick) {
        super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick);
        if (entity.walkAnimation.speed() >= 0.01) {
            float position = entity.walkAnimation.position(partialTick) + 6.0F;
            float sway = (Math.abs(position % 13.0F - 6.5F) - 3.25F) / 3.25F;
            poseStack.mulPose(Axis.ZP.rotationDegrees(6.5F * sway));
        }
    }

    @Override
    protected int getBlockLightLevel(FireGolemEntity entity, net.minecraft.core.BlockPos pos) {
        return 15;
    }
}
