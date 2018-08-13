package net.dumbcode.dumblibrary.server.guidebooks.elements;

import com.google.gson.*;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.guidebooks.Guidebook;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public abstract class GuidebookElement {

    @Nullable
    @Getter
    private TextComponentBase tooltipText;

    @SideOnly(Side.CLIENT)
    public abstract int getWidth(Guidebook guidebook);
    @SideOnly(Side.CLIENT)
    public abstract int getHeight(Guidebook guidebook);
    @SideOnly(Side.CLIENT)
    public abstract void render(Guidebook guidebook);


    public abstract EnumGuidebookElement getElementType();
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
        }
    }

    public static enum EnumGuidebookElement {
        IMAGE, TEXT, ITEM;

        public String getID() {
            return name().toLowerCase();
        }

        public static EnumGuidebookElement fromID(String id) {
            return EnumGuidebookElement.valueOf(id.toUpperCase());
        }
    }

    public static class JsonHandler implements JsonSerializer<GuidebookElement>, JsonDeserializer<GuidebookElement> {

        @Override
        public GuidebookElement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            String type;
            if(object.has("type"))
                type = object.get("type").getAsString();
            else
                type = EnumGuidebookElement.TEXT.getID();
            EnumGuidebookElement elementType = EnumGuidebookElement.fromID(type);
            switch (elementType) {
                case TEXT:
                    return new TextElement(object, context);
                case IMAGE:
                    return new ImageElement(object, context);
                case ITEM:
                    return new ItemElement(object, context);
            }
            return null;
        }

        @Override
        public JsonElement serialize(GuidebookElement element, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.addProperty("type", element.getElementType().getID());
            return result;
        }
    }
}
