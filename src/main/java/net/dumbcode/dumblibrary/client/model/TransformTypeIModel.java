package net.dumbcode.dumblibrary.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.With;
import lombok.val;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * The IModel for the transform type models. Used to store the default IModel and the overrides
 *
 * @author Wyn Price
 * @see TransformTypeModelLoader
 */
@With
public class TransformTypeIModel implements IModel {

    private IModel defaultModel;
    private final Map<ItemCameraTransforms.TransformType, IModel> overrides;

    public TransformTypeIModel(IModel defaultModel, Map<ItemCameraTransforms.TransformType, IModel> overrides) {
        this.defaultModel = defaultModel;
        this.overrides = overrides;
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        //Bake the default model
        IBakedModel model = defaultModel.bake(state, format, bakedTextureGetter);
        //Create a new map, to add the baked overrides to
        Map<ItemCameraTransforms.TransformType, IBakedModel> modelOverrides = Maps.newEnumMap(ItemCameraTransforms.TransformType.class);
        //Iterate through every entry in the overrides
        for (val entry : this.overrides.entrySet()) {
            //Bake the override, and put it in the modelOverrides map
            modelOverrides.put(entry.getKey(), entry.getValue().bake(state, format, bakedTextureGetter));
        }
        //Create a new BakedModel
        return new TransformTypeBakedModel(model, modelOverrides);
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        //Create a copy of the default models textures
        List<ResourceLocation> collection = Lists.newArrayList(this.defaultModel.getTextures());
        //Iterate through the overrides
        for (IModel iModel : this.overrides.values()) {
            //Iterate through the overrides textures
            for (ResourceLocation location : iModel.getTextures()) {
                //Make sure the collection doesn't already contain the texture
                if (!collection.contains(location)) {
                    //Add the texture to the collection
                    collection.add(location);
                }
            }
        }
        return collection;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        //Create a copy of the default models dependencies
        List<ResourceLocation> collection = Lists.newArrayList(this.defaultModel.getDependencies());
        //Iterate through the overrides
        for (IModel iModel : this.overrides.values()) {
            //Iterate through the overrides dependencies
            for (ResourceLocation location : iModel.getDependencies()) {
                //Make sure the collection doesn't already contain the dependency
                if (!collection.contains(location)) {
                    //Add the dependency to the collection
                    collection.add(location);
                }
            }
        }
        return collection;
    }

    @Override
    public IModel uvlock(boolean value) {
        //Apply the uvlock to all the models, including the overrides
        return this.withDefaultModel(this.defaultModel.uvlock(value)).withOverrides(this.transform(model -> model.uvlock(value)));
    }

    @Override
    public IModel smoothLighting(boolean value) {
        //Apply the smoothLighting to all the models, including the overrides
        return this.withDefaultModel(this.defaultModel.smoothLighting(value)).withOverrides(this.transform(model -> model.smoothLighting(value)));
    }

    @Override
    public IModel process(ImmutableMap<String, String> customData) {
        //Apply the process to all the models, including the overrides
        return this.withDefaultModel(this.defaultModel.process(customData)).withOverrides(this.transform(model -> model.process(customData)));
    }

    @Override
    public IModel gui3d(boolean value) {
        //Apply the gui3d to all the models, including the overrides
        return this.withDefaultModel(this.defaultModel.gui3d(value)).withOverrides(this.transform(model -> model.gui3d(value)));

    }

    @Override
    public IModel retexture(ImmutableMap<String, String> textures) {
        //Apply the retexture to all the models, including the overrides
        return this.withDefaultModel(this.defaultModel.retexture(textures)).withOverrides(this.transform(model -> model.retexture(textures)));
    }

    private Map<ItemCameraTransforms.TransformType, IModel> transform(UnaryOperator<IModel> mapper) {
        Map<ItemCameraTransforms.TransformType, IModel> map = Maps.newHashMap();
        for (Map.Entry<ItemCameraTransforms.TransformType, IModel> entry : this.overrides.entrySet()) {
            map.put(entry.getKey(), mapper.apply(entry.getValue()));
        }
        return map;
    }
}
