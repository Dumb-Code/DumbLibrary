package net.dumbcode.dumblibrary.server.entity.component;

import net.dumbcode.dumblibrary.client.component.RenderComponentContext;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface RenderComponent extends EntityComponent {
    @SideOnly(Side.CLIENT)
    void applyContext(Entity entity, RenderComponentContext context);
}
