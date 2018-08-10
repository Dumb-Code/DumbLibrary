package net.dumbcode.dumblibrary.server.guidebooks.elements;

import com.google.gson.*;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.guidebooks.Guidebook;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Type;

public abstract class GuidebookElement {

    @SideOnly(Side.CLIENT)
    public abstract int getWidth(Guidebook guidebook);
    @SideOnly(Side.CLIENT)
    public abstract int getHeight(Guidebook guidebook);
    @SideOnly(Side.CLIENT)
    public abstract void render(Guidebook guidebook);


    public abstract EnumGuidebookElement getElementType();
    public abstract void writeToJSON(JsonObject destination, JsonSerializationContext context);

    public GuidebookElement(JsonObject source, JsonDeserializationContext context) {}

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
