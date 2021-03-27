package net.dumbcode.dumblibrary.server.json.objects;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModelRenderer;
import net.dumbcode.dumblibrary.server.json.JsonAnimator;
import net.minecraft.util.JSONUtils;

import java.util.List;
import java.util.Objects;

public class AnimationInfoBase {

    final List<String> animationPartNames;

    public AnimationInfoBase(JsonObject json, JsonAnimator animator) {
        this.animationPartNames = getParts(json, animator.getConstants());
    }

    public DCMModelRenderer[] getRenderers(DCMModel model) {
        return this.animationPartNames.stream().map(model::getCube).filter(Objects::nonNull).toArray(DCMModelRenderer[]::new);
    }

    public static List<String> getParts(JsonObject json, Constants constants) {
        if (JSONUtils.isStringValue(json, "constant")) {
            return constants.getStringParts(JSONUtils.getAsString(json, "constant"));
        } else if (json.has("names")) {
            List<String> list = Lists.newArrayList();
            for (JsonElement jsonElement : JSONUtils.getAsJsonArray(json, "names")) {
                list.add(JSONUtils.convertToString(jsonElement, "names"));
            }
            return list;
        }
        return Lists.newArrayList();
    }

}
