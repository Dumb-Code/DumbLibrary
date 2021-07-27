package net.dumbcode.dumblibrary.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Deque;

public class StencilStack {
    private static final Deque<Entry> renders = new ArrayDeque<>();

    public static void pushSquareStencil(MatrixStack stack, int left, int top, int right, int bottom) {
        pushSquareStencil(stack, left, top, right, bottom, Type.AND);
    }

    public static void pushSquareStencil(MatrixStack stack, int left, int top, int right, int bottom, Type type) {
        MatrixStack.Entry last = stack.last();
        MatrixStack clone = new MatrixStack();
        MatrixStack.Entry clonedLast = clone.last();
        clonedLast.normal().load(last.normal());
        clonedLast.pose().set(last.pose());
        pushStencil(() ->  AbstractGui.fill(clone, left, top, right, bottom, -1), type);
    }

    public static void pushStencil(Runnable renderCallback, Type type) {
        if(renders.isEmpty()) {
            if(!Minecraft.getInstance().getMainRenderTarget().isStencilEnabled()) {
                Minecraft.getInstance().getMainRenderTarget().enableStencil();
            }
            GL11.glEnable(GL11.GL_STENCIL_TEST);
        } else if(renders.size() > 500) {
            DumbLibrary.getLogger().warn("Stencil Leak", new Exception());
        }
        renders.push(new Entry(renderCallback, type));
        recompute();
    }

    public static void popStencil() {
        renders.pop();
        if(renders.isEmpty()) {
            GL11.glDisable(GL11.GL_STENCIL_TEST);
        } else {
            recompute();
        }
    }

    private static void recompute() {
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);

        GL11.glStencilMask(0xFF);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        int level = 0;
        for (Entry entry : renders) {
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
            entry.runnable.run();
        }

        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glStencilMask(0x00);

        GL11.glStencilFunc(GL11.GL_EQUAL, level, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }

    @AllArgsConstructor
    private static class Entry {
        Runnable runnable;
        Type type;
    }

    @RequiredArgsConstructor
    public enum Type {
        AND, OR, NOT;
    }

}
