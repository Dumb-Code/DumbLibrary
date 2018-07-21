package net.dumbcode.dumblibrary.client.animation;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.animation.objects.EntityAnimator;
import net.dumbcode.dumblibrary.server.entity.GrowthStage;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * The model container. Contains the models and the pose handler
 */
@Getter
public class ModelContainer {
    private final Map<GrowthStage, TabulaModel> modelMap = Maps.newEnumMap(GrowthStage.class);
    private final PoseHandler poseHandler;

    public ModelContainer(ResourceLocation regname, List<GrowthStage> growthStages, Map<GrowthStage, String> mainModelMap,
                          Collection<String> allAnimationNames, AnimatorFactory factory,
                          Function<String, Animation> animationGetter,
                          Animation defaultAnimation, Function<Animation, AnimationInfo> animationInfoGetter,
                          PoseHandler.AnimationPassesFactory... factories) {

        this.poseHandler = new PoseHandler(regname, growthStages, mainModelMap, allAnimationNames, animationGetter);

        for (val entry : mainModelMap.entrySet()) {
            GrowthStage growth = entry.getKey();
            GrowthStage referneced = growth;
            if(!growthStages.contains(growth)) {
                referneced = GrowthStage.ADULT;
            }
            TabulaModel model;
            if(this.modelMap.containsKey(referneced)) {
                model = this.modelMap.get(referneced);
            } else {
                String mainModelName = mainModelMap.get(referneced);
                if(mainModelName == null) {
                    DumbLibrary.getLogger().error("Unable to load model for growth stage {} as main model was not defined", referneced.name());
                    model = null;
                } else {
                    ResourceLocation modelName = new ResourceLocation(regname.getResourceDomain(), "models/entities/" + regname.getResourcePath() + "/" + referneced.name().toLowerCase(Locale.ROOT) + "/" + mainModelName);
                    try {
                        model = TabulaUtils.getModel(modelName, factory.createAnimator(this.poseHandler, defaultAnimation, animationInfoGetter, factories));
                    } catch (Exception e) {
                        DumbLibrary.getLogger().error("Unable to load model: " + modelName.toString(), e);
                        model = null;
                    }
                }
            }
            this.modelMap.put(growth, model);
        }
    }

    @SuppressWarnings("unchecked")
    public interface AnimatorFactory {
        EntityAnimator createAnimator(PoseHandler poseHandler, Animation defaultAnimation,
                                      Function<Animation, AnimationInfo> animationInfoGetter,
                                      PoseHandler.AnimationPassesFactory... factories);
    }
}
