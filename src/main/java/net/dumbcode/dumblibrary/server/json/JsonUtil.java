package net.dumbcode.dumblibrary.server.json;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class JsonUtil
{

    private static final String FOLDER_NAME = "add-ons";

    /**
     * Gets all files with the name modid/folderName(s) from every active mod and registers them.
     * @param registry forge registry
     * @param gson gson to read the json
     * @param modid Your mods mod id
     * @param folderNames folder name(s) to get files from
     * @param <T>
     */
    public static <T extends IForgeRegistryEntry.Impl<T>> void registerModJsons(IForgeRegistry<T> registry, Gson gson, String modid, String... folderNames)
    {
        Loader.instance().getIndexedModList().forEach((s, mod) ->
        {
            Loader.instance().setActiveModContainer(mod);
            Arrays.stream(folderNames).forEach(name ->
                    CraftingHelper.findFiles(mod, "assets/" + mod.getModId() + "/" + modid + "/" + name, null,
                            (root, file) ->
                            {
                                if (!"json".equals(FilenameUtils.getExtension(file.toString())))
                                {
                                    return true;
                                }
                                String relative = root.relativize(file).toString();
                                ResourceLocation key = new ResourceLocation(mod.getModId(), FilenameUtils.removeExtension(relative).replaceAll("\\\\", "/"));
                                BufferedReader reader = null;
                                try
                                {
                                    reader = Files.newBufferedReader(file);
                                    T value = JsonUtils.fromJson(gson, reader, registry.getRegistrySuperType());
                                    if (value == null)
                                    {
                                        return false;
                                    } else
                                    {
                                        registry.register(value.setRegistryName(key));
                                    }
                                } catch (JsonParseException e)
                                {
                                    DumbLibrary.getLogger().error("Parsing error loading json: " + key, e);
                                    return false;
                                } catch (IOException e)
                                {
                                    DumbLibrary.getLogger().error("Couldn't read json " + key + " from " + file, e);
                                    return false;
                                } finally
                                {
                                    IOUtils.closeQuietly(reader);
                                }
                                return true;
                            }, true, true));
        });
        Loader.instance().setActiveModContainer(Loader.instance().getIndexedModList().get(modid));
    }

    /**
     * Gets all files with the name add-ons/modid/folderName(s) and registers them.
     * @param registry forge registry
     * @param gson gson to help read the json
     * @param modid Your mods mod id
     * @param folderNames folder name(s) you want to get files from
     * @param <T>
     */
    public static <T extends IForgeRegistryEntry.Impl<T>> void registerLocalJsons(IForgeRegistry<T> registry, Gson gson, String modid, String... folderNames)
    {
        Arrays.stream(folderNames).forEach(name ->
        {
            try (Stream<Path> paths = Files.walk(Paths.get(".", FOLDER_NAME, modid, name)))
            {
                paths.filter(Files::isRegularFile).forEach(path ->
                {
                    File file = new File(path.toString());
                    if (!"json".equals(FilenameUtils.getExtension(file.toString())))
                    {
                        return;
                    }
                    ResourceLocation key = new ResourceLocation(modid, FilenameUtils.removeExtension(file.getName()).replaceAll("\\\\", "/"));
                    BufferedReader reader = null;
                    try
                    {
                        reader = Files.newBufferedReader(path);
                        T value = JsonUtils.fromJson(gson, reader, registry.getRegistrySuperType());
                        if (value == null)
                        {
                            return;
                        } else
                        {
                            registry.register(value.setRegistryName(key));
                        }
                    } catch (JsonParseException e)
                    {
                        DumbLibrary.getLogger().error("Parsing error loading json: " + key, e);
                        return;
                    } catch (IOException e)
                    {
                        DumbLibrary.getLogger().error("Couldn't read json " + key + " from " + file, e);
                        return;
                    } finally
                    {
                        IOUtils.closeQuietly(reader);
                    }
                });
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        });
    }

    /**
     * Creates a directory outside the mods folder with your mod id, in a folder called add-ons
     * @param modid Your mod id
     * @return
     */
    public static File createModFolder(String modid)
    {
        File folder = new File(".", FOLDER_NAME + "/" + modid);
        if (!folder.exists())
        {
            folder.mkdirs();
        }
        return folder;
    }

    /**
     * Creates sub directories in your mod folder.
     * @param modid Your mod id
     * @param fileNames
     */
    public static void makeSubDirectories(String modid, String... fileNames)
    {
        File file = createModFolder(modid);
        Arrays.stream(fileNames).forEach(s ->
                {
                    File test = new File(file, s);
                    if (!test.exists())
                    {
                        test.mkdirs();
                    }
                }
        );
    }
}