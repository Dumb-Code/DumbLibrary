package net.dumbcode.dumblibrary.client.model.tabula.baked;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelInformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class TabulaIModel implements IModel {

    private final Collection<TabulaModelHandler.TextureLayer> allTextures;
    private final List<TabulaModelHandler.LightupData> lightupData;
    private final TabulaModelInformation model;
    private final ResourceLocation particle;
    private final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;
    private final boolean ambientOcclusion;
    private final boolean gui3d;
    private final List<ItemOverride> itemOverrides;

    public TabulaIModel(Collection<TabulaModelHandler.TextureLayer> allTextures, List<TabulaModelHandler.LightupData> lightupData, ResourceLocation particle, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms, TabulaModelInformation model, boolean ambientOcclusion, boolean gui3d, List<ItemOverride> itemOverrides) {
        this.allTextures = allTextures;
        this.lightupData = lightupData;
        this.particle = particle;
        this.transforms = transforms;
        this.model = model;
        this.ambientOcclusion = ambientOcclusion;
        this.gui3d = gui3d;
        this.itemOverrides = itemOverrides;
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> texFunc) {
        TextureAtlasSprite particle = texFunc.apply(this.particle);
        for (TabulaModelHandler.TextureLayer texture : this.allTextures) {
            texture.setSprite(texFunc.apply(texture.getLoc()));
        }
        List<BakedQuad> quads = new TabulaModelBaker(this.allTextures, this.lightupData, this.model, format, state).bake();

        return new TabulaBakedModel(quads, this.ambientOcclusion, this.gui3d, particle, this.itemOverrides, this.transforms);
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        List<ResourceLocation> out = Lists.newArrayList(this.particle);

        for (TabulaModelHandler.TextureLayer allTexture : this.allTextures) {
            out.add(allTexture.getLoc());
        }

        return out;
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }
}
