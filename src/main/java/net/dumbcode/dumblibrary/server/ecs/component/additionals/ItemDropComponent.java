package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

public interface ItemDropComponent extends EntityComponent {
    void collectItems(ComponentAccess access, Consumer<ItemStack> itemPlacer);
}
