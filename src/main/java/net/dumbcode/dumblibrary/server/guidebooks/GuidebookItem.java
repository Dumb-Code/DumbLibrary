package net.dumbcode.dumblibrary.server.guidebooks;

import lombok.Getter;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class GuidebookItem extends Item {

    @Getter
    private final Guidebook bookData;

    public GuidebookItem(Guidebook bookData) {
        this.bookData = bookData;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        playerIn.openGui(DumbLibrary.MOD_INSTANCE, 0, worldIn, handIn.ordinal(), -1, 0);
        return ActionResult.newResult(EnumActionResult.SUCCESS, itemstack);
    }
}
