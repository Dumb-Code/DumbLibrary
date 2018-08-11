package net.dumbcode.dumblibrary.server.guidebooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.guidebooks.elements.GuidebookElement;
import net.dumbcode.dumblibrary.server.json.JsonUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.registries.IForgeRegistry;

@UtilityClass
public class GuidebooksManager {
    public static IForgeRegistry<Guidebook> GUIDEBOOK_REGISTRY;

    public static void registerBooks(IForgeRegistry<Item> registry) {
        Gson gson = GuidebooksManager.prepareGsonBuilderForGuidebooks(new GsonBuilder()).create();
        JsonUtil.registerModJsons(GuidebooksManager.GUIDEBOOK_REGISTRY, gson, DumbLibrary.MODID, "guidebooks");
        GUIDEBOOK_REGISTRY.forEach(book -> {
            Loader.instance().setActiveModContainer(Loader.instance().getIndexedModList().get(book.getRegistryName().getResourceDomain()));
            String id = "guidebook_"+book.getRegistryName().getResourcePath();
            registry.register(new GuidebookItem(book).setUnlocalizedName(id).setCreativeTab(CreativeTabs.MISC).setRegistryName(new ResourceLocation(book.getRegistryName().getResourceDomain(), id)));

            book.compilePages();
        });
        Loader.instance().setActiveModContainer(Loader.instance().getIndexedModList().get(DumbLibrary.MODID));
    }

    public static GsonBuilder prepareGsonBuilderForGuidebooks(GsonBuilder builder) {
        builder.setPrettyPrinting();
        builder.registerTypeHierarchyAdapter(GuidebookElement.class, new GuidebookElement.JsonHandler());
        return builder;
    }
}
