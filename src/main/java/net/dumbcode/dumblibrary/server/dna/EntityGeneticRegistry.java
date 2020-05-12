package net.dumbcode.dumblibrary.server.dna;

import lombok.Value;
import net.dumbcode.dumblibrary.server.attributes.ModOp;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum EntityGeneticRegistry {
    INSTANCE;

    EntityGeneticRegistry() {
        register(EntityBat.class, GeneticTypes.SIZE, new GeneticFieldModifierStorage().setOperation(ModOp.MULTIPLY_BASE_THEN_ADD), -0.25F);
        registerTargetTint(EntityBat.class, 0x1C1912);
    }

    private final Map<Class<? extends Entity>, List<Entry<?>>> entityEntryList = new HashMap<>();

    public <S extends GeneticFactoryStorage> void register(Class<? extends Entity> clazz, GeneticType<S> type, float value) {
        this.register(clazz, type, type.getStorage().get(), value);
    }

    public <S extends GeneticFactoryStorage> void register(Class<? extends Entity> clazz, GeneticType<S> type, S storage, float value) {
        this.entityEntryList.computeIfAbsent(clazz, c -> new ArrayList<>()).add(new Entry<>(type, storage, value));
    }

    public void registerTargetTint(Class<? extends Entity> clazz, int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        this.register(clazz, GeneticTypes.OVERALL_TINT,
            new GeneticTypeOverallTintStorage().setTintType(GeneticTypeOverallTintStorage.TintType.TARGET),
            GeneticUtils.encode3BitColor(r/255F, g/255F, b/255F)
        );
    }


    @Value
    public static class Entry<S extends GeneticFactoryStorage> {
        GeneticType<S> type;
        S storage;
        float value;
    }

}
