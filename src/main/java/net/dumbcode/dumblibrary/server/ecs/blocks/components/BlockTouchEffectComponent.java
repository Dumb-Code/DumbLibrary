package net.dumbcode.dumblibrary.server.ecs.blocks.components;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.DumbJsonUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.JSONUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockTouchEffectComponent extends EntityComponent {

    private final List<EffectInstance> potionEffectList = new ArrayList<>();


    public List<EffectInstance> getPotionEffectList() {
        return this.potionEffectList.stream().map(EffectInstance::new).collect(Collectors.toList());
    }

    @Accessors(chain = true)
    @Getter
    @Setter
    public static class Storage implements EntityComponentStorage<BlockTouchEffectComponent> {

        private List<EffectInstance> potionEffectList = new ArrayList<>();

        @Override
        public void constructTo(BlockTouchEffectComponent component) {
            component.potionEffectList.addAll(this.potionEffectList);
        }

        @Override
        public void readJson(JsonObject json) {
            StreamUtils.stream(JSONUtils.getAsJsonArray(json, "potions"))
                    .map(DumbJsonUtils::readPotionEffect)
                    .forEach(this.potionEffectList::add);

        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("potions", this.potionEffectList.stream().map(DumbJsonUtils::writePotionEffect).collect(CollectorUtils.toJsonArray()));

        }
    }
}
