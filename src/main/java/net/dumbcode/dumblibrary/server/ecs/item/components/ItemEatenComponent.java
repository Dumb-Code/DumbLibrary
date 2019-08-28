package net.dumbcode.dumblibrary.server.ecs.item.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.utils.DumbJsonUtils;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Getter
public class ItemEatenComponent extends EntityComponent {

    private final List<PotionEffect> potionEffectList = new ArrayList<>();
    private boolean ignoreHunger;
    private int duration;
    private int fillAmount;
    private float saturation;

    public List<PotionEffect> getPotionEffectList() {
        return this.potionEffectList.stream().map(PotionEffect::new).collect(Collectors.toList());
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setTag("effects", this.potionEffectList.stream().map(effect -> effect.writeCustomPotionEffectToNBT(new NBTTagCompound())).collect(IOCollectors.toNBTTagList()));
        compound.setBoolean("ignore_hunger", this.ignoreHunger);
        compound.setInteger("duration", this.duration);
        compound.setInteger("fill_amount", this.fillAmount);
        compound.setFloat("saturation", this.saturation);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        super.deserialize(compound);
        this.potionEffectList.clear();
        StreamSupport.stream(compound.getTagList("effects", Constants.NBT.TAG_COMPOUND).spliterator(), false).map(tag -> PotionEffect.readCustomPotionEffectFromNBT((NBTTagCompound) tag)).forEach(this.potionEffectList::add);
        this.ignoreHunger = compound.getBoolean("ignore_hunger");
        this.duration = compound.getInteger("duration");
        this.fillAmount = compound.getInteger("fill_amount");
        this.saturation = compound.getFloat("saturation");
    }

    @Accessors(chain = true)
    @Getter
    @Setter
    public static class Storage implements EntityComponentStorage<ItemEatenComponent> {

        private List<PotionEffect> potionEffectList = new ArrayList<>();
        private boolean ignoreHunger = true;
        private int duration = 32;
        private int fillAmount;
        private float saturation;

        @Override
        public ItemEatenComponent construct() {
            ItemEatenComponent component = new ItemEatenComponent();
            component.potionEffectList.addAll(this.potionEffectList);
            component.duration = this.duration;
            component.fillAmount = this.fillAmount;
            component.saturation = this.saturation;
            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            StreamSupport.stream(JsonUtils.getJsonArray(json, "effects").spliterator(), false)
                    .map(DumbJsonUtils::readPotionEffect)
                    .forEach(this.potionEffectList::add);

            this.ignoreHunger = JsonUtils.getBoolean(json, "ignore_hunger");
            this.duration = JsonUtils.getInt(json, "duration");
            this.fillAmount = JsonUtils.getInt(json, "fill_amount");
            this.saturation = JsonUtils.getFloat(json, "saturation");
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("effects", this.potionEffectList.stream().map(DumbJsonUtils::writePotionEffect).collect(IOCollectors.toJsonArray()));

            json.addProperty("ignore_hunger", this.ignoreHunger);
            json.addProperty("duration", this.duration);
            json.addProperty("fill_amount", this.fillAmount);
            json.addProperty("saturation", this.saturation);

        }
    }
}
