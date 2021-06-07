package net.dumbcode.dumblibrary.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.utils.MissingModelInfo;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * A simple missing model. Just one 16x16x16 cube
 */
public class ModelMissing extends DCMModel {

    private static final ModelMissing INSTANCE = new ModelMissing();

    private ModelRenderer cube;

    private ModelMissing() {
        super(MissingModelInfo.MISSING);
        this.cube = new ModelRenderer(this, 0, 0);
        this.cube.addBox(-8F, 8F, -8F, 16, 16, 16, 0.0F);
    }

    @Override
    public void renderToBuffer(MatrixStack stack, IVertexBuilder buffer, int overlay, int light, float r, float g, float b, float opacity) {
        this.cube.render(stack, buffer, overlay, light, r, g, b, opacity);
        super.renderToBuffer(stack, buffer, overlay, light, r, g, b, opacity);
    }

    public static <T extends EntityModel<?>> T getInstance() {
        return (T) INSTANCE;
    }
}
