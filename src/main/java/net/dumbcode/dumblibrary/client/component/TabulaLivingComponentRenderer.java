package net.dumbcode.dumblibrary.client.component;

import net.dumbcode.dumblibrary.client.model.ModelMissing;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;

public class TabulaLivingComponentRenderer <E extends LivingEntity> extends LivingRenderer<E, EntityModel<E>> {

    public TabulaLivingComponentRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, ModelMissing.getInstance(), 1f);
    }

    @Override
    public ResourceLocation getTextureLocation(E p_110775_1_) {
        return null;
    }
}
