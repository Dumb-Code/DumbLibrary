package net.dumbcode.dumblibrary.client.model.tabula.baked;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.util.*;
import java.util.function.Function;

import static net.dumbcode.dumblibrary.server.utils.MatrixUtils.*;

/**
 * Used to hold the information about the model, like the texutres used and the lightup data. <br>
 * Also holds any of the custom transforms the user may define.
 *
 * @author Wyn Price
 */
@With
@RequiredArgsConstructor
public class TabulaIModel implements IModel {

    private final Collection<TabulaModelHandler.TextureLayer> allTextures;
    private final List<TabulaModelHandler.LightupData> lightupData;
    private final  Map<Integer, Pair<List<TabulaModelHandler.CubeFacingValues>, Integer>> directCubeTints;
    private final ResourceLocation particle;
    private final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;
    private final TabulaModelInformation model;
    private final boolean ambientOcclusion;
    private final boolean gui3d;
    private final List<ItemOverride> itemOverrides;

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> texFunc) {
        //Create the stack an push a default matrix
        Deque<Matrix4f> stack = new ArrayDeque<>();
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        stack.push(matrix4f);

        Collection<TabulaModelHandler.TextureLayer> textures = Lists.newArrayList(TabulaModelHandler.MISSING);
        //If there are not textures then just add the missing one. Maybe log?
        if (!this.allTextures.isEmpty()) {
            textures = this.allTextures;
        }

        //Go through all the texture layers and set the sprite to them.
        for (TabulaModelHandler.TextureLayer texture : textures) {
            texture.setSprite(texFunc.apply(texture.getLoc()));
        }

        //If it has lightup data, then we need to make sure the vertex format has the lightmap element
        if (!this.lightupData.isEmpty()) {
            if (format == DefaultVertexFormats.ITEM) { // ITEM is convertible to BLOCK (replace normal+padding with lmap)
                format = DefaultVertexFormats.BLOCK;
            } else if (!format.getElements().contains(DefaultVertexFormats.TEX_2S)) { // Otherwise, this format is unknown, add TEX_2S if it does not exist
                format = new VertexFormat(format).addElement(DefaultVertexFormats.TEX_2S);
            }
        }

        List<BakedQuad> quadList = Lists.newArrayList();

        //Set the custom matrix values from the model state, and finalizeComponent the translation and scale to get the model in the correct place. (due to tabula)
        this.getMatrix(stack).mul(state.apply(Optional.empty()).orElse(TRSRTransformation.identity()).getMatrix());
        //Due to minecraft, the ModelRenderer has it's, x and y coords flipped. Tabula then (rightly) saves it like this.
        scale(this.getMatrix(stack), -0.0625F, -0.0625F, 0.0625F);
        //to get from tabula's origin to the world origin you have to go move [-8, -24, 8], thus we need to translate by that amount
        translate(this.getMatrix(stack), -8, -24, 8);

        //Iterate through all the layers, then through every group, and on each group go through all the root cubes.
        for (TabulaModelHandler.TextureLayer layer : textures) {
            for (TabulaModelInformation.CubeGroup group : this.model.getGroups()) {
                for (TabulaModelInformation.Cube cube : group.getCubeList()) {
                    if(layer.getCubePredicate().test(cube.getName())) {
                        this.build(format, quadList, stack, cube, layer);
                    }
                }
            }
        }

        //Return the new model
        return new TabulaBakedModel(quadList, this.ambientOcclusion, this.gui3d, texFunc.apply(this.particle), this.itemOverrides, this.transforms);
    }

    /**
     * Builds the 6 quads from the cube. This is recursive, with the cube calling this again for each of its children. <br>
     * A quad may not be build for the following reasons:
     * <ul>
     * <li>If the quad is 1 dimensional. This occurs when you have a cube acting as a plane. As this quad will never be seen, don't bother rendering it</li>
     * <li>If the quads texture is all transparent. This occurs mostly with overlays, where you only want to overlay a certain part of the model. The parts you don't overlay will not be created.</li>
     * </ul>
     *
     * @param format  The vertex format to build the quads to.
     * @param outList The output list of quads that the generated quads will be added to.
     * @param stack   The matrix stack of which the topmost is the current matrix for this cube. This is used to get the absolute positions of the cubes vertices with parenting rotation/translation
     * @param cube    The cube of which to generated the 6 quads from.
     * @param layer   The texture layer of which to use for the uv coords. If the quad has no texture on this layer then the quad isn't generated and is instead ignored.
     */
    private void build(VertexFormat format, List<BakedQuad> outList, Deque<Matrix4f> stack, TabulaModelInformation.Cube cube, TabulaModelHandler.TextureLayer layer) {

        //Apply the matrix changes for the cube
        this.applyMatrixChanges(stack, cube);

        //Get the minimum and maximum points of this cube, in relative space (no rotation/translation)
        float[] positions = cube.getOffset();
        float[] dims = cube.getDimension();
        float scale = cube.getMcScale();

        Point3f[] vertices = this.generatedAllVertices(new Vec3d(positions[0], positions[1], positions[2]).subtract(scale, scale, scale), dims[0]+scale*2, dims[1]+scale*2, dims[2]+scale*2);

        //Go through all the vertices and transform them with the current matrix
        for (Point3f vertex : vertices) {
            this.getMatrix(stack).transform(vertex);
        }

        Map<EnumFacing, int[]> uvMap = this.generateUvMap(cube);

        //Loop through all of the EnumFacing and create the quad for each face
        for (EnumFacing value : EnumFacing.values()) {
            int[] uvData = uvMap.get(cube.isTextureMirror() && value.getAxis() == EnumFacing.Axis.X ? value.getOpposite() : value); //The uv data

            //Check to make sure that the quad has a texture in the specific uv section
            if (!this.hasSpriteGotTexture(layer.getSprite(), uvData)) {
                continue;
            }


            Point3f[] pointVertices = this.generateSideVertices(value, vertices);

            if (!this.isOneDimensional(pointVertices)) {

                //Flip the uv data if the texture is mirrored. As the model is scaled [-1, -1, 1], the UV data is already flipped.
                //So just un-flip it if the texture isn't mirrored
                if (!cube.isTextureMirror() && value.getAxis() != EnumFacing.Axis.X) {
                    int minU = uvData[0];
                    uvData[0] = uvData[2];
                    uvData[2] = minU;
                }

                outList.add(this.buildQuad(pointVertices, layer, uvData, format, cube));
            }


        }

        //Iterate through this cubes children and build the quads
        for (TabulaModelInformation.Cube child : cube.getChildren()) {
            this.build(format, outList, stack, child, layer);
        }

        stack.pop();
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
    private Point3f[] generatedAllVertices(Vec3d min, float... dims) {
        Vec3d max = min.add(dims[0], dims[1], dims[2]);
        Point3f[] vertices = new Point3f[8];
        for (int i = 0; i < 8; i++) {
            vertices[i] = new Point3f((float) ((i >> 2 & 1) == 1 ? max : min).x, (float) ((i >> 1 & 1) == 1 ? max : min).y, (float) ((i & 1) == 1 ? max : min).z);
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
    private Map<EnumFacing, int[]> generateUvMap(TabulaModelInformation.Cube cube) {

        float[] dims = cube.getDimension();

        int w = (int) dims[0]; //width
        int h = (int) dims[1]; //height
        int d = (int) dims[2]; //depth


        //A map containing the list of faces->uvs[4]
        Map<EnumFacing, int[]> uvMap = Maps.newEnumMap(EnumFacing.class);

        //The line along the bottom of the texture-map (in order)
        EnumFacing[] horizontals = new EnumFacing[]{EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH};

        //This is incremented each iteration to get to the next x position.
        int offX = 0;
        for (int i = 0; i < horizontals.length; i++) {
            int minX = (int) cube.getTexOffset()[0] + offX;
            int minY = (int) cube.getTexOffset()[1] + d;
            //Along the bottom row, the x sizes alternate between depth and width. This sets those distances to xdist
            int xdist = i % 2 == 0 ? d : w;
            offX += xdist;
            uvMap.put(horizontals[i], new int[]{minX, minY, minX + xdist, minY + h});
        }

        //The line along the top of the texture-map (in reverse order)
        EnumFacing[] verticals = new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN};
        for (int i = 0; i < verticals.length; i++) {
            int minX = (int) cube.getTexOffset()[0] + d + w * i;
            int minY = (int) cube.getTexOffset()[1];
            uvMap.put(verticals[i], new int[]{minX, minY, minX + w, minY + d});
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
    private float[] generateLightupData(TabulaModelHandler.TextureLayer layer, TabulaModelInformation.Cube cube, EnumFacing facing) {
        //ts is the custom block-light/skylight data
        float[] ts = new float[2];

        //On UP&DOWN side, directional up on the texture sheet is south for texture
        for (TabulaModelHandler.LightupData datum : this.lightupData) {
            if (datum.getLayersApplied().contains(layer.getLayerName())) {
                for (TabulaModelHandler.CubeFacingValues entry : datum.getEntry()) {
                    if (entry.getCubeName().equals(cube.getName()) && entry.getFacing().contains(facing)) {
                        ts[0] = datum.getBlockLight();
                        ts[1] = datum.getSkyLight();
                    }
                }
            }
        }
        return ts;
    }

    /**
     * Normally, we would want the coords going anti-clockwise starting at the top left, as follows.
     * <pre>{@code
     *
     *      0              3
     *      |              Ʌ
     *      |              |
     *      V              |
     *      1------------->2
     *
     * }</pre>
     * However, due to the fact that model y direction and x direction is flipped, we need to account for this and thus flip the coords vertically then horizontally. <br>
     * This means that if the facing direction is in the {@link EnumFacing#HORIZONTALS} we end up with the coordinates in this configuration:
     * <pre>{@code
     *
     *      2              1
     *      |              Ʌ
     *      |              |
     *      V              |
     *      3------------->0
     *
     * }</pre>
     * If the facing direction is not in the {@link EnumFacing#HORIZONTALS}, then we don't flip is vertically, but instead just horizontally to get:
     * <pre>{@code
     *
     *      3              0
     *      |              Ʌ
     *      |              |
     *      V              |
     *      2------------->1
     *
     * }</pre>     * @param facing The direction facing
     *
     * @param vertices the list of 8 verticies, generated from {@link #generatedAllVertices(Vec3d, float...)}
     * @return a list of 4 vertices, in the order as discussed above.
     */
    private Point3f[] generateSideVertices(EnumFacing facing, Point3f[] vertices) {
        Point3f[] pointVertices = new Point3f[4];

        for (int i = 0; i < 4; i++) {
            boolean horizontal = facing.getAxis().isHorizontal();
            EnumFacing.Axis rotateAxis = horizontal ? EnumFacing.Axis.Y : EnumFacing.Axis.Z;
            int vertex = this.encode(horizontal ? facing : facing.getOpposite());
            //Starting plane is the screens <-- direction you get when looking directly at the plane being drawn. For the UP and DOWN planes, you should face south and north respectively.
            EnumFacing startingPlane = facing.rotateAround(rotateAxis);

            //Due to the fact that the model is scaled -1 on the y axis, we need to account for the fact that our direction is going to be the opposite of tabuls direction.
            if (!horizontal) {
                startingPlane = startingPlane.getOpposite();
            }
            //If it is the last 2 vertices being drawn, then it is at the other-side of the screen, in the other x direction.
            if (i > 1) {
                startingPlane = startingPlane.getOpposite();
            }
            vertex |= this.encode(startingPlane);

            //Mid plane is the screens ↑ direction when looking directly at the plane being drawn
            EnumFacing midPlane = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, rotateAxis);

            //If it is either the second or the third vertex
            if (i % 3 != 0) {
                midPlane = midPlane.getOpposite();
            }
            vertex |= this.encode(midPlane);

            //If the plane is on the horizontals, then we need to account for the fact that the vertices should be in the form 2,3,0,1.
            //This only needs to happen if the quad is a horizontal. The top and bottom plane are fine
            pointVertices[horizontal ? (i + 2) % 4 : i] = vertices[vertex];
        }
        return pointVertices;
    }

    /**
     * Applies the matrix changes for that cube
     *
     * @param stack the stack to finalizeComponent the changes too
     * @param cube  the cube of which to get the values for the transformations
     */
    private void applyMatrixChanges(Deque<Matrix4f> stack, TabulaModelInformation.Cube cube) {
        //Push a new matrix and set the matrix translation/scale/rotation that this cube contains.
        stack.push(new Matrix4f(this.getMatrix(stack)));
        translate(this.getMatrix(stack), cube.getRotationPoint());
        scale(this.getMatrix(stack), cube.getScale());
        float[] rotation = cube.getRotation();
        if (rotation[2] != 0) {
            rotate(this.getMatrix(stack), rotation[2], 0, 0, 1);
        }
        if (rotation[1] != 0) {
            rotate(this.getMatrix(stack), rotation[1], 0, 1, 0);
        }
        if (rotation[0] != 0) {
            rotate(this.getMatrix(stack), rotation[0], 1, 0, 0);
        }
    }

    /**
     * Builds the quad from the given data.
     *
     * @param pointVertices The 4 vertices generated from {@link #generateSideVertices(EnumFacing, Point3f[])}
     * @param layer         The texture layer to generate the texture from
     * @param uvData        The uv data in the form [minU, minV, maxU, maxV]
     * @param format        the vertex format the build too
     * @param cube          the cubes
     * @return the build quad.
     */
    private BakedQuad buildQuad(Point3f[] pointVertices, TabulaModelHandler.TextureLayer layer, int[] uvData, VertexFormat format, TabulaModelInformation.Cube cube) {
        Vector3f normal = MathUtils.calcualeNormalF(pointVertices[0].x, pointVertices[0].y, pointVertices[0].z, pointVertices[1].x, pointVertices[1].y, pointVertices[1].z, pointVertices[2].x, pointVertices[2].y, pointVertices[2].z);
        EnumFacing quadFacing = EnumFacing.getFacingFromVector(normal.x, normal.y, normal.z);
        float[] ts = this.generateLightupData(layer, cube, quadFacing);
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setQuadOrientation(quadFacing);
        builder.setTexture(layer.getSprite());
        int tint = layer.getLayer();
        if(this.directCubeTints.containsKey(tint)) {
            for (TabulaModelHandler.CubeFacingValues values : this.directCubeTints.get(tint).getLeft()) {
                if(values.getCubeName().equals(cube.getName()) && values.getFacing().contains(quadFacing)) {
                    tint = this.directCubeTints.get(tint).getRight();
                }
            }
//            builder.setQuadTint( this.directCubeTints.get(cubeName).get(layer.getLayer()));
        }
        builder.setQuadTint(tint);
        for (int i = 0; i < pointVertices.length; i++) {
            this.putVertexData(builder, pointVertices[i], normal,
                    layer.getSprite().getInterpolatedU(uvData[2 - (i / 2) * 2] / (float) this.model.getTexWidth() * 16D),
                    layer.getSprite().getInterpolatedV(uvData[(i % 3 == 0) ? 1 : 3] / (float) this.model.getTexHeight() * 16D),
                    ts, format);

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
     * @param format  the quads format
     */
    private void putVertexData(UnpackedBakedQuad.Builder builder, Point3f vert, Vector3f normal, float u, float v, float[] ts, VertexFormat format) {
        //Put the data into the quad.
        for (int e = 0; e < format.getElementCount(); e++) {
            switch (format.getElement(e).getUsage()) {
                case POSITION:
                    builder.put(e, vert.x, vert.y, vert.z);
                    break;
                case COLOR:
                    builder.put(e, 1, 1, 1, 1);
                    break;
                case UV:
                    if (format.getElement(e).getIndex() == 0) { //normal uv
                        builder.put(e, u, v);
                    } else { //lightmap uv
                        //The `* 16F` is used to take the data from 0-15 to 0-240
                        //The `/ 0x7FFF` is because forge incorrectly times by 0x7FFF along the pipeline
                        builder.put(e, ts[0] * 16F / 0x7FFF, ts[1] * 16F / 0x7FFF);
                    }
                    break;
                case NORMAL:
                    builder.put(e, normal.x, normal.y, normal.z);
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
    private boolean hasSpriteGotTexture(TextureAtlasSprite sprite, int[] uvData) {
        int width = sprite.getIconWidth();
        int height = sprite.getIconWidth();

        for (int i = 0; i < sprite.getFrameCount(); i++) {
            int[] data = sprite.getFrameTextureData(i)[0]; //mipmap of 0

            for (float x = Math.min(uvData[0], uvData[2]); x < Math.max(uvData[0], uvData[2]); x++) {
                for (float y = Math.min(uvData[1], uvData[3]); y < Math.max(uvData[1], uvData[3]); y++) {
                    int xPos = (int) ((x / this.model.getTexWidth()) * width);
                    int yPos = (int) ((y / this.model.getTexHeight()) * height);
                    int alpha = (data[xPos + yPos * width] >> 24) & 0xFF;
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
    private boolean isOneDimensional(Point3f[] pointVertices) {
        //Make sure that this plane is not 1D, as it would be pointless to create it.
        for (int i = 0; i < pointVertices.length; i++) {
            Point3f vertex = pointVertices[i];
            for (int n = i + 1; n < pointVertices.length; n++) {
                if (vertex.epsilonEquals(pointVertices[n], 1e-3F)) {
                    return true;
                }
            }
        }
        return false;
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

    @Override
    public IModel smoothLighting(boolean value) {
        return this.withAmbientOcclusion(value);
    }

    @Override
    public IModel gui3d(boolean value) {
        return this.withGui3d(value);
    }

    @Override
    public IModel retexture(ImmutableMap<String, String> textures) {
        List<TabulaModelHandler.TextureLayer> textureLayers = new ArrayList<>(this.allTextures.size());
        for (TabulaModelHandler.TextureLayer texture : this.allTextures) {
            if (textures.containsKey(texture.getLayerName())) {
                String remapped = textures.get(texture.getLayerName());
                if (!remapped.isEmpty()) { //Removed texture
                    textureLayers.add(new TabulaModelHandler.TextureLayer(texture.getLayerName(), new ResourceLocation(remapped), texture.getCubePredicate(), texture.getLayer()));
                }
            } else {
                textureLayers.add(texture);
            }
        }
        return this.withAllTextures(textureLayers);
    }

    /**
     * Gets the nonnull matrix
     *
     * @param stack the stack to get the matrix from
     * @return the matrix at the top of the stack
     */
    @Nonnull
    private Matrix4f getMatrix(Deque<Matrix4f> stack) {
        return Objects.requireNonNull(stack.peek());
    }


    /**
     * Encodes the EnumFacing to an integer. This integer's bits are in the form: [x][y][z],
     * where 1 represents the positive direction, and 0 is the negative direction <br>
     * For example;
     * <ul>
     * <li>{@link EnumFacing#UP}    -> 0b010 -> 2</li>
     * <li>{@link EnumFacing#DOWN}  -> 0b000 -> 0</li>
     * <li>{@link EnumFacing#EAST}  -> 0b100 -> 4</li>
     * <li>{@link EnumFacing#NORTH} -> 0b000 -> 0</li>
     * </ul>
     * Note that this is not a 1->1 function. If the {@code facing } is facing the negative direction on it's axis,
     * then the result will just be 0.
     *
     * @param facing the facing
     * @return the encoded integer
     */
    private int encode(EnumFacing facing) {
        return Math.max(facing.getXOffset(), 0) << 2 | Math.max(facing.getYOffset(), 0) << 1 | Math.max(facing.getZOffset(), 0);
    }
}
