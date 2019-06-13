package net.dumbcode.dumblibrary.server;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.gui.GuiGuidebook;
import net.dumbcode.dumblibrary.client.model.ModelHandler;
import net.dumbcode.dumblibrary.server.guidebooks.GuidebookItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class DumbGuiHandler implements IGuiHandler {
    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        DumbLibrary.getLogger().debug(ModelHandler.class);
        if (id == 0) {
            ItemStack stack = player.getHeldItem(EnumHand.values()[x]);
            if (stack.getItem() instanceof GuidebookItem) {
                return new GuiGuidebook(((GuidebookItem) stack.getItem()).getBookData());
            }
            return null;
        }
        return null;
    }
}
