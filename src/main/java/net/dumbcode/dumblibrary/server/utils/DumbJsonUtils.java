package net.dumbcode.dumblibrary.server.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Objects;

public class DumbJsonUtils {

    public static PotionEffect readPotionEffect(JsonElement element) {
        JsonObject obj = element.getAsJsonObject();
        return new PotionEffect(
                Objects.requireNonNull(ForgeRegistries.POTIONS.getValue(new ResourceLocation(JsonUtils.getString(obj, "potion")))),
                JsonUtils.getInt(obj, "duration"),
                JsonUtils.getInt(obj, "amplifier"),
                JsonUtils.getBoolean(obj, "ambient"),
                JsonUtils.getBoolean(obj, "show_particles")
        );
    }

    public static JsonObject writePotionEffect(PotionEffect effect) {
        JsonObject obj = new JsonObject();

        obj.addProperty("potion", Objects.requireNonNull(effect.getPotion().getRegistryName()).toString());
        obj.addProperty("duration", effect.getDuration());
        obj.addProperty("amplifier", effect.getAmplifier());
        obj.addProperty("ambient", effect.getIsAmbient());
        obj.addProperty("show_particles", effect.doesShowParticles());


        return obj;
    }
}
