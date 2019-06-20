package net.dumbcode.dumblibrary.server.utils;

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

@UtilityClass
public class StreamUtils {
    public static InputStream openStream(ResourceLocation location) throws IOException {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            return openClientStream(location);
        }
        return Files.newInputStream(getPath(location));
    }
    public static Path getPath(ResourceLocation location) {
        return getPath(location, true);
    }

    public static Path getPath(ResourceLocation location, boolean mustExist) {
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

                if (!mustExist || (root != null && root.toFile().exists())) {
                    return root;
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
}
