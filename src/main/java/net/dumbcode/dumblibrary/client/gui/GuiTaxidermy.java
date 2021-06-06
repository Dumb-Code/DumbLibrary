package net.dumbcode.dumblibrary.client.gui;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModelRenderer;
import net.dumbcode.dumblibrary.server.network.C2SMoveSelectedSkeletalPart;
import net.dumbcode.dumblibrary.server.network.C2SSkeletalMovement;
import net.dumbcode.dumblibrary.server.network.C2SMoveInHistory;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.dumbcode.dumblibrary.server.utils.XYZAxis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;

import java.util.Map;

public class GuiTaxidermy extends GuiModelPoseEdit {

    private final BaseTaxidermyBlockEntity blockEntity;

    public GuiTaxidermy(DCMModel model, ResourceLocation texture, ITextComponent title, BaseTaxidermyBlockEntity blockEntity) {
        super(model, texture, title);
        this.blockEntity = blockEntity;
    }

    @Override
    protected void undo() {
        DumbLibrary.NETWORK.sendToServer(new C2SMoveInHistory(false));
    }

    @Override
    protected void redo() {
        DumbLibrary.NETWORK.sendToServer(new C2SMoveInHistory(true));
    }

    @Override
    protected void reset() {
        DumbLibrary.NETWORK.sendToServer(new C2SSkeletalMovement(TaxidermyHistory.RESET_NAME, new Vector3f(), new Vector3f()));
    }

    @Override
    protected void exportPose() {

    }

    @Override
    protected void importPose() {

    }

    @Override
    protected TaxidermyHistory getHistory() {
        return this.blockEntity.getHistory();
    }

    @Override
    protected void actualizeRotation(DCMModelRenderer part, XYZAxis axis, float amount) {
        DumbLibrary.NETWORK.sendToServer(new C2SMoveSelectedSkeletalPart(part.getName(), axis, 0, amount));
    }

    @Override
    protected void actualizePosition(DCMModelRenderer part, XYZAxis axis, float amount) {
        DumbLibrary.NETWORK.sendToServer(new C2SMoveSelectedSkeletalPart(part.getName(), axis, 1, amount));
    }

    @Override
    protected void actualizeEdit(DCMModelRenderer part) {
        DumbLibrary.NETWORK.sendToServer(new C2SSkeletalMovement(part.getName(),
            new Vector3f(part.xRot, part.yRot, part.zRot), new Vector3f(part.x, part.y, part.z)
        ));
    }

    @Override
    protected Map<String, TaxidermyHistory.CubeProps> getPoseData() {
        return this.blockEntity.getPoseData();
    }

    public BaseTaxidermyBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
