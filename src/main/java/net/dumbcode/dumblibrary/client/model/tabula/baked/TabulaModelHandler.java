package net.dumbcode.dumblibrary.client.model.tabula.baked;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Cleanup;
import lombok.Data;
import lombok.Value;
import net.dumbcode.dumblibrary.client.animation.TabulaUtils;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelInformation;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import org.apache.commons.io.IOUtils;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TabulaModelHandler implements ICustomModelLoader {
    INSTANCE;

    private static final JsonParser PARSER = new JsonParser();
    private static final Pattern PATTERN = Pattern.compile("layer(\\d+)$");
    private final Set<String> namespaces = Sets.newHashSet();
    private IResourceManager manager;


    public void allow(String namespace) {
        this.namespaces.add(namespace);
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getPath().endsWith(".tbl");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        String path = modelLocation.getPath().replaceAll("\\.tbl", ".json");
        IResource resource = this.manager.getResource(new ResourceLocation(modelLocation.getNamespace(), path));
        @Cleanup InputStreamReader reader = new InputStreamReader(resource.getInputStream());
        String string = IOUtils.toString(reader);
        JsonObject json = PARSER.parse(string).getAsJsonObject();
        TabulaModelInformation information = TabulaUtils.getModelInformation(new ResourceLocation(JsonUtils.getString(json, "tabula")));
        ModelBlock modelBlock = ModelBlock.deserialize(string);
        List<TextureLayer> allTextures = Lists.newArrayList();
        for (String key : modelBlock.textures.keySet()) {
            Matcher matcher = PATTERN.matcher(key);
            if (matcher.matches()) {
                allTextures.add(new TextureLayer(new ResourceLocation(modelBlock.textures.get(key)), Integer.parseInt(matcher.group(1))));
            }
        }
        String particle = modelBlock.textures.get("particle");
        ResourceLocation part = particle == null ? new ResourceLocation("missingno") : new ResourceLocation(particle);
        return new TabulaIModel(Collections.unmodifiableList(allTextures), part, PerspectiveMapWrapper.getTransforms(modelBlock.getAllTransforms()), information, modelBlock.ambientOcclusion, modelBlock.isGui3d(), modelBlock.getOverrides());
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.manager = resourceManager;
    }

    @Data
    public static class TextureLayer{ private final ResourceLocation loc; private final int layer; private TextureAtlasSprite sprite; }
}
