package net.dumbcode.dumblibrary.client.model.dcm;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.animation.AnimatedReferenceCube;
import net.dumbcode.studio.model.CubeInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class DCMModelRenderer extends ModelRenderer implements AnimatedReferenceCube {

    private final DCMModel model;

    private float texWidth = 64.0F;
    private float texHeight = 32.0F;

    private boolean growDirty = true;
    private final CubeInfo info;

    private float growX;
    private float growY;
    private float growZ;

    private ModelRenderer.ModelBox box;

    @Getter
    private final String name;

    @Setter
    private boolean hideButShowChildren;

    private final DCMModelRenderer parent;
    @Getter
    private final List<DCMModelRenderer> childCubes = new ArrayList<>();

    public DCMModelRenderer(DCMModel model, DCMModelRenderer parent, CubeInfo info) {
        super(model);
        this.model = model;
        model.getCubeNameMap().put(info.getName(), this);
        this.parent = parent;
        this.texWidth = model.texWidth;
        this.texHeight = model.texHeight;
        this.info = info;
        this.name = info.getName();
        for (CubeInfo child : info.getChildren()) {
            DCMModelRenderer renderer = new DCMModelRenderer(model, this, child);
            this.addChild(renderer);
            this.childCubes.add(renderer);
        }

        float[] position = info.getRotationPoint();
        this.x = position[0];
        this.y = position[1];
        this.z = position[2];

        float[] rotation = info.getRotation();
        this.xRot = rotation[0];
        this.yRot = rotation[1];
        this.zRot = rotation[2];

        float[] cubeGrow = info.getCubeGrow();
        this.growX = cubeGrow[0];
        this.growY = cubeGrow[1];
        this.growZ = cubeGrow[2];
    }

    @Override
    public CubeInfo getInfo() {
        return this.info;
    }

    @Override
    public DCMModelRenderer getParent() {
        return this.parent;
    }

    @Override
    public void setRotation(float x, float y, float z) {
        this.xRot = x;
        this.yRot = y;
        this.zRot = z;
    }

    @Override
    public float[] getRotation() {
        return new float[] { this.xRot, this.yRot, this.zRot };
    }

    @Override
    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public float[] getPosition() {
        return new float[] { this.x, this.y, this.z };
    }

    @Override
    public void setCubeGrow(float x, float y, float z) {
        this.growX = x;
        this.growY = y;
        this.growZ = z;
        this.growDirty = true;
    }

    @Override
    public float[] getCubeGrow() {
        return new float[] { this.growX, this.growY, this.growZ };
    }

    @Override
    public void render(MatrixStack stack, IVertexBuilder buffer, int light, int overlay, float r, float g, float b, float a) {
        if(this.growDirty || true) {
            this.box = new ModelRenderer.ModelBox(
                this.info.getTextureOffset()[0], this.info.getTextureOffset()[1],
                this.info.getOffset()[0], this.info.getOffset()[1], this.info.getOffset()[2],
                this.info.getDimensions()[0], this.info.getDimensions()[1], this.info.getDimensions()[2],
                this.growX, this.growY, this.growZ,
                this.info.isTextureMirrored(),
                this.texWidth, this.texHeight
            );
            this.growDirty = false;
            this.cubes.clear();
            this.cubes.add(this.box);

            //Vertices on the X and Y axis are now flipped. We need to unflip them.
            for (int i = 0; i < 4; i+=2) {
                ModelRenderer.TexturedQuad top = this.box.polygons[i];
                ModelRenderer.TexturedQuad bottom = this.box.polygons[i+1];

                for (int v = 0; v < 4; v++) {
                    ModelRenderer.PositionTextureVertex topVertex = top.vertices[v];
                    ModelRenderer.PositionTextureVertex bottomVertex = bottom.vertices[v];

                    top.vertices[v] = top.vertices[v].remap(bottomVertex.u, bottomVertex.v);
                    bottom.vertices[v] = bottom.vertices[v].remap(topVertex.u, topVertex.v);
                }
            }

            //We also need to rotate all the vertices twice.
            for (int v = 0; v < 6; v++) {
                ModelRenderer.TexturedQuad quad = this.box.polygons[v];
                PositionTextureVertex v0 = quad.vertices[0];
                PositionTextureVertex v1 = quad.vertices[1];
                PositionTextureVertex v2 = quad.vertices[2];
                PositionTextureVertex v3 = quad.vertices[3];

                quad.vertices[0] = v0.remap(v2.u, v2.v);
                quad.vertices[1] = v1.remap(v3.u, v3.v);
                quad.vertices[2] = v2.remap(v0.u, v0.v);
                quad.vertices[3] = v3.remap(v1.u, v1.v);
            }


        }
        if(this.hideButShowChildren) {
            this.cubes.clear();
        }
        super.render(stack, buffer, light, overlay, r, g, b, a);
        if(this.hideButShowChildren) {
            this.cubes.add(this.box);
        }
    }

    public void resetRotations() {
        float[] rotation = this.info.getRotation();
        this.xRot = rotation[0];
        this.yRot = rotation[1];
        this.zRot = rotation[2];
    }

    public void resetRotationPoint() {
        float[] position = this.info.getRotationPoint();
        this.x = position[0];
        this.y = position[1];
        this.z = position[2];
    }
}
