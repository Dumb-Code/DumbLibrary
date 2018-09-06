package net.dumbcode.dumblibrary.client.gui;

import javafx.scene.transform.Scale;
import net.dumbcode.dumblibrary.server.guidebooks.Guidebook;
import net.dumbcode.dumblibrary.server.guidebooks.GuidebookPage;
import net.dumbcode.dumblibrary.server.guidebooks.elements.GuidebookElement;
import net.dumbcode.dumblibrary.server.guidebooks.functions.GuidebookFunction;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import javax.vecmath.Vector3d;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuiGuidebook extends GuiScreen {

    private final Guidebook bookData;
    private final List<GuidebookPage> allPages;
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

    private FloatBuffer projectionMatrix = BufferUtils.createFloatBuffer(16);
    private FloatBuffer modelviewMatrix = BufferUtils.createFloatBuffer(16);
    // curl parameters
    private int columns = 5*5;
    private int rows = 8*5;
    private Vector3d pos = new Vector3d();
    private Vector3d[] positions = new Vector3d[(rows+1)*(columns+1)];  // includes far edges
    private int pageMouseX;
    private int pageMouseY;

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
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

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
            renderPage(false, (float) Math.toDegrees(modelBook.pagesLeft.rotateAngleY)-90f, 0f, -1, pageOnLeft.getCompiledRenderTexture(bookData));
        }

        if(pageOnRight != null) {
            // page on right
            renderPage(false, (bookOpeness*90f-90f), 1f, pageOnRight.getCompiledRenderTexture(bookData), -1);
        }

        if(flippingProgress < 1f && bookOpeness >= 1f) {
            GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
            // flip page
            renderPage(true, 180f, flipAngle, flippingPageFront.getCompiledRenderTexture(bookData), flippingPageBack.getCompiledRenderTexture(bookData));
        }


        GlStateManager.pushMatrix();
        GlStateManager.rotate(90f, 0f, 1f, 0f);
        projectionMatrix.rewind();
        modelviewMatrix.rewind();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelviewMatrix);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
        projectionMatrix.rewind();
        modelviewMatrix.rewind();
        IntBuffer viewport = BufferUtils.createIntBuffer(4);
        viewport.put(0).put(0).put(Display.getWidth()).put(Display.getHeight());
        viewport.flip();

        FloatBuffer objPos = BufferUtils.createFloatBuffer(3);
        Project.gluProject(5f/16f, 4f/16f, 500, modelviewMatrix, projectionMatrix, viewport, objPos);
        int bookRight = (int) (objPos.get(0) / Display.getWidth() * width);
        int bookTop = (int) (objPos.get(1) / Display.getHeight() * height);
        Project.gluProject(-5f/16f, -4f/16f, 500, modelviewMatrix, projectionMatrix, viewport, objPos);
        int bookLeft = (int) (objPos.get(0) / Display.getWidth() * width);
        int bookBottom = (int) (objPos.get(1) / Display.getHeight() * height);

        GlStateManager.popMatrix();

        GuiHelper.cleanupModelRendering();
        int bookHeight = bookBottom-bookTop;
        int bookMiddle = (bookRight+bookLeft)/2;
        int pageRenderWidth = (bookRight-bookLeft)/2;
        pageMouseY = (int) ((mouseY-bookTop) / (float)bookHeight * bookData.getAvailableHeight());
        pageMouseX = (int) ((mouseX-bookMiddle) / (float)pageRenderWidth * bookData.getPageWidth());
        if(pageOnRight != null) { // TODO: do it for both pages: indev only
            Optional<GuidebookElement> hovered = pageOnRight.getHoveredElement(bookData, pageMouseX, pageMouseY);
            if(hovered.isPresent()) {
                int componentX = (int)(pageMouseX-pageOnRight.getElementPositions().get(hovered.get()).x);
                int componentY = (int)(pageMouseY-pageOnRight.getElementPositions().get(hovered.get()).y);
                List<TextComponentBase> tooltip = hovered.get().getTooltipText(bookData, componentX, componentY);
                List<String> lines = tooltip.stream()
                        .map(TextComponentBase::getUnformattedText)
                        .collect(Collectors.toList());
                drawHoveringText(lines, mouseX, mouseY);
            }
        }
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

    private void renderPage(boolean curl, float pageAngle, float flipProgress, int frontFaceTexture, int backFaceTexture) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        float w = 5f/16f;
        float h = 8f/16f;
        float x = 0f;
        float y = -h/2f;

        if(frontFaceTexture != -1) {
            GlStateManager.bindTexture(frontFaceTexture);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(90f, 0f, 1f, 0f);
            GlStateManager.translate(0f, y, 0f);
            /*
            Adapted from https://wdnuon.blogspot.com/2010/05/implementing-ibooks-page-curling-using.html
            */
            if(curl) {
                drawCurledPage(pageAngle, flipProgress, columns, rows, w, h, false);
            } else {
                GlStateManager.rotate(pageAngle, 0f, 1f, 0f);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(0, h, 0).tex(0, 0).endVertex();
                buffer.pos(w, h, 0).tex(1, 0).endVertex();
                buffer.pos(w, 0, 0).tex(1, 1).endVertex();
                buffer.pos(0, 0, 0).tex(0, 1).endVertex();
                tessellator.draw();
            }
            GlStateManager.popMatrix();
        }
        if(backFaceTexture != -1) {
            GlStateManager.bindTexture(backFaceTexture);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(90f, 0f, 1f, 0f);
            GlStateManager.translate(0f, y, 0f);
            if(curl) {
                drawCurledPage(pageAngle, flipProgress, columns, rows, w, h, true);
            } else {
                GlStateManager.rotate(pageAngle, 0f, 1f, 0f);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(0, 0, 0).tex(1, 1).endVertex();
                buffer.pos(w, 0, 0).tex(0, 1).endVertex();
                buffer.pos(w, h, 0).tex(0, 0).endVertex();
                buffer.pos(0, h, 0).tex(1, 0).endVertex();
                tessellator.draw();
            }
            GlStateManager.popMatrix();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if(mouseButton == 0) {
            Optional<GuidebookElement> hovered = pageOnRight.getHoveredElement(bookData, pageMouseX, pageMouseY);
            if(hovered.isPresent()) {
                GuidebookFunction function = hovered.get().getOnClickFunction();
                if(function != null) {
                    function.onClick(this, pageMouseX, pageMouseY, mouseX, mouseY);
                }
            }
        }
    }

    private void drawCurledPage(double pageAngle, double flipProgress, int columns, int rows, double w, double h, boolean backFace) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        double theta;
        double A;
        double angle = pageAngle*flipProgress;
        double rho = Math.toRadians(angle);

        // Comments are from the source code provided on https://wdnuon.blogspot.com/2010/05/implementing-ibooks-page-curling-using.html
        // Behaviour has been reimplemented from scratch but use the ~same empirical values
        double A1 = -15.0;
        double A2 = -2.0;
        double A3 = -4.0;

        double theta1 = Math.toRadians(90);
        double theta2 = Math.toRadians(10);
        double theta3 = Math.toRadians(5);
        double phase1 = 0.15;
        double phase2 = 0.4;
        double phase3 = 1.0;

        if(flipProgress <= phase1) {
            double progress = flipProgress / phase1;
            double f1 = Math.sin(Math.PI * Math.pow(progress, 0.05) / 2.0);
            double f2 = Math.sin(Math.PI * Math.pow(progress, 0.5) / 2.0);
            A = linear(f1, A1, A2);
            theta = linear(f2, theta1, theta2);
        } else if(flipProgress <= phase2) {
            double progress = (flipProgress-phase1) / (phase2-phase1);
            A = linear(progress, A2, A3);
            theta = linear(progress, theta2, theta3);
        } else {
            double progress = (flipProgress-phase2) / (phase3-phase2);
            double f1 = Math.sin(Math.PI * Math.pow(progress, 10) / 2.0);
            double f2 = Math.sin(Math.PI * Math.pow(progress, 2) / 2.0);
            A = linear(f1, A3, A1);
            theta = linear(f2, theta3, theta1);
        }

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        for (int i = 0; i < positions.length; i++) {
            positions[i] = null;
        }
        for (int i = 0; i < rows*columns; i++) {
            int x = i % columns;
            int y = i / columns;
            double minU = (x) / (double)columns;
            double minV = (y) / (double)rows;
            double maxU = (x+1) / (double)columns;
            double maxV = (y+1) / (double)rows;

            double xInput = minU * w;
            double yInput = minV * h;
            getOrComputeCurlPosition(x, y, xInput, yInput, theta, rho, A, pos);
            double topLeftX = pos.x;
            double topLeftY = pos.y;
            double topLeftZ = pos.z;

            xInput = maxU * w;
            yInput = minV * h;
            getOrComputeCurlPosition(x+1, y, xInput, yInput, theta, rho, A, pos);
            double topRightX = pos.x;
            double topRightY = pos.y;
            double topRightZ = pos.z;

            xInput = minU * w;
            yInput = maxV * h;
            getOrComputeCurlPosition(x, y+1, xInput, yInput, theta, rho, A, pos);
            double bottomLeftX = pos.x;
            double bottomLeftY = pos.y;
            double bottomLeftZ = pos.z;

            xInput = maxU * w;
            yInput = maxV * h;
            getOrComputeCurlPosition(x+1, y+1, xInput, yInput, theta, rho, A, pos);
            double bottomRightX = pos.x;
            double bottomRightY = pos.y;
            double bottomRightZ = pos.z;
            if(backFace) {
                buffer.pos(topLeftX, topLeftY, topLeftZ).tex(1.0-minU, 1f-minV).endVertex();
                buffer.pos(topRightX, topRightY, topRightZ).tex(1.0-maxU, 1f-minV).endVertex();
                buffer.pos(bottomRightX, bottomRightY, bottomRightZ).tex(1.0-maxU, 1f-maxV).endVertex();
                buffer.pos(bottomLeftX, bottomLeftY, bottomLeftZ).tex(1.0-minU, 1f-maxV).endVertex();
            } else {
                buffer.pos(bottomLeftX, bottomLeftY, bottomLeftZ).tex(minU, 1f-maxV).endVertex();
                buffer.pos(bottomRightX, bottomRightY, bottomRightZ).tex(maxU, 1f-maxV).endVertex();
                buffer.pos(topRightX, topRightY, topRightZ).tex(maxU, 1f-minV).endVertex();
                buffer.pos(topLeftX, topLeftY, topLeftZ).tex(minU, 1f-minV).endVertex();
            }
        }
        tessellator.draw();
    }

    private double linear(double progress, double min, double max) {
        return progress * max + (1.0-progress) * min;
    }

    private void getOrComputeCurlPosition(int x, int y, double xInput, double yInput, double theta, double rho, double A, Vector3d out) {
        int index = x+y*(columns+1);
        if(positions[index] == null) {
            calculateCurlPosition(xInput, yInput, theta, rho, A, out);
            positions[index] = new Vector3d(out);
        }
        out.set(positions[index]);
    }

    private void calculateCurlPosition(double xInput, double yInput, double theta, double rho, double A, Vector3d out) {
        double R = Math.sqrt(xInput * xInput + Math.pow(yInput - A, 2));
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);
        double r = R * sinTheta;
        double beta = Math.asin(xInput / R) / sinTheta;
        double cosBeta = Math.cos(beta);
        double sinBeta = Math.sin(beta);
        double cosRho = Math.cos(rho);
        double sinRho = Math.sin(rho);

        double outX = r * sinBeta;
        double outY = R + A - r * (1 - cosBeta) * sinBeta;
        double outZ = r * (1 - Math.cos(beta)) * cosTheta;

        double finalX = outX * cosRho - outZ * sinRho;
        double finalY = outY;
        double finalZ = outX * sinRho + outZ * cosRho;

        out.set(finalX, finalY, finalZ);
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
            turnPage(sign);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if(Keyboard.isKeyDown(Keyboard.KEY_F3) && Keyboard.isKeyDown(Keyboard.KEY_R)) {
            // recompile
            bookData.recompile();
        }

        bookData.update();

        if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            turnPage(-1);
        } else if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            turnPage(1);
        }
        bookOpeness += 1f/20f /2f;
        if(bookOpeness >= 1f)
            bookOpeness = 1f;
        if(flippingProgress < 1f) {
            flippingProgress += 1f/20f*2f;
            if(flippingProgress >= 1f) { // flip done
                if(flippingDirection > 0)
                    pageOnLeft = flippingPageBack;
                else
                    pageOnRight = flippingPageFront;
            }
        }
    }

    /**
     * returns true if the page started being turned
     * @param pageDiff
     * @return
     */
    private boolean turnPage(int pageDiff) {
        if(flippingProgress < 1f)
            return false;
        if(!(allPages.size() > pageOnLeftIndex+pageDiff*2 && pageOnLeftIndex+pageDiff*2 > 0)) {
            return false;
        }
        flippingProgress = 0f;
        flippingDirection = (int) Math.signum(pageDiff);

        pageOnLeftIndex += pageDiff*2;
        if(pageDiff > 0) {
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
        return true;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        super.handleKeyboardInput();
    }

    public Guidebook getBook() {
        return bookData;
    }

    public void showPage(GuidebookPage page) {
        int index = allPages.indexOf(page);
        if(index < 0)
            throw new IllegalArgumentException("The specified page is not in the book!");
        if(index == pageOnLeftIndex || index == pageOnLeftIndex+1) // we might already be on the page
            return;
        int leftIndex = index;
        turnPage((leftIndex - pageOnLeftIndex)/2);
    }
}
