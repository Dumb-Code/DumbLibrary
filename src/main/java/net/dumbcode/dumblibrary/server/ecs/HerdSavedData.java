package net.dumbcode.dumblibrary.server.ecs;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.ecs.component.impl.HerdComponent;
import net.dumbcode.dumblibrary.server.utils.WorldUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class HerdSavedData extends WorldSavedData {

    @Getter private UUID herdUUID;
    public UUID leader;
    @Getter private List<UUID> members = Lists.newArrayList();
    @Getter private List<UUID> enemies = Lists.newArrayList();
    public int tryMoveCooldown;

    public HerdSavedData(String name) {
        super(name);
    }

    public void addMember(UUID uuid, HerdComponent herd) {
        herd.herdUUID = this.herdUUID;
        herd.setHerdData(this);
        this.members.add(uuid);
        this.setDirty();
    }

    public void removeMember(UUID uuid, HerdComponent herd) {
        herd.herdUUID = null;
        herd.clearHerd();
        this.members.remove(uuid);
        this.setDirty();
    }


    public void addEnemy(UUID uuid) {
        this.enemies.add(uuid);
        this.setDirty();
    }

    public void removeEnemy(UUID uuid) {
        this.enemies.remove(uuid);
        this.setDirty();
    }

    public void pickNewLeader(World world) {
        if(!this.members.isEmpty()) {
            WorldUtils.getEntityFromUUID(world, this.members.get(0)).ifPresent(e -> this.leader = e.getUUID());
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        this.leader = nbt.getUUID("leader");

        loadList(nbt.getCompound("members"), this.members);
        loadList(nbt.getCompound("enemies"), this.enemies);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        compound.putUUID("leader", this.leader);

        compound.put("members", saveList(this.members));
        compound.put("enemies", saveList(this.enemies));

        return compound;
    }



    public static CompoundTag saveList(List<UUID> list) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("size", list.size());
        for (int i = 0; i < list.size(); i++) {
            nbt.putUUID(String.valueOf(i), list.get(i));
        }
        return nbt;
    }

    public static void loadList(CompoundTag nbt, List<UUID> list) {
        list.clear();
        for (int i = 0; i < nbt.getInt("size"); i++) {
            list.add(nbt.getUUID(String.valueOf(i)));
        }
    }

    public static HerdSavedData getData(UUID herdUUID) {
        String identifier = "herd_data/" + herdUUID.toString().replaceAll("-", "");
        ensureHerdFolder(identifier);

        HerdSavedData data = ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage().computeIfAbsent(() -> new HerdSavedData(identifier), identifier);
        data.herdUUID = herdUUID;
        return data;
    }

    private static void ensureHerdFolder(String identifier) {
        File file = ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage().getDataFile(identifier).getParentFile();
        if(!file.exists()) {
            file.mkdirs();
        }
    }
}