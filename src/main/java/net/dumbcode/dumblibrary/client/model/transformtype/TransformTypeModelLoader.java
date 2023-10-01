package net.dumbcode.dumblibrary.client.model.transformtype;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;

import java.util.Locale;
import java.util.Map;

/**
 * Transform type models are a way of applying override models for certain {@link ItemCameraTransforms.TransformType} in a model. <br>
 * To have the transform type model applied, simply just add ".ttm" to the end of you model name. (Don't replace a file name if there is one)
 * An example of a model json is as follows:
 * <pre>{@code
 *  {
 *   "default_model": "minecraft:item/apple",
 *   "gui": "minecraft:item/stick",
 *   "ground": "minecraft:item/stone"
 * }
 * }</pre>
 * In this example, the default model will be an apple, however when the model is displayed in the gui, it will look like a stick, and when dropped on the floor will look like a stone block.
 *
 * @author Wyn Price
 */
public enum TransformTypeModelLoader implements IModelLoader<TransformTypeGeometry> {
    INSTANCE;

    @Override
    public TransformTypeGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        ResourceLocation model = new ResourceLocation(JSONUtils.getAsString(modelContents, "default_model"));

        Map<ItemCameraTransforms.TransformType, ResourceLocation> modelMap = Maps.newEnumMap(ItemCameraTransforms.TransformType.class);
        for (ItemCameraTransforms.TransformType transformType : ItemCameraTransforms.TransformType.values()) {
            String typeName = transformType.name().toLowerCase(Locale.ROOT);
            if(modelContents.has(typeName)) {
                modelMap.put(transformType, new ResourceLocation(JSONUtils.getAsString(modelContents, typeName)));
            }
        }

        return new TransformTypeGeometry(model, modelMap);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }
}
