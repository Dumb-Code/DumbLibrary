package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

public interface RenderLocationComponent {
    void editLocations(ConfigurableLocation texture, ConfigurableLocation fileLocation);

    class ConfigurableLocation {

        //Suffix is used for the suffix for the file name. For example, could be a file extension
        private final String suffix;

        @Setter
        @Getter
        private String modid;

        //List of folder names. Sorted by index
        private final ListCache folderNames = new ListCache();

        //List of file names. Sorted by index, then joined with '_'
        private final ListCache fileNames = new ListCache();

        public ConfigurableLocation() {
            this("");
        }

        public ConfigurableLocation(String suffix) {
            this.suffix = suffix;
        }

        public ConfigurableLocation addFolderName(String name, float index) {
            return this.addFolderName(() -> name, index);
        }

        public ConfigurableLocation addFolderName(Supplier<String> name, float index) {
            this.folderNames.names.add(new IndexedObject<>(new LocationCache(name), index));
            this.folderNames.needsRecompiling = true;
            return this;
        }

        public ConfigurableLocation addFileName(String name, float index) {
            return this.addFileName(() -> name, index);
        }

        public ConfigurableLocation addFileName(Supplier<String> name, float index) {
            this.fileNames.names.add(new IndexedObject<>(new LocationCache(name), index));
            this.fileNames.needsRecompiling = true;
            return this;
        }

        public ConfigurableLocation addName(String name, float index) {
            return this.addName(() -> name, index);
        }

        public ConfigurableLocation addName(Supplier<String> name, float index) {
            this.addFolderName(name, index);
            this.addFileName(name, index);
            return this;
        }

        public boolean removeFolder(float index) {
            boolean b = this.folderNames.names.removeIf(i -> i.getIndex() == index);
            if(b) {
                this.folderNames.needsRecompiling = true;
            }
            return b;
        }

        public ResourceLocation getLocation() {
            return new ResourceLocation(this.modid, this.getPath());
        }

        private String gather(ListCache listCache, String delimiter) {
            for (IndexedObject<LocationCache> name : listCache.names) {
                //We need to call `needsRecompiling` on all objects, as it refreshes it's value.
                listCache.needsRecompiling |= name.getObject().needsRecompiling();
            }
            if (listCache.needsRecompiling) {
                listCache.needsRecompiling = false;
                listCache.cache = String.join(delimiter,
                    listCache.names.stream()
                        .map(IndexedObject.mapper(l -> l.getValue().get()))
                        .collect(IndexedObject.sortedList())
                );
            }
            return listCache.cache;
        }

        public String getPath() {
            String joinedFolder = this.gather(this.folderNames, "/") + "/";
            String joinedFile = this.gather(this.fileNames, "_");
            return (this.folderNames.names.isEmpty() ? "" : joinedFolder) + joinedFile + this.suffix;
        }

        public ConfigurableLocation copy() {
            ConfigurableLocation location = new ConfigurableLocation(this.suffix);

            location.modid = this.modid;
            location.folderNames.names.addAll(this.folderNames.names);
            location.folderNames.needsRecompiling = true;
            location.fileNames.names.addAll(this.fileNames.names);
            location.fileNames.needsRecompiling = true;

            return location;
        }

        public void reset() {
            this.modid = "";
            this.folderNames.names.clear();
            this.folderNames.needsRecompiling = true;
            this.fileNames.names.clear();
            this.fileNames.needsRecompiling = true;
        }
    }

    @Data
    class ListCache {
        private final List<IndexedObject<LocationCache>> names = Lists.newArrayList();
        private String cache = "";
        private boolean needsRecompiling;
    }

    @Data
    class LocationCache {
        private final Supplier<String> value;
        private String previous;

        public boolean needsRecompiling() {
            String s = this.value.get();
            if(s.equals(this.previous)) {
                return false;
            }
            this.previous = s;
            return true;
        }
    }
}
