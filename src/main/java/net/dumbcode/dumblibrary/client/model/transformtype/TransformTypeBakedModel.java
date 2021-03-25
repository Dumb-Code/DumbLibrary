package net.dumbcode.dumblibrary.client.model.transformtype;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.experimental.Delegate;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraftforge.client.model.data.IDynamicBakedModel;

import java.util.Map;

/**
 * The transform type baked model class.
 *
 * @author Wyn Price
 * @see TransformTypeModelLoader
 */
public class TransformTypeBakedModel implements IDynamicBakedModel {

    @Delegate(excludes = DelegateExclusions.class)
    private final IBakedModel defaultModel;
    private final Map<ItemCameraTransforms.TransformType, IBakedModel> modelOverrides;

    /**
     * @param defaultModel   The default model
     * @param modelOverrides The map of overrides to use
     */
    public TransformTypeBakedModel(IBakedModel defaultModel, Map<ItemCameraTransforms.TransformType, IBakedModel> modelOverrides) {
        this.defaultModel = defaultModel;
        this.modelOverrides = modelOverrides;
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack stack) {
        return this.modelOverrides.getOrDefault(cameraTransformType, this.defaultModel).handlePerspective(cameraTransformType, stack);
    }

    /**
     * The list of methods that the @Delegate should not use
     */
    @SuppressWarnings("unused")
    private interface DelegateExclusions {
        IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack stack);
    }
}
