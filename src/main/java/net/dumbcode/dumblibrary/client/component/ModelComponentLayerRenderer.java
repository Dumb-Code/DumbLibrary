package net.dumbcode.dumblibrary.client.component;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

@RequiredArgsConstructor
public class ModelComponentLayerRenderer implements LayerRenderer {

    private final Consumer<Runnable> callback;
    private final IntSupplier renderIDGetter;

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        int i = this.renderIDGetter.getAsInt();
        this.callback.accept(() -> GlStateManager.callList(i));
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}