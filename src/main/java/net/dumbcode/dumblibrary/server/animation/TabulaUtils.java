package net.dumbcode.dumblibrary.server.animation;

import com.google.common.collect.Maps;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelAnimator;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.info.ServerAnimatableCube;
import net.dumbcode.dumblibrary.server.tabula.TabulaJsonHandler;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import javax.vecmath.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class, mainly used to get Tabula models
 */
@UtilityClass
public class TabulaUtils {

    public static final ResourceLocation MISSING = new ResourceLocation(DumbLibrary.MODID, "nomodel");

    public static Map<String, AnimationLayer.AnimatableCube> getServersideCubes(ResourceLocation location) {
        if (!location.getPath().endsWith(".tbl")) {
            location = new ResourceLocation(location.getNamespace(), location.getPath() + ".tbl");
        }

        Map<String, AnimationLayer.AnimatableCube> map = Maps.newHashMap();
        try {
            @Cleanup InputStream stream = Files.newInputStream(StreamUtils.getPath(location));
            TabulaModelInformation modelInfo = TabulaJsonHandler.GSON.fromJson(new InputStreamReader(getModelJsonStream(stream)), TabulaModelInformation.class);
            createCubeGroups(modelInfo.getGroups(), map);
        } catch (IOException e) {
            DumbLibrary.getLogger().warn("Unable to load serside cubes from model " + location, e);
        }
        return map;
    }

    public static void createCubeGroups(List<TabulaModelInformation.CubeGroup> groups, Map<String, AnimationLayer.AnimatableCube> map) {
        for (TabulaModelInformation.CubeGroup group : groups) {
            for (TabulaModelInformation.Cube cube : group.getCubeList()) {
                parseCube(cube, null, map);
            }
            createCubeGroups(group.getChildGroups(), map);
        }
    }

    private static void parseCube(TabulaModelInformation.Cube cube, @Nullable ServerAnimatableCube parent, Map<String, AnimationLayer.AnimatableCube> map) {
        ServerAnimatableCube animatableCube = new ServerAnimatableCube(parent, cube);
        map.put(cube.getName(), animatableCube);
        for (TabulaModelInformation.Cube child : cube.getChildren()) {
            parseCube(child, animatableCube, map);
        }
    }

    public static TabulaModel getModel(ResourceLocation location) {
        return getModel(location, null);
    }

    public static TabulaModel getModel(ResourceLocation location, TabulaModelAnimator animator) {
        return getModelInformation(location).createModel().setModelAnimator(animator);
    }

    public static TabulaModelInformation getModelInformation(ResourceLocation location) {
        if(MISSING.equals(location)) {
            return TabulaModelInformation.MISSING;
        }
        if (!location.getPath().endsWith(".tbl")) {
            location = new ResourceLocation(location.getNamespace(), location.getPath() + ".tbl");
        }
        try {
            @Cleanup InputStream stream = StreamUtils.openStream(location);
            return TabulaJsonHandler.GSON.fromJson(new InputStreamReader(getModelJsonStream(stream)), TabulaModelInformation.class);
        } catch (IOException e) {
            DumbLibrary.getLogger().error("Unable to load model " + location, e);
            return TabulaModelInformation.MISSING;
        }
    }

    private static InputStream getModelJsonStream(InputStream file) throws IOException {
        ZipInputStream zip = new ZipInputStream(file);
        ZipEntry entry;

        while ((entry = zip.getNextEntry()) != null) {
            if (entry.getName().equals("model.json")) {
                return zip;
            }
        }

        throw new IOException("No model.json present");
    }

    public static Vec3d getModelPosAlpha(AnimationLayer.AnimatableCube cube, float xalpha, float yalpha, float zalpha) {
        float[] offset = cube.getOffset();
        float[] dimensions = cube.getDimension();
        return getModelPos(cube,
                (offset[0] + dimensions[0] * xalpha) / 16D,
                (offset[1] + dimensions[1] * yalpha) / -16D,
                (offset[2] + dimensions[2] * zalpha) / -16D
        );
    }

    public static Vec3d getModelPos(AnimationLayer.AnimatableCube cube, double x, double y, double z) {
        Point3d rendererPos = new Point3d(x, y, z);

        Matrix4d boxTranslate = new Matrix4d();
        Matrix4d boxRotateX = new Matrix4d();
        Matrix4d boxRotateY = new Matrix4d();
        Matrix4d boxRotateZ = new Matrix4d();

        float[] point = cube.getActualRotationPoint();
        boxTranslate.set(new Vector3d(point[0] / 16D, -point[1] / 16D, -point[2] / 16D));

        float[] rotation = cube.getActualRotation();

        boxRotateX.rotX(rotation[0]);
        boxRotateY.rotY(-rotation[1]);
        boxRotateZ.rotZ(-rotation[2]);

        boxRotateX.transform(rendererPos);
        boxRotateY.transform(rendererPos);
        boxRotateZ.transform(rendererPos);
        boxTranslate.transform(rendererPos);

        AnimationLayer.AnimatableCube parent = cube.getParent();
        if (parent != null) {
            return getModelPos(parent, rendererPos.getX(), rendererPos.getY(), rendererPos.getZ());
        }
        return new Vec3d(rendererPos.getX(), rendererPos.getY(), rendererPos.getZ());
    }

    /**
     * Computes the origin of a given part relative to the model origin. Takes into account parent parts.
     *
     * @param part the model part
     * @return the translation vector relative to the model origin
     */
    public Vector3f computeTranslationVector(TabulaModelRenderer part) {
        Matrix4f transform = computeTransformMatrix(part);
        Vector3f result = new Vector3f(0f, 0f, 0f);
        transform.transform(result);
        return result;
    }

    /**
     * Computes the matrix representing the rotation and translation of a given part. Takes into account parent parts
     *
     * @param part the model part
     * @return the matrix representing the rotation and translation
     */
    public static Matrix4f computeTransformMatrix(TabulaModelRenderer part) {
        Matrix4f result = new Matrix4f();
        result.setIdentity();
        applyTransformations(part, result);
        return result;
    }

    /**
     * Apply the translation and rotations of a part to the given matrix. Takes into account parent parts.
     *
     * @param part the model part
     * @param out  the matrix to finalizeComponent the transformations to
     */
    public static void applyTransformations(TabulaModelRenderer part, Matrix4f out) {
        TabulaModelRenderer parent = part.getParent();
        if (parent != null) {
            applyTransformations(parent, out);
        }
        Matrix4f translation = new Matrix4f();
        translation.setIdentity();
        translation.setTranslation(new Vector3f(part.offsetX, part.offsetY, part.offsetZ));
        out.mul(translation);

        float scale = 1f / 16f;
        translation.setIdentity();
        translation.setTranslation(new Vector3f(part.rotationPointX * scale, part.rotationPointY * scale, part.rotationPointZ * scale));
        out.mul(translation);

        out.rotZ(part.rotateAngleZ);
        out.rotY(part.rotateAngleY);
        out.rotX(part.rotateAngleX);

        if (part.isScaleChildren()) {
            Matrix4f scaling = new Matrix4f();
            scaling.setIdentity();
            scaling.m00 = part.getScaleX();
            scaling.m11 = part.getScaleY();
            scaling.m22 = part.getScaleX();
            out.mul(scaling);
        }
    }

    /**
     * Computes the matrix representing the rotation of a given part. Takes into account parent parts.
     *
     * @param part the model part
     * @return the matrix representing the rotation
     */
    public static Matrix3f computeRotationMatrix(TabulaModelRenderer part) {
        Matrix3f result = new Matrix3f();
        result.setIdentity();
        applyRotations(part, result);
        return result;
    }

    /**
     * Apply only the rotations of a part to the given matrix. Takes into account parent parts.
     *
     * @param part   the model part
     * @param result the matrix to finalizeComponent the rotations to
     */
    public static void applyRotations(TabulaModelRenderer part, Matrix3f result) {
        TabulaModelRenderer parent = part.getParent();
        if (parent != null) {
            applyRotations(part, result);
        }
        result.rotZ(part.rotateAngleZ);
        result.rotY(part.rotateAngleY);
        result.rotX(part.rotateAngleX);
    }

}
