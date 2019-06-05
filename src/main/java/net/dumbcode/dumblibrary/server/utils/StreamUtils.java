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
        if(FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            return openClientStream(location);
        }

        ModContainer container = Loader.instance().getIndexedModList().get(location.getNamespace());
        if(container != null) {
            FileSystem fs = null;
            try {
                String base = "assets/" + container.getModId() + "/" + location.getPath();
                File source = container.getSource();
                Path root = null;
                if (source.isFile()) {
                    fs = FileSystems.newFileSystem(source.toPath(), null);
                    root = fs.getPath("/" + base);
                } else if (source.isDirectory()) {
                    root = source.toPath().resolve(base);
                }

                if (root != null && Files.exists(root)) {
                    return Files.newInputStream(root);
                } else {
                    throw new FileNotFoundException("Could not find file " + root);
                }
            } catch (IOException e) {
                FMLLog.log.error("Error loading FileSystem: ", e);
            } finally {
                IOUtils.closeQuietly(fs);
            }
        }
        throw new IOException("Invalid mod container: " + location.getNamespace());
    }

    @SideOnly(Side.CLIENT)
    private static InputStream openClientStream(ResourceLocation location) throws IOException {
        return Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
    }


}
