package net.dumbcode.dumblibrary.server.animation.keyframes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.animation.objects.AnimatableCube;
import net.dumbcode.dumblibrary.server.animation.objects.CubeReference;
import net.dumbcode.dumblibrary.server.animation.objects.PoseData;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class KeyframeCompiler {

    private final Map<String, AnimatableCube> map = Maps.newHashMap();
    private final int version;

    private final List<Keyframe> keyframes = Lists.newArrayList();

    private KeyframeCompiler(TabulaModelInformation info, int version) {
        TabulaUtils.createCubeGroups(info.getGroups(), this.map);
        this.version = version;
    }

    public static KeyframeCompiler create(TabulaModelInformation info, int version) {
        return new KeyframeCompiler(info, version);
    }

    public Keyframe addKeyframe(float startTime, float duration) {
        Keyframe kf = new Keyframe(startTime, duration);
        this.keyframes.add(kf);
        return kf;
    }

    public List<PoseData> compile() {
        //Reset everything
        this.map.values().forEach(AnimatableCube::reset);

        //Transform from relative to absolute by adding the base tbl model
        if(this.version >= 3) {
            this.map.forEach((name, cube) -> {
                for (Keyframe keyframe : this.keyframes) {
                    for (int i = 0; i < 3; i++) {
                        if(keyframe.rotationMap.containsKey(name)) {
                            keyframe.rotationMap.get(name)[i] += cube.getDefaultRotation()[i];
                        }

                        if(keyframe.rotationPointMap.containsKey(name)) {
                            keyframe.rotationPointMap.get(name)[i] += cube.getDefaultRotationPoint()[i];
                        }
                    }
                }
            });
        }


        //Load the event map and put the first reference in
        Map<Float, List<Pair<String, CubeReference>>> eventMap = Maps.newHashMap();

        eventMap.put(0F, this.getCubeData());

        //Setup all the keyframes so the from maps are correct
        this.keyframes.stream().sorted(Comparator.comparing(Keyframe::getStartTime)).forEachOrdered(kf -> {
            for (Keyframe keyframe : this.keyframes) {
                keyframe.animate(kf.startTime);
            }
            kf.setup();
        });


        //Go through all the keyframes and animate at their start and end times, setting that time into the event map
        for (Keyframe eventFrame : this.keyframes) {
            eventFrame.progressionPoints.add(Pair.of(0F, 0F));
            eventFrame.progressionPoints.add(Pair.of(1F, 1F));
            for (Pair<Float, Float> point : eventFrame.progressionPoints) {
                for (Keyframe keyframe : this.keyframes) {
                    keyframe.animate(eventFrame.startTime + eventFrame.duration*point.getRight());
                }
                eventMap.put(eventFrame.startTime + eventFrame.duration*point.getLeft(), this.getCubeData());
            }

//            for (Keyframe keyframe : this.keyframes) {
//                keyframe.animate(eventFrame.startTime + eventFrame.duration);
//            }
//            eventMap.put(eventFrame.startTime + eventFrame.duration, this.getCubeData());

        }

        List<PoseData> poseData = Lists.newLinkedList();

        List<Float> times = Lists.newArrayList(eventMap.keySet());
        times.sort(null);
        for (int i = 0; i < times.size() - 1; i++) {
            float start = times.get(i);
            float end = times.get(i + 1);

            if(start != end) {
                PoseData data = new PoseData(end - start);

                eventMap.get(end).forEach(pair -> data.getCubes().put(pair.getKey(), pair.getValue()));

                poseData.add(data);
            }
        }
        return poseData;
    }

    private List<Pair<String, CubeReference>> getCubeData() {
        List<Pair<String, CubeReference>> references = Lists.newArrayList();

        this.map.forEach((name, cube) -> {
            float[] rotation = cube.getActualRotation();
            float[] position = cube.getActualRotationPoint();
            references.add(Pair.of(
                    name,
                    new CubeReference(
                            rotation[0],
                            rotation[1],
                            rotation[2],

                            position[0],
                            position[1],
                            position[2]
                    )
            ));
        });

        return references;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public class Keyframe {
        @Getter private final float startTime;
        private final float duration;

        @Getter private final Map<String, float[]> rotationMap = Maps.newHashMap();
        @Getter private final Map<String, float[]> rotationPointMap = Maps.newHashMap();

        private final Map<String, float[]> fromRotationMap = Maps.newHashMap();
        private final Map<String, float[]> fromRotationPointMap = Maps.newHashMap();

        @Getter private final Set<Pair<Float, Float>> progressionPoints = new HashSet<>();

        private boolean setup = false;

        public void setup() {
            this.setup = true;
            KeyframeCompiler.this.map.forEach((name, cube) -> {
                this.fromRotationMap.put(name, cube.getActualRotation());
                this.fromRotationPointMap.put(name, cube.getActualRotationPoint());
            });
        }

        public void animate(float ticks) {
            if(!this.setup) {
                return;
            }

            float percentageDone = (ticks - this.startTime) / this.duration;
            if(percentageDone > 1) {
                percentageDone = 1F;
            }
            if(percentageDone < 0) {
                return;
            }

            final float per = percentageDone;

            KeyframeCompiler.this.map.forEach((name, cube) -> {
                if(this.rotationMap.containsKey(name)) {
                    float[] from = this.fromRotationMap.get(name);
                    float[] to = this.rotationMap.get(name);
                    cube.setRotation(
                            from[0] + (to[0] - from[0]) * per,
                            from[1] + (to[1] - from[1]) * per,
                            from[2] + (to[2] - from[2]) * per
                    );
                }

                if(this.rotationPointMap.containsKey(name)) {
                    float[] from = this.fromRotationPointMap.get(name);
                    float[] to = this.rotationPointMap.get(name);
                    cube.setRotationPoint(
                            from[0] + (to[0] - from[0]) * per,
                            from[1] + (to[1] - from[1]) * per,
                            from[2] + (to[2] - from[2]) * per
                    );
                }
            });

        }
    }
}
