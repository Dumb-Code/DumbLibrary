package net.dumbcode.dumblibrary.server.guidebooks.elements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.dumblibrary.client.gui.GuiHelper;
import net.dumbcode.dumblibrary.server.guidebooks.Guidebook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ItemElement extends GuidebookElement {

    private final ResourceLocation itemLocation;
    private final double scale;
    private final ItemStack stack;

    public ItemElement(JsonObject source, JsonDeserializationContext context) {
        super(source, context);
        itemLocation = new ResourceLocation(source.get("item").getAsString());
        scale = source.get("scale").getAsDouble();
        stack = new ItemStack(Item.REGISTRY.getObject(itemLocation));

        if (baseTooltip.isEmpty() && FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            GuiHelper.getItemToolTip(stack).stream().map(TextComponentString::new).forEach(baseTooltip::add);
        }
    }

    @Override
    public int getWidth(Guidebook guidebook) {
        return (int) (20 * scale);
    }

    @Override
    public int getHeight(Guidebook guidebook) {
        return (int) (20 * scale);
    }

    @Override
    public void render(Guidebook guidebook) {
        drawItemStack(stack, getLeftOffset(guidebook), 0, null);
    }

    private void drawItemStack(ItemStack stack, int x, int y, String altText) {
        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem itemRender = mc.getRenderItem();
        net.minecraft.client.gui.FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = mc.fontRenderer;
        itemRender.zLevel = 500f;
        GlStateManager.translate(x, y, 0f);
        GlStateManager.scale(scale, scale, 1f);
        itemRender.renderItemAndEffectIntoGUI(stack, 0, 0);
        itemRender.renderItemOverlayIntoGUI(font, stack, 0, 0, altText);
        itemRender.zLevel = 0.0F;
    }

    @Override
    public String getElementType() {
        return "item";
    }

    @Override
    public void writeToJSON(JsonObject destination, JsonSerializationContext context) {
        destination.addProperty("scale", scale);
        destination.addProperty("item", itemLocation.toString());
    }

    @Override
    public int getLeftOffset(Guidebook guidebook) {
        return (int) (guidebook.getPageWidth() / 2 - getWidth(guidebook) / 2 - 5 * scale);
    }

    @Override
    public int getTopOffset(Guidebook guidebook) {
        return 0;
    }
}
