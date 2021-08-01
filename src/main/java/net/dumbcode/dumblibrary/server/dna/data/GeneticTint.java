package net.dumbcode.dumblibrary.server.dna.data;

import lombok.Value;
import lombok.With;

@Value
public class GeneticTint {
    Part primary;
    Part secondary;

    @Value
    @With
    public static class Part {
        float r, g, b, a;
        int importance;
        public float[] asArray() {
            return new float[] { r, g, b, a } ;
        }
    }
}
