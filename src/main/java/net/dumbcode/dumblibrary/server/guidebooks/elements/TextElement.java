package net.dumbcode.dumblibrary.server.guidebooks.elements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.dumblibrary.server.guidebooks.Guidebook;
import net.dumbcode.dumblibrary.server.utils.DumbStringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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
    private final DumbStringUtils.TextAlignment alignment;

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
            color = resolveColor(source.get("color"));
        else
            color = 0xFF000000;
        if(source.has("scale"))
            scale = source.get("scale").getAsDouble();
        else
            scale = 1.0;
        if(source.has("alignment")) {
            alignment = DumbStringUtils.TextAlignment.valueOf(source.get("alignment").getAsString().toUpperCase());
        } else {
            alignment = DumbStringUtils.TextAlignment.LEFT;
        }
    }

    private int resolveColor(JsonElement colorElement) {
        try {
            EnumDyeColor color = EnumDyeColor.valueOf(colorElement.getAsString().toUpperCase());
            return color.getColorValue();
        } catch (IllegalArgumentException e) {
            // simply not a valid color string
        }
        return colorElement.getAsInt();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getWidth(Guidebook guidebook) {
        if(alignment == DumbStringUtils.TextAlignment.CENTERED)
            return guidebook.getAvailableWidth();
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

    @Override
    public int getLeftOffset(Guidebook guidebook) {
        int w = getWidth(guidebook);
        switch (alignment) {
            case CENTERED:
                return guidebook.getAvailableWidth()/2-w/2;
            case RIGHT:
                return guidebook.getAvailableWidth()-w;
            default:
                return 0;
        }
    }

    @Override
    public int getTopOffset(Guidebook guidebook) {
        return 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(Guidebook guidebook) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        List<String> lines = fontRenderer.listFormattedStringToWidth(contents, (int) (guidebook.getAvailableWidth()/scale));
        float y = 0f;
        for(String line : lines) {
            GlStateManager.pushMatrix();
            float x;
            switch (alignment) {
                case LEFT:
                    x = 0f;
                    break;
                case RIGHT:
                    x = (float) (guidebook.getAvailableWidth() - fontRenderer.getStringWidth(line) * scale);
                    break;
                case CENTERED:
                    x = (float) (guidebook.getAvailableWidth()/2 - (fontRenderer.getStringWidth(line)  * scale)/2);
                    break;
                default:
                    x = 0f;
                    break;
            }
            GlStateManager.translate(x, y, 0f);
            y += (fontRenderer.FONT_HEIGHT + LINE_SPACING) * scale;
            GlStateManager.scale(scale, scale, 1f);
            fontRenderer.drawString(line, 0, 0, color);
            GlStateManager.popMatrix();
        }
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
