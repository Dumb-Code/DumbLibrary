package net.dumbcode.dumblibrary.server.ecs.blocks.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.item.ItemComponentAccessCreatable;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

public class BlockDropsComponent extends EntityComponent {

    private final List<Supplier<ItemStack>> stackList = new ArrayList<>();

    public void applyStacks(Consumer<ItemStack> spawner) {
        for (Supplier<ItemStack> stack : this.stackList) {
            spawner.accept(stack.get());
        }
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setTag("stacks", this.stackList.stream().map(Supplier::get).map(ItemStack::serializeNBT).collect(IOCollectors.toNBTTagList()));
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        super.deserialize(compound);
        this.stackList.clear();
        StreamUtils.stream(compound.getTagList("stacks", Constants.NBT.TAG_COMPOUND)).map(base -> new ItemStack((NBTTagCompound) base)).<Supplier<ItemStack>>map(stack -> () -> stack).forEach(this.stackList::add);
    }

    @Accessors(chain = true)
    @Getter
    @Setter
    public static class Storage implements EntityComponentStorage<BlockDropsComponent> {

        private List<ItemStack> stackList = new ArrayList<>();
        private List<ItemComponentAccessCreatable> creatables = new ArrayList<>();

        @Override
        public BlockDropsComponent construct() {
            BlockDropsComponent component = new BlockDropsComponent();
            for (ItemStack stack : this.stackList) {
                component.stackList.add(() -> stack);
            }
            for (ItemComponentAccessCreatable creatable : this.creatables) {
                component.stackList.add(creatable.getStack());
            }
            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            this.stackList.clear();

            StreamUtils.stream(JsonUtils.getJsonArray(json, "stacks"))
                    .filter(JsonElement::isJsonObject)
                    .map(JsonElement::getAsJsonObject)
                    .map(BlockDropsComponent::deserializeItem)
                    .forEach(this.stackList::add);

            StreamUtils.stream(JsonUtils.getJsonArray(json, "creatables"))
                    .filter(JsonElement::isJsonObject)
                    .map(elem -> {
                        ItemComponentAccessCreatable creatable = new ItemComponentAccessCreatable();
                        creatable.deserialize(elem.getAsJsonObject());
                        return creatable;
                    }).forEach(this.creatables::add);

        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("stacks", this.stackList.stream().map(BlockDropsComponent::serializeItem).collect(IOCollectors.toJsonArray()));
            json.add("creatables", this.creatables.stream().map(creatable ->  creatable.serialize(new JsonObject())).collect(IOCollectors.toJsonArray()));
        }
    }

    private static ItemStack deserializeItem(JsonObject json) {
        return ShapedRecipes.deserializeItem(json, true);
    }

    private static JsonObject serializeItem(ItemStack stack) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", Objects.requireNonNull(stack.getItem().getRegistryName()).toString());
        if(stack.getItem().getHasSubtypes()) {
            obj.addProperty("data", stack.getItemDamage());
        }
        obj.addProperty("count", stack.getCount());
        return obj;
    }
}
