package net.dumbcode.dumblibrary.client.model.command;

import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.AllArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@AllArgsConstructor
public class ModelCommandModel implements IDynamicBakedModel {
    private final IBakedModel delegate;
    private final boolean modelBlockstateCache;
    private final List<ModelCommandLoader.CommandEntry> commands;

    public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
        List<BakedQuad> quads = this.delegate.getQuads(state, side, rand, extraData);

        CommandDerived.DerivedContext context = new CommandDerived.DerivedContext();
        context.set(CommandDerived.BLOCKSTATE, state);

        List<BakedQuad> out = new ArrayList<>();
        for (BakedQuad quad : quads) {
            BakedQuad copied = new BakedQuad(Arrays.copyOf(quad.getVertices(), quad.getVertices().length), quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
            for (ModelCommandLoader.CommandEntry command : commands) {
                command.getCommand().apply(copied, context.createResolver(command));
            }
            out.add(copied);
        }

        return out;
    }


    public boolean useAmbientOcclusion() {
        return this.delegate.useAmbientOcclusion();
    }

    public boolean isGui3d() {
        return this.delegate.isGui3d();
    }

    public boolean usesBlockLight() {
        return this.delegate.usesBlockLight();
    }

    public boolean isCustomRenderer() {
        return this.delegate.isCustomRenderer();
    }

    public TextureAtlasSprite getParticleIcon() {
        return this.delegate.getParticleIcon();
    }

    public ItemCameraTransforms getTransforms() {
        return this.delegate.getTransforms();
    }

    public ItemOverrideList getOverrides() {
        return this.delegate.getOverrides();
    }

    public boolean isAmbientOcclusion(BlockState state) {
        return this.delegate.isAmbientOcclusion(state);
    }

    public boolean doesHandlePerspectives() {
        return this.delegate.doesHandlePerspectives();
    }

    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, GuiGraphics mat) {
        this.delegate.handlePerspective(cameraTransformType, mat);
        return this;
    }

    public IModelData getModelData(IBlockDisplayReader world, BlockPos pos, BlockState state, IModelData tileData) {
        return this.delegate.getModelData(world, pos, state, tileData);
    }

    public TextureAtlasSprite getParticleTexture(IModelData data) {
        return this.delegate.getParticleTexture(data);
    }
}
