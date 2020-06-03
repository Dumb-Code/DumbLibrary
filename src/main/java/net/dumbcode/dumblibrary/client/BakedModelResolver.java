package net.dumbcode.dumblibrary.client;

import lombok.Getter;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Getter
@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class BakedModelResolver {
    private static final List<BakedModelResolver> RESOLVERS = new ArrayList<>();

    private final ResourceLocation location;

    private IModel iModel;
    private IBakedModel model;

    public BakedModelResolver(ResourceLocation location) {
        this.location = location;
        RESOLVERS.add(this);
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        for (BakedModelResolver resolver : RESOLVERS) {
            try {
                resolver.iModel = ModelLoaderRegistry.getModel(resolver.location);
            } catch (Exception e) {
                DumbLibrary.getLogger().error("Unable to get model at " + resolver.location, e);
                resolver.iModel = ModelLoaderRegistry.getMissingModel();
            }
            for (ResourceLocation texture : resolver.iModel.getTextures()) {
                event.getMap().registerSprite(texture);
            }
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        for (BakedModelResolver resolver : RESOLVERS) {
            resolver.model = resolver.iModel.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
        }
    }

}
