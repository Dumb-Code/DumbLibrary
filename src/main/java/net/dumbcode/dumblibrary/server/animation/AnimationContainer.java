package net.dumbcode.dumblibrary.server.animation;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.dumbcode.studio.animation.info.AnimationInfo;
import net.dumbcode.studio.animation.info.AnimationLoader;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AnimationContainer {
    private final Map<Animation, AnimationInfo> animationMap = new HashMap<>();

    public AnimationContainer(ResourceLocation regName) {
        String baseLoc = "models/entities/" + regName.getPath() + "/";
        try {
            StreamUtils.getPath(new ResourceLocation(regName.getNamespace(), baseLoc), folder -> {
                for (Path path : StreamUtils.listPaths(folder)) {
                    if(Files.isDirectory(path)) {
                        String folderName = path.getFileName().toString();
                        for (Path subFile : StreamUtils.listPaths(path)) {
                            if(Files.isRegularFile(subFile)) {
                                this.loadAnimation(folderName, subFile);
                            }
                        }
                    } else if(Files.isRegularFile(path)) {
                        this.loadAnimation(regName.getNamespace(), path);
                    }
                }
                return null;
            });
        } catch (IOException e) {
            DumbLibrary.getLogger().error("Unable to load path", e);
        }
    }

    private void loadAnimation(String namespace, Path path) {
        Animation animation = new Animation(namespace, FilenameUtils.getBaseName(path.toString()));
        try {
            AnimationInfo info = AnimationLoader.loadAnimation(Files.newInputStream(path));
            this.animationMap.put(animation, info);
        } catch (IOException e) {
            DumbLibrary.getLogger().error("Unable to load animation at " + path.toString(), e);
        }
    }

    public AnimationInfo getInfo(Animation animation) {
        return this.animationMap.getOrDefault(animation, AnimationInfo.EMPTY);
    }
}
