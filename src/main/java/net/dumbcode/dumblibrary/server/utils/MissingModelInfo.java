package net.dumbcode.dumblibrary.server.utils;

import net.dumbcode.studio.model.ModelInfo;
import net.dumbcode.studio.model.RotationOrder;

public class MissingModelInfo {
    public static final ModelInfo MISSING = new ModelInfo(-1, "???", 64, 64, RotationOrder.ZYX);
}
