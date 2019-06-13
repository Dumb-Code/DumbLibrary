package net.dumbcode.dumblibrary.server.guidebooks.elements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.gui.GuiHelper;
import net.dumbcode.dumblibrary.server.guidebooks.Guidebook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeElement extends GuidebookElement {

    public static final ResourceLocation GENERIC_54_GUI = new ResourceLocation("minecraft:textures/gui/container/generic_54.png");
    private final IRecipe recipe;
    private final int width;
    private final int height;
    private final double scale;
    private final NonNullList<ItemStack[]> stacks;
    private int tick;
    private int ingredientIndex;

    public RecipeElement(JsonObject source, JsonDeserializationContext context) {
        super(source, context);
        ResourceLocation name;
        String recipeName = source.get("name").getAsString();
        if (recipeName.contains(":")) {
            name = new ResourceLocation(recipeName);
        } else {
            name = new ResourceLocation(DumbLibrary.MODID, recipeName);
        }
        recipe = CraftingManager.getRecipe(name);
        if (source.has("width")) {
            width = source.get("width").getAsInt();
        } else {
            width = 3;
        }
        if (source.has("height")) {
            height = source.get("height").getAsInt();
        } else {
            height = 3;
        }

        if (source.has("scale")) {
            scale = source.get("scale").getAsDouble();
        } else {
            scale = 1.0;
        }
        stacks = NonNullList.withSize(width * height, new ItemStack[0]);

        initRecipe();
    }

    private void initRecipe() {
        Iterator<Ingredient> iterator = recipe.getIngredients().iterator();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!iterator.hasNext())
                    return;

                Ingredient ingredient = iterator.next();

                if (ingredient != Ingredient.EMPTY) {
                    stacks.set(x + y * width, ingredient.getMatchingStacks());
                }
            }
        }
    }

    @Override
    public int getWidth(Guidebook guidebook) {
        return (int) (scale * (width + 1) * 18);
    }

    @Override
    public int getHeight(Guidebook guidebook) {
        return (int) (scale * height * 18);
    }

    @Override
    public void render(Guidebook guidebook) {
        GlStateManager.color(1f, 1f, 1f);
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem renderItem = mc.getRenderItem();
        renderItem.zLevel = 500f;
        GlStateManager.disableLighting();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ItemStack[] validIngredients = stacks.get(x + y * width);
                float xProgress = x / (float) width;
                float yProgress = y / (float) height;
                int renderX = (int) (getLeftOffset(guidebook) + xProgress * (getWidth(guidebook) - scale * 16));
                int renderY = (int) (getTopOffset(guidebook) + yProgress * getHeight(guidebook));
                GlStateManager.pushMatrix();
                GlStateManager.translate(renderX, renderY, 32.0F);
                GlStateManager.scale(scale, scale, 1f);
                mc.getTextureManager().bindTexture(GENERIC_54_GUI);
                Gui.drawModalRectWithCustomSizedTexture(0, 0, 7, 17, 18, 18, 256f, 256f);

                if (validIngredients.length > 0) {
                    GlStateManager.translate(1f, 1f, 0f);
                    renderItem.renderItemIntoGUI(validIngredients[ingredientIndex % validIngredients.length], 0, 0);
                }
                GlStateManager.popMatrix();
            }
        }

        GlStateManager.enableLighting();

        GlStateManager.pushMatrix();
        GlStateManager.translate(getLeftOffset(guidebook) + (width + 0.5) * scale * 18, getTopOffset(guidebook) + getHeight(guidebook) / 2 - scale * 16 / 2, 32.0F);
        GlStateManager.scale(scale, scale, 1f);
        mc.getTextureManager().bindTexture(GENERIC_54_GUI);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 7, 17, 18, 18, 256f, 256f);
        GlStateManager.translate(1f, 1f, 0f);
        renderItem.renderItemIntoGUI(recipe.getRecipeOutput(), 0, 0);
        GlStateManager.popMatrix();
        renderItem.zLevel = 0.0F;
    }

    @Override
    public List<TextComponentBase> getTooltipText(Guidebook guidebook, int localX, int localY) {
        if (!baseTooltip.isEmpty())
            return baseTooltip;
        localX += getLeftOffset(guidebook);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ItemStack[] validIngredients = stacks.get(x + y * width);
                if (validIngredients.length > 0) {
                    float xProgress = x / (float) width;
                    float yProgress = y / (float) height;
                    int renderX = (int) (getLeftOffset(guidebook) + xProgress * (getWidth(guidebook) - scale * 16)) + 1;
                    int renderY = (int) (getTopOffset(guidebook) + yProgress * getHeight(guidebook)) + 1;
                    if (localX >= renderX && localX < renderX + scale * 16
                            && localY >= renderY && localY < renderY + scale * 16) {
                        return GuiHelper.getItemToolTip(validIngredients[ingredientIndex % validIngredients.length]).stream().map(TextComponentString::new).collect(Collectors.toList());
                    }
                }
            }
        }
        int renderX = (int) (getLeftOffset(guidebook) + (width + 0.5) * scale * 18);
        int renderY = (int) (getTopOffset(guidebook) + getHeight(guidebook) / 2 - scale * 16 / 2);
        if (localX >= renderX && localX < renderX + scale * 18
                && localY >= renderY && localY < renderY + scale * 18) {
            return GuiHelper.getItemToolTip(recipe.getRecipeOutput()).stream().map(TextComponentString::new).collect(Collectors.toList());
        }
        return super.getTooltipText(guidebook, localX, localY);
    }

    @Override
    public void update() {
        tick++;
        if (tick >= 20) {
            tick %= 20;
            ingredientIndex++;
        }
    }

    @Override
    public String getElementType() {
        return "recipe";
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

    @Override
    public boolean isAnimated() {
        return true;
    }
}
