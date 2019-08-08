package net.dumbcode.dumblibrary.server.entity;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class HerdSavedData extends WorldSavedData {

    public UUID leader;
    @Getter private List<UUID> members = Lists.newArrayList();
    @Getter private List<UUID> enemies = Lists.newArrayList();
    public int tryMoveCooldown;

    public HerdSavedData(String name) {
        super(name);
    }

    public void addMember(UUID uuid) {
        this.members.add(uuid);
        this.markDirty();
    }

    public void removeMember(UUID uuid) {
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


    private NBTTagCompound saveList(List<UUID> list) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("size", list.size());
        for (int i = 0; i < list.size(); i++) {
            nbt.setUniqueId(String.valueOf(i), list.get(i));
        }
        return nbt;
    }

    private static void loadList(NBTTagCompound nbt, List<UUID> list) {
        list.clear();
        for (int i = 0; i < nbt.getInteger("size"); i++) {
            list.add(nbt.getUniqueId(String.valueOf(i)));
        }
    }

    public static HerdSavedData getData(World world, UUID herdUUID) {
        String identifier = "herd_data/" + herdUUID.toString().replaceAll("-", "");
        ensureHerdFolder(world, identifier);
        HerdSavedData data = (HerdSavedData) Objects.requireNonNull(world.getMapStorage()).getOrLoadData(HerdSavedData.class, identifier);
        if(data == null) {
            data = new HerdSavedData(identifier);
            world.getMapStorage().setData(identifier, data);
        }
        return data;
    }

    private static void ensureHerdFolder(World world, String identifier) {
        File file = world.getSaveHandler().getMapFileFromName(identifier).getParentFile();
        if(!file.exists()) {
            file.mkdirs();
        }
    }
}