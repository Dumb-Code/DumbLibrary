package net.dumbcode.dumblibrary.server.json.objects.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.json.JsonAnimator;
import net.dumbcode.dumblibrary.server.json.objects.AnimationInfoBase;
import net.dumbcode.dumblibrary.server.json.objects.JsonAnimationModule;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.JSONUtils;

public class ChainSwing extends JsonAnimationModule<ChainSwing.Info> {

    public ChainSwing(JsonArray array, JsonAnimator animator) {
        super(array, animator);
    }

    @Override
    public void performAnimation(DCMModel model, Entity entity, Info info, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale) {
//        model.chainSwing(info.getRenderers(model), info.speed, info.degree, info.rootOffset, ticks, 0.25F);
    }

    @Override
    public Info createValue(JsonObject json, JsonAnimator animator) {
        return new Info(json, animator);
    }

    public static class Info extends AnimationInfoBase {
        private final float speed;
        private final float degree;
        private final float rootOffset;

        protected Info(JsonObject json, JsonAnimator animator) {
            super(json, animator);
            this.speed = JSONUtils.getAsFloat(json, "speed");
            this.degree = JSONUtils.getAsFloat(json, "degree");
            this.rootOffset = JSONUtils.getAsFloat(json, "root_offset");
        }
    }
}
