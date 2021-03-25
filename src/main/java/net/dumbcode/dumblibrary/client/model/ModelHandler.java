package net.dumbcode.dumblibrary.client.model;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.model.command.ModelCommandLoader;
import net.dumbcode.dumblibrary.client.model.dcm.baked.DCMModelHandler;
import net.dumbcode.dumblibrary.client.model.transformtype.TransformTypeModelLoader;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class ModelHandler {
    public static void onModelReady(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(DumbLibrary.MODID, "dcm"), DCMModelHandler.INSTANCE);
        ModelLoaderRegistry.registerLoader(new ResourceLocation(DumbLibrary.MODID, "command"), ModelCommandLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(new ResourceLocation(DumbLibrary.MODID, "transform_type"), TransformTypeModelLoader.INSTANCE);
    }
}
