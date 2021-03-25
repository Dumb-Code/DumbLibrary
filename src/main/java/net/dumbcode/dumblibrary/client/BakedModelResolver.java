package net.dumbcode.dumblibrary.client;

import lombok.Getter;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Getter
public class BakedModelResolver {
    private static final List<BakedModelResolver> RESOLVERS = new ArrayList<>();

    private final ResourceLocation location;

    private IUnbakedModel unbakedModel;
    private IBakedModel model;

    public BakedModelResolver(ResourceLocation location) {
        this.location = location;
        RESOLVERS.add(this);
    }

    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        for (BakedModelResolver resolver : RESOLVERS) {
            resolver.unbakedModel = ModelLoader.defaultModelGetter().apply(resolver.location);
            for (RenderMaterial material : resolver.unbakedModel.getMaterials(ModelLoader.defaultModelGetter(), new HashSet<>())) {
                event.addSprite(material.texture());
            }
        }
    }

    public static void onModelBake(ModelBakeEvent event) {
        for (BakedModelResolver resolver : RESOLVERS) {
            resolver.model = resolver.unbakedModel.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(), ModelRotation.X0_Y0, resolver.location);
        }
    }

}
