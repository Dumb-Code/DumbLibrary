package net.dumbcode.dumblibrary.server.json.objects.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dumbcode.dumblibrary.server.json.JsonAnimator;
import net.dumbcode.dumblibrary.server.json.objects.AnimationInfoBase;
import net.dumbcode.dumblibrary.server.json.objects.Constants;
import net.dumbcode.dumblibrary.server.json.objects.JsonAnimationModule;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.JsonUtils;

public abstract class ChainWave extends JsonAnimationModule<ChainWave.Info> {

    private ChainWave(JsonArray array, JsonAnimator animator) {
        super(array, animator);
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
            this.speed = JsonUtils.getFloat(json, "speed");
            this.degree = JsonUtils.getFloat(json, "degree");
            this.rootOffset = JsonUtils.getFloat(json, "root_offset");
        }
    }

    public static class IdleTick extends ChainWave {

        public IdleTick(JsonArray array, JsonAnimator animator) {
            super(array, animator);
        }

        @Override
        public void performAnimation(TabulaModel model, Entity entity, Info info, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale) {
            model.chainSwing(info.getRenderers(model), info.speed * this.animator.getGlobalSpeed(), info.degree * this.animator.getGlobalDegree(), info.rootOffset, ticks, 0.25F);
        }
    }

    public static class LimbSwing extends ChainWave {

        public LimbSwing(JsonArray array, JsonAnimator animator) {
            super(array, animator);
        }

        @Override
        public void performAnimation(TabulaModel model, Entity entity, Info info, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale) {
            model.chainSwing(info.getRenderers(model), info.speed * this.animator.getGlobalSpeed(), info.degree * this.animator.getGlobalDegree(), info.rootOffset, limbSwing, limbSwingAmount);
        }
    }
}
