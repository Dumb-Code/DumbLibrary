package net.dumbcode.dumblibrary.server.ecs.component.impl;

import lombok.Value;

@Value
public class AgeStage {

    public static final AgeStage MISSING = new AgeStage("missing", -1, "missing");

    private final String name;
    private final int time;
    private final String modelStage;
}
