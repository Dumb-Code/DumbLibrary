package net.dumbcode.dumblibrary.server.guidebooks.elements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.dumblibrary.server.guidebooks.Guidebook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class TextElement extends GuidebookElement {

    public final int LINE_SPACING = 3;

    private final String contents;
    private final boolean translatable;
    private final String rawText;
    private final int color;
    private final double scale;

    public TextElement(JsonObject source, JsonDeserializationContext context) {
        super(source, context);
        String text = source.get("text").getAsString();
        this.rawText = text;
        if(source.has("translation_marker") && source.get("translation_marker").getAsBoolean()) {
            translatable = true;
            contents = new TextComponentTranslation(text).getFormattedText();
        } else {
            translatable = false;
            contents = text;
        }
        if(source.has("color"))
            color = source.get("color").getAsInt();
        else
            color = 0xFFF0F0F0;
        if(source.has("scale"))
            scale = source.get("scale").getAsDouble();
        else
            scale = 1.0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getWidth(Guidebook guidebook) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        List<String> lines = fontRenderer.listFormattedStringToWidth(contents, (int) (guidebook.getAvailableWidth()/scale));
        return (int) (lines.stream().mapToInt(fontRenderer::getStringWidth).max().getAsInt()*scale);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getHeight(Guidebook guidebook) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        List<String> lines = fontRenderer.listFormattedStringToWidth(contents, (int) (guidebook.getAvailableWidth()/scale));
        return (int) ((lines.size() * (fontRenderer.FONT_HEIGHT) + (lines.size()-1) * LINE_SPACING) * scale);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(Guidebook guidebook) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        GlStateManager.scale(scale, scale, 1f);
        fontRenderer.drawSplitString(contents, 0, 0, (int) (guidebook.getAvailableWidth()/scale), color);
    }

    @Override
    public EnumGuidebookElement getElementType() {
        return EnumGuidebookElement.TEXT;
    }

    @Override
    public void writeToJSON(JsonObject destination, JsonSerializationContext context) {
        destination.addProperty("translation_marker", translatable);
        destination.addProperty("text", rawText);
    }
}
