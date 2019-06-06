package net.dumbcode.dumblibrary.client.model.tabula.baked;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelInformation;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nullable;
import javax.vecmath.*;
import java.util.*;

public class TabulaModelBaker {
    private final Stack<Matrix4f> matrixStack = new Stack<>();
    private final List<BakedQuad> quadList = Lists.newArrayList();

    private final Collection<TabulaModelHandler.TextureLayer> allTextures;
    private final  List<TabulaModelHandler.LightupData> lightupData;
    private final TabulaModelInformation model;
    private final VertexFormat format;
    private final IModelState state;

    public TabulaModelBaker(Collection<TabulaModelHandler.TextureLayer> allTextures, List<TabulaModelHandler.LightupData> lightupData, TabulaModelInformation model, VertexFormat format, IModelState state) {
        this.allTextures = allTextures;
        this.lightupData = lightupData;
        this.model = model;
        this.state = state;
        if(!lightupData.isEmpty()) {//has light data
            if (format == DefaultVertexFormats.ITEM) { // ITEM is convertible to BLOCK (replace normal+padding with lmap)
                format = DefaultVertexFormats.BLOCK;
            } else if (!format.getElements().contains(DefaultVertexFormats.TEX_2S)) { // Otherwise, this format is unknown, add TEX_2S if it does not exist
                format = new VertexFormat(format).addElement(DefaultVertexFormats.TEX_2S);
            }
        }
        this.format = format;

    }

    public List<BakedQuad> bake() {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        this.matrixStack.clear();
        this.matrixStack.push(matrix4f);


        this.push();

        this.mul(this.state.apply(Optional.empty()).orElse(TRSRTransformation.identity()).getMatrix());
        this.translate(0.5F, 1.5F, 0.5F);
        this.scale(-0.0625F, -0.0625F, 0.0625F);

        for (TabulaModelHandler.TextureLayer layer : this.allTextures) {
            for (TabulaModelInformation.CubeGroup group : this.model.getGroups()) {
                for (TabulaModelInformation.Cube cube : group.getCubeList()) {
                    build(cube, layer);
                }
            }
        }

        this.pop();
        return this.quadList;
    }

    private void build(TabulaModelInformation.Cube cube, TabulaModelHandler.TextureLayer layer) {

        this.push();

        translate(cube.getRotationPoint());
        scale(cube.getScale());
        float[] rotation = cube.getRotation();
        if (rotation[2] != 0) {
            rotate(rotation[2], 0, 0, 1);
        }
        if (rotation[1] != 0) {
            rotate(rotation[1], 0, 1, 0);
        }
        if (rotation[0] != 0) {
            rotate(rotation[0], 1, 0, 0);
        }
        float[] positions = cube.getOffset();
        float[] dims = cube.getDimension();
        float scale = cube.getMcScale();
        Vec3d min = new Vec3d(positions[0], positions[1], positions[2]).subtract(scale, scale, scale);
        Vec3d max = new Vec3d(positions[0], positions[1], positions[2]).add(scale, scale, scale).add(dims[0], dims[1], dims[2]);

        if (cube.isTextureMirror()) {
            double x = max.x;
            max = new Vec3d(min.x, max.y, max.z);
            min = new Vec3d(x, min.y, min.z);
        }

        Point3f[] vertices = new Point3f[8];
        int[] values = new int[]{0, 1};

        for (int xb : values) {
            for (int yb : values) {
                for (int zb : values) {
                    vertices[(xb << 2) | (yb << 1) | zb] = new Point3f((float) (xb == 1 ? max : min).x, (float) (yb == 1 ? max : min).y, (float) (zb == 1 ? max : min).z);
                }
            }
        }
        for (Point3f vertex : vertices) {
            this.matrixStack.peek().transform(vertex);
        }
        //north = forward
        //east = right
        //south = backwards
        //west left
        int w = (int) dims[0];
        int h = (int) dims[1];
        int d = (int) dims[2];


        Map<EnumFacing, int[]> uvMap = Maps.newEnumMap(EnumFacing.class);
        EnumFacing[] horozontals = new EnumFacing[]{EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH};

        int offX = 0;
        for (int i = 0; i < horozontals.length; i++) {
            int minX = (int) cube.getTexOffset()[0] + offX;
            int minY = (int) cube.getTexOffset()[1] + d;
            int xdist = i % 2 == 0 ? d : w;
            offX += xdist;
            uvMap.put(horozontals[i], new int[]{minX, minY, minX + xdist, minY + h});
        }

        int minX = (int) cube.getTexOffset()[0] + d;
        int minY = (int) cube.getTexOffset()[1];
        uvMap.put(EnumFacing.DOWN, new int[]{minX, minY, minX + w, minY + d});
        uvMap.put(EnumFacing.UP, new int[]{minX + w, minY + d, minX + w * 2, minY});

        faceLoop:
        for (EnumFacing value : new EnumFacing[]{EnumFacing.EAST, EnumFacing.WEST, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH}) {
            boolean positive = value.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE;
            boolean dominant = value == EnumFacing.EAST || value == EnumFacing.NORTH || value == EnumFacing.DOWN;
            int[] numbers = new int[4];

            //The following is convoluted maths in order to get the correct pointVerticies given the enum facing.
            //Could i do a lookup? yes
            if(value.getHorizontalIndex() != -1) {
                numbers[2] |= 0b010;
                numbers[3] |= 0b010;

                int bit = value.getAxis() == EnumFacing.Axis.X ? 0:2;
                for (int i = 0; i < 4; i++) {
                    numbers[i] |= (((i % 3 == 0) == dominant) ? 1 : 0) << bit;
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    numbers[i] |= (((i / 2 == 0) == dominant) ? 1 : 0) | (((i % 3 == 0)) ? 1 : 0) << 2;
                }
            }
            if(positive) {
                for (int i = 0; i < 4; i++) {
                    numbers[i] |= value.getXOffset()<<2|value.getYOffset()<<1|value.getZOffset();
                }
            }

            Point2i ts = new Point2i();

            for (TabulaModelHandler.LightupData datum : this.lightupData) {
                if(datum.getLayersApplied().contains(layer.getName())) {
                    for (TabulaModelHandler.LightupEntry entry : datum.getEntry()) {
                        if(entry.getCubeName().equals(cube.getName())) {
                            ts = new Point2i(datum.getBlockLight(), datum.getSkyLight());
                        }
                    }
                }
            }

            Point3f[] pointVerticies = Arrays.stream(numbers).mapToObj(i -> vertices[i]).toArray(Point3f[]::new);
            int[] aint = uvMap.get(value);

            for (int i = 0; i < pointVerticies.length; i++) {
                Point3f vertex = pointVerticies[i];
                for (int n = i + 1; n < pointVerticies.length; n++) {
                    if (vertex.epsilonEquals(pointVerticies[n], 1e-3F)) {
                        continue faceLoop;
                    }
                }
            }
            Point2i[] uvs = { new Point2i(aint[2], aint[1]), new Point2i(aint[0], aint[1]), new Point2i(aint[0], aint[3]), new Point2i(aint[2], aint[3]) };
            if (cube.isTextureMirror()) {
                Point3f[] verticesMirrored = new Point3f[pointVerticies.length];
                Point2i[] uvsMirrored = new Point2i[pointVerticies.length];
                for (int i = 0, j = pointVerticies.length - 1; i < pointVerticies.length; i++, j--) {
                    verticesMirrored[i] = pointVerticies[j];
                    uvsMirrored[i] = uvs[j];
                }
                pointVerticies = verticesMirrored;
                uvs = uvsMirrored;
            }
            Vector3f normal = MathUtils.calcualeNormalF(pointVerticies[1].x, pointVerticies[1].y, pointVerticies[1].z, pointVerticies[2].x, pointVerticies[2].y, pointVerticies[2].z, pointVerticies[0].x, pointVerticies[0].y, pointVerticies[0].z);
            EnumFacing quadFacing = EnumFacing.getFacingFromVector(normal.x, normal.y, normal.z);
            UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(this.format);
            builder.setQuadOrientation(quadFacing);
            builder.setTexture(layer.getSprite());
            builder.setQuadTint(layer.getLayer());
            float width = this.model.getTexWidth();
            float height = this.model.getTexHeight();
            for (int i = 0; i < pointVerticies.length; i++) {
                Point2i uvi = uvs[i];
                Point2f uv = new Point2f(layer.getSprite().getInterpolatedU(uvi.x / width * 16), layer.getSprite().getInterpolatedV(uvi.y / height * 16));
                Point3f vert = pointVerticies[i];

                for (int e = 0; e < format.getElementCount(); e++) {
                    switch (format.getElement(e).getUsage()) {
                        case POSITION:
                            builder.put(e, vert.x, vert.y, vert.z);
                            break;
                        case COLOR:
                            builder.put(e, 1, 1, 1, 1);
                            break;
                        case UV:
                            if(format.getElement(e).getIndex() == 0) {
                                builder.put(e, uv.x, uv.y);
                            } else {
                                builder.put(e, ((float)(ts.x << 4)) / 0x7FFF, ((float)(ts.y << 4)) / 0x7FFF);
                            }
                            break;
                        case NORMAL:
                            builder.put(e, normal.x, normal.y, normal.z);
                            break;
                        default:
                            builder.put(e);
                    }
                }
            }
            this.quadList.add(builder.build());


        }

        for (TabulaModelInformation.Cube child : cube.getChildren()) {
            this.build(child, layer);
        }
        this.pop();
    }

    private void push() {
        this.matrixStack.push(new Matrix4f(this.matrixStack.peek()));
    }

    private void pop() {
        this.matrixStack.pop();
    }

    public void mul(Matrix4f mat) {
        this.matrixStack.peek().mul(mat);
    }

    public void translate(float... afloats) {
        Matrix4f matrix = this.matrixStack.peek();
        Matrix4f translation = new Matrix4f();
        translation.setIdentity();
        translation.setTranslation(new Vector3f(afloats[0], afloats[1], afloats[2]));
        matrix.mul(translation);
    }

    public void rotate(float angle, int x, int y, int z) {
        Matrix4f matrix = this.matrixStack.peek();
        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();
        rotation.setRotation(new AxisAngle4f(x, y, z, angle));
        matrix.mul(rotation);
    }

    public void scale(float... afloats) {
        Matrix4f matrix = this.matrixStack.peek();
        Matrix4f scale = new Matrix4f();
        scale.m00 = afloats[0];
        scale.m11 = afloats[1];
        scale.m22 = afloats[2];
        scale.m33 = 1;
        matrix.mul(scale);
    }
}
