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

public enum TransformTypeModelLoader implements ICustomModelLoader {
    INSTANCE;
    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        //If the resource location ends with `.ttm`, allow it to be used here
        return modelLocation.getResourcePath().endsWith(".ttm");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        //Reformat the model location to remove the `.ttm` suffix and add the `.json`. Also, if the model path does not start with `models/`, add it.
        modelLocation = new ResourceLocation(modelLocation.getResourceDomain(), (modelLocation.getResourcePath().startsWith("models/") ? "" : "models/") + modelLocation.getResourcePath().substring(0, modelLocation.getResourcePath().length() - 4) + ".json");
        //Create the input stream used to read the json file
        @Cleanup InputStream inputStream = Minecraft.getMinecraft().getResourceManager().getResource(modelLocation).getInputStream();
        //Create the input stream reader for that input stream
        @Cleanup InputStreamReader reader = new InputStreamReader(inputStream);
        //Load the Json object from the reader
        JsonObject json = JsonUtils.getJsonObject(new JsonParser().parse(reader), "root");
        //Create a new map of transform types to unbaked models
        Map<ItemCameraTransforms.TransformType, IModel> modelMap = Maps.newEnumMap(ItemCameraTransforms.TransformType.class);
        //Loads in the default model
        IModel defaultModel = ModelLoaderRegistry.getModel(new ResourceLocation(JsonUtils.getString(json, "deafult_model")));
        //Iterates through every override
        for (val transformType : ItemCameraTransforms.TransformType.values()) {
            //Gets the formatted name of the type transform
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
                    //Catches the error, instead of letting the whole model fail
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
