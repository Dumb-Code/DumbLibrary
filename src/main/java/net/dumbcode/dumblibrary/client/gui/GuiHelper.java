package net.dumbcode.dumblibrary.client.gui;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;

@UtilityClass
public class GuiHelper {

    public static void prepareModelRendering(int posX, int posY, float scale, float cameraPitch, float cameraYaw) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)posX, (float)posY, 500.0F);
        GlStateManager.translate(0f, -20f, 0f);
        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(cameraPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(cameraYaw, 0.0F, 1.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
    }

    public static void cleanupModelRendering() {
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setRenderShadow(true);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
