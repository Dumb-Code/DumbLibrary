package net.dumbcode.dumblibrary.client.model;

import com.mojang.blaze3d.matrix.GuiGraphics;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Vector4f;

import java.util.Arrays;

public interface TransformableModel {
    IBakedModel transform(GuiGraphics stack);

    static BakedQuad transformQuad(BakedQuad quad, GuiGraphics stack) {
        int[] vertices = quad.getVertices();
        int[] newVerts = Arrays.copyOf(quad.getVertices(), quad.getVertices().length);
        int size = DefaultVertexFormats.BLOCK.getIntegerSize();
        for (int v = 0; v < 4; v++) {
            float x = Float.intBitsToFloat(vertices[v*size]);
            float y = Float.intBitsToFloat(vertices[v*size+1]);
            float z = Float.intBitsToFloat(vertices[v*size+2]);

            Vector4f vec = new Vector4f(x, y, z, 1F);
            vec.transform(stack.last().pose());

            newVerts[v*size] = Float.floatToRawIntBits(vec.x());
            newVerts[v*size+1] = Float.floatToRawIntBits(vec.y());
            newVerts[v*size+2] = Float.floatToRawIntBits(vec.z());
        }
        return new BakedQuad(newVerts, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());

    }
}
