package net.dumbcode.dumblibrary.server.taxidermy;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.dumbcode.dumblibrary.server.utils.HistoryList;
import net.dumbcode.dumblibrary.server.utils.RotationAxis;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxidermyHistory {

    public static final String RESET_NAME = "$$RESET_NAME$$";

    private final HistoryList<List<Record>> history = new HistoryList<>();
    private Map<String, Edit> editingData = new HashMap<>();

    private Map<String, Vector3f> poseDataCache = null;

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("Index", this.history.getIndex());
        NBTTagList list = new NBTTagList();
        for (List<Record> records : this.history.getUnindexedList()) {
            NBTTagList taglist = new NBTTagList();
            for (Record record : records) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("Part", record.getPart());
                tag.setFloat("AngleX", record.getAngle().x);
                tag.setFloat("AngleY", record.getAngle().y);
                tag.setFloat("AngleZ", record.getAngle().z);
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
                records.add(new Record(tag.getString("Part"), new Vector3f(tag.getFloat("AngleX"), tag.getFloat("AngleY"), tag.getFloat("AngleZ"))));
            }
            this.history.add(records);
        }
        this.history.setIndex(nbt.getInteger("Index"));
    }

    public Map<String, Vector3f> getPoseData() {
        if(this.poseDataCache != null) {
            return this.poseDataCache;
        }

        this.poseDataCache = Maps.newHashMap();

        this.history.forEach(recordList -> recordList.forEach(record -> {
            if(record.getPart().equals(TaxidermyHistory.RESET_NAME)) {
                this.poseDataCache.clear();
            } else {
                this.poseDataCache.put(record.getPart(), new Vector3f(record.getAngle()));
            }
        }));

        for (Map.Entry<String, TaxidermyHistory.Edit> entry : this.editingData.entrySet()) {
            Vector3f vec = this.poseDataCache.computeIfAbsent(entry.getKey(), s -> new Vector3f());
            TaxidermyHistory.Edit edit = entry.getValue();
            switch (edit.axis) {
                case X_AXIS:
                    vec.x = edit.angle;
                    break;
                case Y_AXIS:
                    vec.y = edit.angle;
                    break;
                case Z_AXIS:
                    vec.z = edit.angle;
                    break;
            }
        }

        return this.poseDataCache;
    }

    public void liveEdit(String partName, RotationAxis axis, float angle) {
        this.editingData.put(partName, new Edit(axis, angle));
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

    @Value public static class Record { String part; Vector3f angle; }

    @AllArgsConstructor private static class Edit { RotationAxis axis; float angle; }
}
