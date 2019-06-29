package net.dumbcode.dumblibrary.server.entity.component.additionals;

import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponent;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public interface ItemDropComponent extends EntityComponent {
    void collectItems(ComponentAccess access, Consumer<ItemStack> itemPlacer);
}
