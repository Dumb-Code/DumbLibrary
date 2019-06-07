package net.dumbcode.dumblibrary.client.model;

import lombok.experimental.Delegate;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.Map;

/**
 * The transform type baked model class.
 * @see TransformTypeModelLoader
 * @author Wyn Price
 */
public class TransformTypeBakedModel implements IBakedModel {

    @Delegate(excludes = DelegateExclusions.class)
    private final IBakedModel defaultModel;
    private final Map<ItemCameraTransforms.TransformType, IBakedModel> modelOverrides;

    /**
     * @param defaultModel The default model
     * @param modelOverrides The map of overrides to use
     */
    public TransformTypeBakedModel(IBakedModel defaultModel, Map<ItemCameraTransforms.TransformType, IBakedModel> modelOverrides) {
        this.defaultModel = defaultModel;
        this.modelOverrides = modelOverrides;
    }
    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        //Get the override, or the default model if there is no override, and let it handle the models.
        return this.modelOverrides.getOrDefault(cameraTransformType, this.defaultModel).handlePerspective(cameraTransformType);
    }

    /**
     * The list of methods that the @Delegate should not use
     */
    @SuppressWarnings("unused")
    private interface DelegateExclusions {
        Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType);
    }
}
