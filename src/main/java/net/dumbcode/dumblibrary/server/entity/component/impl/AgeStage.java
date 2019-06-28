package net.dumbcode.dumblibrary.server.entity.component.impl;

import lombok.Value;

@Value
public class AgeStage {

    public static final AgeStage MISSING = new AgeStage("missing", -1);

    private final String name;
    private final int time;
}
