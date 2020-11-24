package net.dumbcode.dumblibrary.client;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

public class StencilStack {
    private static final FloatBuffer MODEL_MATRIX = BufferUtils.createFloatBuffer(16);
    private static final Deque<Entry> RENDERS = new ArrayDeque<>();

    public static void pushSquareStencil(int left, int top, int right, int bottom) {
        pushStencil(() ->  Gui.drawRect(left, top, right, bottom, -1), Type.AND);
    }

    public static void pushStencil(Runnable renderCallback, Type type) {
        if(RENDERS.isEmpty()) {
            if(!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled()) {
                Minecraft.getMinecraft().getFramebuffer().enableStencil();
            }
            GL11.glEnable(GL11.GL_STENCIL_TEST);
        } else if(RENDERS.size() > 500) {
            DumbLibrary.getLogger().warn("Stencil Leak", new Exception());
        }

        MODEL_MATRIX.rewind();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODEL_MATRIX);
        MODEL_MATRIX.rewind();

        float[] matrix = new float[16];
        for (int i = 0; i < 16; i++) {
            matrix[i] = MODEL_MATRIX.get();
        }
        RENDERS.push(new Entry(renderCallback, type, matrix));
        recompute();
    }

    public static void popStencil() {
        RENDERS.pop();
        if(RENDERS.isEmpty()) {
            GL11.glDisable(GL11.GL_STENCIL_TEST);
        } else {
            recompute();
        }
    }

    private static void recompute() {
        GlStateManager.pushMatrix();
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);

        GL11.glStencilMask(0xFF);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        int level = 0;
        for (Entry entry : RENDERS) {
            switch (entry.type) {
                case AND:
                    level++;
                    GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
                    GL11.glStencilOp(GL11.GL_INCR, GL11.GL_INCR, GL11.GL_INCR);
                    break;
                case OR:
                    if(level == 0) {
                        level = 1;
                    }
                    GL11.glStencilFunc(GL11.GL_ALWAYS, level, 0xFF);
                    GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
                    break;
                case NOT:
                    GL11.glStencilFunc(GL11.GL_ALWAYS, 1000, 0xFF);
                    GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
                    break;

            }

            MODEL_MATRIX.rewind();
            MODEL_MATRIX.put(entry.matrix);
            MODEL_MATRIX.rewind();
            GL11.glLoadMatrix(MODEL_MATRIX);

            entry.runnable.run();
        }

        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glStencilMask(0x00);

        GL11.glStencilFunc(GL11.GL_EQUAL, level, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GlStateManager.popMatrix();
    }

    @AllArgsConstructor
    private static class Entry {
        Runnable runnable;
        Type type;
        float[] matrix;
    }

    @RequiredArgsConstructor
    public enum Type {
        AND, OR, NOT;
    }

}
