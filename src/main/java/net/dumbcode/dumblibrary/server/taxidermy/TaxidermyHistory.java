package net.dumbcode.dumblibrary.server.taxidermy;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.dumbcode.dumblibrary.server.utils.HistoryList;
import net.dumbcode.dumblibrary.server.utils.XYZAxis;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import org.joml.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxidermyHistory {

    public static final String RESET_NAME = "$$RESET_NAME$$";

    private final HistoryList<List<Record>> history = new HistoryList<>();
    private Map<String, Edit> editingData = new HashMap<>();

    private Map<String, CubeProps> poseDataCache = null;

    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putInt("Index", this.history.getIndex());
        ListNBT list = new ListNBT();
        for (List<Record> records : this.history.getUnindexedList()) {
            ListNBT taglist = new ListNBT();
            for (Record record : records) {
                CompoundTag tag = new CompoundTag();
                tag.putString("Part", record.getPart());
                tag.putFloat("AngleX", record.getProps().angle.x());
                tag.putFloat("AngleY", record.getProps().angle.y());
                tag.putFloat("AngleZ", record.getProps().angle.z());
                tag.putFloat("PosX", record.getProps().rotationPoint.x());
                tag.putFloat("PosY", record.getProps().rotationPoint.y());
                tag.putFloat("PosZ", record.getProps().rotationPoint.z());
                taglist.add(tag);
            }
            list.add(taglist);
        }
        nbt.put("Records", list);
        return nbt;
    }

    public void readFromNBT(CompoundTag nbt) {
        this.history.clear();
        for (INBT record : nbt.getList("Records", Constants.NBT.TAG_LIST)) {
            List<Record> records = Lists.newArrayList();
            for (INBT nbtBase : (ListNBT) record) {
                CompoundTag tag = (CompoundTag) nbtBase;
                records.add(new Record(tag.getString("Part"), new CubeProps(
                    new Vector3f(tag.getFloat("AngleX"), tag.getFloat("AngleY"), tag.getFloat("AngleZ")),
                    new Vector3f(tag.getFloat("PosX"), tag.getFloat("PosY"), tag.getFloat("PosZ"))
                )));
            }
            this.history.add(records);
        }
        this.history.setIndex(nbt.getInt("Index"));
    }

    public Map<String, CubeProps> getPoseData() {
        if(this.poseDataCache != null) {
            return this.poseDataCache;
        }

        this.poseDataCache = Maps.newHashMap();

        this.history.forEach(recordList -> recordList.forEach(record -> {
            if(record.getPart().equals(TaxidermyHistory.RESET_NAME)) {
                this.poseDataCache.clear();
            } else {
                this.poseDataCache.put(record.getPart(), record.getProps().clone());
            }
        }));

        for (Map.Entry<String, TaxidermyHistory.Edit> entry : this.editingData.entrySet()) {
            CubeProps cube = this.poseDataCache.computeIfAbsent(entry.getKey(), s -> new CubeProps(new Vector3f(Float.NaN, Float.NaN, Float.NaN), new Vector3f(Float.NaN, Float.NaN, Float.NaN)));
            TaxidermyHistory.Edit edit = entry.getValue();
            Vector3f vec = edit.type == 0 ? cube.angle : cube.rotationPoint;
            float x = vec.x();
            float y = vec.y();
            float z = vec.z();
            switch (edit.axis) {
                case X_AXIS:
                    x = edit.value;
                    break;
                case Y_AXIS:
                    y = edit.value;
                    break;
                case Z_AXIS:
                    z = edit.value;
                    break;
                default:
                    break;
            }

            Vector3f vector3f = new Vector3f(x, y, z);
            if(edit.type == 0) {
                cube.setAngle(vector3f);
            } else {
                cube.setRotationPoint(vector3f);
            }

        }
        return this.poseDataCache;
    }

    public void liveEdit(String partName, int type, XYZAxis axis, float angle) {
        this.editingData.put(partName, new Edit(axis, type, angle));
        this.poseDataCache = null;
    }

    public int getIndex() {
        return this.history.getIndex();
    }

    public int getSize() {
        return this.history.getUnindexedList().size();
    }

    public void clear() {
        this.history.clear();
        this.poseDataCache = null;
    }

    public boolean canRedo() {
        return this.history.canRedo();
    }

    public void redo() {
        this.history.redo();
        this.poseDataCache = null;
    }

    public boolean canUndo() {
        return this.history.canUndo();
    }

    public void undo() {
        this.history.undo();
        this.poseDataCache = null;
    }

    public void add(Record record) {
        this.poseDataCache = null;
        this.editingData.remove(record.getPart());
        this.history.add(Lists.newArrayList(record));
    }

    public void addGroupedRecord(List<Record> records) {
        this.history.add(records);
        this.poseDataCache = null;
    }


    @Value public static class Record { String part; CubeProps props; }

    @AllArgsConstructor
    public static class CubeProps implements Cloneable {
        private Vector3f angle;
        private Vector3f rotationPoint;

        @OnlyIn(Dist.CLIENT)
        public void applyTo(ModelRenderer box) {
            if(!Float.isNaN(this.angle.x())) {
                box.xRot = this.angle.x();
            }
            if(!Float.isNaN(this.angle.y())) {
                box.yRot = this.angle.y();
            }
            if(!Float.isNaN(this.angle.z())) {
                box.zRot = this.angle.z();
            }

            if(!Float.isNaN(this.rotationPoint.x())) {
                box.x = this.rotationPoint.x();
            }
            if(!Float.isNaN(this.rotationPoint.y())) {
                box.y = this.rotationPoint.y();
            }
            if(!Float.isNaN(this.rotationPoint.z())) {
                box.z = this.rotationPoint.z();
            }
        }

        public void setAngle(Vector3f angle) {
            this.angle = angle;
        }

        public void setRotationPoint(Vector3f rotationPoint) {
            this.rotationPoint = rotationPoint;
        }

        public CubeProps clone() {
            return new CubeProps(this.angle.copy(), this.rotationPoint.copy());
        }

    }

    @AllArgsConstructor private static class Edit { XYZAxis axis; int type; float value; }
}
