package net.dumbcode.dumblibrary.client.animation;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.val;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.animation.objects.AnimatedEntity;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;
import java.util.Map;

/**
 * The model container. Contains the models and the pose handler
 */
@Getter
public class ModelContainer<T extends EntityLiving & AnimatedEntity, E extends IStringSerializable> {
    private final Map<E, TabulaModel> modelMap;
    private final PoseHandler<T, E> poseHandler;

    public ModelContainer(ResourceLocation regname, AnimationSystemInfo<E, T> info) {
        this.modelMap = Maps.newHashMap();
        //Create the pose handler
        this.poseHandler = new PoseHandler<>(regname, info);
        //Iterate through all the entries from the mainModel map
        for (val entry : info.allValues()) {
            //Create a referenced ModelStage, as the actual growth stage may differ
            E referneced = entry;
            //If the growth stage is not supported, default the the ADULT growth stage
            if(!info.allAcceptedStages().contains(entry)) {
                referneced = info.defaultStage();
            }
            TabulaModel model;
            //If the model is already created, instead of loading it all again, simply use the previously loaded model
            if(this.modelMap.containsKey(referneced)) {
                model = this.modelMap.get(referneced);
            } else {
                //Get the name of the model
                String mainModelName = info.stageToModelMap().get(referneced);
                //Make sure the model name isnt null
                if(mainModelName == null) {
                    DumbLibrary.getLogger().error("Unable to load model for growth stage {} as main model was not defined", referneced.getName());
                    //If the name is null, set the model to null and continue
                    model = null;
                } else {
                    //Get the resource location of where the model is,
                    ResourceLocation modelName = new ResourceLocation(regname.getResourceDomain(), "models/entities/" + regname.getResourcePath() + "/" + referneced.getName().toLowerCase(Locale.ROOT) + "/" + mainModelName);
                    try {
                        //Try and load the model, and also try to load the EntityAnimator (factory.createAnimator)
                        model = TabulaUtils.getModel(modelName, info.createAnimator(this.poseHandler));
                    } catch (Exception e) {
                        //If for whatever reason theres an error while loading the tabula model, log the error and set the model to null
                        DumbLibrary.getLogger().error("Unable to load model: " + modelName.toString(), e);
                        model = null;
                    }
                }
            }
            //Put the model in the map, with the unchanged ModelStage as the key
            this.modelMap.put(entry, model);
        }
    }
}
