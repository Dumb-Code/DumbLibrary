package net.dumbcode.dumblibrary.server.dna;

import lombok.Value;
import net.dumbcode.dumblibrary.server.attributes.ModOp;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.minecraft.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum EntityGeneticRegistry {
    INSTANCE;

    private final Map<EntityType<?>, List<Entry<?>>> entityEntryList = new HashMap<>();

    EntityGeneticRegistry() {
        register(EntityType.BAT, GeneticTypes.SIZE.get(), new GeneticFieldModifierStorage().setOperation(ModOp.MULTIPLY_BASE_THEN_ADD), -0.25F);
        registerTargetTint(EntityType.BAT, 0x1C1912);
    }

    public <S extends GeneticFactoryStorage> void register(EntityType<?> entityType, GeneticType<S> type, float value) {
        this.register(entityType, type, type.getStorage().get(), value);
    }

    public <S extends GeneticFactoryStorage> void register(EntityType<?> entityType, GeneticType<S> type, S storage, float value) {
        this.entityEntryList.computeIfAbsent(entityType, c -> new ArrayList<>()).add(new Entry<>(type, storage, value));
    }

    public void registerTargetTint(EntityType<?> entityType, int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        this.register(entityType, GeneticTypes.OVERALL_TINT.get(),
            new GeneticTypeOverallTintStorage().setTintType(GeneticTypeOverallTintStorage.TintType.TARGET),
            GeneticUtils.encodeFloatColor(r/255F, g/255F, b/255F, 1F)
        );
    }


    @Value
    public static class Entry<S extends GeneticFactoryStorage> {
        GeneticType<S> type;
        S storage;
        float value;
    }

}
