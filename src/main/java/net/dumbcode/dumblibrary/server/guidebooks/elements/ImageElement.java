package net.dumbcode.dumblibrary.server.guidebooks.elements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.dumblibrary.server.guidebooks.Guidebook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ImageElement extends GuidebookElement {

    private ResourceLocation texture;
    private int textureWidth = -1;
    private int textureHeight = -1;
    private double scale;

    public ImageElement(JsonObject source, JsonDeserializationContext context) {
        super(source, context);
        texture = new ResourceLocation(source.get("location").getAsString());
        JsonElement scaling = source.get("scale");
        if(scaling.getAsString().equalsIgnoreCase("fit")) {
            scale = -1;
        } else {
            scale = scaling.getAsDouble();
        }
    }

    private void readTextureSizeIfNeeded() {
        if(textureWidth == -1 || textureHeight == -1) {
            int previousTexture = GlStateManager.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(texture);
            GlStateManager.bindTexture(textureObj.getGlTextureId());
            textureWidth = GlStateManager.glGetInteger(GL11.GL_TEXTURE_WIDTH);
            textureHeight = GlStateManager.glGetInteger(GL11.GL_TEXTURE_WIDTH);
            GlStateManager.bindTexture(previousTexture);
        }
    }

    private double correctScale(Guidebook guidebook) {
        if(scale < 0) { // fit to page
            // attempt to use max width
            double attemptedScaling = guidebook.getAvailableWidth()/(double)textureWidth;

            double potentialHeight = guidebook.getAvailableWidth()*attemptedScaling;

            if(potentialHeight > guidebook.getAvailableHeight()) { // use max height
                return guidebook.getAvailableHeight()/textureHeight;
            }
            return (int) (attemptedScaling*textureWidth);
        }
        return scale;
    }

    @Override
    public int getWidth(Guidebook guidebook) {
        readTextureSizeIfNeeded();
        return (int) (textureWidth*correctScale(guidebook));
    }

    @Override
    public int getHeight(Guidebook guidebook) {
        readTextureSizeIfNeeded();
        return (int) (textureHeight*correctScale(guidebook));
    }

    @Override
    public void render(Guidebook guidebook) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        int w = getWidth(guidebook);
        int h = getHeight(guidebook);
        int x = guidebook.getAvailableWidth()/2-w/2;
        buffer.pos(x, h, 0).tex(0, 1).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x+w, h, 0).tex(1, 1).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x+w, 0, 0).tex(1, 0).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x, 0, 0).tex(0, 0).color(1f, 1f, 1f, 1f).endVertex();
        tessellator.draw();
    }

    @Override
    public EnumGuidebookElement getElementType() {
        return EnumGuidebookElement.IMAGE;
    }

    @Override
    public void writeToJSON(JsonObject destination, JsonSerializationContext context) {
        destination.addProperty("location", texture.toString());
        if(scale < 0)
            destination.addProperty("scale", "fit");
        else
            destination.addProperty("scale", scale);
    }
}
