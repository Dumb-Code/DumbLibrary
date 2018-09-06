package net.dumbcode.dumblibrary.server.guidebooks.elements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.gui.GuiHelper;
import net.dumbcode.dumblibrary.server.guidebooks.Guidebook;
import net.minecraft.client.Minecraft;
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
import scala.actors.threadpool.Arrays;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeElement extends GuidebookElement {
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
        if(recipeName.contains(":")) {
            name = new ResourceLocation(recipeName);
        } else {
            name = new ResourceLocation(DumbLibrary.MODID, recipeName);
        }
        recipe = CraftingManager.getRecipe(name);
        if(source.has("width")) {
            width = source.get("width").getAsInt();
        } else {
            width = 3;
        }
        if(source.has("height")) {
            height = source.get("height").getAsInt();
        } else {
            height = 3;
        }

        if(source.has("scale")) {
            scale = source.get("scale").getAsDouble();
        } else {
            scale = 1.0;
        }
        stacks = NonNullList.withSize(width*height, new ItemStack[0]);

        initRecipe();
    }

    private void initRecipe() {
        Iterator<Ingredient> iterator = recipe.getIngredients().iterator();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(!iterator.hasNext())
                    return;

                Ingredient ingredient = iterator.next();

                if(ingredient != Ingredient.EMPTY) {
                    stacks.set(x+y*width, ingredient.getMatchingStacks());
                }
            }
        }
    }

    @Override
    public int getWidth(Guidebook guidebook) {
        return (int) (scale * (width+1) *20);
    }

    @Override
    public int getHeight(Guidebook guidebook) {
        return (int) (scale * height *20);
    }

    @Override
    public void render(Guidebook guidebook) {
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem renderItem = mc.getRenderItem();
        renderItem.zLevel = 500f;
        GlStateManager.disableLighting();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ItemStack[] validIngredients = stacks.get(x+y*width);
                if(validIngredients.length > 0) {
                    float xProgress = x/(float)width;
                    float yProgress = y/(float)height;
                    int renderX = (int) (getLeftOffset(guidebook) + xProgress * (getWidth(guidebook)-scale*20));
                    int renderY = (int) (getTopOffset(guidebook) + yProgress * getHeight(guidebook));
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(renderX, renderY, 32.0F);
                    GlStateManager.scale(scale, scale, 1f);
                    renderItem.renderItemIntoGUI(validIngredients[ingredientIndex % validIngredients.length], 0,0);
                    GlStateManager.popMatrix();
                }
            }
        }

        GlStateManager.enableLighting();

        GlStateManager.pushMatrix();
        GlStateManager.translate(getLeftOffset(guidebook)+width*scale*20, getTopOffset(guidebook)+getHeight(guidebook)/2-scale*20/2, 32.0F);
        GlStateManager.scale(scale, scale, 1f);
        renderItem.renderItemIntoGUI(recipe.getRecipeOutput(), 0,0);
        GlStateManager.popMatrix();
        renderItem.zLevel = 0.0F;
    }

    @Override
    public List<TextComponentBase> getTooltipText(Guidebook guidebook, int localX, int localY) {
        if(!baseTooltip.isEmpty())
            return baseTooltip;
        localX += getLeftOffset(guidebook);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ItemStack[] validIngredients = stacks.get(x+y*width);
                if(validIngredients.length > 0) {
                    float xProgress = x/(float)width;
                    float yProgress = y/(float)height;
                    int renderX = (int) (getLeftOffset(guidebook) + xProgress * (getWidth(guidebook)-scale*20));
                    int renderY = (int) (getTopOffset(guidebook) + yProgress * getHeight(guidebook));
                    if(localX >= renderX && localX < renderX + scale*20
                            && localY >= renderY && localY < renderY + scale*20) {
                        return GuiHelper.getItemToolTip(validIngredients[ingredientIndex % validIngredients.length]).stream().map(TextComponentString::new).collect(Collectors.toList());
                    }
                }
            }
        }
        int renderX = (int) (getLeftOffset(guidebook)+width*scale*20);
        int renderY = (int) (getTopOffset(guidebook)+getHeight(guidebook)/2-scale*20/2);
        if(localX >= renderX && localX < renderX + scale*20
                && localY >= renderY && localY < renderY + scale*20) {
            return GuiHelper.getItemToolTip(recipe.getRecipeOutput()).stream().map(TextComponentString::new).collect(Collectors.toList());
        }
        return super.getTooltipText(guidebook, localX, localY);
    }

    @Override
    public void update() {
        tick++;
        if(tick >= 20) {
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
