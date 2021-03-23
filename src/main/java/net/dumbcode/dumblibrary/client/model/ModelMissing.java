package net.dumbcode.dumblibrary.client.model;

import net.dumbcode.dumblibrary.client.model.tabula.DCMModel;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * A simple missing model. Just one 16x16x16 cube
 */
public class ModelMissing extends DCMModel {

    public static final ModelMissing INSTANCE = new ModelMissing();

    private ModelRenderer cube;

    private ModelMissing() {
        super(TabulaModelInformation.MISSING);
        this.cube = new ModelRenderer(this, 0, 0);
        this.cube.addBox(-8F, 8F, -8F, 16, 16, 16, 0.0F);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.cube.render(scale);
        super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }
}
