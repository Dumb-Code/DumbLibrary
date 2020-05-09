package net.dumbcode.dumblibrary.server.ecs.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentAttacher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Objects;
import java.util.function.Supplier;

@Getter
@Setter
@Accessors(chain = true)
public class ItemComponentAccessCreatable {
    private Supplier<Item> item = () -> DumbLibrary.COMPONENT_ITEM;
    private EntityComponentAttacher attacher = new EntityComponentAttacher();

    public JsonObject serialize(JsonObject json) {
        if(this.item.get() != DumbLibrary.COMPONENT_ITEM) {
            json.addProperty("item", Objects.requireNonNull(this.item.get().getRegistryName()).toString());
        }
        json.add("component", this.attacher.writeToJson(new JsonArray()));
        return json;
    }

    public void deserialize(JsonObject json) {
        this.item = () -> JsonUtils.isString(json, "item") ? ForgeRegistries.ITEMS.getValue(new ResourceLocation(JsonUtils.getString(json, "item"))) : DumbLibrary.COMPONENT_ITEM;

        this.attacher = new EntityComponentAttacher();
        this.attacher.readFromJson(JsonUtils.getJsonArray(json, "component"));
    }

    public Supplier<ItemStack> getStack() {
        return () -> {
            ItemStack stack = new ItemStack(this.item.get());
            ItemCompoundAccess.Access access = ItemCompoundAccess.getOrCreateAccess(stack);
            this.attacher.getDefaultConfig().attachAll(access);
            access.saveAll();
            return stack;
        };
    }
}
