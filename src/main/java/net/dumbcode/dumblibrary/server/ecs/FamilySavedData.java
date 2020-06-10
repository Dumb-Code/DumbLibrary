package net.dumbcode.dumblibrary.server.ecs;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class FamilySavedData extends WorldSavedData {
    private UUID familyUUID;

    private UUID parentOne;
    private UUID parentTwo;

    private final List<UUID> children = Lists.newArrayList();
    private final List<UUID> enemies = Lists.newArrayList();

    public FamilySavedData(String name) {
        super(name);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if(this.parentOne != null) {
            compound.setUniqueId("parent_1", this.parentOne);
        }
        if(this.parentTwo != null) {
            compound.setUniqueId("parent_2", this.parentTwo);
        }
        compound.setTag("children", HerdSavedData.saveList(this.children));
        compound.setTag("enemies", HerdSavedData.saveList(this.enemies));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.parentOne = nbt.hasUniqueId("parent_1") ? nbt.getUniqueId("parent_1") : null;
        this.parentTwo = nbt.hasUniqueId("parent_2") ? nbt.getUniqueId("parent_2") : null;
        HerdSavedData.loadList(nbt.getCompoundTag("children"), this.children);
        HerdSavedData.loadList(nbt.getCompoundTag("enemies"), this.enemies);
    }

    public static FamilySavedData getData(UUID familyUUID) {
        World world = DimensionManager.getWorld(0);
        String identifier = "family_data/" + familyUUID.toString().replaceAll("-", "");
        ensurefamilyFolder(world, identifier);

        FamilySavedData data = (FamilySavedData) Objects.requireNonNull(world.getMapStorage()).getOrLoadData(FamilySavedData.class, identifier);
        if(data == null) {
            data = new FamilySavedData(identifier);
            world.getMapStorage().setData(identifier, data);
        }
        data.familyUUID = familyUUID;
        return data;
    }

    private static void ensurefamilyFolder(World world, String identifier) {
        File file = world.getSaveHandler().getMapFileFromName(identifier).getParentFile();
        if(!file.exists()) {
            file.mkdirs();
        }
    }
}
