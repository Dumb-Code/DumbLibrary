package net.dumbcode.dumblibrary.client.model.tabula.baked;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Wither;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelInformation;
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

import javax.vecmath.*;
import java.util.*;
import java.util.function.Function;

/**
 * Used to hold the information about the model, like the texutres used and the lightup data. <br>
 * Also holds any of the custom transforms the user may define.
 * @author Wyn Price
 */
@Wither
@RequiredArgsConstructor
public class TabulaIModel implements IModel {

    private final Collection<TabulaModelHandler.TextureLayer> allTextures;
    private final List<TabulaModelHandler.LightupData> lightupData;
    private final ResourceLocation particle;
    private final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;
    private final TabulaModelInformation model;
    private final boolean ambientOcclusion;
    private final boolean gui3d;
    private final List<ItemOverride> itemOverrides;

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> texFunc) {
        //Create the stack an push a default matrix
        Stack<Matrix4f> stack = new Stack<>();
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        stack.push(matrix4f);

        Collection<TabulaModelHandler.TextureLayer> textures = Lists.newArrayList(TabulaModelHandler.MISSING);
        //If there are not textures then just add the missing one. Maybe log?
        if(!this.allTextures.isEmpty()) {
            textures = this.allTextures;
        }

        //Go through all the texture layers and set the sprite to them.
        for (TabulaModelHandler.TextureLayer texture : textures) {
            texture.setSprite(texFunc.apply(texture.getLoc()));
        }

        //If it has lightup data, then we need to make sure the vertex format has the lightmap element
        if(!this.lightupData.isEmpty()) {
            if (format == DefaultVertexFormats.ITEM) { // ITEM is convertible to BLOCK (replace normal+padding with lmap)
                format = DefaultVertexFormats.BLOCK;
            } else if (!format.getElements().contains(DefaultVertexFormats.TEX_2S)) { // Otherwise, this format is unknown, add TEX_2S if it does not exist
                format = new VertexFormat(format).addElement(DefaultVertexFormats.TEX_2S);
            }
        }

        List<BakedQuad> quadList = Lists.newArrayList();

        //Set the custom matrix values from the model state, and apply the translation and scale to get the model in the correct place. (due to tabula)
        stack.peek().mul(state.apply(Optional.empty()).orElse(TRSRTransformation.identity()).getMatrix());
        this.translate(stack, 0.5F, -0.5F, 0.5F);
        this.scale(stack, 0.0625F, 0.0625F, 0.0625F);

        //Iterate through all the layers, then through every group, and on each group go through all the root cubes.
        for (TabulaModelHandler.TextureLayer layer : textures) {
            for (TabulaModelInformation.CubeGroup group : this.model.getGroups()) {
                for (TabulaModelInformation.Cube cube : group.getCubeList()) {
                    build(format, quadList, stack, cube, layer);
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
     *     <li>If the quad is 1 dimensional. This occurs when you have a cube acting as a plane. As this quad will never be seen, don't bother rendering it</li>
     *     <li>If the quads texture is all transparent. This occurs mostly with overlays, where you only want to overlay a certain part of the model. The parts you don't overlay will not be created.</li>
     * </ul>
     * @param format The vertex format to build the quads to.
     * @param outList The output list of quads that the generated quads will be added to.
     * @param stack The matrix stack of which the topmost is the current matrix for this cube. This is used to get the absolute positions of the cubes vertices with parenting rotation/translation
     * @param cube The cube of which to generated the 6 quads from.
     * @param layer The texture layer of which to use for the uv coords. If the quad has no texture on this layer then the quad isn't generated and is instead ignored.
     */
    private void build(VertexFormat format, List<BakedQuad> outList, Stack<Matrix4f> stack, TabulaModelInformation.Cube cube, TabulaModelHandler.TextureLayer layer) {
        //Push a new matrix and set the matrix translation/scale/rotation that this cube contains.
        stack.push(new Matrix4f(stack.peek()));
        translate(stack, cube.getRotationPoint());
        scale(stack, cube.getScale());
        float[] rotation = cube.getRotation();
        if (rotation[2] != 0) {
            rotate(stack, rotation[2], 0, 0, 1);
        }
        if (rotation[1] != 0) {
            rotate(stack, rotation[1], 0, 1, 0);
        }
        if (rotation[0] != 0) {
            rotate(stack, rotation[0], 1, 0, 0);
        }

        //Get the minimum and maximum points of this cube, in relative space (no rotation/translation)
        float[] positions = cube.getOffset();
        float[] dims = cube.getDimension();
        float scale = cube.getMcScale();
        Vec3d min = new Vec3d(positions[0], positions[1], positions[2]).subtract(scale, scale, scale);
        Vec3d max = new Vec3d(positions[0], positions[1], positions[2]).add(scale, scale, scale).add(dims[0], dims[1], dims[2]);

//        if (cube.isTextureMirror()) {
//            double x = max.x;
//            max = new Vec3d(min.x, max.y, max.z);
//            min = new Vec3d(x, min.y, min.z);
//        }

        //A list of all the vertices. Uses bit-math to be organised.
        //The bit third from the right is used for the x coord, where 1 is the max x position and 0 is the minimum.
        //The bit second from the right is used for the y coord, and the bit rightmost is used for the z coord.
        Point3f[] vertices = new Point3f[8];
        int[] values = new int[]{0, 1};

        for (int xb : values) {
            for (int yb : values) {
                for (int zb : values) {
                    vertices[(xb << 2) | (yb << 1) | zb] = new Point3f((float) (xb == 1 ? max : min).x, (float) (yb == 1 ? max : min).y, (float) (zb == 1 ? max : min).z);
                }
            }
        }
        //Go through all the verticies and transform them with the current matrix
        for (Point3f vertex : vertices) {
            stack.peek().transform(vertex);
        }

        int w = (int) dims[0]; //width
        int h = (int) dims[1]; //height
        int d = (int) dims[2]; //depth

        //The texture format is as follows:
        //
        //
        //       This is the minimum XY coord defined in the cube.getTexOffset()
        //               \
        //                \               width       width
        //                 \         <------------><----------->
        //                  \
        //              Ʌ    X       ---------------------------
        //       depth  |            |     UP     |    DOWN    |
        //              V            |            |            |
        //              Ʌ    -------------------------------------------
        //              |    |       |            |       |            |
        //              |    |       |            |       |            |
        //      height  |    |  WEST |   NORTH    |  EAST |    SOUTH   |
        //              |    |       |            |       |            |
        //              |    |       |            |       |            |
        //              V    -------------------------------------------
        //
        //                   <-------><-----------><------><----------->
        //                     depth      width      depth      width
        //
        //      The UV coords on the UP and DOWN sections are inverse, with the minXY to the right than the maxXY

        //A map containing the list of faces->uvs[4]
        Map<EnumFacing, int[]> uvMap = Maps.newEnumMap(EnumFacing.class);

        //The line along the bottom of the texture-map (in order)
        EnumFacing[] horizontals = new EnumFacing[] { EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH };

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

        //The line along the top of the texture-map (in order)
        EnumFacing[] verticals = new EnumFacing[] { EnumFacing.UP, EnumFacing.DOWN };
        for (int i = 0; i < verticals.length; i++) {
            int minX = (int) cube.getTexOffset()[0] + d + w * i;
            int minY = (int) cube.getTexOffset()[1];
            uvMap.put(verticals[i], new int[]{minX, minY, minX + w, minY + d});

        }

        //Loop through all of the EnumFacing and create the quad for each face
        faceLoop:
        for (EnumFacing value : EnumFacing.values()) {
            int[] uvData = uvMap.get(value); //The uv data

            //Test to see if the texture is there at all. For example, in an overlay, the texture map not be set for all parts.
            //The parts with no texture should not have quads created for them.
            alphaTestSection: {
                for (int i = 0; i < layer.getSprite().getFrameCount(); i++) {
                    int[] data = layer.getSprite().getFrameTextureData(i)[0]; //mipmap of 0
                    int width = layer.getSprite().getIconWidth();
                    int height = layer.getSprite().getIconWidth();

                    for (float x = Math.min(uvData[0], uvData[2]); x < Math.max(uvData[0], uvData[2]); x++) {
                        for (float y = Math.min(uvData[1], uvData[3]); y < Math.max(uvData[1], uvData[3]); y++) {
                            int xPos = (int) ((x / this.model.getTexWidth()) * width);
                            int yPos = (int) ((y / this.model.getTexHeight()) * height);
                            int alpha = (data[xPos + yPos * width] >> 24) & 0xFF;
                            if(alpha != 0) { //If it has alpha, then break the alpha testing section.
                                break alphaTestSection;
                            }
                        }
                    }
                }
                continue;
            }

            //ts is the custom blocklight/skylight data
            float[] ts = new float[2];

            //On UP&DOWN side, directional up on the texture sheet is south for texture

            for (TabulaModelHandler.LightupData datum : this.lightupData) {
                if(datum.getLayersApplied().contains(layer.getLayerName())) {
                    for (TabulaModelHandler.LightupEntry entry : datum.getEntry()) {
                        if(entry.getCubeName().equals(cube.getName())) {
                            ts[0] = datum.getBlockLight();
                            ts[1] = datum.getSkyLight();
                        }
                    }
                }
            }

            Point3f[] pointVertices = new Point3f[4];

            for (int i = 0; i < 4; i++) {
                EnumFacing.Axis rotateAxis = value.getAxis().isHorizontal() ? EnumFacing.Axis.Y : EnumFacing.Axis.Z;
                int vertex = this.encode(value);
                //Starting plane is the screens <-- direction you get when looking directly at the plane being drawn. For the UP and DOWN planes, you should face south and north respectively.
                EnumFacing startingPlane = value.rotateAround(rotateAxis);

                //If it is the last 2 vertices being drawn, then it is at the other-side of the screen, in the other x direction.
                if(i > 1) {
                    startingPlane = startingPlane.getOpposite();
                }
                vertex |= this.encode(startingPlane);

                //Mid plane is the screens ↑ direction when looking directly at the plane being drawn
                EnumFacing midPlane = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, rotateAxis);

                //If it is either the second or the third vertex
                if(i % 3 != 0) {
                    midPlane = midPlane.getOpposite();
                }
                vertex |= this.encode(midPlane);

                pointVertices[i] = vertices[vertex];
            }

            //Make sure that this plane is not 1D, as it would be pointless to create it.
            for (int i = 0; i < pointVertices.length; i++) {
                Point3f vertex = pointVertices[i];
                for (int n = i + 1; n < pointVertices.length; n++) {
                    if (vertex.epsilonEquals(pointVertices[n], 1e-3F)) {
                        continue faceLoop;
                    }
                }
            }
            //Flip the uv data if the texture is mirrored
            if (cube.isTextureMirror()) {
                int minU = uvData[0];
                uvData[0] = uvData[2];
                uvData[2] = minU;
            }

            Vector3f normal = MathUtils.calcualeNormalF(pointVertices[0].x, pointVertices[0].y, pointVertices[0].z, pointVertices[1].x, pointVertices[1].y, pointVertices[1].z, pointVertices[2].x, pointVertices[2].y, pointVertices[2].z);
            EnumFacing quadFacing = EnumFacing.getFacingFromVector(normal.x, normal.y, normal.z);
            UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
            builder.setQuadOrientation(quadFacing);
            builder.setTexture(layer.getSprite());
            builder.setQuadTint(layer.getLayer());
            for (int i = 0; i < pointVertices.length; i++) {
                Point2f uv = new Point2f(layer.getSprite().getInterpolatedU(uvData[(i / 2)*2] / (float) this.model.getTexWidth() * 16D), layer.getSprite().getInterpolatedV(uvData[(i%3==0)?1:3] / (float) this.model.getTexHeight() * 16D));
                Point3f vert = pointVertices[i];

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
                            if(format.getElement(e).getIndex() == 0) { //normal uv
                                builder.put(e, uv.x, uv.y);
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
            outList.add(builder.build());
        }

        //Iterate through this cubes children and build the quads
        for (TabulaModelInformation.Cube child : cube.getChildren()) {
            this.build(format, outList, stack, child, layer);
        }
        
        stack.pop();
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
            if(textures.containsKey(texture.getLayerName())) {
                String remapped = textures.get(texture.getLayerName());
                if(!remapped.isEmpty()) { //Removed texture
                    textureLayers.add(new TabulaModelHandler.TextureLayer(texture.getLayerName(), new ResourceLocation(remapped), texture.getLayer()));
                }
            } else {
                textureLayers.add(texture);
            }
        }
        return this.withAllTextures(textureLayers);
    }

    /**
     * Translate a matrix stack
     * @param stack the matrix stack of which the top element will be translated
     * @param afloats an array of length 3 defined as [x, y, z]
     */
    private void translate(Stack<Matrix4f> stack, float... afloats) {
        Matrix4f matrix = stack.peek();
        Matrix4f translation = new Matrix4f();
        translation.setIdentity();
        translation.setTranslation(new Vector3f(afloats[0], afloats[1], afloats[2]));
        matrix.mul(translation);
    }

    /**
     * Rotate a matrix stack by an angle
     * @param stack The matrix stack of which the topmost element will be rotated
     * @param angle the angle, in radians, of which to rotate it by
     * @param x the x magnitude. 1 or 0
     * @param y the y magnitude. 1 or 0
     * @param z the z magnitude. 1 or 0
     */
    private void rotate(Stack<Matrix4f> stack, float angle, int x, int y, int z) {
        Matrix4f matrix = stack.peek();
        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();
        rotation.setRotation(new AxisAngle4f(x, y, z, angle));
        matrix.mul(rotation);
    }

    /**
     * Scales a matrix stack by an angle
     * @param stack The stack of which the topmost element will be scale
     * @param afloats an array of length 3, defined as [x, y, z]
     */
    private void scale(Stack<Matrix4f> stack, float... afloats) {
        Matrix4f matrix = stack.peek();
        Matrix4f scale = new Matrix4f();
        scale.m00 = afloats[0];
        scale.m11 = afloats[1];
        scale.m22 = afloats[2];
        scale.m33 = 1;
        matrix.mul(scale);
    }

    /**
     * Encodes the EnumFacing to an integer. This integer's bits are in the form: [x][y][z],
     * where 1 represents the positive direction, and 0 is the negative direction <br>
     * For example;
     *     <ul>
     *         <li>{@link EnumFacing#UP}    -> 0b010 -> 2</li>
     *         <li>{@link EnumFacing#DOWN}  -> 0b000 -> 0</li>
     *         <li>{@link EnumFacing#EAST}  -> 0b100 -> 4</li>
     *         <li>{@link EnumFacing#NORTH} -> 0b000 -> 0</li>
     *     </ul>
     * Note that this is not a 1->1 function. If the {@code facing } is facing the negative direction on it's axis,
     * then the result will just be 0.
     * @param facing the facing
     * @return the encoded integer
     */
    private int encode(EnumFacing facing) {
        return Math.max(facing.getXOffset(), 0)<<2 | Math.max(facing.getYOffset(), 0)<<1 | Math.max(facing.getZOffset(), 0);
    }
}
