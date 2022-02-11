package net.dumbcode.dumblibrary.client.model.dcm.baked;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.dumbcode.studio.model.CubeInfo;
import net.dumbcode.studio.model.ModelInfo;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.*;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Used to hold the information about the model, like the texutres used and the lightup data. <br>
 * Also holds any of the custom transforms the user may define.
 *
 * @author Wyn Price
 */
@With
@RequiredArgsConstructor
public class DCMModelGeometry implements IModelGeometry<DCMModelGeometry> {

    private static final int[][] QUAD_VERTICES_INDICES = {
        {1, 0, 4, 5}, {2, 3, 7, 6}, {6, 4, 0, 2}, {3, 1, 5, 7}, {2, 0, 1, 3}, {7, 5, 4, 6}
    }; //See https://gist.github.com/Wyn-Price/a6c86c3b3469624f4799fa8b8ccec959 on how to generate.

    private static final int[] REGULAR_U = {2, 2, 0, 0}; //When axis = xz
    private static final int[] FLIPPED_U = {0, 0, 2, 2}; //When axis = y
    private static final int[] REGULAR_V = {3, 1, 1, 3}; //When axis = xz
    private static final int[] FLIPPED_V = {1, 3, 3, 1}; //When axis = y

    private final Collection<DCMModelHandler.TextureLayer> allTextures;
    private final List<DCMModelHandler.LightupData> lightupData;
    private final Map<String, Pair<List<DCMModelHandler.CubeFacingValues>, Integer>> directCubeTints;
    private final Map<String, String> layerMap;
//    private final ResourceLocation particle;
    private final ModelInfo model;
    private final boolean ambientOcclusion;
//    private final ItemCameraTransforms cameraTransforms;

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        //Create the stack an push a default matrix
        MatrixStack stack = new MatrixStack();

        Collection<DCMModelHandler.TextureLayer> textures = Lists.newArrayList(DCMModelHandler.MISSING);
        //If there are not textures then just add the missing one. Maybe log?
        if (!this.allTextures.isEmpty()) {
            textures = this.allTextures;
        }

        //Go through all the texture layers and set the sprite to them.
        for (DCMModelHandler.TextureLayer texture : textures) {
            texture.setSprite(spriteGetter.apply(texture.getMaterial()));
        }

//        //If it has lightup data, then we need to make sure the vertex format has the lightmap element
//        if (!this.lightupData.isEmpty() && !format.getElements().contains(DefaultVertexFormats.TEX_2S)) {
//            format = new VertexFormat(format).addElement(DefaultVertexFormats.TEX_2S);
//        }

        List<BakedQuad> allLayerQuads = new ArrayList<>();
        Map<String, List<BakedQuad>> quadMap = new HashMap<>();

        //Set the custom matrix values from the model state, and finalize the translation and scale to get the model in the correct place. (due to tabula)
//        this.getMatrix(stack).mul(state.apply(Optional.empty()).orElse(TRSRTransformation.identity()).getMatrix());

        stack.scale(0.0625F, 0.0625F, 0.0625F);
        stack.translate(8F, 8F, 8F);
        stack.mulPose(modelTransform.getRotation().getLeftRotation());
        stack.translate(0F, -8F, 0F);


        //Iterate through all the layers, then through every group, and on each group go through all the root cubes.
        for (DCMModelHandler.TextureLayer layer : textures) {
            List<BakedQuad> quadList = !this.layerMap.containsKey(layer.getLayerName()) ? allLayerQuads : quadMap.computeIfAbsent(this.layerMap.get(layer.getLayerName()), l -> new ArrayList<>());
            for (CubeInfo root : this.model.getRoots()) {
                this.build(quadList, stack, root, layer);
            }
        }

        quadMap.put(null, allLayerQuads);

        //Return the new model
        return new DCMBakedModel(quadMap, this.ambientOcclusion, spriteGetter.apply(owner.resolveTexture("particle")), owner.getCameraTransforms());
    }


    /**
     * Builds the 6 quads from the cube. This is recursive, with the cube calling this again for each of its children. <br>
     * A quad may not be build for the following reasons:
     * <ul>
     * <li>If the quad is 1 dimensional. This occurs when you have a cube acting as a plane. As this quad will never be seen, don't bother rendering it</li>
     * <li>If the quads texture is all transparent. This occurs mostly with overlays, where you only want to overlay a certain part of the model. The parts you don't overlay will not be created.</li>
     * </ul>
     *
     * @param outList The output list of quads that the generated quads will be added to.
     * @param stack   The matrix stack of which the topmost is the current matrix for this cube. This is used to get the absolute positions of the cubes vertices with parenting rotation/translation
     * @param cube    The cube of which to generated the 6 quads from.
     * @param layer   The texture layer of which to use for the uv coords. If the quad has no texture on this layer then the quad isn't generated and is instead ignored.
     */
    private void build(List<BakedQuad> outList, MatrixStack stack, CubeInfo cube, DCMModelHandler.TextureLayer layer) {
        //Apply the matrix changes for the cube
        this.applyMatrixChanges(stack, cube);

        if(layer.getCubePredicate().test(cube.getName())) {
            //Get the minimum and maximum points of this cube, in relative space (no rotation/translation)
            float[] positions = cube.getOffset();
            int[] dims = cube.getDimensions();
            float[] cubeGrow = cube.getCubeGrow();

            Vector3f[] vertices = this.generatedAllVertices(
                new Vector3d(positions[0]-cubeGrow[0], positions[1]-cubeGrow[1], positions[2]-cubeGrow[2]),
                dims[0]+cubeGrow[0]*2, dims[1]+cubeGrow[1]*2, dims[2]+cubeGrow[2]*2);

            //Go through all the vertices and transform them with the current matrix
            for (Vector3f vertex : vertices) {
                Vector4f vec4 = new Vector4f(vertex);
                vec4.transform(stack.last().pose());
                vertex.set(vec4.x(), vec4.y(), vec4.z());
            }


            Map<Direction, float[]> uvMap = this.generateUvMap(cube);

            //Loop through all of the EnumFacing and create the quad for each face
            for (Direction value : Direction.values()) {
                float[] uvData = uvMap.get(value);

                int[] ints = QUAD_VERTICES_INDICES[value.ordinal()];
                VertexInfo[] vertexInfos = new VertexInfo[4];
                for (int i = 0; i < ints.length; i++) {
                    int v = ints[i];
                    vertexInfos[i] = new VertexInfo(vertices[v], v);
                }

                //Check to make sure that the quad has a texture in the specific uv section
                if (!this.hasSpriteGotTexture(layer.getSprite(), uvData)) {
                    continue;
                }

                if (!this.isOneDimensional(vertexInfos)) {
                    outList.add(this.buildQuad(vertexInfos, stack, layer, uvData, cube, value));
                }
            }
        }

        for (CubeInfo child : cube.getChildren()) {
            this.build(outList, stack, child, layer);
        }
        stack.popPose();
    }



    /**
     * Generates a list of all the verticies. Uses bit-math to organize. <br>
     * The maximum vector is defined by {@code min.add(dims)}<br>
     * The list uses numbers 0->7 to generate the vertices.<br>
     * If the leftmost bit is 1, then the maximum vector is used for the x coordinate, otherwise the minimum vector is used <br>
     * If the middle bit is 1, then the maximum vector is used for the y coordinate, otherwise the minimum vector is used <br>
     * If the rightmost bit is 1, then the maximum vector is used for the z coordinate, otherwise the minimum vector is used <br>
     *
     * @param min  The minimum position of the cube
     * @param dims The dimensions of the cube
     * @return an array[8] of points, organized as mentioned above.
     */
    private Vector3f[] generatedAllVertices(Vector3d min, float... dims) {
        Vector3d max = min.add(dims[0], dims[1], dims[2]);
        Vector3f[] vertices = new Vector3f[8];
        for (int i = 0; i < 8; i++) {
            vertices[i] = new Vector3f((float) ((i >> 2 & 1) == 1 ? max : min).x, (float) ((i >> 1 & 1) == 1 ? max : min).y, (float) ((i & 1) == 1 ? max : min).z);
        }
        return vertices;
    }

    /**
     * Generates the uv map for each face.
     * <pre>{@code
     * The texture format is as follows:
     *        This is the minimum XY coord defined in the cube.getTexOffset()
     *                \
     *                 \               width       width
     *                  \         <------------><----------->
     *                   \
     *               Ʌ    X       ---------------------------
     *        depth  |            |     UP     |    DOWN    |
     *               V            |            |            |
     *               Ʌ    -------------------------------------------
     *               |    |       |            |       |            |
     *               |    |       |            |       |            |
     *       height  |    |  WEST |   NORTH    |  EAST |    SOUTH   |
     *               |    |       |            |       |            |
     *               |    |       |            |       |            |
     *               V    -------------------------------------------
     *                    <-------><-----------><------><----------->
     *                      depth      width      depth      width
     * }</pre>
     * All uvs are from bottom left to top right of their section
     *
     * @param cube The cube to generate the map from
     * @return A map of EnumFacing -> int[4] of uvs
     */
    private Map<Direction, float[]> generateUvMap(CubeInfo cube) {
        Map<Direction, float[]> uvMap = new EnumMap<>(Direction.class);
        Direction[] order = {Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH};
        for (int i = 0; i < order.length; i++) {
            float[] arr;
            if(order[i].getAxis() == Direction.Axis.Y) {
                float[] uvs = cube.getGeneratedUVs()[i];
                //Flip the u and the v. Basically, 0 <-> 2, 1 <-> 3
                arr = new float[] { uvs[2], uvs[3], uvs[0], uvs[1] };
            } else {
                arr = cube.getGeneratedUVs()[i];
            }
            uvMap.put(order[i], arr);
        }
        return uvMap;
    }

    /**
     * Generates the lightup data given the layer and the cube
     *
     * @param layer The layer to generate the lightup data with
     * @param cube  The cube of which to generate the data from
     * @param facing The cubes facing
     * @return a float[2] of lightup data ranging from 0-16 on each component
     */
    private float[] generateLightupData(DCMModelHandler.TextureLayer layer, CubeInfo cube, Direction facing, int vertexID) {
        //ts is the custom block-light/skylight data
        float[] ts = new float[2];

        //On UP&DOWN side, directional up on the texture sheet is south for texture
        for (DCMModelHandler.LightupData datum : this.lightupData) {
            if (datum.getLayersApplied() == null || datum.getLayersApplied().contains(layer.getLayerName())) {
                for (DCMModelHandler.SmoothFace face : datum.getSmoothFace()) {
                    if(face.getCube().equals(cube.getName())) {
                        Direction origin = face.getSmoothFaceOrigin();
                        boolean originData = origin.getAxisDirection().getStep() > 0;
                        int mask = (Math.abs(origin.getStepX()) << 2) | (Math.abs(origin.getStepY()) << 1) | Math.abs(origin.getStepZ());

                        int data = vertexID & mask;

                        if((data == 0) == originData) {
                            ts[0] = Math.max(datum.getBlockLight(), ts[0]);
                            ts[1] = Math.max(datum.getSkyLight(), ts[1]);
                        } else {
                            ts[0] = Math.max(datum.getBlockLight() - face.getBlockAmount(), ts[0]);
                            ts[1] = Math.max(datum.getSkyLight() - face.getSkyAmount(), ts[1]);
                        }
                    }
                }
                for (DCMModelHandler.CubeFacingValues entry : datum.getEntry()) {
                    if (entry.getCubeName().equals(cube.getName()) && entry.getFacing().contains(facing)) {
                        ts[0] = Math.max(datum.getBlockLight(), ts[0]);
                        ts[1] = Math.max(datum.getSkyLight(), ts[1]);
                    }
                }

            }
        }
        return ts;
    }

    /**
     * Applies the matrix changes for that cube
     *
     * @param stack the stack to finalize the changes too
     * @param cube  the cube of which to get the values for the transformations
     */
    private void applyMatrixChanges(MatrixStack stack, CubeInfo cube) {
        //Push a new matrix and set the matrix translation/scale/rotation that this cube contains.
        stack.pushPose();
        stack.translate(cube.getRotationPoint()[0], cube.getRotationPoint()[1], cube.getRotationPoint()[2]);
        float[] rotation = cube.getRotation();
        if (rotation[2] != 0) {
            stack.mulPose(Vector3f.ZP.rotation(rotation[2]));
        }
        if (rotation[1] != 0) {
            stack.mulPose(Vector3f.YP.rotation(rotation[1]));
        }
        if (rotation[0] != 0) {
            stack.mulPose(Vector3f.XP.rotation(rotation[0]));
        }
    }

    /**
     * Builds the quad from the given data.
     *
     * @param vertices      The 4 vertices
     * @param layer         The texture layer to generate the texture from
     * @param uvData        The uv data in the form [minU, minV, maxU, maxV]
     * @param cube          the cubes
     * @return the build quad.
     */
    private BakedQuad buildQuad(VertexInfo[] vertices, MatrixStack stack, DCMModelHandler.TextureLayer layer, float[] uvData, CubeInfo cube, Direction cubeDirection) {
        Vector3i n = cubeDirection.getNormal();
        Vector3f normal = new Vector3f(n.getX(), n.getY(), n.getZ());
        normal.transform(stack.last().normal());
        Direction quadFacing = Direction.getNearest(normal.x(), normal.y(), normal.z());
        BakedQuadBuilder builder = new BakedQuadBuilder();
        builder.setQuadOrientation(quadFacing);
        builder.setTexture(layer.getSprite());
        int tint = layer.getIndex();
        if(this.directCubeTints.containsKey(layer.getLayerName())) {
            for (DCMModelHandler.CubeFacingValues values : this.directCubeTints.get(layer.getLayerName()).getFirst()) {
                if(values.getCubeName().equals(cube.getName()) && values.getFacing().contains(cubeDirection)) {
                    tint = this.directCubeTints.get(layer.getLayerName()).getSecond();
                }
            }
        }
        builder.setQuadTint(tint);

        int[] u = cubeDirection.getAxis() == Direction.Axis.Y ? FLIPPED_U : REGULAR_U;
        int[] v = cubeDirection.getAxis() == Direction.Axis.Y ? FLIPPED_V : REGULAR_V;

        for (int i = 0; i < vertices.length; i++) {
            float[] ts = this.generateLightupData(layer, cube, cubeDirection, vertices[i].index);
            this.putVertexData(builder, vertices[i].point, normal,
                    layer.getSprite().getU(uvData[u[i]] * 16D),
                    layer.getSprite().getV(uvData[v[i]] * 16D),
                    ts);

        }
        return builder.build();
    }

    /**
     * Puts the vertex data inside a quad
     *
     * @param builder the quad builder to add the data too
     * @param vert    the quad's vertex that this quad is being drawn from
     * @param normal  the vertex's normal data
     * @param u       the vertex's texture U
     * @param v       the vertex's texture'V
     * @param ts      the vertex's lightup data
     */
    private void putVertexData(BakedQuadBuilder builder, Vector3f vert, Vector3f normal, float u, float v, float[] ts) {
        //Put the data into the quad.
        VertexFormat format = DefaultVertexFormats.BLOCK;
        for (int e = 0; e < format.getElements().size(); e++) {
            switch (format.getElements().get(e).getUsage()) {
                case POSITION:
                    builder.put(e, vert.x(), vert.y(), vert.z());
                    break;
                case COLOR:
                    builder.put(e, 1, 1, 1, 1);
                    break;
                case UV:
                    if (format.getElements().get(e).getIndex() == 0) { //normal uv
                        builder.put(e, u, v);
                    } else { //lightmap uv
                        //The `* 16F` is used to take the data from 0-15 to 0-240
                        //The `/ 0x7FFF` is because forge incorrectly times by 0x7FFF along the pipeline
                        builder.put(e, ts[0] * 16F / 0x7FFF, ts[1] * 16F / 0x7FFF);
                    }
                    break;
                case NORMAL:
                    builder.put(e, normal.x(), normal.y(), normal.z());
                    break;
                default: //Log that we don't know what to do with the element?
                    builder.put(e);
            }
        }
    }

    /**
     * Tests to check whether the sprite has a texture within the specified section
     *
     * @param sprite The sprite to test on
     * @param uvData the uv data [minU, minV, maxU, maxV]
     * @return Whether the sprite has a texture in the specified section
     */
    private boolean hasSpriteGotTexture(TextureAtlasSprite sprite, float[] uvData) {
        int width = sprite.getWidth();
        int height = sprite.getHeight();

        for (int i = 0; i < sprite.getFrameCount(); i++) {
            for (float x = Math.min(uvData[0], uvData[2]); x < Math.max(uvData[0], uvData[2]); x += 1F/width) {
                for (float y = Math.min(uvData[1], uvData[3]); y < Math.max(uvData[1], uvData[3]); y += 1F/height) {
                    if(x >= 1 || y >= 1) {
                        continue;
                    }
                    int xPos = (int) (x * width);
                    int yPos = (int) (y * height);
                    int alpha = (sprite.getPixelRGBA(i, xPos, yPos) >> 24) & 0xFF;
                    if (alpha != 0) { //If it has alpha, then break the alpha testing section.
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks to make sure that the quad produced by these vertices isn't one dimensional
     *
     * @param pointVertices the vertices to check on
     * @return true if it is one dimensional, false otherwise
     */
    private boolean isOneDimensional(VertexInfo[] pointVertices) {
        //Make sure that this plane is not 1D, as it would be pointless to create it.
        for (int i = 0; i < pointVertices.length; i++) {
            Vector3f vertex = pointVertices[i].point;
            for (int n = i + 1; n < pointVertices.length; n++) {
                Vector3f other = pointVertices[n].point;
                if (Math.abs(vertex.x() - other.x()) < 1e-3F &&
                    Math.abs(vertex.y() - other.y()) < 1e-3F &&
                    Math.abs(vertex.z() - other.z()) < 1e-3F
                ) {
                        return true;
                }
            }
        }
        return false;
    }


    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return this.allTextures.stream()
            .map(t -> {
                RenderMaterial material = owner.resolveTexture(t.getLayerName());
                t.setMaterial(material);
                return material;
            })
            .collect(Collectors.toSet());
    }

    @Data
    private static class VertexInfo {
        private final Vector3f point;
        private final int index;
    }
}
