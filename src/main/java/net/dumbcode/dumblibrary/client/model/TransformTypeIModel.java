package net.dumbcode.dumblibrary.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
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
        IBakedModel defaultModel = this.defaultModel.bake(state, format, bakedTextureGetter);
        //Create a new map, to add the baked overrides to
        Map<ItemCameraTransforms.TransformType, IBakedModel> modelOverrides = Maps.newEnumMap(ItemCameraTransforms.TransformType.class);
        //Iterate through every entry in the overrides
        for (val entry : this.overrides.entrySet()) {
            //Bake the override, and put it in the modelOverrides map
            modelOverrides.put(entry.getKey(), entry.getValue().bake(state, format, bakedTextureGetter));
        }
        //Create a new BakedModel
        return new TransformTypeBakedModel(defaultModel, modelOverrides);
    }

    public Collection<ResourceLocation> getTextures() {
        //Create a copy of the default models textures
        List<ResourceLocation> collection = Lists.newArrayList(this.defaultModel.getTextures());
        //Iterate through the overrides
        for (IModel iModel : this.overrides.values()) {
            //Iterate through the overrides textures
            for (ResourceLocation location : iModel.getTextures()) {
                //Make sure the collection doesn't already contain the texture
                if(!collection.contains(location)) {
                    //Add the texture to the collection
                    collection.add(location);
                }
            }
        }
        return collection;
    }

    public Collection<ResourceLocation> getDependencies() {
        //Create a copy of the default models dependencies
        List<ResourceLocation> collection = Lists.newArrayList(this.defaultModel.getDependencies());
        //Iterate through the overrides
        for (IModel iModel : this.overrides.values()) {
            //Iterate through the overrides dependencies
            for (ResourceLocation location : iModel.getDependencies()) {
                //Make sure the collection doesn't already contain the dependency
                if(!collection.contains(location)) {
                    //Add the dependency to the collection
                    collection.add(location);
                }
            }
        }
        return collection;
    }

    public IModel uvlock(boolean value) {
        //Apply the uvlock to all the models, including the overrides
        this.defaultModel = this.defaultModel.uvlock(value);
        this.overrides.replaceAll((transformType, iModel) -> iModel.uvlock(value));
        return this;
    }

    public IModel smoothLighting(boolean value) {
        //Apply the smoothLighting to all the models, including the overrides
        this.defaultModel = this.defaultModel.smoothLighting(value);
        this.overrides.replaceAll((transformType, iModel) -> iModel.smoothLighting(value));
        return this;
    }

    public IModel process(ImmutableMap<String, String> customData) {
        //Apply the process to all the models, including the overrides
        this.defaultModel = this.defaultModel.process(customData);
        this.overrides.replaceAll((transformType, iModel) -> iModel.process(customData));
        return this;
    }

    public IModel gui3d(boolean value) {
        //Apply the gui3d to all the models, including the overrides
        this.defaultModel = this.defaultModel.gui3d(value);
        this.overrides.replaceAll((transformType, iModel) -> iModel.gui3d(value));
        return this;
    }

    public IModel retexture(ImmutableMap<String, String> textures) {
        //Apply the retexture to all the models, including the overrides
        this.defaultModel = this.defaultModel.retexture(textures);
        this.overrides.replaceAll((transformType, iModel) -> iModel.retexture(textures));
        return this;
    }
}
