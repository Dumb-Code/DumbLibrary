package net.dumbcode.dumblibrary.server.json.objects;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dumbcode.dumblibrary.client.model.tabula.DCMModel;
import net.dumbcode.dumblibrary.server.json.JsonAnimator;
import net.minecraft.util.JsonUtils;

import java.util.List;
import java.util.Objects;

public class AnimationInfoBase {

    final List<String> animationPartNames;

    public AnimationInfoBase(JsonObject json, JsonAnimator animator) {
        this.animationPartNames = getParts(json, animator.getConstants());
    }

    public TabulaModelRenderer[] getRenderers(DCMModel model) {
        return this.animationPartNames.stream().map(model::getCube).filter(Objects::nonNull).toArray(TabulaModelRenderer[]::new);
    }

    public static List<String> getParts(JsonObject json, Constants constants) {
        if (JsonUtils.isString(json, "constant")) {
            return constants.getStringParts(JsonUtils.getString(json, "constant"));
        } else if (JsonUtils.isJsonArray(json, "names")) {
            List<String> list = Lists.newArrayList();
            for (JsonElement jsonElement : JsonUtils.getJsonArray(json, "names")) {
                list.add(JsonUtils.getString(jsonElement, "names"));
            }
            return list;
        }
        return Lists.newArrayList();
    }

}
