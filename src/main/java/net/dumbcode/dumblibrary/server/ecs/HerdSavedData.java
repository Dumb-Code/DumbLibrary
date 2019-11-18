package net.dumbcode.dumblibrary.server.ecs;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.ecs.component.impl.HerdComponent;
import net.dumbcode.dumblibrary.server.utils.WorldUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

import java.io.File;
import java.util.List;
import java.util.Objects;
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
        this.markDirty();
    }

    public void removeMember(UUID uuid, HerdComponent herd) {
        herd.herdUUID = null;
        herd.clearHerd();
        this.members.remove(uuid);
        this.markDirty();
    }


    public void addEnemy(UUID uuid) {
        this.enemies.add(uuid);
        this.markDirty();
    }

    public void removeEnemy(UUID uuid) {
        this.enemies.remove(uuid);
        this.markDirty();
    }

    public void pickNewLeader(World world) {
        if(!this.members.isEmpty()) {
            WorldUtils.getEntityFromUUID(world, this.members.get(0)).ifPresent(e -> this.leader = e.getUniqueID());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.leader = nbt.getUniqueId("leader");

        loadList(nbt.getCompoundTag("members"), this.members);
        loadList(nbt.getCompoundTag("enemies"), this.enemies);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setUniqueId("leader", this.leader);

        compound.setTag("members", saveList(this.members));
        compound.setTag("enemies", saveList(this.enemies));

        return compound;
    }


    public static NBTTagCompound saveList(List<UUID> list) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("size", list.size());
        for (int i = 0; i < list.size(); i++) {
            nbt.setUniqueId(String.valueOf(i), list.get(i));
        }
        return nbt;
    }

    public static void loadList(NBTTagCompound nbt, List<UUID> list) {
        list.clear();
        for (int i = 0; i < nbt.getInteger("size"); i++) {
            list.add(nbt.getUniqueId(String.valueOf(i)));
        }
    }

    public static HerdSavedData getData(UUID herdUUID) {
        World world = DimensionManager.getWorld(0);
        String identifier = "herd_data/" + herdUUID.toString().replaceAll("-", "");
        ensureHerdFolder(world, identifier);

        HerdSavedData data = (HerdSavedData) Objects.requireNonNull(world.getMapStorage()).getOrLoadData(HerdSavedData.class, identifier);
        if(data == null) {
            data = new HerdSavedData(identifier);
            world.getMapStorage().setData(identifier, data);
        }
        data.herdUUID = herdUUID;
        return data;
    }

    private static void ensureHerdFolder(World world, String identifier) {
        File file = world.getSaveHandler().getMapFileFromName(identifier).getParentFile();
        if(!file.exists()) {
            file.mkdirs();
        }
    }
}