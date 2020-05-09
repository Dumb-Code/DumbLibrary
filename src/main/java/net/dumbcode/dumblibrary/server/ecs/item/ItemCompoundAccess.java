package net.dumbcode.dumblibrary.server.ecs.item;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.ComponentMapWriteAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentMap;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.Optional;

public class ItemCompoundAccess extends EntityComponentMap {

    private static final String TAG_NAME = "component";

    private final transient NBTTagCompound base;

    public ItemCompoundAccess(ItemStack stack) {
        this(stack.getOrCreateSubCompound(DumbLibrary.MODID));
    }

    public ItemCompoundAccess(NBTTagCompound base) {
        this.base = base;
        this.deserialize(this.base.getTagList(TAG_NAME, Constants.NBT.TAG_COMPOUND));
    }


    public static Access getOrCreateAccess(ItemStack stack) {
        return new ItemCompoundAccess(stack).new Access();
    }

    public static Optional<ComponentAccess> getAccess(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if(compound == null || !compound.hasKey(DumbLibrary.MODID, Constants.NBT.TAG_COMPOUND)) {
            return Optional.empty();
        }
        NBTTagCompound tag = compound.getCompoundTag(DumbLibrary.MODID);
        if(!tag.hasKey(TAG_NAME, Constants.NBT.TAG_LIST)) {
            return Optional.empty();
        }

        NBTTagList list = tag.getTagList(TAG_NAME, Constants.NBT.TAG_COMPOUND);

        return list.isEmpty() ? Optional.empty() : Optional.of(new ItemCompoundAccess(tag).new Access());

    }

    public void saveAll() {
        this.base.setTag(TAG_NAME, this.serialize(new NBTTagList()));
    }

    public class Access implements ComponentMapWriteAccess {

        @Override
        public EntityComponentMap getComponentMap() {
            return ItemCompoundAccess.this;
        }

        @Override
        public boolean contains(EntityComponentType<?, ?> type) {
            return base.getKeySet().contains(type.getIdentifier().toString());
        }

        public void saveAll() {
            ItemCompoundAccess.this.saveAll();
        }
    }


}
