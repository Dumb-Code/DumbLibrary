package net.dumbcode.dumblibrary.server.animation;

import com.google.common.collect.Maps;
import lombok.experimental.UtilityClass;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.info.ServerAnimatableCube;
import net.dumbcode.dumblibrary.server.utils.MissingModelInfo;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.dumbcode.studio.model.CubeInfo;
import net.dumbcode.studio.model.ModelInfo;
import net.dumbcode.studio.model.ModelLoader;
import net.dumbcode.studio.model.ModelWriter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Utility class, mainly used to get Tabula models
 */
@UtilityClass
public class DCMUtils {

    public static final ResourceLocation MISSING = new ResourceLocation(DumbLibrary.MODID, "nomodel");

    public static Map<String, AnimatedReferenceCube> getServersideCubes(ResourceLocation location) {
        if (!location.getPath().endsWith(".tbl")) {
            location = new ResourceLocation(location.getNamespace(), location.getPath() + ".tbl");
        }

        Map<String, AnimatedReferenceCube> map = Maps.newHashMap();
        ModelInfo modelInfo = getModelInformation(location);
        parseCubes(modelInfo.getRoots(), null, map);
        return map;
    }


    private static void parseCubes(List<CubeInfo> cubes, @Nullable ServerAnimatableCube parent, Map<String, AnimatedReferenceCube> map) {
        for (CubeInfo cube : cubes) {
            ServerAnimatableCube animatableCube = new ServerAnimatableCube(parent, cube);
            map.put(cube.getName(), animatableCube);
            parseCubes(cube.getChildren(), animatableCube, map);
        }
    }

    public static DCMModel getModel(ResourceLocation location) {
        return new DCMModel(getModelInformation(location));
    }

    public static ModelInfo getModelInformation(ResourceLocation location) {
        if(MISSING.equals(location)) {
            return MissingModelInfo.MISSING;
        }
        if (!location.getPath().endsWith(".tbl")) {
            location = new ResourceLocation(location.getNamespace(), location.getPath() + ".tbl");
        }
        try {
            return StreamUtils.openStream(location, DCMUtils::getModelInformation);
        } catch (IOException e) {
            DumbLibrary.getLogger().error("Unable to load model " + location, e);
            return MissingModelInfo.MISSING;
        }
    }

    @Deprecated
    public static ModelInfo getModelInformation(InputStream stream) throws IOException {
        return ModelLoader.loadModel(stream);
    }


    @Deprecated
    public static void writeToStream(ModelInfo model, OutputStream stream) throws IOException {
        ModelWriter.writeModel(model, stream);
    }

    public static Vector3f getModelPosAlpha(AnimatedReferenceCube cube, float xalpha, float yalpha, float zalpha) {
        CubeInfo info = cube.getInfo();
        float[] offset = info.getOffset();
        int[] dimensions = info.getDimensions();
        return getModelPos(cube,
            (offset[0] + dimensions[0] * xalpha) / 16F,
            (offset[1] + dimensions[1] * yalpha) / -16F,
            (offset[2] + dimensions[2] * zalpha) / -16F
        );
    }

    public static Vector3f getModelPos(AnimatedReferenceCube cube, float x, float y, float z) {
        Vector4f rendererPos = new Vector4f(x, y, z, 1);

        Matrix4f boxTranslate = new Matrix4f();
        Matrix4f boxRotateX = new Matrix4f();
        Matrix4f boxRotateY = new Matrix4f();
        Matrix4f boxRotateZ = new Matrix4f();

        float[] point = cube.getPosition();
        boxTranslate.translate(new Vector3f(point[0] / 16F, -point[1] / 16F, -point[2] / 16F));

        float[] rotation = cube.getRotation();
        boxRotateX.multiply(Vector3f.XP.rotation(rotation[0]));
        boxRotateX.multiply(Vector3f.YP.rotation(-rotation[1]));
        boxRotateX.multiply(Vector3f.ZP.rotation(-rotation[2]));

        rendererPos.transform(boxRotateX);
        rendererPos.transform(boxRotateY);
        rendererPos.transform(boxRotateZ);
        rendererPos.transform(boxTranslate);

        AnimatedReferenceCube parent = cube.getParent();
        if (parent != null) {
            return getModelPos(parent, rendererPos.x(), rendererPos.y(), rendererPos.z());
        }
        return new Vector3f(rendererPos.x(), rendererPos.y(), rendererPos.z());
    }

    /**
     * Computes the origin of a given part relative to the model origin. Takes into account parent parts.
     *
     * @param part the model part
     * @return the translation vector relative to the model origin
     */
    public Vector3f computeTranslationVector(AnimatedReferenceCube part) {
        Matrix4f transform = computeTransformMatrix(part);
        Vector4f result = new Vector4f(0f, 0f, 0f, 1F);
        result.transform(transform);
        return new Vector3f(result.x(), result.y(), result.z());
    }

    /**
     * Computes the matrix representing the rotation and translation of a given part. Takes into account parent parts
     *
     * @param part the model part
     * @return the matrix representing the rotation and translation
     */
    public static Matrix4f computeTransformMatrix(AnimatedReferenceCube part) {
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
    public static void applyTransformations(AnimatedReferenceCube part, Matrix4f out) {
        AnimatedReferenceCube parent = part.getParent();
        if (parent != null) {
            applyTransformations(parent, out);
        }
        Matrix4f translation = new Matrix4f();
        translation.setIdentity();
        float[] offset = part.getInfo().getOffset();
        translation.setTranslation(offset[0], offset[1], offset[2]);
        out.multiply(translation);

        float scale = 1f / 16f;
        translation.setIdentity();
        float[] position = part.getPosition();
        translation.setTranslation(position[0] * scale, position[1] * scale, position[2] * scale);
        out.multiply(translation);

        float[] rotation = part.getRotation();
        out.multiply(Vector3f.ZP.rotation(rotation[2]));
        out.multiply(Vector3f.YP.rotation(rotation[1]));
        out.multiply(Vector3f.XP.rotation(rotation[0]));
    }

    /**
     * Computes the matrix representing the rotation of a given part. Takes into account parent parts.
     *
     * @param part the model part
     * @return the matrix representing the rotation
     */
    public static Matrix3f computeRotationMatrix(AnimatedReferenceCube part) {
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
    public static void applyRotations(AnimatedReferenceCube part, Matrix3f result) {
        AnimatedReferenceCube parent = part.getParent();
        if (parent != null) {
            applyRotations(part, result);
        }
        float[] rotation = part.getRotation();
        result.mul(Vector3f.ZP.rotation(rotation[2]));
        result.mul(Vector3f.YP.rotation(rotation[1]));
        result.mul(Vector3f.XP.rotation(rotation[0]));
    }

}