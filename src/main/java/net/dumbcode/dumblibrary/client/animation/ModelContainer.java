package net.dumbcode.dumblibrary.client.animation;

import com.google.common.collect.Maps;
import lombok.Getter;
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

    /**
     * @param regname The registry name used to look up all the models/jsons
     * @param growthStages The acceptable growth stages. If the growth stage isnt in this list, it will default do {@link GrowthStage#ADULT}
     * @param mainModelMap A map of all the main models, compared to the growth stages
     * @param allAnimationNames A collection of all the animation names. These are case sensitive.
     * @param factory The Animation Factory, used to create the EntityAnimator
     * @param animationGetter A function to get an animation from a string
     * @param defaultAnimation the default animation for the entity. Should be a idle animation, or something similar
     * @param animationInfoGetter a function to get the animation information from an animation
     * @param factories a list of {@link PoseHandler.AnimationPassesFactory} (Note these should be Object::new)
     */
    public ModelContainer(ResourceLocation regname, List<GrowthStage> growthStages, Map<GrowthStage, String> mainModelMap,
                          Collection<String> allAnimationNames, AnimatorFactory factory,
                          Function<String, Animation> animationGetter,
                          Animation defaultAnimation, Function<Animation, AnimationInfo> animationInfoGetter,
                          PoseHandler.AnimationPassesFactory... factories) {

        //Create the pose handler
        this.poseHandler = new PoseHandler(regname, growthStages, mainModelMap, allAnimationNames, animationGetter);
        //Iterate through all the entries from the mainModel map
        for (val entry : mainModelMap.entrySet()) {
            //Get the GrowthStage from the entry
            GrowthStage growth = entry.getKey();
            //Create a referenced GrowthStage, as the actual growth stage may differ
            GrowthStage referneced = growth;
            //If the growth stage is not supported, default the the ADULT growth stage
            if(!growthStages.contains(growth)) {
                referneced = GrowthStage.ADULT;
            }
            TabulaModel model;
            //If the model is already created, instead of loading it all again, simply use the previously loaded model
            if(this.modelMap.containsKey(referneced)) {
                model = this.modelMap.get(referneced);
            } else {
                //Get the name of the model
                String mainModelName = mainModelMap.get(referneced);
                //Make sure the model name isnt null
                if(mainModelName == null) {
                    DumbLibrary.getLogger().error("Unable to load model for growth stage {} as main model was not defined", referneced.name());
                    //If the name is null, set the model to null and continue
                    model = null;
                } else {
                    //Get the resource location of where the model is,
                    ResourceLocation modelName = new ResourceLocation(regname.getResourceDomain(), "models/entities/" + regname.getResourcePath() + "/" + referneced.name().toLowerCase(Locale.ROOT) + "/" + mainModelName);
                    try {
                        //Try and load the model, and also try to load the EntityAnimator (factory.createAnimator)
                        model = TabulaUtils.getModel(modelName, factory.createAnimator(this.poseHandler, defaultAnimation, animationInfoGetter, factories));
                    } catch (Exception e) {
                        //If for whatever reason theres an error while loading the tabula model, log the error and set the model to null
                        DumbLibrary.getLogger().error("Unable to load model: " + modelName.toString(), e);
                        model = null;
                    }
                }
            }
            //Put the model in the map, with the unchanged GrowthStage as the key
            this.modelMap.put(growth, model);
        }
    }

    /**
     * The animation factory used for creating EntityAnimator
     */
    @SuppressWarnings("unchecked")
    public interface AnimatorFactory {

        /**
         * Creates the EntityAnimator
         * @param poseHandler The {@link PoseHandler}, needed for information about poses
         * @param defaultAnimation the default animation for the entity. Should be a idle animation, or something similar
         * @param animationInfoGetter a function to get the animation information from an animation
         * @param factories a list of {@link PoseHandler.AnimationPassesFactory} (Note these should be Object::new)
         * @return the entity animator
         */
        EntityAnimator createAnimator(PoseHandler poseHandler, Animation defaultAnimation,
                                      Function<Animation, AnimationInfo> animationInfoGetter,
                                      PoseHandler.AnimationPassesFactory... factories);
    }
}
