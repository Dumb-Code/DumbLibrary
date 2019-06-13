package net.dumbcode.dumblibrary.server.guidebooks.elements;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.guidebooks.Guidebook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class MissingPageElement extends GuidebookElement {

    public static final ResourceLocation MISSING_PAGE_TEXTURE = new ResourceLocation(DumbLibrary.MODID, "textures/guidebook/missing_page.png");

    public MissingPageElement() {
        super(null, null);
    }

    @Override
    public int getWidth(Guidebook guidebook) {
        return guidebook.getPageWidth();
    }

    @Override
    public int getHeight(Guidebook guidebook) {
        return guidebook.getAvailableHeight();
    }

    @Override
    public void render(Guidebook guidebook) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(MISSING_PAGE_TEXTURE);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        int w = getWidth(guidebook);
        int h = getHeight(guidebook);
        int x = -guidebook.getPageMargins();
        buffer.pos(x, h, 0).tex(0, 1).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x + w, h, 0).tex(1, 1).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x + w, 0, 0).tex(1, 0).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x, 0, 0).tex(0, 0).color(1f, 1f, 1f, 1f).endVertex();
        tessellator.draw();
    }

    @Override
    public String getElementType() {
        return "missing";
    }

    @Override
    public void writeToJSON(JsonObject destination, JsonSerializationContext context) {

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
