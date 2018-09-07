package net.dumbcode.dumblibrary.server.guidebooks.elements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.dumblibrary.server.guidebooks.Guidebook;

public class GapElement extends GuidebookElement {
    private final int size;

    public GapElement(JsonObject source, JsonDeserializationContext context) {
        super(source, context);
        size = source.get("size").getAsInt();
    }

    @Override
    public int getWidth(Guidebook guidebook) {
        return guidebook.getAvailableWidth();
    }

    @Override
    public int getHeight(Guidebook guidebook) {
        return size;
    }

    @Override
    public void render(Guidebook guidebook) {

    }

    @Override
    public String getElementType() {
        return "gap";
    }

    @Override
    public void writeToJSON(JsonObject destination, JsonSerializationContext context) {
        destination.addProperty("size", size);
    }

    @Override
    public int getLeftOffset(Guidebook guidebook) {
        return 0;
    }

    @Override
    public int getTopOffset(Guidebook guidebook) {
        return 0;
    }
}
