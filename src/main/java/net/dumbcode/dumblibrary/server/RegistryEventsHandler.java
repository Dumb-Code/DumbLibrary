package net.dumbcode.dumblibrary.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.guidebooks.Guidebook;
import net.dumbcode.dumblibrary.server.guidebooks.GuidebooksManager;
import net.dumbcode.dumblibrary.server.json.JsonUtil;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
@UtilityClass
public class RegistryEventsHandler {
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        GuidebooksManager.registerBooks(event.getRegistry());
    }

    @SubscribeEvent
    public static void createRegistries(RegistryEvent.NewRegistry event) {
        GuidebooksManager.GUIDEBOOK_REGISTRY = new RegistryBuilder<Guidebook>()
                .setName(new ResourceLocation(DumbLibrary.MODID, "guidebooks"))
                .setType(Guidebook.class)
                .setDefaultKey(new ResourceLocation(DumbLibrary.MODID, "missing"))
                .set((key, isNetwork) -> Guidebook.MISSING)
                .setMaxID(500)
                .create();
    }
}
