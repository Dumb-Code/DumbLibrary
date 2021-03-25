package net.dumbcode.dumblibrary.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Deque;

public class StencilStack {
    private static final Deque<Entry> renders = new ArrayDeque<>();

    public static void pushSquareStencil(MatrixStack stack, int left, int top, int right, int bottom) {
        pushStencil(() ->  AbstractGui.fill(stack, left, top, right, bottom, -1), Type.AND);
    }

    public static void pushStencil(Runnable renderCallback, Type type) {
        if(renders.isEmpty()) {
            if(!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled()) {
                Minecraft.getMinecraft().getFramebuffer().enableStencil();
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
