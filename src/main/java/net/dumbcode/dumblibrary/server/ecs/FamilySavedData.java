package net.dumbcode.dumblibrary.server.ecs;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import java.io.File;
import java.util.List;
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
    public CompoundNBT save(CompoundNBT compound) {
        if(this.parentOne != null) {
            compound.putUUID("parent_1", this.parentOne);
        }
        if(this.parentTwo != null) {
            compound.putUUID("parent_2", this.parentTwo);
        }
        compound.put("children", HerdSavedData.saveList(this.children));
        compound.put("enemies", HerdSavedData.saveList(this.enemies));
        return compound;
    }

    @Override
    public void load(CompoundNBT nbt) {
        this.parentOne = nbt.hasUUID("parent_1") ? nbt.getUUID("parent_1") : null;
        this.parentTwo = nbt.hasUUID("parent_2") ? nbt.getUUID("parent_2") : null;
        HerdSavedData.loadList(nbt.getCompound("children"), this.children);
        HerdSavedData.loadList(nbt.getCompound("enemies"), this.enemies);
    }

    public static FamilySavedData getData(ServerWorld world, UUID familyUUID) {
        String identifier = "family_data/" + familyUUID.toString().replaceAll("-", "");
        ensureFamilyFolder(world, identifier);

        FamilySavedData data = world.getDataStorage().computeIfAbsent(() -> new FamilySavedData(identifier), identifier);
        data.familyUUID = familyUUID;
        return data;
    }

    private static void ensureFamilyFolder(ServerWorld world, String identifier) {
        File file = world.getDataStorage().getDataFile(identifier).getParentFile();
        if(!file.exists()) {
            file.mkdirs();
        }
    }
}
