package net.dumbcode.dumblibrary.client.model;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.model.tabula.baked.TabulaModelHandler;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = DumbLibrary.MODID)
public class ModelHandler {

    @SubscribeEvent
    public static void onModelReady(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(TabulaModelHandler.INSTANCE);
    }
}
