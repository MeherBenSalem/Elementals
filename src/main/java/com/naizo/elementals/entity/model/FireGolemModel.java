package com.naizo.elementals.entity.model;

import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.constant.DataTickets;

import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

import com.naizo.elementals.entity.FireGolemEntity;

public class FireGolemModel extends GeoModel<FireGolemEntity> {
	@Override
	public ResourceLocation getAnimationResource(FireGolemEntity entity) {
		return new ResourceLocation("elementals", "animations/molten_golem.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(FireGolemEntity entity) {
		return new ResourceLocation("elementals", "geo/molten_golem.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(FireGolemEntity entity) {
		return new ResourceLocation("elementals", "textures/entities/" + entity.getTexture() + ".png");
	}

	@Override
	public void setCustomAnimations(FireGolemEntity animatable, long instanceId, AnimationState animationState) {
		CoreGeoBone head = getAnimationProcessor().getBone("Head");
		if (head != null) {
			EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);
			head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
			head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
		}

	}
}
