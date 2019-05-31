package net.dumbcode.dumblibrary.server.guidebooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.guidebooks.elements.*;
import net.dumbcode.dumblibrary.server.guidebooks.functions.GuidebookFunction;
import net.dumbcode.dumblibrary.server.guidebooks.functions.OpenPageFunction;
import net.dumbcode.dumblibrary.server.guidebooks.functions.RunCommandFunction;
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
            Loader.instance().setActiveModContainer(Loader.instance().getIndexedModList().get(book.getRegistryName().getNamespace()));
            String id = "guidebook_"+book.getRegistryName().getPath();
            registry.register(new GuidebookItem(book).setTranslationKey(id).setCreativeTab(CreativeTabs.MISC).setRegistryName(new ResourceLocation(book.getRegistryName().getNamespace(), id)));

            book.compilePages();
        });
        Loader.instance().setActiveModContainer(Loader.instance().getIndexedModList().get(DumbLibrary.MODID));
    }

    public static GsonBuilder prepareGsonBuilderForGuidebooks(GsonBuilder builder) {
        builder.setPrettyPrinting();
        builder.registerTypeHierarchyAdapter(GuidebookElement.class, new GuidebookElement.JsonHandler());
        return builder;
    }

    public static void createGuidebookFactories() {
        GuidebookElement.ELEMENT_FACTORIES.put(new ResourceLocation(DumbLibrary.MODID, "text"), TextElement::new);
        GuidebookElement.ELEMENT_FACTORIES.put(new ResourceLocation(DumbLibrary.MODID, "image"), ImageElement::new);
        GuidebookElement.ELEMENT_FACTORIES.put(new ResourceLocation(DumbLibrary.MODID, "item"), ItemElement::new);
        GuidebookElement.ELEMENT_FACTORIES.put(new ResourceLocation(DumbLibrary.MODID, "recipe"), RecipeElement::new);
        GuidebookElement.ELEMENT_FACTORIES.put(new ResourceLocation(DumbLibrary.MODID, "gap"), GapElement::new);

        GuidebookFunction.FUNCTION_FACTORIES.put(new ResourceLocation(DumbLibrary.MODID, "open_page"), (element, functionObject, context) -> new OpenPageFunction(functionObject.get("page").getAsString()));
        GuidebookFunction.FUNCTION_FACTORIES.put(new ResourceLocation(DumbLibrary.MODID, "run_command"), (element, functionObject, context) -> new RunCommandFunction(functionObject.get("command").getAsString()));
    }
}
