package net.dumbcode.dumblibrary.client.model.dcm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicDoubleArray;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.animation.EntityWithAnimation;
import net.dumbcode.studio.model.CubeInfo;
import net.dumbcode.studio.model.ModelInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * The ModelBase handler for a tabula model
 *
 * @author Wyn Price
 */
@Getter
@Setter
@Accessors(chain = true)
public class DCMModel extends EntityModel<Entity> {
    private final ModelInfo information;
    private final List<DCMModelRenderer> roots = Lists.newArrayList();
    @Getter
    private final Map<String, DCMModelRenderer> cubeNameMap = Maps.newHashMap();

    @Accessors(chain = true)
    private RenderCallbackConsumer onRenderCallback;


    public DCMModel(ModelInfo information) {
        this.information = information;
        this.texWidth = information.getTextureWidth();
        this.texHeight = information.getTextureHeight();

        for (CubeInfo root : information.getRoots()) {
            this.roots.add(new DCMModelRenderer(this, null, root));
        }
    }

    @Override
    public void renderToBuffer(MatrixStack stack, IVertexBuilder buffer, int light, int overlay, float r, float g, float b, float opacity) {
        for (DCMModelRenderer root : this.roots) {
            root.render(stack, buffer, light, overlay, r, g, b, opacity);
        }
    }

    @Override
    public void prepareMobModel(Entity entityIn, float p_212843_2_, float p_212843_3_, float partialTickTime) {
        if(entityIn instanceof EntityWithAnimation) {
            ((EntityWithAnimation) entityIn).getAnimationEntry().applyAnimations(this, partialTickTime);
        }
        super.prepareMobModel(entityIn, p_212843_2_, p_212843_3_, partialTickTime);
    }

    @Override
    public void setupAnim(Entity entityIn, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {

    }

    @Deprecated
    public void renderBoxes(MatrixStack stack, int light, ResourceLocation texture) {
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        this.renderToBuffer(stack, buffer.getBuffer(this.renderType(texture)), light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        buffer.endBatch();
    }

    public void renderBoxes(MatrixStack stack, int light, IVertexBuilder buff) {
        this.renderToBuffer(stack, buff, light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }

    public void resetAnimations() {

    }

    public Collection<DCMModelRenderer> getAllCubes() {
        return this.cubeNameMap.values();
    }

    public Collection<String> getAllCubesNames() {
        return this.cubeNameMap.keySet();
    }

    public DCMModelRenderer getCube(String cubeName) {
        return this.cubeNameMap.get(cubeName);
    }

    public interface RenderCallbackConsumer {
        void onFrame(DCMModelRenderer cube, AtomicInteger light, AtomicInteger overlay, AtomicDoubleArray color);
    }
}
