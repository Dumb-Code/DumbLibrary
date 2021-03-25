package net.dumbcode.dumblibrary.client.model.command;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.state.Property;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.*;
import java.util.function.Function;

/**
 * Mockup:
 * <pre>
 *{
 * 	"model": "some_model",
 * 	"model_blockstate_cache": true,
 * 	"commands": [{
 * 			"command": "translate",
 * 			"amount": [0, 8, 0]
 *                },
 *        {
 * 			"command": "rotate",
 * 			"degrees": [0, 8, 0],
 * 			"origin": [51, 2, 1]
 *        },
 *        {
 * 			"command": "rotate",
 * 			"values": [{
 * 				"name": "rotYValue",
 * 				"derived_from": "blockstate",
 * 				"blockstate_key": "rotation"
 *            }],
 * 			"cache_transform": {
 * 				"from": 0,
 * 				"to": 360,
 * 				"step": 1
 *            },
 * 			"origin": [0, 8, 0],
 * 			"degrees": [0, "$rotYValue", 0]
 *        }
 * 	]
 * }
 * </pre>
 *
 * @author Wyn Price
 */
public enum ModelCommandLoader implements IModelLoader<ModelCommandLoader.CommandModelGeometry> {
    INSTANCE;

    @Override
    public CommandModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        ResourceLocation model = new ResourceLocation(JSONUtils.getAsString(modelContents, "model"));
        boolean modelBlockstateCache = JSONUtils.getAsBoolean(modelContents, "model_blockstate_cache", false);
        List<CommandEntry> commandEntries = new ArrayList<>();
        for (JsonElement element : modelContents.getAsJsonArray("commands")) {
            JsonObject object = element.getAsJsonObject();

            ModelCommandRegistry.Command command = ModelCommandRegistry.get(JSONUtils.getAsString(object, "command"), object);
            Map<String, CommandDerived.Derived<?>> variables = new HashMap<>();
            if(object.has("values")) {
                for (JsonElement values : JSONUtils.getAsJsonArray(object, "values")) {
                    JsonObject valueObject = values.getAsJsonObject();
                    CommandDerived.Derived<?> type = CommandDerived.getType(valueObject);
                    if(type != null) {
                        variables.put(JSONUtils.getAsString(valueObject, "name"), type);
                    }
                }
            }
            commandEntries.add(new CommandEntry(command, variables));
        }
        return new CommandModelGeometry(model, modelBlockstateCache, commandEntries);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
//        this.manager = resourceManager;
    }


    @Data
    public static class CommandEntry {
        private final ModelCommandRegistry.Command command;
        private final Map<String, CommandDerived.Derived<?>> valueSupplier;
    }


    @RequiredArgsConstructor
    public static class CommandModelGeometry implements IModelGeometry<CommandModelGeometry> {
        private final ResourceLocation delegateLocation;
        private final boolean modelBlockstateCache;
        private final List<CommandEntry> commands;

        private IUnbakedModel delegate;

        @Override
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
            IBakedModel model = this.delegate.bake(bakery, spriteGetter, modelTransform, modelLocation);
            return new ModelCommandModel(model, this.modelBlockstateCache, this.commands);
        }

        @Override
        public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            this.delegate = modelGetter.apply(this.delegateLocation);
            return this.delegate.getMaterials(modelGetter, missingTextureErrors);
        }


    }
}
