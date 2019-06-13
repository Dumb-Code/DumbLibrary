package net.dumbcode.dumblibrary.server.json.objects;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import net.dumbcode.dumblibrary.server.json.JsonAnimator;
import net.dumbcode.dumblibrary.server.json.objects.animation.Bob;
import net.dumbcode.dumblibrary.server.json.objects.animation.ChainSwing;
import net.dumbcode.dumblibrary.server.json.objects.animation.ChainWave;
import net.dumbcode.dumblibrary.server.json.objects.animation.Facing;

import java.util.Map;
import java.util.function.BiFunction;

public enum JsonAnimationRegistry {

    INSTANCE;

    public final Map<String, BiFunction<JsonArray, JsonAnimator, JsonAnimationModule>> factoryMap = Maps.newHashMap();

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(JsonAnimator.class, new JsonAnimator.Deserializer())
            .registerTypeAdapter(Constants.class, new Constants.Deserializer())
            .create();

    JsonAnimationRegistry() {
        this.factoryMap.put("bob", Bob::new);
        this.factoryMap.put("chain_wave", ChainWave.LimbSwing::new);
        this.factoryMap.put("chain_swing", ChainSwing::new);
        this.factoryMap.put("idle_chain_wave", ChainWave.IdleTick::new);
        this.factoryMap.put("facing", Facing::new);
    }

}
