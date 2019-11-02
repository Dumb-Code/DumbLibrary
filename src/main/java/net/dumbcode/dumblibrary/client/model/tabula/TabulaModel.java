package net.dumbcode.dumblibrary.client.model.tabula;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The ModelBase handler for a tabula model
 *
 * @author Wyn Price
 */
@Getter
@Setter
@Accessors(chain = true)
public class TabulaModel extends ModelBase {
    @Setter(AccessLevel.NONE)
    private boolean rendered = false;
    private final TabulaModelInformation information;
    private final List<TabulaModelRenderer> roots = Lists.newArrayList();
    @Getter
    private final Map<String, TabulaModelRenderer> cubeNameMap = Maps.newHashMap();
    private TabulaModelAnimator modelAnimator;

    public TabulaModel(TabulaModelInformation information) {
        this.information = information;
        this.textureWidth = information.getTexWidth();
        this.textureHeight = information.getTexHeight();
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.renderBoxes(scale);
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
