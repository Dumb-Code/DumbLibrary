package net.dumbcode.dumblibrary.server;

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
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        System.out.println(ModelHandler.class);
        switch (ID) {
            case 0:
                int handOrdinal = x;
                ItemStack stack = player.getHeldItem(EnumHand.values()[handOrdinal]);
                if(stack.getItem() instanceof GuidebookItem) {
                    return new GuiGuidebook(((GuidebookItem) stack.getItem()).getBookData());
                }
                return null;
        }
        return null;
    }
}
