package net.dumbcode.dumblibrary.server.ecs.blocks.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.item.ItemComponentAccessCreatable;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockDropsComponent extends EntityComponent {

    private final List<Supplier<ItemStack>> stackList = new ArrayList<>();

    public void applyStacks(Consumer<ItemStack> spawner) {
        for (Supplier<ItemStack> stack : this.stackList) {
            spawner.accept(stack.get());
        }
    }

    @Override
    public CompoundTag serialize(CompoundTag compound) {
        compound.put("stacks", this.stackList.stream().map(Supplier::get).map(ItemStack::serializeNBT).collect(CollectorUtils.toNBTTagList()));
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundTag compound) {
        super.deserialize(compound);
        this.stackList.clear();
        StreamUtils.stream(compound.getList("stacks", Constants.NBT.TAG_COMPOUND)).map(base -> ItemStack.of((CompoundTag) base)).<Supplier<ItemStack>>map(stack -> () -> stack).forEach(this.stackList::add);
    }

    @Accessors(chain = true)
    @Getter
    @Setter
    public static class Storage implements EntityComponentStorage<BlockDropsComponent> {

        private List<ItemStack> stackList = new ArrayList<>();
        private List<ItemComponentAccessCreatable> creatables = new ArrayList<>();

        @Override
        public void constructTo(BlockDropsComponent component) {
            for (ItemStack stack : this.stackList) {
                component.stackList.add(() -> stack);
            }
            for (ItemComponentAccessCreatable creatable : this.creatables) {
                component.stackList.add(creatable.getStack());
            }
        }

        @Override
        public void readJson(JsonObject json) {
            this.stackList.clear();

            StreamUtils.stream(JSONUtils.getAsJsonArray(json, "stacks"))
                    .filter(JsonElement::isJsonObject)
                    .map(JsonElement::getAsJsonObject)
                    .map(BlockDropsComponent::deserializeItem)
                    .forEach(this.stackList::add);

            StreamUtils.stream(JSONUtils.getAsJsonArray(json, "creatables"))
                    .filter(JsonElement::isJsonObject)
                    .map(elem -> {
                        ItemComponentAccessCreatable creatable = new ItemComponentAccessCreatable();
                        creatable.deserialize(elem.getAsJsonObject());
                        return creatable;
                    }).forEach(this.creatables::add);

        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("stacks", this.stackList.stream().map(BlockDropsComponent::serializeItem).collect(CollectorUtils.toJsonArray()));
            json.add("creatables", this.creatables.stream().map(creatable ->  creatable.serialize(new JsonObject())).collect(CollectorUtils.toJsonArray()));
        }
    }

    private static ItemStack deserializeItem(JsonObject json) {
        return ShapedRecipe.itemFromJson(json);
    }

    private static JsonObject serializeItem(ItemStack stack) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", Objects.requireNonNull(stack.getItem().getRegistryName()).toString());
        obj.addProperty("count", stack.getCount());
        return obj;
    }
}
