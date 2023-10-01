package net.dumbcode.dumblibrary.server.ecs.item;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.ComponentMapWriteAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentMap;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.Optional;

public class ItemCompoundAccess extends EntityComponentMap {

    private static final String TAG_NAME = "component";

    private final transient CompoundNBT base;

    public ItemCompoundAccess(ItemStack stack) {
        this(stack.getOrCreateTagElement(DumbLibrary.MODID));
    }

    public ItemCompoundAccess(CompoundNBT base) {
        this.base = base;
        this.deserialize(this.base.getList(TAG_NAME, Constants.NBT.TAG_COMPOUND));
    }


    public static Access getOrCreateAccess(ItemStack stack) {
        return new ItemCompoundAccess(stack).new Access();
    }

    public static Optional<ComponentAccess> getAccess(ItemStack stack) {
        CompoundNBT compound = stack.getTag();
        if(compound == null || !compound.contains(DumbLibrary.MODID, Constants.NBT.TAG_COMPOUND)) {
            return Optional.empty();
        }
        CompoundNBT tag = compound.getCompound(DumbLibrary.MODID);
        if(!tag.contains(TAG_NAME, Constants.NBT.TAG_LIST)) {
            return Optional.empty();
        }

        ListNBT list = tag.getList(TAG_NAME, Constants.NBT.TAG_COMPOUND);

        return list.isEmpty() ? Optional.empty() : Optional.of(new ItemCompoundAccess(tag).new Access());

    }

    public void saveAll() {
        this.base.put(TAG_NAME, this.serialize(new ListNBT()));
    }

    public class Access implements ComponentMapWriteAccess {

        @Override
        public EntityComponentMap getComponentMap() {
            return ItemCompoundAccess.this;
        }

        @Override
        public boolean contains(EntityComponentType<?, ?> type) {
            return base.getAllKeys().contains(type.getIdentifier().toString());
        }

        public void saveAll() {
            ItemCompoundAccess.this.saveAll();
        }
    }


}
