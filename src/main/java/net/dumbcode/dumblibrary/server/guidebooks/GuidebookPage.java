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

import javax.vecmath.Vector2f;
import java.nio.FloatBuffer;
import java.util.*;

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

    @Expose(deserialize = false, serialize = false)
    private Map<GuidebookElement, Vector2f> elementPositions = new HashMap<>();

    @Expose(deserialize = false, serialize = false)
    private FloatBuffer projectionMatrix = BufferUtils.createFloatBuffer(16);

    @Expose(deserialize = false, serialize = false)
    private FloatBuffer modelMatrix = BufferUtils.createFloatBuffer(16);

    @Expose(deserialize = false, serialize = false)
    private Framebuffer framebuffer;

    @SideOnly(Side.CLIENT)
    public int getCompiledRenderTexture(Guidebook guidebook) {
        if(compiledRenderTexture == -1)
            compiledRenderTexture = compilePageRender(guidebook);
        return compiledRenderTexture;
    }

    @SideOnly(Side.CLIENT)
    public int compilePageRender(Guidebook guidebook) {
        if(framebuffer == null)
            framebuffer = new Framebuffer(guidebook.getPageWidth(), guidebook.getAvailableHeight(), true);
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
        projectionMatrix.rewind();
        GlStateManager.getFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
        modelMatrix.rewind();
        GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, modelMatrix);

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0, guidebook.getPageWidth(), guidebook.getAvailableHeight(), 0, -1000f, 1000f);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();

        GlStateManager.pushMatrix();
        GlStateManager.translate(guidebook.getPageMargins(), 0f, 0f);
        if(elements != null) {
            int y = 0;
            elementPositions.clear();
            for(GuidebookElement element : elements) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GlStateManager.enableCull();
                GlStateManager.enableDepth();
                GlStateManager.disableLighting();
                element.render(guidebook);
                GlStateManager.popMatrix();
                elementPositions.put(element, new Vector2f(guidebook.getPageMargins(), y));
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

    public Optional<GuidebookElement> getHoveredElement(Guidebook guidebook, int pageMouseX, int pageMouseY) {
        return elements.stream().filter(elem -> {
            Vector2f pos = elementPositions.get(elem);
            int x = (int) pos.x;
            int y = (int) pos.y;
            return elem.isMouseOn(guidebook, x, y, pageMouseX, pageMouseY);
        }).findFirst();
    }

    public void recompile(Guidebook guidebook) {
        compiledRenderTexture = -1;
    }
}
