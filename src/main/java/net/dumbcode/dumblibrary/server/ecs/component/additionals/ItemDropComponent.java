package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public interface ItemDropComponent {
    void collectItems(ComponentAccess access, Consumer<ItemStack> itemPlacer);
}
