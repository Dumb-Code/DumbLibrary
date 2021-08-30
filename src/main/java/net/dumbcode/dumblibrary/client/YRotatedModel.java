package net.dumbcode.dumblibrary.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.model.TransformableModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.*;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class YRotatedModel {
    private static final Map<Block, DirectionProperty> BLOCKS = new HashMap<>();
    public static void addYRotation(Block... blocks) {
        for (Block block : blocks) {
            addYRotation(block, BlockStateProperties.HORIZONTAL_FACING);
        }
    }
    public static void addYRotation(Block block, DirectionProperty property) {
        BLOCKS.put(block, property);
    }

    public static void rotateStack(MatrixStack stack, Direction direction) {
        int rotation;
        switch (direction) {
            default:
            case EAST:
                rotation = 0;
                break;
            case SOUTH:
                rotation = 90;
                break;
            case WEST:
                rotation = 180;
                break;
            case NORTH:
                rotation = 270;
                break;
        }
        stack.translate(0.5, 0, 0.5);
        stack.mulPose(Vector3f.YP.rotationDegrees(rotation));
        stack.translate(-0.5, 0, -0.5);
    }

    public static void onModelBakeEvent(ModelBakeEvent event) {
        MatrixStack stack = new MatrixStack();
        Map<ResourceLocation, IBakedModel> replacementMap = new HashMap<>();
        for (Map.Entry<Block, DirectionProperty> entry : BLOCKS.entrySet()) {
            Block block = entry.getKey();
            DirectionProperty property = entry.getValue();
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                ModelResourceLocation location = BlockModelShapes.stateToModelLocation(state);
                IBakedModel model = event.getModelRegistry().get(location);
                stack.pushPose();
                rotateStack(stack, state.getValue(property));
                applyTo(model, stack).ifPresent(b -> replacementMap.put(location, b));
                stack.popPose();

            }
        }
        event.getModelRegistry().putAll(replacementMap);
    }

    private static Optional<IBakedModel> applyTo(IBakedModel model, MatrixStack stack) {
        if(model instanceof MultipartBakedModel) {
            List<Pair<Predicate<BlockState>, IBakedModel>> list = new ArrayList<>();
            for (Pair<Predicate<BlockState>, IBakedModel> pair : ((MultipartBakedModel) model).selectors) {
                list.add(Pair.of(pair.getLeft(), applyTo(pair.getRight(), stack).orElseGet(pair::getRight)));
            }
            return Optional.of(new MultipartBakedModel(list));
        } else if(model instanceof SimpleBakedModel) {
            Random random = new Random();
            List<BakedQuad> unculledFaces = new ArrayList<>();
            for (BakedQuad q : model.getQuads(Blocks.AIR.defaultBlockState(), null, random, EmptyModelData.INSTANCE)) {
                BakedQuad bakedQuad = TransformableModel.transformQuad(q, stack);
                unculledFaces.add(bakedQuad);
            }
            Map<Direction, List<BakedQuad>> culledFaces = new HashMap<>();
            for (Direction d : Direction.values()) {
                List<BakedQuad> quads = new ArrayList<>();
                for (BakedQuad q : model.getQuads(Blocks.AIR.defaultBlockState(), d, random, EmptyModelData.INSTANCE)) {
                    BakedQuad bakedQuad = TransformableModel.transformQuad(q, stack);
                    quads.add(bakedQuad);
                }
                if (culledFaces.put(d, quads) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            }
            return Optional.of(new SimpleBakedModel(
                unculledFaces, culledFaces, model.useAmbientOcclusion(), model.usesBlockLight(),
                model.isGui3d(), model.getParticleIcon(), model.getTransforms(), model.getOverrides()
            ));
        } else if(model instanceof TransformableModel) {
            return Optional.of(((TransformableModel) model).transform(stack));
        } else {
            DumbLibrary.getLogger().warn("Tried to modify model of class {}, but it was not transformable.", model.getClass().getSimpleName());
            return Optional.empty();
        }
    }
}
