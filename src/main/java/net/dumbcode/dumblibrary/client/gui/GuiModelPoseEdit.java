package net.dumbcode.dumblibrary.client.gui;

import com.mojang.blaze3d.matrix.GuiGraphics;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.netty.util.internal.IntegerHolder;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.MutVector2f;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModelRenderer;
import net.dumbcode.dumblibrary.server.utils.DCMUtils;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.dumbcode.dumblibrary.server.utils.XYZAxis;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.Mth;
import org.joml.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;
import net.minecraftforge.fml.client.gui.widget.Slider;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


public abstract class GuiModelPoseEdit extends Screen {

    private final DCMModel model;
    private final ResourceLocation texture;
    private final ITextComponent titleText;
    private boolean shouldSelectMouseUnderPart;
    private boolean ignoreClick;
    private MutVector2f lastClickPosition = new MutVector2f(0, 0);
    private IntBuffer colorBuffer = BufferUtils.createIntBuffer(1);
    private DCMModelRenderer selectedPart;
    private boolean movedPart;
    private TranslationTextComponent undoText = Component.translatable(DumbLibrary.MODID+".gui.model_pose_edit.undo");
    private TranslationTextComponent redoText = Component.translatable(DumbLibrary.MODID+".gui.model_pose_edit.redo");
    private TranslationTextComponent resetText = Component.translatable(DumbLibrary.MODID+".gui.model_pose_edit.reset");
    private TranslationTextComponent propertiesGui = Component.translatable(DumbLibrary.MODID+".gui.model_pose_edit.edit_properties");
    protected float cameraPitch;
    protected float cameraYaw = 90f;
    protected double zoom = 1.0;
    private DCMModel rotationRingModel;
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

    private TranslationTextComponent noPartSelectedText = Component.translatable(DumbLibrary.MODID+".gui.model_pose_edit.no_part_selected");
    private TranslationTextComponent zoomText = Component.translatable(DumbLibrary.MODID+".gui.model_pose_edit.controls.zoom");
    private TranslationTextComponent selectModelPartText = Component.translatable(DumbLibrary.MODID+".gui.model_pose_edit.controls.select_part");
    private TranslationTextComponent rotateCameraText = Component.translatable(DumbLibrary.MODID+".gui.model_pose_edit.controls.rotate_camera");

//    private DialogBox dialogBox = new DialogBox()
//            .root(new File(Minecraft.getMinecraft().gameDir, "dinosaur_poses"))
//            .extension("ProjectNublar Dinosaur Pose (.dpose)", true, "*.dpose");


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

    private GuiNumberEntry mouseOverEntry;


//    private GuiButton propertiesButton = new GuiButtonExt(8, 0, 0, propertiesGui.getUnformattedText());

    public GuiModelPoseEdit(DCMModel model, ResourceLocation texture, ITextComponent title) {
        super(title);
        this.model = model; // TODO: child models? -> Selectable
        this.texture = texture;
        this.titleText = title;
        this.rotationRingModel = DCMUtils.getModel(GuiConstants.ROTATION_RING_LOCATION);
        this.cameraPitch = cameraPitch;
        this.cameraYaw = cameraYaw;
        this.zoom = zoom;
    }

    @Override
    public void init() {
        super.init();
        int buttonWidth = width/3;

        //Undo
        this.addWidget(undoButton = new ExtendedButton(0, height-21, buttonWidth, 20, undoText, b -> this.undo()));
        //Redo
        this.addWidget(redoButton = new ExtendedButton(buttonWidth, height-21, buttonWidth, 20, redoText, b -> this.redo()));
        //Reset
        this.addWidget(resetButton = new ExtendedButton(buttonWidth*2, height-21, buttonWidth, 20, resetText, b -> this.reset()));

        //Rotation slider X
        this.addWidget(xRotationSlider = new WrappedSlider(
            width-201, baseYOffset+font.lineHeight+2, 200, 20,
            Component.translatable(PREFIX_KEY, "X"),
            Component.translatable(SUFFIX_KEY),
            -180.0, 180.0, 0.0, true, true,
            slider -> this.sliderChanged(slider.getValue(), XYZAxis.X_AXIS),
            this::actualizeEdit));

        //Rotation slider Y
        this.addWidget(yRotationSlider = new WrappedSlider(
            width-201, baseYOffset+font.lineHeight+27, 200, 20,
            Component.translatable(PREFIX_KEY, "Y"),
            Component.translatable(SUFFIX_KEY),
            -180.0, 180.0, 0.0, true, true,
            slider -> this.sliderChanged(slider.getValue(), XYZAxis.Y_AXIS),
            this::actualizeEdit));

        //Rotation slider Z
        this.addWidget(zRotationSlider = new WrappedSlider(
            width-201, baseYOffset+font.lineHeight+52, 200, 20,
            Component.translatable(PREFIX_KEY, "Z"),
            Component.translatable(SUFFIX_KEY),
            -180.0, 180.0, 0.0, true, true,
            slider -> this.sliderChanged(slider.getValue(), XYZAxis.Z_AXIS),
            this::actualizeEdit));

        this.addWidget(xPosition = new GuiNumberEntry(
            0, 0, 1/4F, 2, width - 168, zRotationSlider.y+zRotationSlider.getHeight()+15,
            66, 20, this::positionChanged));

        this.addWidget(yPosition = new GuiNumberEntry(
            1, 0, 1/4F, 2, width - 101, zRotationSlider.y+zRotationSlider.getHeight()+15,
            66, 20, this::positionChanged));

        this.addWidget(zPosition = new GuiNumberEntry(
            2, 0, 1/4F, 2, width - 34, zRotationSlider.y+zRotationSlider.getHeight()+15,
            66, 20, this::positionChanged));
    }

    protected abstract void undo();
    protected abstract void redo();
    protected abstract void reset();
    protected abstract void exportPose();
    protected abstract void importPose();
    protected abstract TaxidermyHistory getHistory();
    protected abstract void actualizeRotation(DCMModelRenderer part, XYZAxis axis, float amount);
    protected abstract void actualizePosition(DCMModelRenderer part, XYZAxis axis, float amount);
    protected abstract void actualizeEdit(DCMModelRenderer part);
    protected abstract Map<String, TaxidermyHistory.CubeProps> getPoseData();

    public void positionChanged(GuiNumberEntry entry, int id) {
        if(selectedPart != null) {
            actualizePosition(selectedPart, XYZAxis.values()[id], (float) entry.getValue());
        }
    }

    public void actualizeEdit() {
        if(selectedPart != null) {
            actualizeEdit(selectedPart);
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
    public void render(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        this.onFrame(mouseX, mouseY);

        this.stack.pose.translate(0, 0, -1000);
        this.renderBackground(stack);
        this.stack.pose.translate(0, 0, 0);

        this.renderModelUI(stack, mouseX, mouseY);
        this.renderUI(stack, mouseX, mouseY, partialTicks);
    }

    private void onFrame(int mouseX, int mouseY) {
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
    }

    private void renderModelUI(GuiGraphics stack, int mouseX, int mouseY) {
        setModelToPose();
        GL11.glPushMatrix();
        GuiGraphics modelRendering = prepareModelRendering(width / 8 * 3, height / 2, 30f);
        XYZAxis ringBelowMouse = findRingBelowMouse();
        if(draggingRing) {
            if(ringBelowMouse != XYZAxis.NONE) {
                handleRingDrag(dMouse.x, dMouse.y);
            }
            dMouse.set(0f, 0f);
            draggingRing = false;
        }
        DCMModelRenderer partBelowMouse = findPartBelowMouse(modelRendering);

        if(shouldSelectMouseUnderPart) {
            if(ringBelowMouse == XYZAxis.NONE) {
                this.selectedPart = partBelowMouse;
                this.currentSelectedRing = XYZAxis.NONE;
                if(selectedPart != null) {
                    xRotationSlider.setValue(Mth.wrapDegrees(Math.toDegrees(selectedPart.xRot)));
                    yRotationSlider.setValue(Mth.wrapDegrees(Math.toDegrees(selectedPart.yRot)));
                    zRotationSlider.setValue(Mth.wrapDegrees(Math.toDegrees(selectedPart.zRot)));

                    xPosition.setValue(selectedPart.x, false);
                    yPosition.setValue(selectedPart.y, false);
                    zPosition.setValue(selectedPart.z, false);
                }
            } else {
                this.currentSelectedRing = ringBelowMouse;
            }
            shouldSelectMouseUnderPart = false;
        }
        actualModelRender(modelRendering, partBelowMouse);

        RenderSystem.popMatrix();

        if(partBelowMouse != null) {
            stack.drawString(font, partBelowMouse.getName(), mouseX, mouseY, -1);
        }
    }

    private void renderUI(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        TaxidermyHistory history = getHistory();
        FontRenderer font = Minecraft.getInstance().font;
        stack.drawCenteredString(font, (history.getIndex()+1)+"/"+history.getSize(), width/2, height-redoButton.getHeight()-font.lineHeight, GuiConstants.NICE_WHITE);
        stack.drawCenteredString(font, titleText, width/2, 1, GuiConstants.NICE_WHITE);

        int yOffset = baseYOffset;
        stack.drawString(font, GuiConstants.CONTROLS_TEXT.copy().setStyle(Style.EMPTY.withBold(true).withUnderlined(true)), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 15;
        stack.drawString(font, selectModelPartText.copy().setStyle(Style.EMPTY.withUnderlined(true)), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 12;
        stack.drawString(font, GuiConstants.LEFT_CLICK_TEXT, 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        stack.drawString(font, rotateCameraText.copy().setStyle(Style.EMPTY.withUnderlined(true)), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 12;
        stack.drawString(font, GuiConstants.MIDDLE_CLICK_DRAG_TEXT, 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        stack.drawString(font, GuiConstants.MOVEMENT_KEYS_TEXT, 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        stack.drawString(font, GuiConstants.ARROW_KEYS_TEXT, 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        stack.drawString(font, zoomText.copy().withStyle(Style.EMPTY.withUnderlined(true)), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 12;
        stack.drawString(font, GuiConstants.MOUSE_WHEEL_TEXT, 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        stack.drawString(font, GuiConstants.TRACKPAD_ZOOM_TEXT, 10, yOffset, GuiConstants.NICE_WHITE);

        ITextComponent selectionText;
        if(selectedPart == null)
            selectionText = noPartSelectedText;
        else
            selectionText = Component.translatable(DumbLibrary.MODID+".gui.model_pose_edit.selected_part", selectedPart.getInfo().getName());
        stack.drawCenteredString(font, selectionText, xRotationSlider.x+xRotationSlider.getWidth()/2, baseYOffset, GuiConstants.NICE_WHITE);

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
        MainWindow window = Minecraft.getInstance().getWindow();
        MouseHelper handler = Minecraft.getInstance().mouseHandler;
        GL11.glReadPixels((int) handler.xpos(), window.getHeight()-(int)handler.ypos(), 1, 1, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, colorBuffer);
        return colorBuffer.get(0);
    }

    private DCMModelRenderer findPartBelowMouse(GuiGraphics stack) {
        GL11.glPushMatrix();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();

        Map<Integer, DCMModelRenderer> cubeMap = new HashMap<>();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.NEW_ENTITY);
        IntegerHolder idx = new IntegerHolder();
        renderRecursively(buffer, stack, model.getRoots(), cube -> {
            int id = idx.value++*3;
            int r = id & 255;
            int g = Math.floorDiv(id, 255) & 255;
            int b = Math.floorDiv(id, 255*255) & 255;
            cubeMap.put((r << 16) | (g << 8) | b, cube);
            return new int[] { r, g, b };
        });
        Tesselator.getInstance().end();

        RenderSystem.color3f(1f, 1f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.popMatrix();

        return cubeMap.get(getColorUnderMouse() & 0xFFFFFF);
    }

    private void renderRecursively(IVertexBuilder buffer, GuiGraphics stack, List<DCMModelRenderer> cubes, Function<DCMModelRenderer, int[]> colorGetter) {
        for (DCMModelRenderer cube : cubes) {
            stack.pushPose();

            int[] color = colorGetter.apply(cube);

            cube.translateAndRotate(stack);
            cube.compile(stack.last(), buffer, 15728880, OverlayTexture.NO_OVERLAY, color[0] / 255F , color[1] / 255F, color[2] / 255F, 1);

            renderRecursively(buffer, stack, cube.getChildCubes(), colorGetter);

            stack.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        MouseHelper handler = Minecraft.getInstance().mouseHandler;
        lastClickPosition.set((float) handler.xpos(), (float) handler.ypos());
        if(super.mouseClicked(mouseX, mouseY, mouseButton)) {
            ignoreClick = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        ignoreClick = true;
        for (Widget button : this.buttons) {
            if(button.isMouseOver(mouseX, mouseY)) {
                return false;
            }
        }
        if(xRotationSlider.dragging || yRotationSlider.dragging || zRotationSlider.dragging) {
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
        return true;
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
        if(button == 0 && !ignoreClick) {
            shouldSelectMouseUnderPart = true;
        }
        ignoreClick = false;
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
        return super.mouseReleased(mouseX, mouseY, button);
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


    private GuiGraphics prepareModelRendering(int posX, int posY, float scale) {
        scale *= zoom;
        GL11.glTranslatef((float)posX, (float)posY, -500);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        GuiGraphics GuiGraphics = new GuiGraphics();
        GuiGraphics.scale(scale, scale, scale);
        GuiGraphics.mulPose(Vector3f.XP.rotationDegrees(cameraPitch));
        GuiGraphics.mulPose(Vector3f.YP.rotationDegrees(cameraYaw));
        return GuiGraphics;
    }

    private void actualModelRender(GuiGraphics stack, DCMModelRenderer partBelowMouse) {
        RenderSystem.enableLighting();
        setModelToPose();
        Minecraft instance = Minecraft.getInstance();
        IRenderTypeBuffer.Impl impl = instance.renderBuffers().bufferSource();
        IVertexBuilder buffer = impl.getBuffer(this.model.renderType(this.texture));
        this.renderRecursively(buffer, stack, this.model.getRoots(), cube -> {
            if(cube == partBelowMouse) {
                return new int[] { 127, 127, 255 };
            }
            if(cube == this.selectedPart) {
                return new int[] { 255, 127, 127 };
            }
            return new int[] { 255, 255, 255 };
        });
        impl.endBatch();
    }

    private void renderRotationRing() {
//        final float ringScale = 1.5f;
//        GlStateManager.disableLighting();
//        GlStateManager.disableTexture2D();
//        GlStateManager.pushMatrix();
//        selectedPart.setParentedAngles(1f/16f);
//        GlStateManager.color(1f, 0f, 0f);
//        GlStateManager.scale(ringScale, ringScale, ringScale);
//        rotationRingModel.render(null, 0f, 0f, 0f, 0f, 0f, 1f/16f);
//        GlStateManager.popMatrix();
//
//        GlStateManager.pushMatrix();
//        selectedPart.setParentedAngles(1f/16f);
//        GlStateManager.rotate(90f, 1f, 0f, 0f);
//        GlStateManager.color(0f, 1f, 0f);
//        GlStateManager.scale(ringScale, ringScale, ringScale);
//        rotationRingModel.render(null, 0f, 0f, 0f, 0f, 0f, 1f/16f);
//        GlStateManager.popMatrix();
//
//        GlStateManager.pushMatrix();
//        selectedPart.setParentedAngles(1f/16f);
//        GlStateManager.rotate(90f, 0f, 0f, 1f);
//        GlStateManager.color(0f, 0f, 1f);
//        GlStateManager.scale(ringScale, ringScale, ringScale);
//        rotationRingModel.render(null, 0f, 0f, 0f, 0f, 0f, 1f/16f);
//        GlStateManager.popMatrix();
//        GlStateManager.enableTexture2D();
//        GlStateManager.enableLighting();
    }

    private void setModelToPose() {
        Map<String, TaxidermyHistory.CubeProps> poseData = this.getPoseData();
        for(DCMModelRenderer box : model.getAllCubes()) {
            TaxidermyHistory.CubeProps cube = poseData.get(box.getInfo().getName());
            if(cube != null) {
                cube.applyTo(box);
            } else {
                box.resetRotations();
                box.resetRotationPoint();
            }
        }
    }
}
