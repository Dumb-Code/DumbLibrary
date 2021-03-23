package net.dumbcode.dumblibrary.client.gui;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.model.tabula.DCMModel;
import net.dumbcode.dumblibrary.server.network.C4MoveSelectedSkeletalPart;
import net.dumbcode.dumblibrary.server.network.C6SkeletalMovement;
import net.dumbcode.dumblibrary.server.network.C8MoveInHistory;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.dumbcode.dumblibrary.server.utils.XYZAxis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.vecmath.Vector3f;
import java.util.Map;

public class GuiTaxidermy extends GuiModelPoseEdit {

    private final BaseTaxidermyBlockEntity blockEntity;

    public GuiTaxidermy(DCMModel model, ResourceLocation texture, ITextComponent title, BaseTaxidermyBlockEntity blockEntity) {
        super(model, texture, title);
        this.blockEntity = blockEntity;
    }

    @Override
    protected void undo() {
        DumbLibrary.NETWORK.sendToServer(new C8MoveInHistory(this.blockEntity.getPos(), -1));
    }

    @Override
    protected void redo() {
        DumbLibrary.NETWORK.sendToServer(new C8MoveInHistory(this.blockEntity.getPos(), +1));
    }

    @Override
    protected void reset() {
        DumbLibrary.NETWORK.sendToServer(new C6SkeletalMovement(this.blockEntity.getPos(), TaxidermyHistory.RESET_NAME, new Vector3f(), new Vector3f()));
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
    protected void actualizeRotation(TabulaModelRenderer part, XYZAxis axis, float amount) {
        DumbLibrary.NETWORK.sendToServer(new C4MoveSelectedSkeletalPart(this.blockEntity.getPos(), part.boxName, 0, axis, amount));
    }

    @Override
    protected void actualizePosition(TabulaModelRenderer part, XYZAxis axis, float amount) {
        DumbLibrary.NETWORK.sendToServer(new C4MoveSelectedSkeletalPart(this.blockEntity.getPos(), part.boxName, 1, axis, amount));
    }

    @Override
    protected void actualizeEdit(TabulaModelRenderer part) {
        DumbLibrary.NETWORK.sendToServer(new C6SkeletalMovement(this.blockEntity.getPos(), part.boxName,
            new Vector3f(part.rotateAngleX, part.rotateAngleY, part.rotateAngleZ), new Vector3f(part.rotationPointX, part.rotationPointY, part.rotationPointZ)
        ));

    }

    @Override
    protected Map<String, TaxidermyHistory.CubeProps> getPoseData() {
        return this.blockEntity.getPoseData();
    }
}
