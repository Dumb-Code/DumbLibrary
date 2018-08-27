package net.dumbcode.dumblibrary.server.guidebooks.elements;

import com.google.gson.*;
import lombok.Getter;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.guidebooks.Guidebook;
import net.dumbcode.dumblibrary.server.guidebooks.functions.GuidebookFunction;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;

public abstract class GuidebookElement {

    public static final HashMap<ResourceLocation, Factory> ELEMENT_FACTORIES = new HashMap<>();
    public static final Factory MISSING_FACTORY = ((object, context) -> new MissingPageElement());

    @Nullable
    @Getter
    private GuidebookFunction onClickFunction;

    @Nullable
    @Getter
    private TextComponentBase tooltipText;

    public GuidebookElement(JsonObject source, JsonDeserializationContext context) {
        if(source != null) {
            if(source.has("tooltip")) {
                if(source.get("tooltip").isJsonObject()) {
                    JsonObject tooltipObject = source.getAsJsonObject("tooltip");
                    String text = tooltipObject.get("text").getAsString();
                    if(tooltipObject.has("translation_marker") && tooltipObject.get("translation_marker").getAsBoolean()) {
                        tooltipText = new TextComponentTranslation(text);
                    } else {
                        tooltipText = new TextComponentString(text);
                    }
                } else {
                    tooltipText = new TextComponentString(source.get("tooltip").getAsString());
                }
            }

            if(source.has("onclick")) {
                JsonObject onClickObject = source.getAsJsonObject("onclick");
                String functionID = onClickObject.get("function").getAsString();
                ResourceLocation functionLocation;
                if(functionID.contains(":")) {
                    functionLocation = new ResourceLocation(functionID);
                } else {
                    functionLocation = new ResourceLocation(DumbLibrary.MODID, functionID);
                }
                onClickFunction = GuidebookFunction.FUNCTION_FACTORIES.getOrDefault(functionLocation, GuidebookFunction.MISSING_FACTORY).create(this, onClickObject, context);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public abstract int getWidth(Guidebook guidebook);
    @SideOnly(Side.CLIENT)
    public abstract int getHeight(Guidebook guidebook);

    @SideOnly(Side.CLIENT)
    public abstract void render(Guidebook guidebook);

    public abstract String getElementType();

    public abstract void writeToJSON(JsonObject destination, JsonSerializationContext context);
    /**
     * Returns the offset on the left relative to the page edge (used to know if the mouse is on the element)
     * @param guidebook the guidebook in which the element is
     * @return the offset on the left relative to the page edge
     */
    public abstract int getLeftOffset(Guidebook guidebook);

    /**
     * Returns the offset above relative to the page edge (used to know if the mouse is on the element)
     * @param guidebook the guidebook in which the element is
     * @return the offset above relative to the page edge
     */
    public abstract int getTopOffset(Guidebook guidebook);

    public boolean isMouseOn(Guidebook guidebook, int elementX, int elementY, int pageMouseX, int pageMouseY) {
        return pageMouseX-elementX >= getLeftOffset(guidebook) && pageMouseX-elementX < getLeftOffset(guidebook)+getWidth(guidebook)
        && pageMouseY-elementY >= getTopOffset(guidebook) && pageMouseY-elementY < getTopOffset(guidebook)+getHeight(guidebook);
    }

    @FunctionalInterface
    public interface Factory {
        GuidebookElement create(JsonObject object, JsonDeserializationContext context);
    }

    public static class JsonHandler implements JsonSerializer<GuidebookElement>, JsonDeserializer<GuidebookElement> {

        @Override
        public GuidebookElement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            String type;
            if(object.has("type"))
                type = object.get("type").getAsString();
            else
                type = "text";
            if(type.contains(":"))
                return GuidebookElement.ELEMENT_FACTORIES.getOrDefault(new ResourceLocation(type), MISSING_FACTORY).create(object, context);
            return GuidebookElement.ELEMENT_FACTORIES.getOrDefault(new ResourceLocation(DumbLibrary.MODID, type), MISSING_FACTORY).create(object, context);
        }

        @Override
        public JsonElement serialize(GuidebookElement element, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.addProperty("type", element.getElementType());
            return result;
        }
    }
}
