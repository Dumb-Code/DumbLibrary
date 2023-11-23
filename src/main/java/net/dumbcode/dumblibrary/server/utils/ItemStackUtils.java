package net.dumbcode.dumblibrary.server.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Iterator;

public class ItemStackUtils {
    public static boolean compareControlledNbt(@Nullable INBT controlled, @Nullable INBT tested) {
        if(controlled == null) {
            return true;
        }
        if(tested == null) {
            return false;
        }
        if(controlled.getId() != tested.getId()) {
            return false;
        }

        switch (controlled.getId()) {
            case Constants.NBT.TAG_END: return true;
            case Constants.NBT.TAG_LIST:
                ListNBT controlledList = (ListNBT) controlled;
                ListNBT testedList = (ListNBT) tested;
                if(controlledList.size() != testedList.size() || controlledList.getType() != testedList.getType()) {
                    return false;
                }
                Iterator<INBT> citer = controlledList.iterator();
                Iterator<INBT> titer = testedList.iterator();
                while (citer.hasNext() && titer.hasNext()) {
                    if(!compareControlledNbt(citer.next(), titer.next())) {
                        return false;
                    }
                }
                return true;
            case Constants.NBT.TAG_COMPOUND:
                CompoundTag controlledTag = (CompoundTag) controlled;
                CompoundTag testedTag = (CompoundTag) tested;
                for (String key : controlledTag.getAllKeys()) {
                    if(!compareControlledNbt(controlledTag.get(key), testedTag.get(key))) {
                        return false;
                    }
                }
                return true;
            default: return controlled.equals(tested);
        }
    }
}
