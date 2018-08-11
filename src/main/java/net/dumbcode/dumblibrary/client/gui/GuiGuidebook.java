package net.dumbcode.dumblibrary.client.gui;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.guidebooks.Guidebook;
import net.dumbcode.dumblibrary.server.guidebooks.GuidebookPage;
import net.ilexiconn.llibrary.client.model.tabula.ITabulaModelAnimator;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

public class GuiGuidebook extends GuiScreen {

    private final Guidebook bookData;
    private final List<GuidebookPage> allPages;
    private String titleText;
    private float bookOpeness = 0f;

    private ModelBook modelBook = new ModelBook();
    private static final ResourceLocation TEXTURE_BOOK = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private float flippingProgress;
    private int flippingDirection;

    private int pageOnLeftIndex = 1; // 0 is the cover page
    private GuidebookPage pageOnLeft;
    private GuidebookPage pageOnRight;
    private GuidebookPage flippingPageFront;
    private GuidebookPage flippingPageBack;

    public GuiGuidebook(Guidebook bookData) {
        this.bookData = bookData;
        flippingProgress = 2f;
        this.allPages = bookData.getCompiledPages();
        if(allPages.size() > 1)
            pageOnLeft = allPages.get(1);
        if(allPages.size() > 2)
            pageOnRight = allPages.get(2);
    }

    @Override
    public void initGui() {
        super.initGui();
        titleText = new TextComponentTranslation(bookData.getTitleKey()).getUnformattedText();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRenderer.drawString(titleText, 0, 0, 0xFFF0F0F0);


        GuiHelper.prepareModelRendering(width/2, height/2+20, 350f, 0f, bookOpeness*90f);
        GlStateManager.disableLighting();
        GlStateManager.scale(-1f, 1f, 1f);
        mc.getTextureManager().bindTexture(TEXTURE_BOOK);
        float openAngle = (float) ((Math.PI/2f)*bookOpeness);
        renderBook(openAngle);

        float flipAngle = 0f;
        if(flippingProgress < 1f) {
            if(flippingDirection > 0) {
                flipAngle = flippingProgress;
            } else {
                flipAngle = 1f-flippingProgress;
            }
        }

        renderCover(bookData.getCover().getCompiledRenderTexture(bookData));

        // render pages
        GlStateManager.rotate(bookOpeness*90f, 0f, -1f, 0f);
        GlStateManager.translate(0f, 0f, -0.001);
        GlStateManager.rotate(bookOpeness*90f, 0f, 1f, 0f);

        if(pageOnLeft != null) {
            // page on left
            renderPage((float) Math.toDegrees(modelBook.pagesLeft.rotateAngleY)-90f, -1, pageOnLeft.getCompiledRenderTexture(bookData));
        }

        if(pageOnRight != null) {
            // page on right
            renderPage((bookOpeness*90f-90f), pageOnRight.getCompiledRenderTexture(bookData), -1);
        }

        if(flippingProgress < 1f && bookOpeness >= 1f) {
            GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
            // flip page
            renderPage(180f*flipAngle, flippingPageFront.getCompiledRenderTexture(bookData), flippingPageBack.getCompiledRenderTexture(bookData));
        }
        GuiHelper.cleanupModelRendering();
    }

    private void renderBook(float angle) {
        modelBook.coverRight.rotateAngleY = (float)Math.PI + angle;
        modelBook.coverLeft.rotateAngleY = -angle;
        modelBook.pagesRight.rotateAngleY = angle;
        modelBook.pagesLeft.rotateAngleY = -angle;
        modelBook.flippingPageRight.rotateAngleY = angle;
        modelBook.flippingPageLeft.rotateAngleY = angle;
        modelBook.pagesRight.rotationPointX = 0f;
        modelBook.pagesLeft.rotationPointX = 0f;
        modelBook.flippingPageLeft.rotationPointX = 0f;
        modelBook.flippingPageRight.rotationPointX = 0f;
        for(ModelRenderer part : modelBook.boxList) {
            part.render(1f/16f);
        }
    }

    private void renderPage(float pageAngle, int frontFaceTexture, int backFaceTexture) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        float w = 5f/16f;
        float h = 8f/16f;
        float x = 0f;
        float y = -h/2f;
        float z = 0f;
        GlStateManager.pushMatrix();

        GlStateManager.rotate(pageAngle+90f, 0f, 1f, 0f);

        if(frontFaceTexture != -1) {
            GlStateManager.bindTexture(frontFaceTexture);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            // front face
            buffer.pos(x, y+h, z).tex(0, 0).endVertex();
            buffer.pos(x+w, y+h, z).tex(1, 0).endVertex();
            buffer.pos(x+w, y, z).tex(1, 1).endVertex();
            buffer.pos(x, y, z).tex(0, 1).endVertex();
            tessellator.draw();
        }
        if(backFaceTexture != -1) {
            GlStateManager.bindTexture(backFaceTexture);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            // back face
            buffer.pos(x, y, z).tex(1, 1).endVertex();
            buffer.pos(x+w, y, z).tex(0, 1).endVertex();
            buffer.pos(x+w, y+h, z).tex(0, 0).endVertex();
            buffer.pos(x, y+h, z).tex(1, 0).endVertex();
            tessellator.draw();
        }
        GlStateManager.popMatrix();
    }

    private void renderCover(int frontFaceTexture) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        float w = 6f/16f;
        float h = 10f/16f;
        float x = 0f;
        float y = -h/2f;
        float z = 1f/16f;
        GlStateManager.pushMatrix();

        GlStateManager.rotate((float) Math.toDegrees(modelBook.coverLeft.rotateAngleY), 0f, 1f, 0f);

        GlStateManager.bindTexture(frontFaceTexture);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        // front face
        buffer.pos(x, y+h, z).tex(0, 0).endVertex();
        buffer.pos(x+w, y+h, z).tex(1, 0).endVertex();
        buffer.pos(x+w, y, z).tex(1, 1).endVertex();
        buffer.pos(x, y, z).tex(0, 1).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int sign = -(int)Math.signum(Mouse.getDWheel());
        if(sign != 0) {
            if(allPages.size() > pageOnLeftIndex+sign*2 && pageOnLeftIndex+sign*2 > 0) {
                flippingProgress = 0f;
                flippingDirection = sign;

                pageOnLeftIndex += sign*2;
                if(sign > 0) {
                    flippingPageFront = pageOnRight;
                    flippingPageBack = allPages.get(pageOnLeftIndex);
                    if(pageOnLeftIndex+1 < allPages.size())
                        pageOnRight = allPages.get(pageOnLeftIndex+1);
                    else
                        pageOnRight = null;
                } else {
                    flippingPageFront = allPages.get(pageOnLeftIndex+1);
                    flippingPageBack = pageOnLeft;
                    pageOnLeft = allPages.get(pageOnLeftIndex);
                }
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        bookOpeness += 1f/20f /2f;
        if(bookOpeness >= 1f)
            bookOpeness = 1f;
        if(flippingProgress < 1f) {
            flippingProgress += 1f/20f;
            if(flippingProgress >= 1f) { // flip done
                if(flippingDirection > 0)
                    pageOnLeft = flippingPageBack;
                else
                    pageOnRight = flippingPageFront;
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
