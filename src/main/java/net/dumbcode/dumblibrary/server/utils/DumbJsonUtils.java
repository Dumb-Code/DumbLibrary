package net.dumbcode.dumblibrary.server.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class DumbJsonUtils {

    public static EffectInstance readPotionEffect(JsonElement element) {
        JsonObject obj = element.getAsJsonObject();
        return new EffectInstance(
            Objects.requireNonNull(ForgeRegistries.POTIONS.getValue(new ResourceLocation(JSONUtils.getAsString(obj, "potion")))),
            JSONUtils.getAsInt(obj, "duration"),
            JSONUtils.getAsInt(obj, "amplifier"),
            JSONUtils.getAsBoolean(obj, "ambient"),
            JSONUtils.getAsBoolean(obj, "show_particles")
        );
    }

    public static JsonObject writePotionEffect(EffectInstance effect) {
        JsonObject obj = new JsonObject();

        obj.addProperty("potion", Objects.requireNonNull(effect.getEffect().getRegistryName()).toString());
        obj.addProperty("duration", effect.getDuration());
        obj.addProperty("amplifier", effect.getAmplifier());
        obj.addProperty("ambient", effect.isAmbient());
        obj.addProperty("show_particles", effect.isVisible());

        return obj;
    }
}
