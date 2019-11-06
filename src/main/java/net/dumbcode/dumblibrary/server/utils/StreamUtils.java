package net.dumbcode.dumblibrary.server.utils;

import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

//Utility class for input streams, and the java stream api.
@UtilityClass
public class StreamUtils {
    public static <R> R openStream(ResourceLocation location, FunctionException<InputStream, R, IOException> consumer) throws IOException {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            return consumer.accept(openClientStream(location));
        }
        return getPath(location, path -> {
            @Cleanup InputStream stream = Files.newInputStream(path);
            return consumer.accept(stream);
        });
    }
    public static <R> R getPath(ResourceLocation location, FunctionException<Path, R, IOException> consumer) {
         return getPath(location, true, consumer);
    }

    public static <R> R getPath(ResourceLocation location, boolean mustExist, FunctionException<Path, R, IOException> consumer) {
        ModContainer container = Loader.instance().getIndexedModList().get(location.getNamespace());
        if (container != null) {
            String base = "assets/" + container.getModId() + "/" + location.getPath();
            File source = container.getSource();
            FileSystem fs = null;
            try {
                Path root = null;
                if (source.isFile()) {
                    fs = FileSystems.newFileSystem(source.toPath(), null);
                    root = fs.getPath("/" + base);
                } else if (source.isDirectory()) {
                    root = source.toPath().resolve(base);
                }

                if (!mustExist || (root != null && Files.exists(root))) {
                    return consumer.accept(root);
                } else {
                    throw new FileNotFoundException("Could not find file " + root);
                }
            } catch (IOException e) {
                FMLLog.log.error("Error loading FileSystem: ", e);
            } finally {
                IOUtils.closeQuietly(fs);
            }
        }
        throw new IllegalArgumentException("Invalid mod container: " + location.getNamespace());
    }

    @SideOnly(Side.CLIENT)
    private static InputStream openClientStream(ResourceLocation location) throws IOException {
        return Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
    }

    public static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
