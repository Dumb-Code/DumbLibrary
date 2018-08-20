package net.dumbcode.dumblibrary.client.model;

import com.google.common.collect.Maps;
import net.ilexiconn.llibrary.client.model.tabula.ITabulaModelAnimator;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tabula.container.TabulaCubeContainer;
import net.ilexiconn.llibrary.client.model.tabula.container.TabulaModelContainer;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;

import java.util.Map;

public class InfoTabulaModel extends TabulaModel {

    private final Map<String, int[]> dimensionMap = Maps.newHashMap();

    public InfoTabulaModel(TabulaModelContainer container, ITabulaModelAnimator tabulaAnimator) {
        super(container, tabulaAnimator);
        for (TabulaCubeContainer cube : container.getCubes()) {
            dimensionMap.put(cube.getName(), cube.getDimensions());
        }
    }

    public int[] getDimension(AdvancedModelRenderer cube) {
        return this.dimensionMap.getOrDefault(cube.boxName, new int[]{0, 0, 0});
    }

}
