package com.naizo.elementals.client;

import com.naizo.elementals.entity.ExplosiveFireOrbEntity;
import com.naizo.elementals.entity.FireGolemEntity;
import com.naizo.elementals.entity.FireSignatureEntity;
import com.naizo.elementals.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/** Entity renderers, registered through each loader's own hook. Minecraft 26.2 and 26.3 edition. */
public final class ClientSetup {
    public interface RendererSink {
        <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider);
    }

    private ClientSetup() {
    }

    public static void renderers(RendererSink sink) {
        sink.<FireSignatureEntity>register(ModEntities.FIRE_SIGNATURE.get(), NoopRenderer::new);
        sink.<ExplosiveFireOrbEntity>register(ModEntities.EXPLOSIVE_FIRE_ORB.get(), ctx -> new ThrownItemRenderer<>(ctx, 1.25F, true));
        sink.<FireGolemEntity>register(ModEntities.FIRE_GOLEM.get(), FireGolemRenderer::new);
    }

    public static void disconnected() {
        ClientPowers.reset();
        TreeView.resetCamera();
    }
}
