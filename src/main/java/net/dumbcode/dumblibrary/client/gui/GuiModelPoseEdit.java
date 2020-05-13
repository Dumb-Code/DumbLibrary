package net.dumbcode.dumblibrary.client.gui;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.dumbcode.dumblibrary.server.utils.XYZAxis;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.Project;
import org.lwjgl.util.vector.Vector2f;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;


public abstract class GuiModelPoseEdit extends GuiScreen {

    private final TabulaModel model;
    private final ResourceLocation texture;
    private final ITextComponent titleText;
    private boolean registeredLeftClick;
    private Vector2f lastClickPosition = new Vector2f();
    private IntBuffer colorBuffer = BufferUtils.createIntBuffer(1);
    private TabulaModelRenderer selectedPart;
    private boolean movedPart;
    private TextComponentTranslation undoText = new TextComponentTranslation(DumbLibrary.MODID+".gui.model_pose_edit.undo");
    private TextComponentTranslation redoText = new TextComponentTranslation(DumbLibrary.MODID+".gui.model_pose_edit.redo");
    private TextComponentTranslation resetText = new TextComponentTranslation(DumbLibrary.MODID+".gui.model_pose_edit.reset");
    private TextComponentTranslation propertiesGui = new TextComponentTranslation(DumbLibrary.MODID+".gui.model_pose_edit.edit_properties");
    private GuiButton undoButton = new GuiButtonExt(0, 0, 0, undoText.getUnformattedText());
    private GuiButton redoButton = new GuiButtonExt(1, 0, 0, redoText.getUnformattedText());
    private GuiButton resetButton = new GuiButtonExt(2, 0, 0, resetText.getUnformattedText());
    private GuiButton exportButton = new GuiButtonExt(3, 0, 0, 20, 20, "E");
    private GuiButton importButton = new GuiButtonExt(4, 20, 0, 20, 20, "I");
    protected float cameraPitch;
    protected float cameraYaw = 90f;
    protected double zoom = 1.0;
    private TabulaModel rotationRingModel;
    private XYZAxis currentSelectedRing = XYZAxis.NONE;
    private boolean draggingRing = false;
    private Vector2f dMouse = new Vector2f();

    private FloatBuffer modelMatrix = BufferUtils.createFloatBuffer(16);
    private FloatBuffer projectionMatrix = BufferUtils.createFloatBuffer(16);
    private IntBuffer viewport = BufferUtils.createIntBuffer(4);

    /**
     * Base Y component for control text, selected part text & sliders
     */
    private int baseYOffset = 20;

    private TextComponentTranslation noPartSelectedText = new TextComponentTranslation(DumbLibrary.MODID+".gui.model_pose_edit.no_part_selected");
    private TextComponentTranslation zoomText = new TextComponentTranslation(DumbLibrary.MODID+".gui.model_pose_edit.controls.zoom");
    private TextComponentTranslation selectModelPartText = new TextComponentTranslation(DumbLibrary.MODID+".gui.model_pose_edit.controls.select_part");
    private TextComponentTranslation rotateCameraText = new TextComponentTranslation(DumbLibrary.MODID+".gui.model_pose_edit.controls.rotate_camera");

//    private DialogBox dialogBox = new DialogBox()
//            .root(new File(Minecraft.getMinecraft().gameDir, "dinosaur_poses"))
//            .extension("ProjectNublar Dinosaur Pose (.dpose)", true, "*.dpose");

    private double prevXSlider;
    private double prevYSlider;
    private double prevZSlider;

    public static final String PREFIX_KEY = DumbLibrary.MODID + ".gui.model_pose_edit.rotation_slider.prefix";
    public static final String SUFFIX_KEY = DumbLibrary.MODID + ".gui.model_pose_edit.rotation_slider.suffix";

    private GuiSlider xRotationSlider = new GuiSlider(5, 0, 0, 200, 20,
            new TextComponentTranslation(PREFIX_KEY, "X").getUnformattedText(),
            new TextComponentTranslation(SUFFIX_KEY).getUnformattedText(),
            -180.0, 180.0, 0.0, true, true);

    private GuiSlider yRotationSlider = new GuiSlider(6, 0, 0, 200, 20,
            new TextComponentTranslation(PREFIX_KEY, "Y").getUnformattedText(),
            new TextComponentTranslation(SUFFIX_KEY).getUnformattedText(),
            -180.0, 180.0, 0.0, true, true);

    private GuiSlider zRotationSlider = new GuiSlider(7, 0, 0, 200, 20,
            new TextComponentTranslation(PREFIX_KEY, "Z").getUnformattedText(),
            new TextComponentTranslation(SUFFIX_KEY).getUnformattedText(),
            -180.0, 180.0, 0.0, true, true);

    private GuiNumberEntry xPosition;
    private GuiNumberEntry yPosition;
    private GuiNumberEntry zPosition;

    private GuiNumberEntry mouseOverEntry;


    private GuiButton propertiesButton = new GuiButtonExt(8, 0, 0, propertiesGui.getUnformattedText());

    public GuiModelPoseEdit(TabulaModel model, ResourceLocation texture, ITextComponent title) {
        this.model = model; // TODO: child models? -> Selectable
        this.texture = texture;
        this.titleText = title;
        this.rotationRingModel = TabulaUtils.getModel(GuiConstants.ROTATION_RING_LOCATION);
        this.cameraPitch = cameraPitch;
        this.cameraYaw = cameraYaw;
        this.zoom = zoom;
    }

    @Override
    public void initGui() {
        super.initGui();
        int buttonWidth = width/3;
        undoButton.x = 0;
        redoButton.x = buttonWidth;
        resetButton.x = buttonWidth*2;

        undoButton.width = buttonWidth;
        redoButton.width = buttonWidth;
        resetButton.width = buttonWidth;

        undoButton.y = height-undoButton.height-1;
        redoButton.y = height-redoButton.height-1;
        resetButton.y = height-resetButton.height-1;

        xRotationSlider.x = width-xRotationSlider.width-1;
        yRotationSlider.x = width-yRotationSlider.width-1;
        zRotationSlider.x = width-zRotationSlider.width-1;

        propertiesButton.x = width-propertiesButton.width-1;

        // + height+2 to leave space for the text
        xRotationSlider.y = baseYOffset+fontRenderer.FONT_HEIGHT+2;
        yRotationSlider.y = baseYOffset+fontRenderer.FONT_HEIGHT+2+xRotationSlider.height+5;
        zRotationSlider.y = baseYOffset+fontRenderer.FONT_HEIGHT+2+xRotationSlider.height+yRotationSlider.height+5+5;

        propertiesButton.y = resetButton.y-2-propertiesButton.height;

        xRotationSlider.setValue(180.0);
        yRotationSlider.setValue(180.0);
        zRotationSlider.setValue(180.0);

        addButton(undoButton);
        addButton(redoButton);
        addButton(resetButton);
        addButton(xRotationSlider);
        addButton(yRotationSlider);
        addButton(zRotationSlider);
        addButton(propertiesButton);
        addButton(exportButton);
        addButton(importButton);

        xPosition = new GuiNumberEntry(
            0, 0, 1/4F, 2, width - 168, zRotationSlider.y+zRotationSlider.height+15,
            66, 20, this::addButton, this::positionChanged);

        yPosition = new GuiNumberEntry(
            1, 0, 1/4F, 2, width - 101, zRotationSlider.y+zRotationSlider.height+15,
            66, 20, this::addButton, this::positionChanged);

        zPosition = new GuiNumberEntry(
            2, 0, 1/4F, 2, width - 34, zRotationSlider.y+zRotationSlider.height+15,
            66, 20, this::addButton, this::positionChanged);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if(button == undoButton) {
            this.undo();
        } else if(button == redoButton) {
            this.redo();
        } else if(button == resetButton) {
            this.reset();
        } else if(button == exportButton) {
            this.exportPose();
//            this.dialogBox
//                    .title("Export pose")
//                    .showBox(DialogBox.Type.SAVE, file -> SkeletalBuilderFileHandler.serialize(new SkeletalBuilderFileInfomation(this.getDinosaur().getRegName(), this.builder.getPoseData()), file));
//            this.mc.displayGuiScreen(new GuiFileExplorer(this, "dinosaur poses", "Export", file -> SkeletalBuilderFileHandler.serilize(new SkeletalBuilderFileInfomation(this.getDinosaur().getRegName(), this.builder.getPoseData()), file))); //TODO: localize
        } else if(button == importButton) {
            this.importPose();
//            this.mc.displayGuiScreen(new GuiFileExplorer(this, "dinosaur poses", "Import", file -> ProjectNublar.NETWORK.sendToServer(new C8FullPoseChange(this.builder, SkeletalBuilderFileHandler.deserilize(file).getPoseData())))); //TODO: localize
        }
//        else if(button == propertiesButton) {
//            this.mc.displayGuiScreen(new GuiSkeletalProperties(this, this.builder));
//        }
        xPosition.buttonClicked(button);
        yPosition.buttonClicked(button);
        zPosition.buttonClicked(button);
    }

    protected abstract void undo();
    protected abstract void redo();
    protected abstract void reset();
    protected abstract void exportPose();
    protected abstract void importPose();
    protected abstract TaxidermyHistory getHistory();
    protected abstract void actualizeRotation(TabulaModelRenderer part, XYZAxis axis, float amount);
    protected abstract void actualizePosition(TabulaModelRenderer part, XYZAxis axis, float amount);
    protected abstract void actualizeEdit(TabulaModelRenderer part);
    protected abstract Map<String, TaxidermyHistory.CubeProps> getPoseData();

    public void positionChanged(GuiNumberEntry entry, int id) {
        if(selectedPart != null) {
            actualizePosition(selectedPart, XYZAxis.values()[id], (float) entry.getValue());
        }
    }

    public void sliderChanged(GuiSlider slider) {
        if(currentSelectedRing != XYZAxis.NONE)
            return;
        if(selectedPart == null)
            return;
        XYZAxis axis;
        if(slider == xRotationSlider) {
            axis = XYZAxis.X_AXIS;
        } else if(slider == yRotationSlider) {
            axis = XYZAxis.Y_AXIS;
        } else {
            axis = XYZAxis.Z_AXIS;
        }
        actualizeRotation(selectedPart, axis, (float)Math.toRadians(slider.getValue()));
    }

    @Override
    public void updateScreen() {
//        if(this.dialogBox.isOpen()) {
//            return;
//        }
        xPosition.updateEntry();
        yPosition.updateEntry();
        zPosition.updateEntry();
        super.updateScreen();
        undoButton.enabled = getHistory().canUndo();
        redoButton.enabled = getHistory().canRedo();

        xRotationSlider.enabled = selectedPart != null;
        yRotationSlider.enabled = selectedPart != null;
        zRotationSlider.enabled = selectedPart != null;
        xRotationSlider.updateSlider();
        yRotationSlider.updateSlider();
        zRotationSlider.updateSlider();

        if(xRotationSlider.getValue() != prevXSlider) {
            sliderChanged(xRotationSlider);
            prevXSlider = xRotationSlider.getValue();
        }
        if(yRotationSlider.getValue() != prevYSlider) {
            sliderChanged(yRotationSlider);
            prevYSlider = yRotationSlider.getValue();
        }
        if(zRotationSlider.getValue() != prevZSlider) {
            sliderChanged(zRotationSlider);
            prevZSlider = zRotationSlider.getValue();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawBackground(int tint) {
        drawDefaultBackground();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
//        if(this.dialogBox.isOpen()) {
//            mouseX = mouseY = -1;
//        }

        GuiNumberEntry mouseOver = null;
        if(xPosition.mouseOver(mouseX, mouseY)) {
            mouseOver = xPosition;
        } else if(yPosition.mouseOver(mouseX, mouseY)) {
            mouseOver = yPosition;
        } else if(zPosition.mouseOver(mouseX, mouseY)) {
            mouseOver = zPosition;
        }

        int scrollDirection = (int) Math.signum(Mouse.getDWheel());
        if(mouseOver == null) {
            final double zoomSpeed = 0.1;
            zoom += scrollDirection * zoomSpeed;
            if(zoom < zoomSpeed)
                zoom = zoomSpeed;
        }

        if(selectedPart != null && mouseOverEntry != null && !mouseOverEntry.isSyncedSinceEdit() && (mouseOver != mouseOverEntry || mouseOverEntry.getTicksSinceChanged() > 20)) {
            actualizeEdit(selectedPart);
            mouseOverEntry.setSyncedSinceEdit(true);
        }
        mouseOverEntry = mouseOver;

        drawBackground(0);

        GlStateManager.pushMatrix();
        // ensures that the buttons show above the model
        GlStateManager.translate(0f, 0f, 1000f);
        super.drawScreen(mouseX, mouseY, partialTicks);
        TaxidermyHistory history = getHistory();
        drawCenteredString(fontRenderer, (history.getIndex()+1)+"/"+history.getSize(), width/2, height-redoButton.height-fontRenderer.FONT_HEIGHT, GuiConstants.NICE_WHITE);
        drawCenteredString(fontRenderer, titleText.getUnformattedText(), width/2, 1, GuiConstants.NICE_WHITE);

        int yOffset = baseYOffset;
        drawString(fontRenderer, TextFormatting.BOLD.toString()+TextFormatting.UNDERLINE.toString()+GuiConstants.CONTROLS_TEXT.getUnformattedText(), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 15;
        drawString(fontRenderer, TextFormatting.UNDERLINE.toString()+selectModelPartText.getUnformattedText(), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 12;
        drawString(fontRenderer, GuiConstants.LEFT_CLICK_TEXT.getUnformattedText(), 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(fontRenderer, TextFormatting.UNDERLINE.toString()+rotateCameraText.getUnformattedText(), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 12;
        drawString(fontRenderer, GuiConstants.MIDDLE_CLICK_DRAG_TEXT.getUnformattedText(), 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(fontRenderer, GuiConstants.MOVEMENT_KEYS_TEXT.getUnformattedText(), 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(fontRenderer, GuiConstants.ARROW_KEYS_TEXT.getUnformattedText(), 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(fontRenderer, TextFormatting.UNDERLINE.toString()+zoomText.getUnformattedText(), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 12;
        drawString(fontRenderer, GuiConstants.MOUSE_WHEEL_TEXT.getUnformattedText(), 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(fontRenderer, GuiConstants.TRACKPAD_ZOOM_TEXT.getUnformattedText(), 10, yOffset, GuiConstants.NICE_WHITE);

        String selectionText;
        if(selectedPart == null)
            selectionText = noPartSelectedText.getUnformattedText();
        else
            selectionText = new TextComponentTranslation(DumbLibrary.MODID+".gui.model_pose_edit.selected_part", selectedPart.boxName).getUnformattedText();
        drawCenteredString(fontRenderer, selectionText, xRotationSlider.x+xRotationSlider.width/2, baseYOffset, GuiConstants.NICE_WHITE);
        GlStateManager.popMatrix();

        setModelToPose();
        prepareModelRendering(width/8*3, height/2, 30f);
        XYZAxis ringBelowMouse = findRingBelowMouse();
        if(draggingRing) {
            if(ringBelowMouse != XYZAxis.NONE) {
                handleRingDrag(dMouse.x, dMouse.y);
            }
            dMouse.set(0f, 0f);
            draggingRing = false;
        }
        TabulaModelRenderer partBelowMouse = findPartBelowMouse();
        if(registeredLeftClick) {
            if(ringBelowMouse == XYZAxis.NONE) {
                this.selectedPart = partBelowMouse;
                this.currentSelectedRing = XYZAxis.NONE;
                if(selectedPart != null) {
                    xRotationSlider.setValue(MathHelper.wrapDegrees(Math.toDegrees(selectedPart.rotateAngleX)));
                    yRotationSlider.setValue(MathHelper.wrapDegrees(Math.toDegrees(selectedPart.rotateAngleY)));
                    zRotationSlider.setValue(MathHelper.wrapDegrees(Math.toDegrees(selectedPart.rotateAngleZ)));

                    xPosition.setValue(selectedPart.rotationPointX, false);
                    yPosition.setValue(selectedPart.rotationPointY, false);
                    zPosition.setValue(selectedPart.rotationPointZ, false);

                    prevXSlider = xRotationSlider.getValue();
                    prevYSlider = yRotationSlider.getValue();
                    prevZSlider = zRotationSlider.getValue();
                }
            } else {
                this.currentSelectedRing = ringBelowMouse;
            }
            registeredLeftClick = false;
        }
        actualModelRender(partBelowMouse);
        GuiHelper.cleanupModelRendering();

        xPosition.render();
        yPosition.render();
        zPosition.render();

        if(partBelowMouse != null) {
            drawHoveringText(partBelowMouse.boxName, mouseX, mouseY);
        }
    }

    private XYZAxis findRingBelowMouse() {
        if(selectedPart == null)
            return XYZAxis.NONE;
        int color = getColorUnderMouse();
        renderRotationRing();
        int newColor = getColorUnderMouse();
        if(newColor != color) {
            int red = (newColor >> 16) & 0xFF;
            int green = (newColor >> 8) & 0xFF;
            int blue = newColor & 0xFF;
            if(red > 0xF0 && green < 0x0A && blue < 0x0A) {
                return XYZAxis.Y_AXIS;
            }

            if(green > 0xF0 && red < 0x0A && blue < 0x0A) {
                return XYZAxis.Z_AXIS;
            }

            if(blue > 0xF0 && red < 0x0A && green < 0x0A) {
                return XYZAxis.X_AXIS;
            }
        }
        return XYZAxis.NONE;
    }

    private int getColorUnderMouse() {
        int x = Mouse.getX();
        int y = Mouse.getY();
//        if(this.dialogBox.isOpen()) {
//            x = y = 0;
//        }
        colorBuffer.rewind();
        GL11.glReadPixels(x, y, 1, 1, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, colorBuffer);
        return colorBuffer.get(0);
    }

    private TabulaModelRenderer findPartBelowMouse() {
        TabulaModelRenderer newSelection = null;
        hideAllModelParts();

        GlStateManager.pushMatrix();
        GlStateManager.disableBlend();
        GlStateManager.disableTexture2D();

        for (int index = 0; index < model.boxList.size(); index++) {
            // Render the model part with a specific color and check if the color below the mouse has changed.
            // If it did, the mouse is over this given box
            TabulaModelRenderer box = (TabulaModelRenderer) model.boxList.get(index);

            // multiply by 2 because in some cases, the colors are not far enough to allow to pick the correct part
            // (a box behind another may be picked instead because the color are too close)
            float color = index*2 / 255f; // TODO: 128 boxes MAX - can be done by having the int id as the color index, or a random color. Maybe make it so it splits 0 - 0xFFFFFF by the model box size and sets that as the color

            GlStateManager.color(color, color, color);
            int prevColor = getColorUnderMouse();

            box.setHideButShowChildren(false);
            renderModel();
            box.setHideButShowChildren(true);

            int newColor = getColorUnderMouse();

            if (newColor != prevColor) {
                newSelection = box;
            }
        }
        GlStateManager.color(1f, 1f, 1f);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
        return newSelection;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        xPosition.mouseClicked(mouseX, mouseY, mouseButton);
        yPosition.mouseClicked(mouseX, mouseY, mouseButton);
        zPosition.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if(onSliders(mouseX, mouseY) || xPosition.mouseOver(mouseX, mouseY) || yPosition.mouseOver(mouseX, mouseY) || zPosition.mouseOver(mouseX, mouseY))
            return;
        if(mouseButton == 0) {
            registeredLeftClick = true;
        }
        lastClickPosition.set(Mouse.getX(), Mouse.getY());
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if(onButtons(mouseX, mouseY))
            return;
        if(onSliders(mouseX, mouseY)) {
            if(clickedMouseButton == 0 && !movedPart) {
                movedPart = true;
            }
            return;
        }
        if(clickedMouseButton == 0 && currentSelectedRing == XYZAxis.NONE) {
            float dx = Mouse.getX() - lastClickPosition.x;
            float dy = Mouse.getY() - lastClickPosition.y;
            cameraPitch += dy;
            cameraYaw -= dx;

            cameraPitch %= 360f;
            cameraYaw %= 360f;
        } else if(clickedMouseButton == 0) {
            float dx = Mouse.getX() - lastClickPosition.x;
            float dy = Mouse.getY() - lastClickPosition.y;
            draggingRing = true;
            // set, don't set. This method can be called multiple types before rendering the screen, which causes the dMouse vector to be nil more often that it should
            dMouse.x += dx;
            dMouse.y += dy;

            if(!movedPart) {
                movedPart = true;
            }
        }
        lastClickPosition.set(Mouse.getX(), Mouse.getY());
    }

    private boolean onSliders(int mouseX, int mouseY) {
        if(GuiConstants.mouseOn(xRotationSlider, mouseX, mouseY))
            return true;
        if(GuiConstants.mouseOn(yRotationSlider, mouseX, mouseY))
            return true;
        return GuiConstants.mouseOn(zRotationSlider, mouseX, mouseY);
    }

    private boolean onButtons(int mouseX, int mouseY) {
        for (GuiButton button : this.buttonList) {
            if(button != xRotationSlider && button != yRotationSlider && button != zRotationSlider && GuiConstants.mouseOn(button, mouseX, mouseY))
                return true;
        }
        return false;
    }

    /**
     * Rotate the selected model part according the mouse movement
     * Basically <a href=https://en.wikipedia.org/wiki/Angular_momentum>Angular momentum on Wikipédia</a>
     */
    private void handleRingDrag(float dx, float dy) {
        if(selectedPart == null)
            return;
        if(currentSelectedRing == XYZAxis.NONE)
            return;
        Matrix3f rotationMatrix = computeRotationMatrix(selectedPart);
        Vector3f force = new Vector3f(-dx, -dy, 0f);
        rotationMatrix.transform(force);

        // === START OF CODE FOR MOUSE WORLD POS ===
        modelMatrix.rewind();
        projectionMatrix.rewind();
        viewport.rewind();
        modelMatrix.rewind();
        viewport.put(0).put(0).put(Display.getWidth()).put(Display.getHeight());
        viewport.flip();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelMatrix);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
        modelMatrix.rewind();
        projectionMatrix.rewind();
        FloatBuffer out = BufferUtils.createFloatBuffer(3);
        Project.gluUnProject(Mouse.getX(), Mouse.getY(), 0f, modelMatrix, projectionMatrix, viewport, out);

        // === END OF CODE FOR MOUSE WORLD POS ===

        float mouseZ = 400f;

        Vector3f offset = new Vector3f(out.get(0), out.get(1), mouseZ);
        Matrix4f modelMatrixCopy = new Matrix4f();
        float[] modelCopy = new float[16];
        for (int j = 0;j<4;j++) {
            for(int i = 0;i<4;i++) {
                modelCopy[i*4+j] = modelMatrix.get(i*4+j);
            }
        }
        modelMatrixCopy.set(modelCopy);
        Vector3f partOrigin = computeTranslationVector(selectedPart);

        // Make sure our vectors are in the correct space
        modelMatrixCopy.transform(partOrigin);
        modelMatrixCopy.transform(force);

        offset.sub(partOrigin);
        Vector3f moment = new Vector3f();
        moment.cross(offset, force);
        float rotAmount = Math.signum(moment.dot(currentSelectedRing.getAxis()))*0.1f; // only get the sign to have total control on the speed (and avoid unit conversion)

        float previousAngle;
        GuiSlider slider;
        switch (currentSelectedRing) {
            case X_AXIS:
                previousAngle = selectedPart.rotateAngleX;
                slider = xRotationSlider;
                break;
            case Y_AXIS:
                previousAngle = selectedPart.rotateAngleY;
                slider = yRotationSlider;
                break;
            case Z_AXIS:
                previousAngle = selectedPart.rotateAngleZ;
                slider = zRotationSlider;
                break;
            default:
                return;
        }
        float angle = previousAngle+rotAmount;
        slider.setValue(Math.toDegrees(angle));
        actualizeRotation(selectedPart, currentSelectedRing, angle);
    }

    private Vector3f computeTranslationVector(TabulaModelRenderer part) {
        Matrix4f transform = computeTransformMatrix(part);
        Vector3f result = new Vector3f(0f, 0f, 0f);
        transform.transform(result);
        return result;
    }

    private Matrix4f computeTransformMatrix(TabulaModelRenderer part) {
        Matrix4f result = new Matrix4f();
        result.setIdentity();
        TabulaUtils.applyTransformations(part, result);
        return result;
    }

    private Matrix3f computeRotationMatrix(TabulaModelRenderer part) {
        Matrix3f result = new Matrix3f();
        result.setIdentity();
        TabulaModelRenderer parent = part.getParent();
        if(parent != null) {
            result.mul(computeRotationMatrix(parent));
        }
        result.rotZ(part.rotateAngleZ);
        result.rotY(part.rotateAngleY);
        result.rotX(part.rotateAngleX);
        return result;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        if(button == 0) {
            if(movedPart) {
                movedPart = false;
                if(selectedPart != null)
                    actualizeEdit(selectedPart);
            }
            currentSelectedRing = XYZAxis.NONE;
        }
    }

    @Override
    public void handleKeyboardInput() throws IOException {
//        if(this.dialogBox.isOpen()) {
//            return;
//        }
        super.handleKeyboardInput();
        GameSettings settings = mc.gameSettings;
        final float cameraSpeed = 10f;
        if(Keyboard.isKeyDown(settings.keyBindLeft.getKeyCode()) || Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            cameraYaw -= cameraSpeed;
        }
        if(Keyboard.isKeyDown(settings.keyBindRight.getKeyCode()) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            cameraYaw += cameraSpeed;
        }
        if(Keyboard.isKeyDown(settings.keyBindBack.getKeyCode()) || Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            cameraPitch += cameraSpeed;
        }
        if(Keyboard.isKeyDown(settings.keyBindForward.getKeyCode()) || Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            cameraPitch -= cameraSpeed;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
//        if(this.dialogBox.isOpen()) {
//            return;
//        }
        xPosition.handleMouseInput(this.width, this.height);
        yPosition.handleMouseInput(this.width, this.height);
        zPosition.handleMouseInput(this.width, this.height);
        super.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        xPosition.keyTyped(typedChar, keyCode);
        yPosition.keyTyped(typedChar, keyCode);
        zPosition.keyTyped(typedChar, keyCode);
    }

    private void prepareModelRendering(int posX, int posY, float scale) {
        scale *= zoom;
        GuiHelper.prepareModelRendering(posX, posY, scale, cameraPitch, cameraYaw);
    }

    private void actualModelRender(TabulaModelRenderer partBelowMouse) {
        highlight(selectedPart, 0f, 0f, 1f);
        highlight(partBelowMouse, 1f, 1f, 0f);
        resetScalings();
        hidePart(selectedPart);
        hidePart(partBelowMouse);
        GlStateManager.color(1f, 1f, 1f);
        renderModel();
        resetScalings();
    }

    private void renderRotationRing() {
        final float ringScale = 1.5f;
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.pushMatrix();
        selectedPart.setParentedAngles(1f/16f);
        GlStateManager.color(1f, 0f, 0f);
        GlStateManager.scale(ringScale, ringScale, ringScale);
        rotationRingModel.render(null, 0f, 0f, 0f, 0f, 0f, 1f/16f);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        selectedPart.setParentedAngles(1f/16f);
        GlStateManager.rotate(90f, 1f, 0f, 0f);
        GlStateManager.color(0f, 1f, 0f);
        GlStateManager.scale(ringScale, ringScale, ringScale);
        rotationRingModel.render(null, 0f, 0f, 0f, 0f, 0f, 1f/16f);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        selectedPart.setParentedAngles(1f/16f);
        GlStateManager.rotate(90f, 0f, 0f, 1f);
        GlStateManager.color(0f, 0f, 1f);
        GlStateManager.scale(ringScale, ringScale, ringScale);
        rotationRingModel.render(null, 0f, 0f, 0f, 0f, 0f, 1f/16f);
        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
    }

    private void hidePart(TabulaModelRenderer part) {
        if(part == null)
            return;
        part.setHideButShowChildren(true);
    }

    /**
     * Renders a single model part with the given color
     */
    private void highlight(TabulaModelRenderer part, float red, float green, float blue) {
        if(part != null) {
            hideAllModelParts();
            part.setHideButShowChildren(false);
            GlStateManager.disableTexture2D();
            GlStateManager.color(red, green, blue);
            renderModel();
            GlStateManager.enableTexture2D();

            resetScalings();
        }
    }

    private void resetScalings() {
        for(ModelRenderer renderer : model.boxList) {
            if(renderer instanceof TabulaModelRenderer) {
                TabulaModelRenderer part = (TabulaModelRenderer)renderer;
                part.setHideButShowChildren(false);
            }
        }
    }

    private void renderModel() {
        setModelToPose();

        mc.getTextureManager().bindTexture(this.texture);
        model.renderBoxes(1f/16f);
    }

    private void setModelToPose() {
        Map<String, TaxidermyHistory.CubeProps> poseData = this.getPoseData();
        for(TabulaModelRenderer box : model.getAllCubes()) {
            TaxidermyHistory.CubeProps cube = poseData.get(box.boxName);
            if(cube != null) {
                if(!Float.isNaN(cube.getAngle().x)) {
                    box.rotateAngleX = cube.getAngle().x;
                }
                if(!Float.isNaN(cube.getAngle().y)) {
                    box.rotateAngleY = cube.getAngle().y;
                }
                if(!Float.isNaN(cube.getAngle().z)) {
                    box.rotateAngleZ= cube.getAngle().z;
                }

                if(!Float.isNaN(cube.getRotationPoint().x)) {
                    box.rotationPointX = cube.getRotationPoint().x;
                }
                if(!Float.isNaN(cube.getRotationPoint().y)) {
                    box.rotationPointY = cube.getRotationPoint().y;
                }
                if(!Float.isNaN(cube.getRotationPoint().z)) {
                    box.rotationPointZ= cube.getRotationPoint().z;
                }
            } else {
                box.resetRotations();
                box.resetRotationPoint();
            }
        }
    }

    private void hideAllModelParts() {
        model.getAllCubes().forEach(r -> r.setHideButShowChildren(true));
    }
}
