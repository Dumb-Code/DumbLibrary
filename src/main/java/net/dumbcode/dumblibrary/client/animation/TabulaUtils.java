package net.dumbcode.dumblibrary.client.animation;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import net.dumbcode.dumblibrary.client.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.client.model.InfoTabulaModel;
import net.dumbcode.dumblibrary.server.info.ServerAnimatableCube;
import net.ilexiconn.llibrary.client.model.tabula.ITabulaModelAnimator;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tabula.container.TabulaCubeContainer;
import net.ilexiconn.llibrary.client.model.tabula.container.TabulaModelContainer;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class, mainly used to get Tabula models
 */
@UtilityClass
public class TabulaUtils {

    public static Map<String, AnimationLayer.AnimatableCube> getServersideCubes(ResourceLocation location) {
        if(!location.getResourcePath().endsWith(".tbl")) {
            location = new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".tbl");
        }
        Map<String, AnimationLayer.AnimatableCube> map = Maps.newHashMap();
        ModContainer container = Loader.instance().getIndexedModList().get(location.getResourceDomain());
        if(container != null) {
            FileSystem fs = null;
            try {
                String base = "assets/" + container.getModId() + "/" + location.getResourcePath();
                File source = container.getSource();
                Path root = null;
                if (source.isFile()) {
                    fs = FileSystems.newFileSystem(source.toPath(), null);
                    root = fs.getPath("/" + base);
                } else if (source.isDirectory()) {
                    root = source.toPath().resolve(base);
                }

                if (root != null && Files.exists(root)) {
                    @Cleanup InputStream stream = Files.newInputStream(root);
                    TabulaModelContainer modelContainer = new Gson().fromJson(new InputStreamReader(getModelJsonStream(stream)), TabulaModelContainer.class);
                    for (TabulaCubeContainer containerCube : modelContainer.getCubes()) {
                        parseCube(containerCube, null, map);
                    }

                }
            } catch (IOException e) {
                FMLLog.log.error("Error loading FileSystem: ", e);
            } finally {
                IOUtils.closeQuietly(fs);
            }
        }
        return map;
    }

    private static void parseCube(TabulaCubeContainer cube, @Nullable ServerAnimatableCube parent, Map<String, AnimationLayer.AnimatableCube> map) {
        ServerAnimatableCube animatableCube = new ServerAnimatableCube(parent, cube);
        map.put(cube.getName(), animatableCube);
        for (TabulaCubeContainer child : cube.getChildren()) {
            parseCube(child, animatableCube, map);
        }
    }

    public static TabulaModel getModel(ResourceLocation location){
        return getModel(location, null);
    }

    public static TabulaModel getModel(ResourceLocation location, ITabulaModelAnimator animator){
        if(!location.getResourcePath().endsWith(".tbl")) {
            location = new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".tbl");
        }
        try {
            @Cleanup InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
            return new InfoTabulaModel(new Gson().fromJson(new InputStreamReader(getModelJsonStream(stream)), TabulaModelContainer.class), animator);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load model " + location, e);
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

    /**
     * Computes the origin of a given part relative to the model origin. Takes into account parent parts.
     * @param part the model part
     * @return the translation vector relative to the model origin
     */
    public Vector3f computeTranslationVector(AdvancedModelRenderer part) {
        Matrix4f transform = computeTransformMatrix(part);
        Vector3f result = new Vector3f(0f, 0f, 0f);
        transform.transform(result);
        return result;
    }

    /**
     * Computes the matrix representing the rotation and translation of a given part. Takes into account parent parts
     * @param part the model part
     * @return the matrix representing the rotation and translation
     */
    public static Matrix4f computeTransformMatrix(AdvancedModelRenderer part) {
        Matrix4f result = new Matrix4f();
        result.setIdentity();
        applyTransformations(part, result);
        return result;
    }

    /**
     * Apply the translation and rotations of a part to the given matrix. Takes into account parent parts.
     * @param part the model part
     * @param out the matrix to apply the transformations to
     */
    public static void applyTransformations(AdvancedModelRenderer part, Matrix4f out) {
        AdvancedModelRenderer parent = part.getParent();
        if(parent != null) {
            applyTransformations(parent, out);
        }
        Matrix4f translation = new Matrix4f();
        translation.setIdentity();
        translation.setTranslation(new Vector3f(part.offsetX, part.offsetY, part.offsetZ));
        out.mul(translation);

        float scale = 1f/16f;
        translation.setIdentity();
        translation.setTranslation(new Vector3f(part.rotationPointX*scale, part.rotationPointY*scale, part.rotationPointZ*scale));
        out.mul(translation);

        out.rotZ(part.rotateAngleZ);
        out.rotY(part.rotateAngleY);
        out.rotX(part.rotateAngleX);

        if(part.scaleChildren) {
            Matrix4f scaling = new Matrix4f();
            scaling.setIdentity();
            scaling.m00 = part.scaleX;
            scaling.m11 = part.scaleY;
            scaling.m22 = part.scaleZ;
            out.mul(scaling);
        }
    }

    /**
     * Computes the matrix representing the rotation of a given part. Takes into account parent parts.
     * @param part the model part
     * @return the matrix representing the rotation
     */
    public static Matrix3f computeRotationMatrix(AdvancedModelRenderer part) {
        Matrix3f result = new Matrix3f();
        result.setIdentity();
        applyRotations(part, result);
        return result;
    }

    /**
     * Apply only the rotations of a part to the given matrix. Takes into account parent parts.
     * @param part the model part
     * @param result the matrix to apply the rotations to
     */
    public static void applyRotations(AdvancedModelRenderer part, Matrix3f result) {
        AdvancedModelRenderer parent = part.getParent();
        if(parent != null) {
            applyRotations(part, result);
        }
        result.rotZ(part.rotateAngleZ);
        result.rotY(part.rotateAngleY);
        result.rotX(part.rotateAngleX);
    }

}
