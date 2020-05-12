package net.dumbcode.dumblibrary.client.gui;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.dumbcode.dumblibrary.server.network.C4MoveSelectedSkeletalPart;
import net.dumbcode.dumblibrary.server.network.C6SkeletalMovement;
import net.dumbcode.dumblibrary.server.network.C8MoveInHistory;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.dumbcode.dumblibrary.server.utils.RotationAxis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.vecmath.Vector3f;
import java.util.Map;

public class GuiTaxidermy extends GuiModelPoseEdit {

    private final BaseTaxidermyBlockEntity blockEntity;

    public GuiTaxidermy(TabulaModel model, ResourceLocation texture, ITextComponent title, BaseTaxidermyBlockEntity blockEntity) {
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
        DumbLibrary.NETWORK.sendToServer(new C6SkeletalMovement(this.blockEntity.getPos(), TaxidermyHistory.RESET_NAME, new Vector3f()));
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
    protected void actualizeRotation(TabulaModelRenderer part, RotationAxis axis, float amount) {
        DumbLibrary.NETWORK.sendToServer(new C4MoveSelectedSkeletalPart(this.blockEntity.getPos(), part.boxName, axis, amount));
    }

    @Override
    protected void actualizeEdit(TabulaModelRenderer part) {
        DumbLibrary.NETWORK.sendToServer(new C6SkeletalMovement(this.blockEntity.getPos(), part.boxName, new Vector3f(part.rotateAngleX, part.rotateAngleY, part.rotateAngleZ)));

    }

    @Override
    protected Map<String, Vector3f> getPoseData() {
        return this.blockEntity.getPoseData();
    }
}
