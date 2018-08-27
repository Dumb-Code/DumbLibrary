package net.dumbcode.dumblibrary.server.guidebooks.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.dumbcode.dumblibrary.client.gui.GuiGuidebook;
import net.dumbcode.dumblibrary.server.guidebooks.elements.GuidebookElement;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

@FunctionalInterface
public interface GuidebookFunction {
    HashMap<ResourceLocation, Factory> FUNCTION_FACTORIES = new HashMap<>();
    Factory MISSING_FACTORY = (element, functionObject, context) -> (guiGuidebook, localPageX, localPageY, mouseX, mouseY) -> {};

    void onClick(GuiGuidebook guiGuidebook, int localPageX, int localPageY, int mouseX, int mouseY);

    interface Factory {
        GuidebookFunction create(GuidebookElement element, JsonObject functionObject, JsonDeserializationContext context);
    }
}
