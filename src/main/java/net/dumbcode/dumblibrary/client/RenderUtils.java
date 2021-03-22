package net.dumbcode.dumblibrary.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class RenderUtils {
    public static void drawCubeoid(Vector3d s, Vector3d e, IVertexBuilder buff) {
//        buff.(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buff.vertex(s.x, e.y, s.z).normal(0, 1, 0).endVertex();
        buff.vertex(s.x, e.y, e.z).normal(0, 1, 0).endVertex();
        buff.vertex(e.x, e.y, e.z).normal(0, 1, 0).endVertex();
        buff.vertex(e.x, e.y, s.z).normal(0, 1, 0).endVertex();
        buff.vertex(s.x, s.y, e.z).normal(0, -1, 0).endVertex();
        buff.vertex(s.x, s.y, s.z).normal(0, -1, 0).endVertex();
        buff.vertex(e.x, s.y, s.z).normal(0, -1, 0).endVertex();
        buff.vertex(e.x, s.y, e.z).normal(0, -1, 0).endVertex();
        buff.vertex(e.x, e.y, e.z).normal(1, 0, 0).endVertex();
        buff.vertex(e.x, s.y, e.z).normal(1, 0, 0).endVertex();
        buff.vertex(e.x, s.y, s.z).normal(1, 0, 0).endVertex();
        buff.vertex(e.x, e.y, s.z).normal(1, 0, 0).endVertex();
        buff.vertex(s.x, s.y, e.z).normal(-1, 0, 0).endVertex();
        buff.vertex(s.x, e.y, e.z).normal(-1, 0, 0).endVertex();
        buff.vertex(s.x, e.y, s.z).normal(-1, 0, 0).endVertex();
        buff.vertex(s.x, s.y, s.z).normal(-1, 0, 0).endVertex();
        buff.vertex(s.x, e.y, e.z).normal(0, 0, 1).endVertex();
        buff.vertex(s.x, s.y, e.z).normal(0, 0, 1).endVertex();
        buff.vertex(e.x, s.y, e.z).normal(0, 0, 1).endVertex();
        buff.vertex(e.x, e.y, e.z).normal(0, 0, 1).endVertex();
        buff.vertex(s.x, s.y, s.z).normal(0, 0, -1).endVertex();
        buff.vertex(s.x, e.y, s.z).normal(0, 0, -1).endVertex();
        buff.vertex(e.x, e.y, s.z).normal(0, 0, -1).endVertex();
        buff.vertex(e.x, s.y, s.z).normal(0, 0, -1).endVertex();
    }

//    public static void drawSpacedCube(double ulfx, double ulfy, double ulfz, double ulbx, double ulby, double ulbz, double urfx, double urfy, double urfz, double urbx, double urby, double urbz, double dlfx, double dlfy, double dlfz, double dlbx, double dlby, double dlbz, double drfx, double drfy, double drfz, double drbx, double drby, double drbz, double uu, double uv, double du, double dv, double lu, double lv, double ru, double rv, double fu, double fv,double bu, double bv, double tw,double th, double td, IVertexBuilder buff) {
//        drawSpacedCube(buff, ulfx, ulfy, ulfz, ulbx, ulby, ulbz, urfx, urfy, urfz, urbx, urby, urbz, dlfx, dlfy, dlfz, dlbx, dlby, dlbz, drfx, drfy, drfz, drbx, drby, drbz, uu, uv, du, dv, lu, lv, ru, rv, fu, fv, bu, bv, tw, th, td);
//    }

    //ulf, ulb, urf, urb, dlf, dlb, drf, drb
    public static void drawSpacedCube(IVertexBuilder buff, float ulfx, float ulfy, float ulfz, float ulbx, float ulby, float ulbz, float urfx, float urfy, float urfz, float urbx, float urby, float urbz, float dlfx, float dlfy, float dlfz, float dlbx, float dlby, float dlbz, float drfx, float drfy, float drfz, float drbx, float drby, float drbz, float uu, float uv, float du, float dv, float lu, float lv, float ru, float rv, float fu, float fv,float bu, float bv, float tw,float th, float td) {
        Vector3f xNorm = MathUtils.calculateNormalF(urfx, urfy, urfz, drfx, drfy, drfz, dlfx, dlfy, dlfz);
        Vector3f yNorm = MathUtils.calculateNormalF(ulfx, ulfy, ulfz, ulbx, ulby, ulbz, urbx, urby, urbz);
        Vector3f zNorm = MathUtils.calculateNormalF(drfx, drfy, drfz, urfx, urfy, urfz, urbx, urby, urbz);

        buff.vertex(urfx, urfy, urfz).uv(fu, fv).normal(xNorm.x(), xNorm.y(), xNorm.z()).endVertex();
        buff.vertex(drfx, drfy, drfz).uv(fu, fv+th).normal(xNorm.x(), xNorm.y(), xNorm.z()).endVertex();
        buff.vertex(dlfx, dlfy, dlfz).uv(fu+td, fv+th).normal(xNorm.x(), xNorm.y(), xNorm.z()).endVertex();
        buff.vertex(ulfx, ulfy, ulfz).uv(fu+td, fv).normal(xNorm.x(), xNorm.y(), xNorm.z()).endVertex();
        buff.vertex(drbx, drby, drbz).uv(bu, bv).normal(-xNorm.x(), -xNorm.y(), -xNorm.z()).endVertex();
        buff.vertex(urbx, urby, urbz).uv(bu, bv+th).normal(-xNorm.x(), -xNorm.y(), -xNorm.z()).endVertex();
        buff.vertex(ulbx, ulby, ulbz).uv(bu+td, bv+th).normal(-xNorm.x(), -xNorm.y(), -xNorm.z()).endVertex();
        buff.vertex(dlbx, dlby, dlbz).uv(bu+td, bv).normal(-xNorm.x(), -xNorm.y(), -xNorm.z()).endVertex();
        buff.vertex(ulfx, ulfy, ulfz).uv(uu, uv).normal(yNorm.x(), yNorm.y(), yNorm.z()).endVertex();
        buff.vertex(ulbx, ulby, ulbz).uv(uu, uv+tw).normal(yNorm.x(), yNorm.y(), yNorm.z()).endVertex();
        buff.vertex(urbx, urby, urbz).uv(uu+td, uv+tw).normal(yNorm.x(), yNorm.y(), yNorm.z()).endVertex();
        buff.vertex(urfx, urfy, urfz).uv(uu+td, uv).normal(yNorm.x(), yNorm.y(), yNorm.z()).endVertex();
        buff.vertex(dlbx, dlby, dlbz).uv(du, dv).normal(-yNorm.x(), -yNorm.y(), -yNorm.z()).endVertex();
        buff.vertex(dlfx, dlfy, dlfz).uv(du, dv+tw).normal(-yNorm.x(), -yNorm.y(), -yNorm.z()).endVertex();
        buff.vertex(drfx, drfy, drfz).uv(du+td, dv+tw).normal(-yNorm.x(), -yNorm.y(), -yNorm.z()).endVertex();
        buff.vertex(drbx, drby, drbz).uv(du+td, dv).normal(-yNorm.x(), -yNorm.y(), -yNorm.z()).endVertex();
        buff.vertex(drfx, drfy, drfz).uv(ru, rv).normal(zNorm.x(), zNorm.y(), zNorm.z()).endVertex();
        buff.vertex(urfx, urfy, urfz).uv(ru+th, rv).normal(zNorm.x(), zNorm.y(), zNorm.z()).endVertex();
        buff.vertex(urbx, urby, urbz).uv(ru+th, rv+tw).normal(zNorm.x(), zNorm.y(), zNorm.z()).endVertex();
        buff.vertex(drbx, drby, drbz).uv(ru, rv+tw).normal(zNorm.x(), zNorm.y(), zNorm.z()).endVertex();
        buff.vertex(ulfx, ulfy, ulfz).uv(lu, lv).normal(-zNorm.x(), -zNorm.y(), -zNorm.z()).endVertex();
        buff.vertex(dlfx, dlfy, dlfz).uv(lu+th, lv).normal(-zNorm.x(), -zNorm.y(), -zNorm.z()).endVertex();
        buff.vertex(dlbx, dlby, dlbz).uv(lu+th, lv+tw).normal(-zNorm.x(), -zNorm.y(), -zNorm.z()).endVertex();
        buff.vertex(ulbx, ulby, ulbz).uv(lu, lv+tw).normal(-zNorm.x(), -zNorm.y(), -zNorm.z()).endVertex();
    }

    public static void renderBoxLines(IVertexBuilder buff, Vector3d[] points, Direction... blocked) { //todo: color params
        render(buff, points, blocked, 0b100, 0b101, 0b111, 0b110);
        render(buff, points, blocked, 0b000, 0b001, 0b011, 0b010);
        render(buff, points, blocked, 0b011, 0b111);
        render(buff, points, blocked, 0b110, 0b010);
        render(buff, points, blocked, 0b001, 0b101);
        render(buff, points, blocked, 0b100, 0b000);
    }

    public static void render(IVertexBuilder buff, Vector3d[] points, Direction[] blocked, int... ints) {
//        buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
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
            buff.vertex(vec.x, vec.y, vec.z).color(0f, 0f, 0f,0.4f).endVertex();
            buff.vertex(next.x, next.y, next.z).color(0f, 0f, 0f,0.4f).endVertex();

        }
    }

//    public static void drawTextureAtlasSprite(double x, double y, TextureAtlasSprite sprite, double width, double height, IVertexBuilder buff) {
//        drawTextureAtlasSprite(x, y, sprite, width, height, 0F, 0F, 16F, 16F, buff);
//    }
//
//    public static void drawTextureAtlasSprite(double x, double y, TextureAtlasSprite sprite, double width, double height, double minU, double minV, double maxU, double maxV, IVertexBuilder buff) {
////        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
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

    public static void drawTexturedQuad(IVertexBuilder buffer, float left, float top, float right, float bottom, float minU, float minV, float maxU, float maxV, float zLevel) {

//        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        buffer.vertex(left, top, zLevel).uv(minU, minV).endVertex();
        buffer.vertex(left, bottom, zLevel).uv(minU, maxV).endVertex();
        buffer.vertex(right, bottom, zLevel).uv(maxU, maxV).endVertex();
        buffer.vertex(right, top, zLevel).uv(maxU, minV).endVertex();

//        Tessellator.getInstance().draw();
    }
}
