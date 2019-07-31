package net.dumbcode.dumblibrary.server.entity;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

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

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return null;
    }

    public static HerdSavedData getData(World world, UUID herdUUID) {
        return (HerdSavedData) Objects.requireNonNull(world.getMapStorage()).getOrLoadData(HerdSavedData.class, "herd_data/" + herdUUID);
    }
}