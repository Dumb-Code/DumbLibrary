package net.dumbcode.dumblibrary.server.guidebooks;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.guidebooks.elements.GuidebookElement;
import net.dumbcode.dumblibrary.server.guidebooks.elements.MissingPageElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

public class GuidebookPage {

    public static final GuidebookPage MISSING_PAGE = new GuidebookPage() {
        {
            this.setCoverPage(false);
            this.getElements().add(new MissingPageElement());
        }
    };
    @Getter
    private List<GuidebookElement> elements = new LinkedList<>();

    @Getter
    @Setter
    private boolean isCoverPage;

    @Expose(deserialize = false, serialize = false)
    private int compiledRenderTexture = -1;

    @SideOnly(Side.CLIENT)
    public int getCompiledRenderTexture(Guidebook guidebook) {
        if(compiledRenderTexture == -1)
            compiledRenderTexture = compilePageRender(guidebook);
        return compiledRenderTexture;
    }

    @SideOnly(Side.CLIENT)
    public int compilePageRender(Guidebook guidebook) {
        Framebuffer framebuffer = new Framebuffer(guidebook.getAvailableWidth(), guidebook.getAvailableHeight(), true);
        framebuffer.bindFramebuffer(true);
        if(isCoverPage) {
            GlStateManager.clearColor(1f, 1f, 1f, 0f);
        } else {
            GlStateManager.clearColor(1f, 1f, 1f, 1f);
        }
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        compileAndRender(guidebook);
        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
        // can't (because we are using its color texture) : framebuffer.deleteFramebuffer();

        return framebuffer.framebufferTexture;
    }

    @SideOnly(Side.CLIENT)
    public void compileAndRender(Guidebook guidebook) {
        FloatBuffer projectionMatrix = BufferUtils.createFloatBuffer(16);
        FloatBuffer modelMatrix = BufferUtils.createFloatBuffer(16);
        projectionMatrix.rewind();
        GlStateManager.getFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
        modelMatrix.rewind();
        GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, modelMatrix);

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0, guidebook.getAvailableWidth(), guidebook.getAvailableHeight(), 0, -1000f, 1000f);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();

        GlStateManager.pushMatrix();
        if(elements != null) {
            int y = 0;
            for(GuidebookElement element : elements) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GlStateManager.enableCull();
                GlStateManager.enableDepth();
                GlStateManager.disableLighting();
                element.render(guidebook);
                GlStateManager.popMatrix();
                int height = element.getHeight(guidebook);
                y += height;
                GlStateManager.translate(0f, height, 0f);
            }
        }
        GlStateManager.popMatrix();

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        projectionMatrix.rewind();
        GlStateManager.multMatrix(projectionMatrix);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        modelMatrix.rewind();
        GlStateManager.multMatrix(modelMatrix);
    }
}
