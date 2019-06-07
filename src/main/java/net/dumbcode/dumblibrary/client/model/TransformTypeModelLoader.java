package net.dumbcode.dumblibrary.client.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Cleanup;
import lombok.val;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import java.io.InputStream;
import java.io.InputStreamReader;
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
 * @author Wyn Price
 */
public enum TransformTypeModelLoader implements ICustomModelLoader {
    INSTANCE;
    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        //If the resource location ends with `.ttm`, allow it to be used here
        return modelLocation.getPath().endsWith(".ttm");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        //Reformat the model location to remove the `.ttm` suffix and add the `.json`. Also, if the model path does not start with `models/`, add it.
        modelLocation = new ResourceLocation(modelLocation.getNamespace(), (modelLocation.getPath().startsWith("models/") ? "" : "models/") + modelLocation.getPath().substring(0, modelLocation.getPath().length() - 4) + ".json");

        @Cleanup InputStream inputStream = Minecraft.getMinecraft().getResourceManager().getResource(modelLocation).getInputStream();
        @Cleanup InputStreamReader reader = new InputStreamReader(inputStream);
        JsonObject json = JsonUtils.getJsonObject(new JsonParser().parse(reader), "root");
        Map<ItemCameraTransforms.TransformType, IModel> modelMap = Maps.newEnumMap(ItemCameraTransforms.TransformType.class);
        //get the default model
        IModel defaultModel = ModelLoaderRegistry.getModel(new ResourceLocation(JsonUtils.getString(json, "default_model")));
        for (ItemCameraTransforms.TransformType transformType : ItemCameraTransforms.TransformType.values()) {
            String typeName = transformType.name().toLowerCase(Locale.ROOT);
            //If the json object has the type as a string
            if(JsonUtils.isString(json, typeName)) {
                //Create the resource location from the string
                ResourceLocation location = new ResourceLocation(JsonUtils.getString(json, typeName));
                IModel model;
                try {
                    //Loads the model
                    model = ModelLoaderRegistry.getModel(location);
                } catch (Exception e) {
                    //Catches the error for this model, instead of letting the whole model fail
                    DumbLibrary.getLogger().error("[TTM] Unable to get sub-model " + location.toString() + " for model " + modelLocation.toString(), e );
                    //Puts the model as the missing model, as to make it easier to see when somthing goes wrong
                    model = ModelLoaderRegistry.getMissingModel();
                }
                //Put the model into the override map
                modelMap.put(transformType, model);
            }
        }
        //Create the new IModel with the overrides
        return new TransformTypeIModel(defaultModel, modelMap);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }
}
