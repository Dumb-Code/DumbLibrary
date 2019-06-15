package net.dumbcode.dumblibrary.server.entity.component;

import com.google.gson.JsonObject;

public interface EntityComponentStorage<T extends EntityComponent> {

    T construct();

    default void readJson(JsonObject json) {

    }

    default void writeJson(JsonObject json){

    }

}
