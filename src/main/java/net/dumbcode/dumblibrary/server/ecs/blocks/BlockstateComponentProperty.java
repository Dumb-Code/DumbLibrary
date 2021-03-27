package net.dumbcode.dumblibrary.server.ecs.blocks;

import lombok.Value;
import net.dumbcode.dumblibrary.server.ecs.ComponentMapWriteAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentAttacher;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentMap;
import net.minecraft.state.Property;

import java.util.*;

/**
 * The blockstate property used to hold the {@link net.dumbcode.dumblibrary.server.ecs.ComponentAccess} for the ecs.
 * @author Wyn Price
 */
public class BlockstateComponentProperty extends Property<BlockstateComponentProperty.Entry> {

    private final List<Entry> entries = new ArrayList<>();
    private final Map<String, Entry> stringToEntries = new HashMap<>();

    public BlockstateComponentProperty(String name, EntityComponentAttacher baseAttacher, Map<String, EntityComponentAttacher> stateOverrides) {
        super(name, BlockstateComponentProperty.Entry.class);
        if(stateOverrides.isEmpty()) {
            throw new IllegalArgumentException("Need to have at least one property for a state");
        } else {
            stateOverrides.forEach((componentName, attacher) -> {
                EntityComponentMap map = new EntityComponentMap();
                Entry entry = new Entry(this, componentName, map);

                baseAttacher.getDefaultConfig().attachAll(entry);
                attacher.getDefaultConfig().attachAll(entry);

                this.entries.add(entry);
                this.stringToEntries.put(componentName, entry);
            });
        }

        this.entries.sort(Comparator.comparing(Entry::getName));
    }


    @Override
    public Collection<Entry> getPossibleValues() {
        return this.entries;
    }

    @Override
    public Class<Entry> getValueClass() {
        return Entry.class;
    }

    @Override
    public Optional<Entry> getValue(String value) {
        return Optional.ofNullable(this.stringToEntries.get(value));
    }

    @Override
    public String getName(Entry value) {
        return value.name;
    }


    @Value
    public static class Entry implements ComponentMapWriteAccess, Comparable<Entry> {
        private final BlockstateComponentProperty property;
        private final String name;
        private final EntityComponentMap componentMap;

        @Override
        public int compareTo(Entry o) {
            return this.name.compareTo(o.name);
        }
    }



}
