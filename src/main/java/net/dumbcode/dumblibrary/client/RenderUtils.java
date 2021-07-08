package net.dumbcode.dumblibrary.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.Arrays;

public class RenderUtils {
    public static void drawCubeoid(MatrixStack stack, Vector3d si, Vector3d ei, IVertexBuilder buff) {
        Matrix4f pose = stack.last().pose();
        Matrix3f normal = stack.last().normal();

        Vector3f s = new Vector3f(si);
        Vector3f e = new Vector3f(ei);

//        buff.(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buff.vertex(pose, s.x(), e.y(), s.z()).normal(normal, 0, 1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), e.y(), e.z()).normal(normal, 0, 1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), e.y(), e.z()).normal(normal, 0, 1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), e.y(), s.z()).normal(normal, 0, 1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), s.y(), e.z()).normal(normal, 0, -1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), s.y(), s.z()).normal(normal, 0, -1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), s.y(), s.z()).normal(normal, 0, -1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), s.y(), e.z()).normal(normal, 0, -1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), e.y(), e.z()).normal(normal, 1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), s.y(), e.z()).normal(normal, 1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), s.y(), s.z()).normal(normal, 1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), e.y(), s.z()).normal(normal, 1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), s.y(), e.z()).normal(normal, -1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), e.y(), e.z()).normal(normal, -1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), e.y(), s.z()).normal(normal, -1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), s.y(), s.z()).normal(normal, -1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), e.y(), e.z()).normal(normal, 0, 0, 1).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), s.y(), e.z()).normal(normal, 0, 0, 1).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), s.y(), e.z()).normal(normal, 0, 0, 1).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), e.y(), e.z()).normal(normal, 0, 0, 1).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), s.y(), s.z()).normal(normal, 0, 0, -1).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), e.y(), s.z()).normal(normal, 0, 0, -1).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), e.y(), s.z()).normal(normal, 0, 0, -1).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), s.y(), s.z()).normal(normal, 0, 0, -1).color(1F, 1F, 1F, 1F).endVertex();
    }

//    public static void drawSpacedCube(double ulfx, double ulfy, double ulfz, double ulbx, double ulby, double ulbz, double urfx, double urfy, double urfz, double urbx, double urby, double urbz, double dlfx, double dlfy, double dlfz, double dlbx, double dlby, double dlbz, double drfx, double drfy, double drfz, double drbx, double drby, double drbz, double uu, double uv, double du, double dv, double lu, double lv, double ru, double rv, double fu, double fv,double bu, double bv, double tw,double th, double td, IVertexBuilder buff) {
//        drawSpacedCube(buff, ulfx, ulfy, ulfz, ulbx, ulby, ulbz, urfx, urfy, urfz, urbx, urby, urbz, dlfx, dlfy, dlfz, dlbx, dlby, dlbz, drfx, drfy, drfz, drbx, drby, drbz, uu, uv, du, dv, lu, lv, ru, rv, fu, fv, bu, bv, tw, th, td);
//    }

    //ulf, ulb, urf, urb, dlf, dlb, drf, drb
    public static void drawSpacedCube(MatrixStack stack, IVertexBuilder buff, float r, float g, float b, float a, int light, int overlay, float ulfx, float ulfy, float ulfz, float ulbx, float ulby, float ulbz, float urfx, float urfy, float urfz, float urbx, float urby, float urbz, float dlfx, float dlfy, float dlfz, float dlbx, float dlby, float dlbz, float drfx, float drfy, float drfz, float drbx, float drby, float drbz, float uu, float uv, float du, float dv, float lu, float lv, float ru, float rv, float fu, float fv,float bu, float bv, float tw,float th, float td) {
        Vector3f xNorm = MathUtils.calculateNormalF(urfx, urfy, urfz, drfx, drfy, drfz, dlfx, dlfy, dlfz);
        Vector3f yNorm = MathUtils.calculateNormalF(ulfx, ulfy, ulfz, ulbx, ulby, ulbz, urbx, urby, urbz);
        Vector3f zNorm = MathUtils.calculateNormalF(drfx, drfy, drfz, urfx, urfy, urfz, urbx, urby, urbz);

        Matrix4f pose = stack.last().pose();
        Matrix3f normal = stack.last().normal();

        buff.vertex(pose, urfx, urfy, urfz).color(r, g, b, a).uv(fu, fv).overlayCoords(overlay).uv2(light).normal(normal, xNorm.x(), xNorm.y(), xNorm.z()).endVertex();
        buff.vertex(pose, drfx, drfy, drfz).color(r, g, b, a).uv(fu, fv+th).overlayCoords(overlay).uv2(light).normal(normal, xNorm.x(), xNorm.y(), xNorm.z()).endVertex();
        buff.vertex(pose, dlfx, dlfy, dlfz).color(r, g, b, a).uv(fu+td, fv+th).overlayCoords(overlay).uv2(light).normal(normal, xNorm.x(), xNorm.y(), xNorm.z()).endVertex();
        buff.vertex(pose, ulfx, ulfy, ulfz).color(r, g, b, a).uv(fu+td, fv).overlayCoords(overlay).uv2(light).normal(normal, xNorm.x(), xNorm.y(), xNorm.z()).endVertex();
        buff.vertex(pose, drbx, drby, drbz).color(r, g, b, a).uv(bu, bv).overlayCoords(overlay).uv2(light).normal(normal, -xNorm.x(), -xNorm.y(), -xNorm.z()).endVertex();
        buff.vertex(pose, urbx, urby, urbz).color(r, g, b, a).uv(bu, bv+th).overlayCoords(overlay).uv2(light).normal(normal, -xNorm.x(), -xNorm.y(), -xNorm.z()).endVertex();
        buff.vertex(pose, ulbx, ulby, ulbz).color(r, g, b, a).uv(bu+td, bv+th).overlayCoords(overlay).uv2(light).normal(normal, -xNorm.x(), -xNorm.y(), -xNorm.z()).endVertex();
        buff.vertex(pose, dlbx, dlby, dlbz).color(r, g, b, a).uv(bu+td, bv).overlayCoords(overlay).uv2(light).normal(normal, -xNorm.x(), -xNorm.y(), -xNorm.z()).endVertex();
        buff.vertex(pose, ulfx, ulfy, ulfz).color(r, g, b, a).uv(uu, uv).overlayCoords(overlay).uv2(light).normal(normal, yNorm.x(), yNorm.y(), yNorm.z()).endVertex();
        buff.vertex(pose, ulbx, ulby, ulbz).color(r, g, b, a).uv(uu, uv+tw).overlayCoords(overlay).uv2(light).normal(normal, yNorm.x(), yNorm.y(), yNorm.z()).endVertex();
        buff.vertex(pose, urbx, urby, urbz).color(r, g, b, a).uv(uu+td, uv+tw).overlayCoords(overlay).uv2(light).normal(normal, yNorm.x(), yNorm.y(), yNorm.z()).endVertex();
        buff.vertex(pose, urfx, urfy, urfz).color(r, g, b, a).uv(uu+td, uv).overlayCoords(overlay).uv2(light).normal(normal, yNorm.x(), yNorm.y(), yNorm.z()).endVertex();
        buff.vertex(pose, dlbx, dlby, dlbz).color(r, g, b, a).uv(du, dv).overlayCoords(overlay).uv2(light).normal(normal, -yNorm.x(), -yNorm.y(), -yNorm.z()).endVertex();
        buff.vertex(pose, dlfx, dlfy, dlfz).color(r, g, b, a).uv(du, dv+tw).overlayCoords(overlay).uv2(light).normal(normal, -yNorm.x(), -yNorm.y(), -yNorm.z()).endVertex();
        buff.vertex(pose, drfx, drfy, drfz).color(r, g, b, a).uv(du+td, dv+tw).overlayCoords(overlay).uv2(light).normal(normal, -yNorm.x(), -yNorm.y(), -yNorm.z()).endVertex();
        buff.vertex(pose, drbx, drby, drbz).color(r, g, b, a).uv(du+td, dv).overlayCoords(overlay).uv2(light).normal(normal, -yNorm.x(), -yNorm.y(), -yNorm.z()).endVertex();
        buff.vertex(pose, drfx, drfy, drfz).color(r, g, b, a).uv(ru, rv).overlayCoords(overlay).uv2(light).normal(normal, zNorm.x(), zNorm.y(), zNorm.z()).endVertex();
        buff.vertex(pose, urfx, urfy, urfz).color(r, g, b, a).uv(ru+th, rv).overlayCoords(overlay).uv2(light).normal(normal, zNorm.x(), zNorm.y(), zNorm.z()).endVertex();
        buff.vertex(pose, urbx, urby, urbz).color(r, g, b, a).uv(ru+th, rv+tw).overlayCoords(overlay).uv2(light).normal(normal, zNorm.x(), zNorm.y(), zNorm.z()).endVertex();
        buff.vertex(pose, drbx, drby, drbz).color(r, g, b, a).uv(ru, rv+tw).overlayCoords(overlay).uv2(light).normal(normal, zNorm.x(), zNorm.y(), zNorm.z()).endVertex();
        buff.vertex(pose, ulfx, ulfy, ulfz).color(r, g, b, a).uv(lu, lv).overlayCoords(overlay).uv2(light).normal(normal, -zNorm.x(), -zNorm.y(), -zNorm.z()).endVertex();
        buff.vertex(pose, dlfx, dlfy, dlfz).color(r, g, b, a).uv(lu+th, lv).overlayCoords(overlay).uv2(light).normal(normal, -zNorm.x(), -zNorm.y(), -zNorm.z()).endVertex();
        buff.vertex(pose, dlbx, dlby, dlbz).color(r, g, b, a).uv(lu+th, lv+tw).overlayCoords(overlay).uv2(light).normal(normal, -zNorm.x(), -zNorm.y(), -zNorm.z()).endVertex();
        buff.vertex(pose, ulbx, ulby, ulbz).color(r, g, b, a).uv(lu, lv+tw).overlayCoords(overlay).uv2(light).normal(normal, -zNorm.x(), -zNorm.y(), -zNorm.z()).endVertex();
    }

    public static void renderBoxLines(MatrixStack stack, IVertexBuilder buff, Vector3f[] points, Direction... blocked) { //todo: color params
        renderBoxLines(
            stack, buff,
            Arrays.stream(points).map(Vector3d::new).toArray(Vector3d[]::new),
            blocked
        );
    }
    public static void renderBoxLines(MatrixStack stack, IVertexBuilder buff, Vector3d[] points, Direction... blocked) { //todo: color params
        renderLineSegment(stack, buff, points, blocked, 0b100, 0b101, 0b111, 0b110);
        renderLineSegment(stack, buff, points, blocked, 0b000, 0b001, 0b011, 0b010);
        renderLineSegment(stack, buff, points, blocked, 0b011, 0b111);
        renderLineSegment(stack, buff, points, blocked, 0b110, 0b010);
        renderLineSegment(stack, buff, points, blocked, 0b001, 0b101);
        renderLineSegment(stack, buff, points, blocked, 0b100, 0b000);
    }

    public static void renderLineSegment(MatrixStack stack, IVertexBuilder buff, Vector3d[] points, Direction[] blocked, int... ints) {
        Matrix4f pose = stack.last().pose();
        over:
        for (int i = 0; i < ints.length; i++) {
            int nextID = (i + 1) % ints.length;
            if(ints.length == 2 && i == 1) {
                break;
            }
            Vector3d vec = points[ints[i]];
            Vector3d next = points[ints[nextID]];
            for (Direction face : blocked) {
                int bit = face.getAxis().ordinal();
                int shifted = (ints[i]>>bit)&1;
                if(shifted == ((ints[nextID]>>bit)&1) && shifted == face.getAxisDirection().ordinal()) {
                    continue over;
                }
            }
            buff.vertex(pose, (float) vec.x, (float) vec.y, (float) vec.z).color(0f, 0f, 0f,0.4f).endVertex();
            buff.vertex(pose, (float) next.x, (float) next.y, (float) next.z).color(0f, 0f, 0f,0.4f).endVertex();

        }
    }

//    public static void drawTextureAtlasSprite(double x, double y, TextureAtlasSprite sprite, double width, double height, IVertexBuilder buff) {
//        drawTextureAtlasSprite(x, y, sprite, width, height, 0F, 0F, 16F, 16F, buff);
//    }
//
//    public static void drawTextureAtlasSprite(double x, double y, TextureAtlasSprite sprite, double width, double height, double minU, double minV, double maxU, double maxV, IVertexBuilder buff) {
//        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
//        buff.pos(x, y + height, 0).uv(sprite.getInterpolatedU(minU), sprite.getInterpolatedV(maxV)).endVertex();
//        buff.pos(x + width, y + height, 0).uv(sprite.getInterpolatedU(maxU), sprite.getInterpolatedV(maxV)).endVertex();
//        buff.pos(x + width, y, 0).uv(sprite.getInterpolatedU(maxU), sprite.getInterpolatedV(minV)).endVertex();
//        buff.pos(x, y, 0).uv(sprite.getInterpolatedU(minU), sprite.getInterpolatedV(minV)).endVertex();
//    }

    public static void renderBorderExclusive(MatrixStack stack, int left, int top, int right, int bottom, int borderSize, int borderColor) {
        renderBorder(stack, left - borderSize, top - borderSize, right + borderSize, bottom + borderSize, borderSize, borderColor);
    }

    public static void renderBorder(MatrixStack stack, int left, int top, int right, int bottom, int borderSize, int borderColor) {
        AbstractGui.fill(stack, left, top, right, top + borderSize, borderColor);
        AbstractGui.fill(stack, left, bottom, right, bottom - borderSize, borderColor);
        AbstractGui.fill(stack, left, top, left + borderSize, bottom, borderColor);
        AbstractGui.fill(stack, right, top, right - borderSize, bottom, borderColor);
    }

    public static void drawTexturedQuad(MatrixStack stack, IVertexBuilder buffer, float left, float top, float right, float bottom, float minU, float minV, float maxU, float maxV, float zLevel) {

//        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        Matrix4f pose = stack.last().pose();

        buffer.vertex(pose, left, top, zLevel).uv(minU, minV).endVertex();
        buffer.vertex(pose, left, bottom, zLevel).uv(minU, maxV).endVertex();
        buffer.vertex(pose, right, bottom, zLevel).uv(maxU, maxV).endVertex();
        buffer.vertex(pose, right, top, zLevel).uv(maxU, minV).endVertex();

//        Tessellator.getInstance().draw();
    }

    public static void draw256Texture(MatrixStack stack, int x, int y, int u, int v, int sizeX, int sizeU) {
        AbstractGui.blit(stack, x, y, 0, u, v, sizeX, sizeU, 256, 256);
    }

    public static void drawTextureAtlasSprite(MatrixStack stack, double x, double y, TextureAtlasSprite sprite, double width, double height) {
        drawTextureAtlasSprite(stack, x, y, sprite, width, height, 0F, 0F, 16F, 16F);
    }

    public static void drawTextureAtlasSprite(MatrixStack stack, double x, double y, TextureAtlasSprite sprite, double width, double height, double minU, double minV, double maxU, double maxV) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();

        Matrix4f pose = stack.last().pose();

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.vertex(pose, (float) x, (float) (y + height), 0).uv(sprite.getU(minU), sprite.getV(maxV)).endVertex();
        bufferbuilder.vertex(pose, (float) (x + width), (float) (y + height), 0).uv(sprite.getU(maxU), sprite.getV(maxV)).endVertex();
        bufferbuilder.vertex(pose, (float) (x + width), (float) y, 0).uv(sprite.getU(maxU), sprite.getV(minV)).endVertex();
        bufferbuilder.vertex(pose, (float) x, (float) y, 0).uv(sprite.getU(minU), sprite.getV(minV)).endVertex();
        tessellator.end();
    }

    public static void drawScaledCustomSizeModalRect(MatrixStack stack, int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight)
    {
        Matrix4f pose = stack.last().pose();
        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.vertex(pose, x, (y + height), 0.0F).uv(u * f, (v + vHeight) * f1).endVertex();
        buffer.vertex(pose, (x + width), (y + height), 0.0F).uv((u + uWidth) * f, (v + vHeight) * f1).endVertex();
        buffer.vertex(pose, (x + width), y, 0.0F).uv((u + uWidth) * f, v * f1).endVertex();
        buffer.vertex(pose, x, y, 0.0F).uv(u * f, v * f1).endVertex();
        tessellator.end();
    }
}