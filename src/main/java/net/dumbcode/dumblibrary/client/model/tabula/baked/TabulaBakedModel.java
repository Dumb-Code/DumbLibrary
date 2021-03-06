package net.dumbcode.dumblibrary.client.model.tabula.baked;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The baked model representing the .tbl model. Simply just holds the list of quads along with the other necessary data
 *
 * @author Wyn Price
 */
public class TabulaBakedModel implements IBakedModel {

    private static final List<BakedQuad> EMPTY = Collections.emptyList();

    private final Map<BlockRenderLayer, List<BakedQuad>> quadMap;

    private final boolean ambientOcclusion;
    private final boolean gui3d;
    private final TextureAtlasSprite particleTexture;
    private final ItemOverrideList itemOverrides;
    private final Map<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;

    public TabulaBakedModel(Map<BlockRenderLayer, List<BakedQuad>> quadMap, boolean ambientOcclusion, boolean gui3d, TextureAtlasSprite particleTexture, List<ItemOverride> itemOverrides, Map<ItemCameraTransforms.TransformType, TRSRTransformation> transforms) {
        this.quadMap = quadMap;
        this.ambientOcclusion = ambientOcclusion;
        this.gui3d = gui3d;
        this.particleTexture = particleTexture;
        this.itemOverrides = new ItemOverrideList(itemOverrides);
        this.transforms = transforms;
    }


    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        return side == null ? this.quadMap.getOrDefault(layer == null ? BlockRenderLayer.SOLID : layer, EMPTY) : EMPTY;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return this.gui3d;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.particleTexture;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return this.itemOverrides;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return Pair.of(this, this.transforms.get(cameraTransformType).getMatrix());
    }
}
