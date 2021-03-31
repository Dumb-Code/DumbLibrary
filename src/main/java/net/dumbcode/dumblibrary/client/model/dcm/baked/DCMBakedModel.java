package net.dumbcode.dumblibrary.client.model.dcm.baked;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.MinecraftForgeClient;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The baked model representing the .tbl model. Simply just holds the list of quads along with the other necessary data
 *
 * @author Wyn Price
 */
public class DCMBakedModel implements IBakedModel {

    private static final List<BakedQuad> EMPTY = Collections.emptyList();

    private final Map<String, List<BakedQuad>> quadMap;

    private final boolean ambientOcclusion;
    private final TextureAtlasSprite particleTexture;
    private final ItemCameraTransforms transforms;

    public DCMBakedModel(Map<String, List<BakedQuad>> quadMap, boolean ambientOcclusion, TextureAtlasSprite particleTexture, ItemCameraTransforms transforms) {
        this.quadMap = quadMap;
        this.ambientOcclusion = ambientOcclusion;
        this.particleTexture = particleTexture;
        this.transforms = transforms;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState p_200117_1_, @Nullable Direction p_200117_2_, Random p_200117_3_) {
        RenderType layer = MinecraftForgeClient.getRenderLayer();
        if(layer == null) {
            return this.quadMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        }
        return this.quadMap.getOrDefault(layer, EMPTY);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.ambientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.particleTexture;
    }

    @Override
    public ItemCameraTransforms getTransforms() {
        return transforms;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }


}