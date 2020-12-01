package net.dumbcode.dumblibrary.server.ecs.component.storge;

import net.dumbcode.dumblibrary.server.utils.IndexedObject;

import java.util.List;
import java.util.function.Consumer;

public interface ShowcasingTextureStorage {
    void gatherTextures(Consumer<IndexedObject<String>> consumer);
}
