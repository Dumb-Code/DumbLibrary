package net.dumbcode.dumblibrary.server.attributes;

import lombok.Value;

import java.util.UUID;

@Value
public class ModifiableFieldModifier {
    private final UUID uuid;
    private final double value;
}
