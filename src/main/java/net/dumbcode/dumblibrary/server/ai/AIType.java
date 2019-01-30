package net.dumbcode.dumblibrary.server.ai;

import lombok.Getter;

@Getter
public enum AIType {
    METABOLISM(1.3), MOVEMENT(1.1), ANIMATION(0);

    private final double weight;

    AIType(double weight) {
        this.weight = weight;
    }
}