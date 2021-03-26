package net.dumbcode.dumblibrary.server.utils;

import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

//Utility class for input streams, and the java stream api.
@UtilityClass
public class StreamUtils {
    public static <R> R openStream(ResourceLocation location, FunctionException<InputStream, R, IOException> consumer) throws IOException {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return consumer.accept(openClientStream(location));
        }
        return getPath(location, path -> {
            @Cleanup InputStream stream = Files.newInputStream(path);
            return consumer.accept(stream);
        });
    }
    public static <R> R getPath(ResourceLocation location, FunctionException<Path, R, IOException> consumer) throws IOException{
         return getPath(location, true, consumer);
    }

    public static <R> R getPath(ResourceLocation location, boolean mustExist, FunctionException<Path, R, IOException> consumer) throws IOException {
        ModFileInfo info = ModList.get().getModFileById(location.getNamespace());
        if (info != null) {
            String base = "assets/" + location.getNamespace() + "/" + location.getPath();
            FileSystem fs = null;
            try {
                Path root = null;
                Path source = info.getFile().getFilePath();
                if (Files.isRegularFile(source)) {
                    fs = FileSystems.newFileSystem(source, null);
                    root = fs.getPath("/" + base);
                } else if (Files.isDirectory(source)) {
                    root = source.resolve(base);
                }

                if (!mustExist || (root != null && Files.exists(root))) {
                    return consumer.accept(root);
                } else {
                    throw new FileNotFoundException("Could not find file " + root);
                }
            } finally {
                IOUtils.closeQuietly(fs);
            }
        }
        throw new IllegalArgumentException("Invalid mod container: " + location.getNamespace());
    }

    public Collection<Path> listPaths(Path path) throws IOException {
        try(Stream<Path> stream = Files.walk(path, 1)) {
            return stream.filter(p -> p != path).collect(Collectors.toList());
        }
    }

    private static InputStream openClientStream(ResourceLocation location) throws IOException {
        return Minecraft.getInstance().getResourceManager().getResource(location).getInputStream();
    }

    public static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
