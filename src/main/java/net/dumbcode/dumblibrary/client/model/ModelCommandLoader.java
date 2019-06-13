package net.dumbcode.dumblibrary.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model commands are a way to define commands within a blockstate definition or model name.
 * A model command has to be in the format name:args and must end with a ; EVEN if it is the last in the command array.
 * The args for a model command can be anything, as long as they don't contain a ; <br>
 * Commands can be chained, for example if I wanted to scale a model then rotate it, I can have the model as being:
 * <pre>{@code mymodid:mymodel##scale:0.5,2,1@0.5,0.5,0.5;rotate:0,20,0@0.5,0,0.5;.command }</pre>
 * This would first scale the model by [0.5, 2, 1] around the point [0.5, 0.5, 0.5], then rotate the model around [0.5, 0, 0.5] with angles [0, 20, 0] <br>
 * So far, these are the following commands:
 * <ul>
 * <li>Rotation - rotates the model around a point by given angles (in degrees). Format: <br>
 * {@code rotate:angle.x,angle.y,angle.z@point.x,point.y,point.z } <br>
 * <pre>Examples:
 *     rotate:25, 45, 90@0.22, 0.5, 0.75;   -- Rotates the model by [25, 45, 90] degrees around the point [0.22, 0.5, 0.75]
 *     rotate:0, -45, 0@0.5, 0.5, 0.5;      -- Rotates the model by [0, -45, 0] degrees around the point [0.5, 0.5, 0.5]
 *         </pre>
 * </li>
 * <li>Scale - scales the model from a certain point by a given amount in each axis: <br>
 * {@code scale:scale.x,scale.y,scale.z@point.x,point.y,point.z }
 * <pre>Examples:
 *     scale:2, 2, 2@0.5, 0.5, 0.5;         -- Scales the model by [2, 2, 2] around the point [0.5, 0.5, 0.5]. This is essentially doubling the size of the model.
 *     scale:0.5, 1, 0@1, 0.5, 0.25;        -- Scales the model by [0.5, 1, 0] around the point [1, 0.5, 0.25]. This removes the z depth the model had and would put all cubes on the z = 0.25 plane
 *         </pre>
 * </li>
 * </ul>
 *
 * @author Wyn Price
 */
public enum ModelCommandLoader implements ICustomModelLoader {
    INSTANCE;

    private final Pattern filter = Pattern.compile("(.+)##(.+)\\.command");
    private final Pattern command = Pattern.compile("(.+?):(.+?);");

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getPath().endsWith(".command");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        String loc = modelLocation.toString();
        Matcher filterMatcher = filter.matcher(loc);
        if (!filterMatcher.find()) {
            throw new IllegalStateException("No match found");
        }
        ResourceLocation location = new ResourceLocation(filterMatcher.group(1));
        IModel model;
        try {
            model = ModelLoaderRegistry.getModel(new ResourceLocation(location.getNamespace(), location.getPath().substring("models/".length())));
        } catch (Exception e) {
            DumbLibrary.getLogger().warn(e);
            throw e;
        }

        Matcher commandMacher = this.command.matcher(filterMatcher.group(2));
        List<CommandEntry> list = Lists.newArrayList();
        while (commandMacher.find()) {
            list.add(new CommandEntry(ModelCommandRegistry.get(commandMacher.group(1)), commandMacher.group(2).replaceAll("\\s+", "")));
        }

        return new Model(model, list);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @AllArgsConstructor
    public class CommandEntry {
        ModelCommandRegistry.Command command;
        String data;
    }

    public class Model implements IModel {
        private final IModel delegate;
        private final List<CommandEntry> commands;

        public Model(IModel delegate, List<CommandEntry> commands) {
            this.delegate = delegate;
            this.commands = commands;
        }

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
            IBakedModel model = delegate.bake(state, format, bakedTextureGetter);
            for (CommandEntry cmd : commands) {
                cmd.command.applyChanges(model, cmd.data);
            }
            return model;
        }

        @Override
        public Collection<ResourceLocation> getDependencies() {
            return this.delegate.getDependencies();
        }

        @Override
        public Collection<ResourceLocation> getTextures() {
            return this.delegate.getTextures();
        }

        @Override
        public IModelState getDefaultState() {
            return this.delegate.getDefaultState();
        }

        @Override
        public IModel process(ImmutableMap<String, String> customData) {
            return new Model(this.delegate.process(customData), this.commands);
        }

        @Override
        public IModel smoothLighting(boolean value) {
            return new Model(this.delegate.smoothLighting(value), this.commands);
        }

        @Override
        public IModel gui3d(boolean value) {
            return new Model(this.delegate.gui3d(value), this.commands);
        }

        @Override
        public IModel uvlock(boolean value) {
            return new Model(this.delegate.uvlock(value), this.commands);
        }

        @Override
        public IModel retexture(ImmutableMap<String, String> textures) {
            return new Model(this.delegate.retexture(textures), this.commands);
        }
    }
}
