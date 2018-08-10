package net.dumbcode.dumblibrary.server.guidebooks;

import com.google.gson.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.lang.reflect.Type;

@Data
public class Guidebook extends IForgeRegistryEntry.Impl<Guidebook> {

    @Getter
    private final ModContainer modContainer;

    @Getter
    @Setter
    private String titleKey;

    public Guidebook() {
        modContainer = Loader.instance().activeModContainer();
    }

}
