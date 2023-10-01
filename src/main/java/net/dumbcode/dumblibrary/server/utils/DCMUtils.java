package net.dumbcode.dumblibrary.server.utils;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.experimental.UtilityClass;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.animation.AnimatedReferenceCube;
import net.dumbcode.dumblibrary.server.info.ServerAnimatableCube;
import net.dumbcode.studio.model.*;
import net.dumbcode.studio.util.CubeUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import org.joml.Vector3f;
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
        if (!location.getPath().endsWith(".dcm")) {
            location = new ResourceLocation(location.getNamespace(), location.getPath() + ".dcm");
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
        return getModel(location, true);
    }

    public static DCMModel getModel(ResourceLocation location, boolean flipped) {
        return new DCMModel(getModelInformation(location, flipped));
    }

    public static ModelInfo getModelInformation(ResourceLocation location) {
        return getModelInformation(location, true);
    }

    public static ModelInfo getModelInformation(ResourceLocation location, boolean flipped) {
        if(MISSING.equals(location)) {
            return MissingModelInfo.MISSING;
        }
        if (!location.getPath().endsWith(".dcm")) {
            location = new ResourceLocation(location.getNamespace(), location.getPath() + ".dcm");
        }
        try {
            return StreamUtils.openStream(location, inputStream -> ModelLoader.loadModel(inputStream, RotationOrder.ZYX, flipped ? ModelMirror.global : ModelMirror.NONE));
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
        double[] position = CubeUtils.getWorldPosition(cube, xalpha, yalpha, zalpha);
        return new Vector3f(
            (float) position[0] / 16F, (float) position[1] / 16F, (float) position[2] / 16F
        );
    }

}