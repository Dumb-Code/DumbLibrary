package net.dumbcode.dumblibrary.client.model.transformtype;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.*;
import java.util.function.Function;

/**
 * The IModel for the transform type models. Used to store the default IModel and the overrides
 *
 * @author Wyn Price
 * @see TransformTypeModelLoader
 */
@RequiredArgsConstructor
public class TransformTypeGeometry implements IModelGeometry<TransformTypeGeometry> {

    private final ResourceLocation defaultModelLocation;
    private final Map<ItemCameraTransforms.TransformType, ResourceLocation> overrideLocations;

    private IUnbakedModel defaultModel;
    private final Map<ItemCameraTransforms.TransformType, IUnbakedModel> overrides = new HashMap<>();

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        IBakedModel model = this.defaultModel.bake(bakery, spriteGetter, modelTransform, this.defaultModelLocation);

        Map<ItemCameraTransforms.TransformType, IBakedModel> modelOverrides = Maps.newEnumMap(ItemCameraTransforms.TransformType.class);
        this.overrides.forEach((transformType, unbaked) -> modelOverrides.put(transformType, unbaked.bake(bakery, spriteGetter, modelTransform, this.overrideLocations.get(transformType))));

        return new TransformTypeBakedModel(model, modelOverrides);
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        this.defaultModel = modelGetter.apply(this.defaultModelLocation);
        List<RenderMaterial> materials = new ArrayList<>(this.defaultModel.getMaterials(modelGetter, missingTextureErrors));

        this.overrideLocations.forEach((transformType, resourceLocation) -> {
            IUnbakedModel unbakedModel = modelGetter.apply(resourceLocation);
            this.overrides.put(transformType, unbakedModel);
            materials.addAll(unbakedModel.getMaterials(modelGetter, missingTextureErrors));

        });

        return materials;
    }
}
