package net.dumbcode.dumblibrary.server.ecs.item.components;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.DumbJsonUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ItemEatenComponent extends EntityComponent {

    private final List<EffectInstance> potionEffectList = new ArrayList<>();
    private boolean ignoreHunger;
    private int duration;
    private int fillAmount;
    private float saturation;

    public List<EffectInstance> getPotionEffectList() {
        return this.potionEffectList.stream().map(EffectInstance::new).collect(Collectors.toList());
    }

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.put("effects", this.potionEffectList.stream().map(effect -> effect.save(new CompoundNBT())).collect(CollectorUtils.toNBTTagList()));
        compound.putBoolean("ignore_hunger", this.ignoreHunger);
        compound.putInt("duration", this.duration);
        compound.putInt("fill_amount", this.fillAmount);
        compound.putFloat("saturation", this.saturation);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        super.deserialize(compound);
        this.potionEffectList.clear();
        StreamUtils.stream(compound.getList("effects", Constants.NBT.TAG_COMPOUND)).map(tag -> EffectInstance.load((CompoundNBT) tag)).forEach(this.potionEffectList::add);
        this.ignoreHunger = compound.getBoolean("ignore_hunger");
        this.duration = compound.getInt("duration");
        this.fillAmount = compound.getInt("fill_amount");
        this.saturation = compound.getFloat("saturation");
    }

    @Accessors(chain = true)
    @Getter
    @Setter
    public static class Storage implements EntityComponentStorage<ItemEatenComponent> {

        private List<EffectInstance> potionEffectList = new ArrayList<>();
        private boolean ignoreHunger = true;
        private int duration = 32;
        private int fillAmount;
        private float saturation;

        @Override
        public void constructTo(ItemEatenComponent component) {
            component.potionEffectList.addAll(this.potionEffectList);
            component.duration = this.duration;
            component.fillAmount = this.fillAmount;
            component.saturation = this.saturation;
        }

        @Override
        public void readJson(JsonObject json) {
            StreamUtils.stream(JSONUtils.getAsJsonArray(json, "effects"))
                    .map(DumbJsonUtils::readPotionEffect)
                    .forEach(this.potionEffectList::add);

            this.ignoreHunger = JSONUtils.getAsBoolean(json, "ignore_hunger");
            this.duration = JSONUtils.getAsInt(json, "duration");
            this.fillAmount = JSONUtils.getAsInt(json, "fill_amount");
            this.saturation = JSONUtils.getAsFloat(json, "saturation");
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("effects", this.potionEffectList.stream().map(DumbJsonUtils::writePotionEffect).collect(CollectorUtils.toJsonArray()));

            json.addProperty("ignore_hunger", this.ignoreHunger);
            json.addProperty("duration", this.duration);
            json.addProperty("fill_amount", this.fillAmount);
            json.addProperty("saturation", this.saturation);

        }
    }
}
