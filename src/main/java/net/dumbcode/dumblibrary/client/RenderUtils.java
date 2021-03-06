package net.dumbcode.dumblibrary.client;

import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLLog;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class RenderUtils {
    public static void setupPointers(VertexFormat format) {
        int stride = format.getSize();
        int offset = 0;
        for (VertexFormatElement element : format.getElements()) {
            switch (element.getUsage()) {
                case POSITION:
                    GlStateManager.glVertexPointer(element.getElementCount(), element.getType().getGlConstant(), stride, offset);
                    GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    break;
                case NORMAL:
                    GL11.glNormalPointer(element.getType().getGlConstant(), stride, offset);
                    GlStateManager.glEnableClientState(GL11.GL_NORMAL_ARRAY);
                    break;
                case COLOR:
                    GlStateManager.glColorPointer(element.getElementCount(), element.getType().getGlConstant(), stride, offset);
                    GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
                    break;
                case UV:
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + element.getIndex());
                    GlStateManager.glTexCoordPointer(element.getElementCount(), element.getType().getGlConstant(), stride, offset);
                    GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                case PADDING:
                    break;
                default:
                    FMLLog.log.fatal("Unimplemented vanilla attribute upload: {}", element.getUsage().getDisplayName());
            }
            offset += element.getSize();
        }
    }

    public static void disableStates(VertexFormat format) {
        for (VertexFormatElement element : format.getElements()) {
            switch (element.getUsage()) {
                case POSITION:
                    GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                    break;
                case NORMAL:
                    GlStateManager.glDisableClientState(GL11.GL_NORMAL_ARRAY);
                    break;
                case COLOR:
                    GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
                    break;
                case UV:
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + element.getIndex());
                    GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                case PADDING:
                    break;
                default:
                    FMLLog.log.fatal("Unimplemented vanilla attribute upload: {}", element.getUsage().getDisplayName());
            }
        }
    }

    public static void drawCubeoid(Vec3d s, Vec3d e) {
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_NORMAL);
        buff.pos(s.x, e.y, s.z).normal(0, 1, 0).endVertex();
        buff.pos(s.x, e.y, e.z).normal(0, 1, 0).endVertex();
        buff.pos(e.x, e.y, e.z).normal(0, 1, 0).endVertex();
        buff.pos(e.x, e.y, s.z).normal(0, 1, 0).endVertex();
        buff.pos(s.x, s.y, e.z).normal(0, -1, 0).endVertex();
        buff.pos(s.x, s.y, s.z).normal(0, -1, 0).endVertex();
        buff.pos(e.x, s.y, s.z).normal(0, -1, 0).endVertex();
        buff.pos(e.x, s.y, e.z).normal(0, -1, 0).endVertex();
        buff.pos(e.x, e.y, e.z).normal(1, 0, 0).endVertex();
        buff.pos(e.x, s.y, e.z).normal(1, 0, 0).endVertex();
        buff.pos(e.x, s.y, s.z).normal(1, 0, 0).endVertex();
        buff.pos(e.x, e.y, s.z).normal(1, 0, 0).endVertex();
        buff.pos(s.x, s.y, e.z).normal(-1, 0, 0).endVertex();
        buff.pos(s.x, e.y, e.z).normal(-1, 0, 0).endVertex();
        buff.pos(s.x, e.y, s.z).normal(-1, 0, 0).endVertex();
        buff.pos(s.x, s.y, s.z).normal(-1, 0, 0).endVertex();
        buff.pos(s.x, e.y, e.z).normal(0, 0, 1).endVertex();
        buff.pos(s.x, s.y, e.z).normal(0, 0, 1).endVertex();
        buff.pos(e.x, s.y, e.z).normal(0, 0, 1).endVertex();
        buff.pos(e.x, e.y, e.z).normal(0, 0, 1).endVertex();
        buff.pos(s.x, s.y, s.z).normal(0, 0, -1).endVertex();
        buff.pos(s.x, e.y, s.z).normal(0, 0, -1).endVertex();
        buff.pos(e.x, e.y, s.z).normal(0, 0, -1).endVertex();
        buff.pos(e.x, s.y, s.z).normal(0, 0, -1).endVertex();
        Tessellator.getInstance().draw();
    }

    public static void drawSpacedCube(double ulfx, double ulfy, double ulfz, double ulbx, double ulby, double ulbz, double urfx, double urfy, double urfz, double urbx, double urby, double urbz, double dlfx, double dlfy, double dlfz, double dlbx, double dlby, double dlbz, double drfx, double drfy, double drfz, double drbx, double drby, double drbz, double uu, double uv, double du, double dv, double lu, double lv, double ru, double rv, double fu, double fv,double bu, double bv, double tw,double th, double td) {
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
        drawSpacedCube(buff, ulfx, ulfy, ulfz, ulbx, ulby, ulbz, urfx, urfy, urfz, urbx, urby, urbz, dlfx, dlfy, dlfz, dlbx, dlby, dlbz, drfx, drfy, drfz, drbx, drby, drbz, uu, uv, du, dv, lu, lv, ru, rv, fu, fv, bu, bv, tw, th, td);
        Tessellator.getInstance().draw();
    }

    //ulf, ulb, urf, urb, dlf, dlb, drf, drb
    public static void drawSpacedCube(BufferBuilder buff, double ulfx, double ulfy, double ulfz, double ulbx, double ulby, double ulbz, double urfx, double urfy, double urfz, double urbx, double urby, double urbz, double dlfx, double dlfy, double dlfz, double dlbx, double dlby, double dlbz, double drfx, double drfy, double drfz, double drbx, double drby, double drbz, double uu, double uv, double du, double dv, double lu, double lv, double ru, double rv, double fu, double fv,double bu, double bv, double tw,double th, double td) {
        Vector3f xNorm = MathUtils.calculateNormalF(urfx, urfy, urfz, drfx, drfy, drfz, dlfx, dlfy, dlfz);
        Vector3f yNorm = MathUtils.calculateNormalF(ulfx, ulfy, ulfz, ulbx, ulby, ulbz, urbx, urby, urbz);
        Vector3f zNorm = MathUtils.calculateNormalF(drfx, drfy, drfz, urfx, urfy, urfz, urbx, urby, urbz);

        buff.pos(urfx, urfy, urfz).tex(fu, fv).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
        buff.pos(drfx, drfy, drfz).tex(fu, fv+th).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
        buff.pos(dlfx, dlfy, dlfz).tex(fu+td, fv+th).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
        buff.pos(ulfx, ulfy, ulfz).tex(fu+td, fv).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
        buff.pos(drbx, drby, drbz).tex(bu, bv).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
        buff.pos(urbx, urby, urbz).tex(bu, bv+th).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
        buff.pos(ulbx, ulby, ulbz).tex(bu+td, bv+th).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
        buff.pos(dlbx, dlby, dlbz).tex(bu+td, bv).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
        buff.pos(ulfx, ulfy, ulfz).tex(uu, uv).normal(yNorm.x, yNorm.y, yNorm.z).endVertex();
        buff.pos(ulbx, ulby, ulbz).tex(uu, uv+tw).normal(yNorm.x, yNorm.y, yNorm.z).endVertex();
        buff.pos(urbx, urby, urbz).tex(uu+td, uv+tw).normal(yNorm.x, yNorm.y, yNorm.z).endVertex();
        buff.pos(urfx, urfy, urfz).tex(uu+td, uv).normal(yNorm.x, yNorm.y, yNorm.z).endVertex();
        buff.pos(dlbx, dlby, dlbz).tex(du, dv).normal(-yNorm.x, -yNorm.y, -yNorm.z).endVertex();
        buff.pos(dlfx, dlfy, dlfz).tex(du, dv+tw).normal(-yNorm.x, -yNorm.y, -yNorm.z).endVertex();
        buff.pos(drfx, drfy, drfz).tex(du+td, dv+tw).normal(-yNorm.x, -yNorm.y, -yNorm.z).endVertex();
        buff.pos(drbx, drby, drbz).tex(du+td, dv).normal(-yNorm.x, -yNorm.y, -yNorm.z).endVertex();
        buff.pos(drfx, drfy, drfz).tex(ru, rv).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
        buff.pos(urfx, urfy, urfz).tex(ru+th, rv).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
        buff.pos(urbx, urby, urbz).tex(ru+th, rv+tw).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
        buff.pos(drbx, drby, drbz).tex(ru, rv+tw).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
        buff.pos(ulfx, ulfy, ulfz).tex(lu, lv).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
        buff.pos(dlfx, dlfy, dlfz).tex(lu+th, lv).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
        buff.pos(dlbx, dlby, dlbz).tex(lu+th, lv+tw).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
        buff.pos(ulbx, ulby, ulbz).tex(lu, lv+tw).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
    }

    public static void renderBoxLines(Vector3d[] points, EnumFacing... blocked) { //todo: color params
        render(points, blocked, 0b100, 0b101, 0b111, 0b110);
        render(points, blocked, 0b000, 0b001, 0b011, 0b010);
        render(points, blocked, 0b011, 0b111);
        render(points, blocked, 0b110, 0b010);
        render(points, blocked, 0b001, 0b101);
        render(points, blocked, 0b100, 0b000);
    }

    public static void render(Vector3d[] points, EnumFacing[] blocked, int... ints) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buff = tessellator.getBuffer();
        buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        over:
        for (int i = 0; i < ints.length; i++) {
            int nextID = (i + 1) % ints.length;
            if(ints.length == 2 && i == 1) {
                break;
            }
            Vector3d vec = points[ints[i]];
            Vector3d next = points[ints[nextID]];
            for (EnumFacing face : blocked) {
                int bit = face.getAxis().ordinal();
                int shifted = (ints[i]>>bit)&1;
                if(shifted == ((ints[nextID]>>bit)&1) && shifted == face.getAxisDirection().ordinal()) {
                    continue over;
                }
            }
            buff.pos(vec.x, vec.y, vec.z).color(0f, 0f, 0f,0.4f).endVertex();
            buff.pos(next.x, next.y, next.z).color(0f, 0f, 0f,0.4f).endVertex();

        }
        tessellator.draw();
    }

    //Coped from GuiSCreen
    public static void draw256Texture(double x, double y, double textureX, double textureY, double width, double height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((x), (y + height), 0).tex((textureX * 0.00390625F), ((textureY + height) * 0.00390625F)).endVertex();
        bufferbuilder.pos((x + width), (y + height), 0).tex(((textureX + width) * 0.00390625F), ((textureY + height) * 0.00390625F)).endVertex();
        bufferbuilder.pos((x + width), (y), 0).tex(((textureX + width) * 0.00390625F), (textureY * 0.00390625F)).endVertex();
        bufferbuilder.pos((x), (y), 0).tex((textureX * 0.00390625F), (textureY * 0.00390625F)).endVertex();
        tessellator.draw();
    }

    public static void drawTextureAtlasSprite(double x, double y, TextureAtlasSprite sprite, double width, double height) {
        drawTextureAtlasSprite(x, y, sprite, width, height, 0F, 0F, 16F, 16F);
    }

    public static void drawTextureAtlasSprite(double x, double y, TextureAtlasSprite sprite, double width, double height, double minU, double minV, double maxU, double maxV) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0).tex(sprite.getInterpolatedU(minU), sprite.getInterpolatedV(maxV)).endVertex();
        bufferbuilder.pos(x + width, y + height, 0).tex(sprite.getInterpolatedU(maxU), sprite.getInterpolatedV(maxV)).endVertex();
        bufferbuilder.pos(x + width, y, 0).tex(sprite.getInterpolatedU(maxU), sprite.getInterpolatedV(minV)).endVertex();
        bufferbuilder.pos(x, y, 0).tex(sprite.getInterpolatedU(minU), sprite.getInterpolatedV(minV)).endVertex();
        tessellator.draw();
    }

    public static void renderBorderExclusive(int left, int top, int right, int bottom, int borderSize, int borderColor) {
        renderBorder(left - borderSize, top - borderSize, right + borderSize, bottom + borderSize, borderSize, borderColor);
    }

    public static void renderBorder(int left, int top, int right, int bottom, int borderSize, int borderColor) {
        Gui.drawRect(left, top, right, top + borderSize, borderColor);
        Gui.drawRect(left, bottom, right, bottom - borderSize, borderColor);
        Gui.drawRect(left, top, left + borderSize, bottom, borderColor);
        Gui.drawRect(right, top, right - borderSize, bottom, borderColor);
    }

    public static void drawTexturedQuad(float left, float top, float right, float bottom, float minU, float minV, float maxU, float maxV, float zLevel) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        buffer.pos(left, top, zLevel).tex(minU, minV).endVertex();
        buffer.pos(left, bottom, zLevel).tex(minU, maxV).endVertex();
        buffer.pos(right, bottom, zLevel).tex(maxU, maxV).endVertex();
        buffer.pos(right, top, zLevel).tex(maxU, minV).endVertex();

        Tessellator.getInstance().draw();
    }
}
