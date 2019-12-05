package net.dumbcode.dumblibrary.server.ecs.component;

import com.google.gson.JsonObject;

public interface EntityComponentStorage<T extends EntityComponent> {

    T constructTo(T component);

    default void writeJson(JsonObject json){

    }

    default void readJson(JsonObject json) {

    }

}
