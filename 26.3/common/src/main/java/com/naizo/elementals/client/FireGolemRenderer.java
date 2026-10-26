package com.naizo.elementals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.naizo.elementals.Elementals;
import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.entity.FireGolemEntity;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/** The Fire Golem: iron golem geometry with its own magma texture. Minecraft 26.3 edition. */
public class FireGolemRenderer extends MobRenderer<FireGolemEntity, IronGolemRenderState, IronGolemModel> {
    private static final Identifier TEXTURE = Compat.id(Elementals.MOD_ID, "textures/entity/fire_golem.png");

    public FireGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new IronGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM)), 0.7F);
    }

    @Override
    public Identifier getTextureLocation(IronGolemRenderState state) {
        return TEXTURE;
    }

    @Override
    public IronGolemRenderState createRenderState() {
        return new IronGolemRenderState();
    }

    @Override
    public void extractRenderState(FireGolemEntity entity, IronGolemRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attackTicksRemaining = entity.getAttackAnimationTick() > 0 ? entity.getAttackAnimationTick() - partialTicks : 0;
        state.offerFlowerTick = 0;
        state.flowerBlock.clear();
        state.crackiness = entity.getCrackiness();
    }

    @Override
    protected void setupRotations(IronGolemRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        if (state.walkAnimationSpeed >= 0.01) {
            float position = state.walkAnimationPos + 6.0F;
            float sway = (Math.abs(position % 13.0F - 6.5F) - 3.25F) / 3.25F;
            poseStack.rotateDegrees(Axis.ZP, 6.5F * sway);
        }
    }

    @Override
    protected int getBlockLightLevel(FireGolemEntity entity, BlockPos pos) {
        return 15;
    }
}
