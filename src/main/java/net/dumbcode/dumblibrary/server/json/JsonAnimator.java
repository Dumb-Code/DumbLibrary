package net.dumbcode.dumblibrary.server.json;

import com.google.common.collect.Lists;
import com.google.gson.*;
import lombok.Data;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelAnimator;
import net.dumbcode.dumblibrary.server.json.objects.Constants;
import net.dumbcode.dumblibrary.server.json.objects.JsonAnimationModule;
import net.dumbcode.dumblibrary.server.json.objects.JsonAnimationRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.util.JsonUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BiFunction;

/**
 * The Json handler for
 *
 * @author Wyn Price
 */
@Data
public class JsonAnimator implements TabulaModelAnimator<Entity> {

    private final float globalSpeed;
    private final float globalDegree;
    private final Constants constants;
    private final List<JsonAnimationModule> animationModules;


    @Override
    public void setRotationAngles(TabulaModel model, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        for (JsonAnimationModule animationModule : this.animationModules) {
            animationModule.performAnimations(model, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        }
    }

    public static final class Deserializer implements JsonDeserializer<JsonAnimator> {
        @Override
        public JsonAnimator deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!element.isJsonObject()) {
                throw new JsonParseException("Expected a json object, found " + JsonUtils.toString(element));
            }
            JsonObject json = element.getAsJsonObject();
            List<JsonAnimationModule> animationList = Lists.newArrayList();
            List<Pair<JsonArray, BiFunction<JsonArray, JsonAnimator, JsonAnimationModule>>> factoryList = Lists.newArrayList();
            for (JsonElement jsonElement : JsonUtils.getJsonArray(json, "animation_tasks")) {
                if (!element.isJsonObject()) {
                    throw new JsonParseException("Expected a json object, found " + JsonUtils.toString(element));
                }
                JsonObject factoryObject = jsonElement.getAsJsonObject();
                String type = JsonUtils.getString(factoryObject, "type");
                if (!JsonAnimationRegistry.factoryMap.containsKey(type)) {
                    throw new JsonParseException("Illegal type: " + type);
                }
                factoryList.add(Pair.of(JsonUtils.getJsonArray(factoryObject, "array"), JsonAnimationRegistry.factoryMap.get(type)));
            }
            JsonAnimator animator = new JsonAnimator(
                    JsonUtils.getFloat(json, "global_speed"),
                    JsonUtils.getFloat(json, "global_degree"),
                    context.deserialize(JsonUtils.getJsonArray(json, "constants"), Constants.class),
                    animationList
            );

            return animator;
        }
    }
}
