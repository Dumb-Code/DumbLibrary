package net.dumbcode.dumblibrary.server.json.objects;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.json.JsonAnimator;
import net.minecraft.entity.Entity;
import net.minecraft.util.JSONUtils;

import java.util.List;

public abstract class JsonAnimationModule<V> {

    protected final List<V> list = Lists.newArrayList();
    protected final JsonAnimator animator;

    protected JsonAnimationModule(JsonArray array, JsonAnimator animator) {
        this.animator = animator;
        String name = this.getClass().toString();
        for (JsonElement jsonElement : array) {
            list.add(this.createValue(JSONUtils.convertToJsonObject(jsonElement, name), animator));
        }
    }

    public void performAnimations(DCMModel model, Entity entity, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale) {
        for (V value : this.list) {
            this.performAnimation(model, entity, value, limbSwing, limbSwingAmount, ticks, rotationYaw, rotationPitch, scale);
        }
    }

    protected abstract void performAnimation(DCMModel model, Entity entity, V value, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale);

    public abstract V createValue(JsonObject json, JsonAnimator animator);
}