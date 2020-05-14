package net.dumbcode.dumblibrary.server.taxidermy;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.dumbcode.dumblibrary.server.utils.HistoryList;
import net.dumbcode.dumblibrary.server.utils.XYZAxis;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxidermyHistory {

    public static final String RESET_NAME = "$$RESET_NAME$$";

    private final HistoryList<List<Record>> history = new HistoryList<>();
    private Map<String, Edit> editingData = new HashMap<>();

    private Map<String, CubeProps> poseDataCache = null;

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("Index", this.history.getIndex());
        NBTTagList list = new NBTTagList();
        for (List<Record> records : this.history.getUnindexedList()) {
            NBTTagList taglist = new NBTTagList();
            for (Record record : records) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("Part", record.getPart());
                tag.setFloat("AngleX", record.getProps().angle.x);
                tag.setFloat("AngleY", record.getProps().angle.y);
                tag.setFloat("AngleZ", record.getProps().angle.z);
                tag.setFloat("PosX", record.getProps().rotationPoint.x);
                tag.setFloat("PosY", record.getProps().rotationPoint.y);
                tag.setFloat("PosZ", record.getProps().rotationPoint.z);
                taglist.appendTag(tag);
            }
            list.appendTag(taglist);
        }
        nbt.setTag("Records", list);
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        this.history.clear();
        for (NBTBase record : nbt.getTagList("Records", Constants.NBT.TAG_LIST)) {
            List<Record> records = Lists.newArrayList();
            for (NBTBase nbtBase : (NBTTagList) record) {
                NBTTagCompound tag = (NBTTagCompound) nbtBase;
                records.add(new Record(tag.getString("Part"), new CubeProps(
                    new Vector3f(tag.getFloat("AngleX"), tag.getFloat("AngleY"), tag.getFloat("AngleZ")),
                    new Vector3f(tag.getFloat("PosX"), tag.getFloat("PosY"), tag.getFloat("PosZ"))
                )));
            }
            this.history.add(records);
        }
        this.history.setIndex(nbt.getInteger("Index"));
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
            switch (edit.axis) {
                case X_AXIS:
                    vec.x = edit.value;
                    break;
                case Y_AXIS:
                    vec.y = edit.value;
                    break;
                case Z_AXIS:
                    vec.z = edit.value;
                    break;
                default:
                    break;
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
        private final Vector3f angle;
        private final Vector3f rotationPoint;

        @SideOnly(Side.CLIENT)
        public void applyTo(ModelRenderer box) {
            if(!Float.isNaN(this.angle.x)) {
                box.rotateAngleX = this.angle.x;
            }
            if(!Float.isNaN(this.angle.y)) {
                box.rotateAngleY = this.angle.y;
            }
            if(!Float.isNaN(this.angle.z)) {
                box.rotateAngleZ= this.angle.z;
            }

            if(!Float.isNaN(this.rotationPoint.x)) {
                box.rotationPointX = this.rotationPoint.x;
            }
            if(!Float.isNaN(this.rotationPoint.y)) {
                box.rotationPointY = this.rotationPoint.y;
            }
            if(!Float.isNaN(this.rotationPoint.z)) {
                box.rotationPointZ = this.rotationPoint.z;
            }
        }

        public CubeProps clone() {
            return new CubeProps(new Vector3f(this.angle), new Vector3f(this.rotationPoint));
        }

    }

    @AllArgsConstructor private static class Edit { XYZAxis axis; int type; float value; }
}
