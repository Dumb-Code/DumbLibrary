package net.dumbcode.dumblibrary.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.MutVector2f;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.dumbcode.dumblibrary.server.utils.XYZAxis;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;
import net.minecraftforge.fml.client.gui.widget.Slider;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.util.glu.Project;
import sun.tools.jstack.JStack;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class GuiModelPoseEdit extends Screen {

    private final TabulaModel model;
    private final ResourceLocation texture;
    private final ITextComponent titleText;
    private boolean registeredLeftClick;
    private MutVector2f lastClickPosition = new MutVector2f(0, 0);
    private IntBuffer colorBuffer = BufferUtils.createIntBuffer(1);
    private TabulaModelRenderer selectedPart;
    private boolean movedPart;
    private TranslationTextComponent undoText = new TranslationTextComponent(DumbLibrary.MODID+".gui.model_pose_edit.undo");
    private TranslationTextComponent redoText = new TranslationTextComponent(DumbLibrary.MODID+".gui.model_pose_edit.redo");
    private TranslationTextComponent resetText = new TranslationTextComponent(DumbLibrary.MODID+".gui.model_pose_edit.reset");
    private TranslationTextComponent propertiesGui = new TranslationTextComponent(DumbLibrary.MODID+".gui.model_pose_edit.edit_properties");
    protected float cameraPitch;
    protected float cameraYaw = 90f;
    protected double zoom = 1.0;
    private TabulaModel rotationRingModel;
    private XYZAxis currentSelectedRing = XYZAxis.NONE;
    private boolean draggingRing = false;
    private MutVector2f dMouse = new MutVector2f(0, 0);

    private FloatBuffer modelMatrix = BufferUtils.createFloatBuffer(16);
    private FloatBuffer projectionMatrix = BufferUtils.createFloatBuffer(16);
    private IntBuffer viewport = BufferUtils.createIntBuffer(4);

    /**
     * Base Y component for control text, selected part text & sliders
     */
    private int baseYOffset = 20;

    private TranslationTextComponent noPartSelectedText = new TranslationTextComponent(DumbLibrary.MODID+".gui.model_pose_edit.no_part_selected");
    private TranslationTextComponent zoomText = new TranslationTextComponent(DumbLibrary.MODID+".gui.model_pose_edit.controls.zoom");
    private TranslationTextComponent selectModelPartText = new TranslationTextComponent(DumbLibrary.MODID+".gui.model_pose_edit.controls.select_part");
    private TranslationTextComponent rotateCameraText = new TranslationTextComponent(DumbLibrary.MODID+".gui.model_pose_edit.controls.rotate_camera");

//    private DialogBox dialogBox = new DialogBox()
//            .root(new File(Minecraft.getMinecraft().gameDir, "dinosaur_poses"))
//            .extension("ProjectNublar Dinosaur Pose (.dpose)", true, "*.dpose");

    private double prevXSlider;
    private double prevYSlider;
    private double prevZSlider;

    public static final String PREFIX_KEY = DumbLibrary.MODID + ".gui.model_pose_edit.rotation_slider.prefix";
    public static final String SUFFIX_KEY = DumbLibrary.MODID + ".gui.model_pose_edit.rotation_slider.suffix";

    private Slider xRotationSlider;
    private Slider yRotationSlider;
    private Slider zRotationSlider;

    private GuiNumberEntry xPosition;
    private GuiNumberEntry yPosition;
    private GuiNumberEntry zPosition;

    private Button undoButton;
    private Button redoButton;
    private Button resetButton;
    private Button exportButton;
    private Button importButton;
    private Button propertiesButton;

    private GuiNumberEntry mouseOverEntry;


//    private GuiButton propertiesButton = new GuiButtonExt(8, 0, 0, propertiesGui.getUnformattedText());

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
    public void init() {
        super.init();
        int buttonWidth = width/3;

        //Undo
        this.addButton(undoButton = new ExtendedButton(0, height-21, buttonWidth, 20, undoText, b -> this.undo()));
        //Redo
        this.addButton(redoButton = new ExtendedButton(buttonWidth, height-21, buttonWidth, 20, redoText, b -> this.redo()));
        //Reset
        this.addButton(resetButton = new ExtendedButton(buttonWidth*2, height-21, buttonWidth, 20, resetText, b -> this.reset()));
        //Properties
        this.addButton(propertiesButton = new ExtendedButton(width-201, height-43, 200, 20, propertiesGui, p_onPress_1_ -> {
            //this.mc.displayGuiScreen(new GuiSkeletalProperties(this, this.builder));
        }));

        //Rotation slider X
        this.addButton(xRotationSlider = new Slider(
            width-201, baseYOffset+font.lineHeight+2, 200, 20,
            new TranslationTextComponent(PREFIX_KEY, "X"),
            new TranslationTextComponent(SUFFIX_KEY),
            -180.0, 180.0, 0.0, true, true,
            p_onPress_1_ -> {}, slider -> this.sliderChanged(slider.getValue(), XYZAxis.X_AXIS)
        ));

        //Rotation slider Y
        this.addButton(yRotationSlider = new Slider(
            width-201, baseYOffset+font.lineHeight+27, 200, 20,
            new TranslationTextComponent(PREFIX_KEY, "Y"),
            new TranslationTextComponent(SUFFIX_KEY),
            -180.0, 180.0, 0.0, true, true,
            p_onPress_1_ -> {}, slider -> this.sliderChanged(slider.getValue(), XYZAxis.Y_AXIS)
        ));

        //Rotation slider Z
        this.addButton(zRotationSlider = new Slider(
            width-201, baseYOffset+font.lineHeight+52, 200, 20,
            new TranslationTextComponent(PREFIX_KEY, "Z"),
            new TranslationTextComponent(SUFFIX_KEY),
            -180.0, 180.0, 0.0, true, true,
            p_onPress_1_ -> {}, slider -> this.sliderChanged(slider.getValue(), XYZAxis.Z_AXIS)
        ));

        this.addWidget(xPosition = new GuiNumberEntry(
            0, 0, 1/4F, 2, width - 168, zRotationSlider.y+zRotationSlider.getHeight()+15,
            66, 20, this::addButton, this::positionChanged));

        this.addWidget(yPosition = new GuiNumberEntry(
            1, 0, 1/4F, 2, width - 101, zRotationSlider.y+zRotationSlider.getHeight()+15,
            66, 20, this::addButton, this::positionChanged));

        this.addWidget(zPosition = new GuiNumberEntry(
            2, 0, 1/4F, 2, width - 34, zRotationSlider.y+zRotationSlider.getHeight()+15,
            66, 20, this::addButton, this::positionChanged));
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

    public void sliderChanged(double value, XYZAxis axis) {
        if(currentSelectedRing != XYZAxis.NONE)
            return;
        if(selectedPart == null)
            return;
        actualizeRotation(selectedPart, axis, (float)Math.toRadians(value));
    }

    @Override
    public void tick() {
        xPosition.tick();
        yPosition.tick();
        zPosition.tick();


        super.tick();
        undoButton.active = getHistory().canUndo();
        redoButton.active = getHistory().canRedo();

        xRotationSlider.active = selectedPart != null;
        yRotationSlider.active = selectedPart != null;
        zRotationSlider.active = selectedPart != null;
        xRotationSlider.updateSlider();
        yRotationSlider.updateSlider();
        zRotationSlider.updateSlider();

        if(xRotationSlider.getValue() != prevXSlider) {
            sliderChanged(xRotationSlider.getValue(), XYZAxis.X_AXIS);
            prevXSlider = xRotationSlider.getValue();
        }
        if(yRotationSlider.getValue() != prevYSlider) {
            sliderChanged(yRotationSlider.getValue(), XYZAxis.Y_AXIS);
            prevYSlider = yRotationSlider.getValue();
        }
        if(zRotationSlider.getValue() != prevZSlider) {
            sliderChanged(zRotationSlider.getValue(), XYZAxis.Z_AXIS);
            prevZSlider = zRotationSlider.getValue();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        GuiNumberEntry mouseOver = null;
        if(xPosition.mouseOver(mouseX, mouseY)) {
            mouseOver = xPosition;
        } else if(yPosition.mouseOver(mouseX, mouseY)) {
            mouseOver = yPosition;
        } else if(zPosition.mouseOver(mouseX, mouseY)) {
            mouseOver = zPosition;
        }

        if(mouseOver == null) {
            final double zoomSpeed = 0.1;
            zoom += amount * zoomSpeed;
            if(zoom < zoomSpeed)
                zoom = zoomSpeed;
        }
        return false;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        GuiNumberEntry mouseOver = null;
        if(xPosition.mouseOver(mouseX, mouseY)) {
            mouseOver = xPosition;
        } else if(yPosition.mouseOver(mouseX, mouseY)) {
            mouseOver = yPosition;
        } else if(zPosition.mouseOver(mouseX, mouseY)) {
            mouseOver = zPosition;
        }
        if(selectedPart != null && mouseOverEntry != null && !mouseOverEntry.isSyncedSinceEdit() && (mouseOver != mouseOverEntry || mouseOverEntry.getTicksSinceChanged() > 20)) {
            actualizeEdit(selectedPart);
            mouseOverEntry.setSyncedSinceEdit(true);
        }
        mouseOverEntry = mouseOver;


        stack.pushPose();
        // ensures that the buttons show above the model
        stack.translate(0f, 0f, 1000f);
        super.render(stack, mouseX, mouseY, partialTicks);
        TaxidermyHistory history = getHistory();
        FontRenderer font = Minecraft.getInstance().font;
        drawCenteredString(stack, font, (history.getIndex()+1)+"/"+history.getSize(), width/2, height-redoButton.getHeight()-font.lineHeight, GuiConstants.NICE_WHITE);
        drawCenteredString(stack, font, titleText, width/2, 1, GuiConstants.NICE_WHITE);

        int yOffset = baseYOffset;
        drawString(stack, font, GuiConstants.CONTROLS_TEXT.copy().setStyle(Style.EMPTY.withBold(true).withUnderlined(true)), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 15;
        drawString(stack, font, selectModelPartText.copy().setStyle(Style.EMPTY.withUnderlined(true)), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 12;
        drawString(stack, font, GuiConstants.LEFT_CLICK_TEXT, 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(stack, font, rotateCameraText.copy().setStyle(Style.EMPTY.withUnderlined(true)), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 12;
        drawString(stack, font, GuiConstants.MIDDLE_CLICK_DRAG_TEXT, 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(stack, font, GuiConstants.MOVEMENT_KEYS_TEXT, 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(stack, font, GuiConstants.ARROW_KEYS_TEXT, 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(stack, font, zoomText.copy().withStyle(Style.EMPTY.withUnderlined(true)), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 12;
        drawString(stack, font, GuiConstants.MOUSE_WHEEL_TEXT, 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(stack, font, GuiConstants.TRACKPAD_ZOOM_TEXT, 10, yOffset, GuiConstants.NICE_WHITE);

        ITextComponent selectionText;
        if(selectedPart == null)
            selectionText = noPartSelectedText;
        else
            selectionText = new TranslationTextComponent(DumbLibrary.MODID+".gui.model_pose_edit.selected_part", selectedPart.getCube().getName());
        drawCenteredString(stack, font, selectionText, xRotationSlider.x+xRotationSlider.getWidth()/2, baseYOffset, GuiConstants.NICE_WHITE);
        stack.popPose();

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
                    xRotationSlider.setValue(MathHelper.wrapDegrees(Math.toDegrees(selectedPart.xRot)));
                    yRotationSlider.setValue(MathHelper.wrapDegrees(Math.toDegrees(selectedPart.yRot)));
                    zRotationSlider.setValue(MathHelper.wrapDegrees(Math.toDegrees(selectedPart.zRot)));

                    xPosition.setValue(selectedPart.xRot, false);
                    yPosition.setValue(selectedPart.yRot, false);
                    zPosition.setValue(selectedPart.zRot, false);

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

        if(partBelowMouse != null) {
//            drawHoveringText(partBelowMouse.boxName, mouseX, mouseY);
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
//        if(this.dialogBox.isOpen()) {
//            x = y = 0;
//        }
        colorBuffer.rewind();
        MouseHelper handler = Minecraft.getInstance().mouseHandler;
        GL11.glReadPixels((int) handler.xpos(), (int)handler.ypos(), 1, 1, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, colorBuffer);
        return colorBuffer.get(0);
    }

    private TabulaModelRenderer findPartBelowMouse() {
        TabulaModelRenderer newSelection = null;
        hideAllModelParts();

        RenderSystem.pushMatrix();
        RenderSystem.disableBlend();
        RenderSystem.disableTexture();

        List<TabulaModelRenderer> boxes = new ArrayList<TabulaModelRenderer>(model.getAllCubes());
        for (int index = 0; index < boxes.size(); index++) {
            // Render the model part with a specific color and check if the color below the mouse has changed.
            // If it did, the mouse is over this given box
            TabulaModelRenderer box = boxes.get(index);

            // multiply by 2 because in some cases, the colors are not far enough to allow to pick the correct part
            // (a box behind another may be picked instead because the color are too close)
            float color = index*2 / 255f; // TODO: 128 boxes MAX - can be done by having the int id as the color index, or a random color. Maybe make it so it splits 0 - 0xFFFFFF by the model box size and sets that as the color

            RenderSystem.color3f(color, color, color);
            int prevColor = getColorUnderMouse();

            box.setHideButShowChildren(false);
            renderModel();
            box.setHideButShowChildren(true);

            int newColor = getColorUnderMouse();

            if (newColor != prevColor) {
                newSelection = box;
            }
        }
        RenderSystem.color3f(1f, 1f, 1f);
        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.popMatrix();
        return newSelection;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if(onSliders(mouseX, mouseY) || xPosition.mouseOver(mouseX, mouseY) || yPosition.mouseOver(mouseX, mouseY) || zPosition.mouseOver(mouseX, mouseY))
            return false;
        if(mouseButton == 0) {
            registeredLeftClick = true;
        }
        MouseHelper handler = Minecraft.getInstance().mouseHandler;
        lastClickPosition.set((float) handler.xpos(), (float) handler.xpos());
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        if(onButtons(mouseX, mouseY)) {
            return false;
        }
        if(onSliders(mouseX, mouseY)) {
            if(mouseButton == 0 && !movedPart) {
                movedPart = true;
            }
            return false;
        }
        MouseHelper handler = Minecraft.getInstance().mouseHandler;
        float dx = (float) (handler.xpos() - lastClickPosition.x);
        float dy = (float) (handler.ypos() - lastClickPosition.y);
        if(mouseButton == 0 && currentSelectedRing == XYZAxis.NONE) {

            cameraPitch += dy;
            cameraYaw -= dx;

            cameraPitch %= 360f;
            cameraYaw %= 360f;
        } else if(mouseButton == 0) {
            draggingRing = true;
            // set, don't set. This method can be called multiple types before rendering the screen, which causes the dMouse vector to be nil more often that it should
            dMouse.x += dx;
            dMouse.y += dy;

            if(!movedPart) {
                movedPart = true;
            }
        }
        lastClickPosition.set((float)handler.xpos(), (float)handler.ypos());
        return false;
    }

    private boolean onSliders(double mouseX, double mouseY) {
        if(GuiConstants.mouseOn(xRotationSlider, mouseX, mouseY))
            return true;
        if(GuiConstants.mouseOn(yRotationSlider, mouseX, mouseY))
            return true;
        return GuiConstants.mouseOn(zRotationSlider, mouseX, mouseY);
    }

    private boolean onButtons(double mouseX, double mouseY) {
        for (Widget button : this.buttons) {
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
//        if(selectedPart == null)
//            return;
//        if(currentSelectedRing == XYZAxis.NONE)
//            return;
//        Matrix3f rotationMatrix = computeRotationMatrix(selectedPart);
//        Vector3f force = new Vector3f(-dx, -dy, 0f);
//        force.transform(rotationMatrix);
//
//        // === START OF CODE FOR MOUSE WORLD POS ===
//        MainWindow window = Minecraft.getInstance().getWindow();
//        modelMatrix.rewind();
//        projectionMatrix.rewind();
//        viewport.rewind();
//        modelMatrix.rewind();
//        viewport.put(0).put(0).put(window.getWidth()).put(window.getHeight());
//        viewport.flip();
//        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelMatrix);
//        GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
//        modelMatrix.rewind();
//        projectionMatrix.rewind();
//        FloatBuffer out = BufferUtils.createFloatBuffer(3);
//        gluUnProject(Mouse.getX(), Mouse.getY(), 0f, modelMatrix, projectionMatrix, viewport, out);
//
//        // === END OF CODE FOR MOUSE WORLD POS ===
//
//        float mouseZ = 400f;
//
//        Vector3f offset = new Vector3f(out.get(0), out.get(1), mouseZ);
//        Matrix4f modelMatrixCopy = new Matrix4f();
//        float[] modelCopy = new float[16];
//        for (int j = 0;j<4;j++) {
//            for(int i = 0;i<4;i++) {
//                modelCopy[i*4+j] = modelMatrix.get(i*4+j);
//            }
//        }
//        modelMatrixCopy.set(modelCopy);
//        Vector3f partOrigin = computeTranslationVector(selectedPart);
//
//        // Make sure our vectors are in the correct space
//        modelMatrixCopy.transform(partOrigin);
//        modelMatrixCopy.transform(force);
//
//        offset.sub(partOrigin);
//        Vector3f moment = new Vector3f();
//        moment.cross(offset, force);
//        float rotAmount = Math.signum(moment.dot(currentSelectedRing.getAxis()))*0.1f; // only get the sign to have total control on the speed (and avoid unit conversion)
//
//        float previousAngle;
//        GuiSlider slider;
//        switch (currentSelectedRing) {
//            case X_AXIS:
//                previousAngle = selectedPart.rotateAngleX;
//                slider = xRotationSlider;
//                break;
//            case Y_AXIS:
//                previousAngle = selectedPart.rotateAngleY;
//                slider = yRotationSlider;
//                break;
//            case Z_AXIS:
//                previousAngle = selectedPart.rotateAngleZ;
//                slider = zRotationSlider;
//                break;
//            default:
//                return;
//        }
//        float angle = previousAngle+rotAmount;
//        slider.setValue(Math.toDegrees(angle));
//        actualizeRotation(selectedPart, currentSelectedRing, angle);
    }

//    private Vector3f computeTranslationVector(TabulaModelRenderer part) {
//        Matrix4f transform = computeTransformMatrix(part);
//        Vector3f result = new Vector3f(0f, 0f, 0f);
//        transform.transform(result);
//        return result;
//    }

//    private Matrix4f computeTransformMatrix(TabulaModelRenderer part) {
//        Matrix4f result = new Matrix4f();
//        result.setIdentity();
//        TabulaUtils.applyTransformations(part, result);
//        return result;
//    }

//    private Matrix3f computeRotationMatrix(TabulaModelRenderer part) {
//        Matrix3f result = new Matrix3f();
//        result.setIdentity();
//        TabulaModelRenderer parent = part.getParent();
//        if(parent != null) {
//            result.mul(computeRotationMatrix(parent));
//        }
//        result.rotZ(part.rotateAngleZ);
//        result.rotY(part.rotateAngleY);
//        result.rotX(part.rotateAngleX);
//        return result;
//    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(button == 0) {
            if(movedPart) {
                movedPart = false;
                if(selectedPart != null) {
                    actualizeEdit(selectedPart);
                    return true;
                }
            }
            currentSelectedRing = XYZAxis.NONE;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        GameSettings settings = Minecraft.getInstance().options;
        final float cameraSpeed = 10f;
        if(settings.keyLeft.isDown()) {
            cameraYaw -= cameraSpeed;
        }
        if(settings.keyRight.isDown()) {
            cameraYaw += cameraSpeed;
        }
        if(settings.keyDown.isDown()) {
            cameraPitch += cameraSpeed;
        }
        if(settings.keyUp.isDown()) {
            cameraPitch -= cameraSpeed;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
        RenderSystem.color3f(1f, 1f, 1f);
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
                cube.applyTo(box);
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
