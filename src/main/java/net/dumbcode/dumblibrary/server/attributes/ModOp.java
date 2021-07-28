package net.dumbcode.dumblibrary.server.attributes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.ai.attributes.AttributeModifier;

@Getter
@RequiredArgsConstructor
public enum ModOp {
    ADD(AttributeModifier.Operation.ADDITION),
    MULTIPLY_BASE_THEN_ADD(AttributeModifier.Operation.MULTIPLY_BASE),
    MULTIPLY(AttributeModifier.Operation.MULTIPLY_TOTAL);

    private final AttributeModifier.Operation vanilla;
}
