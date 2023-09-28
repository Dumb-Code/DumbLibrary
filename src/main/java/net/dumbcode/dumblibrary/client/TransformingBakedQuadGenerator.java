package net.dumbcode.dumblibrary.client;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import java.util.ArrayList;
import java.util.List;

public class TransformingBakedQuadGenerator implements IVertexBuilder {
    private final List<BakedQuad> out = new ArrayList<>();
    private final TextureAtlasSprite texture;
    private BakedQuadBuilder builder;
    private final TransformationMatrix transform;
    private int vertexCount;
    private float x;
    private float y;
    private float z;
    private float u;
    private float v;
    private float nx;
    private float ny;
    private float nz;

    public TransformingBakedQuadGenerator(TextureAtlasSprite texture, TransformationMatrix transform) {
        this.texture = texture;
        this.builder = new BakedQuadBuilder(texture);
        this.transform = transform;
        this.resetState();
    }

    private void resetState() {
        this.x = 0.0F;
        this.y = 0.0F;
        this.z = 0.0F;
        this.u = 0.0F;
        this.v = 0.0F;
        this.nx = 0.0F;
        this.ny = 1.0F;
        this.nz = 0.0F;
    }

    public void endVertex() {

        if(this.vertexCount == 0) {
            this.builder.setQuadOrientation(Direction.getNearest(this.nx, this.ny, this.nz));
        }

        VertexFormat format = DefaultVertexFormats.BLOCK;
        for (int e = 0; e < format.getElements().size(); e++) {
            switch (format.getElements().get(e).getUsage()) {
                case POSITION:
                    builder.put(e, this.x, this.y, this.z);
                    break;
                case COLOR:
                    builder.put(e, 1, 1, 1, 1);
                    break;
                case UV:
                    if (format.getElements().get(e).getIndex() == 0) { //normal uv
                        builder.put(e, this.u, this.v);
                    } else {
                        builder.put(e,0, 0, 0, 1);
                    }
                    break;
                case NORMAL:
                    builder.put(e, this.nx, this.ny, this.nz);
                    break;
                default: //Log that we don't know what to do with the element?
                    builder.put(e);
            }
        }
        if(++this.vertexCount == 4) {
            out.add(builder.build());
            builder = new BakedQuadBuilder(texture);
            this.vertexCount = 0;
        }

        this.resetState();
    }

    public List<BakedQuad> poll() {
        List<BakedQuad> out = new ArrayList<>(this.out);
        this.out.clear();
        return out;
    }

    public IVertexBuilder vertex(double x, double y, double z) {
        Vector4f pos = new Vector4f((float) x, (float) y, (float) z, 1);
        transform.transformPosition(pos);
        this.x = pos.x();
        this.y = pos.y();
        this.z = pos.z();
        return this;
    }

    public IVertexBuilder color(int p_225586_1_, int p_225586_2_, int p_225586_3_, int p_225586_4_) {
        return this;
    }

    public IVertexBuilder uv(float u, float v) {
        this.u = u;
        this.v = v;
        return this;
    }

    public IVertexBuilder overlayCoords(int p_225585_1_, int p_225585_2_) {
        return this;
    }

    public IVertexBuilder uv2(int p_225587_1_, int p_225587_2_) {
        return this;
    }

    public IVertexBuilder normal(float nx, float ny, float nz) {
        Vector3f normal = new Vector3f(nx, ny, nz);
        transform.transformNormal(normal);
        this.nx = normal.x();
        this.ny = normal.y();
        this.nz = normal.z();
        return this;
    }
}
