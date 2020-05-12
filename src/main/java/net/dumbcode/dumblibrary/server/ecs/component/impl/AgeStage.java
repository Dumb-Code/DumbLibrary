package net.dumbcode.dumblibrary.server.ecs.component.impl;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AgeStage {

    public static final AgeStage MISSING = new AgeStage("missing", -1, "missing");

    private final String name;
    private final int time;
    private final String modelStage;
    private boolean canBreed = false;
}
