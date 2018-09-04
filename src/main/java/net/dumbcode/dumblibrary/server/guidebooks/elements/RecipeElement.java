package net.dumbcode.dumblibrary.server.guidebooks.elements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.dumblibrary.DumbLibrary;
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

import java.util.Iterator;

public class RecipeElement extends GuidebookElement {
    private final IRecipe recipe;
    private final int width;
    private final int height;
    private final double scale;
    private final NonNullList<ItemStack[]> stacks;

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
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ItemStack[] validIngredients = stacks.get(x+y*width);
                if(validIngredients.length > 0) {
                    float xProgress = x/(float)width;
                    float yProgress = (height-y-1)/(float)height;
                    int renderX = (int) (getLeftOffset(guidebook) + xProgress * (getWidth(guidebook)-scale*20));
                    int renderY = (int) (getTopOffset(guidebook) + yProgress * getHeight(guidebook));
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(renderX, renderY, 32.0F);
                    GlStateManager.scale(scale, scale, 1f);
                    renderItem.renderItemIntoGUI(validIngredients[0], 0,0);
                    GlStateManager.popMatrix();
                }
            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(getLeftOffset(guidebook)+width*scale*20, getTopOffset(guidebook)+getHeight(guidebook)/2-scale*20/2, 32.0F);
        GlStateManager.scale(scale, scale, 1f);
        renderItem.renderItemIntoGUI(recipe.getRecipeOutput(), 0,0);
        GlStateManager.popMatrix();
        renderItem.zLevel = 0.0F;
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
}
