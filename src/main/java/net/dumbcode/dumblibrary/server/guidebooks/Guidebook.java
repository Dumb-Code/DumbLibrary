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

    @Getter
    @Setter
    private GuidebookPage cover;

    @Getter
    @Setter
    private int pageWidth;

    @Getter
    @Setter
    private int pageMargins;

    public Guidebook() {
        modContainer = Loader.instance().activeModContainer();
    }

    public int getAvailableWidth() {
        return pageWidth-pageMargins*2;
    }

    public float getAspectRatio() {
        return 5f/8f; // corresponds to the aspect ratio of page in the book model from vanilla
    }

    public int getAvailableHeight() {
        return (int) (getAvailableWidth() / getAspectRatio());
    }
}
