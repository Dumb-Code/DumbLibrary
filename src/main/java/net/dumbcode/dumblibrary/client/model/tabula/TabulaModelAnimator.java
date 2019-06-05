package net.dumbcode.dumblibrary.client.model.tabula;

import net.minecraft.entity.Entity;

@FunctionalInterface
public interface TabulaModelAnimator<E extends Entity> {
    void setRotationAngles(TabulaModel model, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, E entityIn);
}
