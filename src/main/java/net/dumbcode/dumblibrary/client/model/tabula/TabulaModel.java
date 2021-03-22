package net.dumbcode.dumblibrary.client.model.tabula;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.animation.EntityWithAnimation;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The ModelBase handler for a tabula model
 *
 * @author Wyn Price
 */
@Getter
@Setter
@Accessors(chain = true)
public class TabulaModel extends EntityModel<Entity> {
    @Setter(AccessLevel.NONE)
    private boolean rendered = false;
    private final TabulaModelInformation information;
    private final List<TabulaModelRenderer> roots = Lists.newArrayList();
    @Getter
    private final Map<String, TabulaModelRenderer> cubeNameMap = Maps.newHashMap();
    private TabulaModelAnimator modelAnimator;

    private Consumer<TabulaModelRenderer> onRenderCallback ;

    public TabulaModel(TabulaModelInformation information) {
        this.information = information;
        this.texWidth = information.getTexWidth();
        this.texHeight = information.getTexHeight();
    }

    @Override
    public void renderToBuffer(MatrixStack p_225598_1_, IVertexBuilder p_225598_2_, int p_225598_3_, int p_225598_4_, float r, float g, float b, float opacity) {
        this.renderBoxes(1/16F);
    }

    @Override
    public void prepareMobModel(Entity entityIn, float p_212843_2_, float p_212843_3_, float partialTickTime) {
        if(entityIn instanceof EntityWithAnimation) {
            ((EntityWithAnimation) entityIn).getAnimationEntry().applyAnimations(partialTickTime, this);
        }
        super.prepareMobModel(entityIn, p_212843_2_, p_212843_3_, partialTickTime);
    }

    @Override
    public void setupAnim(Entity entityIn, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {

    }

    public void resetAnimations() {
        for (TabulaModelRenderer cube : this.getAllCubes()) {
            cube.resetRotations();
            cube.resetRotationPoint();
        }
    }

    public void renderBoxes(float scale) {
        this.rendered = true;
        GlStateManager.pushMatrix();
        float[] scales = this.information.getScale();
        GlStateManager.scale(scales[0], scales[1], scales[2]);
        for (TabulaModelRenderer root : this.roots) {
            root.render(scale);
        }
        GlStateManager.popMatrix();
    }

    public Collection<TabulaModelRenderer> getAllCubes() {
        return this.cubeNameMap.values();
    }

    public Collection<String> getAllCubesNames() {
        return this.cubeNameMap.keySet();
    }

    public TabulaModelRenderer getCube(String cubeName) {
        return this.cubeNameMap.get(cubeName);
    }
}
