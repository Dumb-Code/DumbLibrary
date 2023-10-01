package net.dumbcode.dumblibrary.server.animation;

import lombok.NonNull;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.dumbcode.studio.animation.info.AnimationInfo;
import net.dumbcode.studio.animation.info.AnimationLoader;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AnimationContainer {
    private final Map<Animation, AnimationInfo> animationMap = new HashMap<>();

    private AnimationContainer(ResourceLocation regName) {
        if(regName == null) {
            return;
        }
        String baseLoc = "models/entities/" + regName.getPath() + "/";
        try {
            StreamUtils.getPath(new ResourceLocation(regName.getNamespace(), baseLoc), folder -> {
                for (Path path : StreamUtils.listPaths(folder)) {
                    if(Files.isDirectory(path)) {
                        String folderName = path.getFileName().toString();
                        if(folderName.endsWith("/")) {
                            folderName = folderName.substring(0, folderName.length() - 1);
                        }
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

    public static AnimationContainer of(@NonNull ResourceLocation regName) {
        return new AnimationContainer(regName);
    }

    public static AnimationContainer empty() {
        return new AnimationContainer(null);
    }
}
