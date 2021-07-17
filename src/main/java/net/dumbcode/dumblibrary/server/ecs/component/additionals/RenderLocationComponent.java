package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface RenderLocationComponent {
    void editLocations(ConfigurableLocation texture, ConfigurableLocation fileLocation);

    class ConfigurableLocation {

        //Suffix is used for the suffix for the file name. For example, could be a file extension
        private final String suffix;

        @Setter
        @Getter
        private String modid;

        //List of folder names. Sorted by index
        private final List<IndexedObject<CharSequence>> folderNames = Lists.newArrayList();

        //List of file names. Sorted by index, then joined with '_'
        private final List<IndexedObject<CharSequence>> fileNames = Lists.newArrayList();

        public ConfigurableLocation() {
            this("");
        }

        public ConfigurableLocation(String suffix) {
            this.suffix = suffix;
        }

        public ConfigurableLocation addFolderName(String name, float index) {
            this.folderNames.add(new IndexedObject<>(name, index));
            return this;
        }

        public ConfigurableLocation addFileName(String name, float index) {
            this.fileNames.add(new IndexedObject<>(name, index));
            return this;
        }

        public ConfigurableLocation addName(String name, float index) {
            this.addFolderName(name, index);
            this.addFileName(name, index);
            return this;
        }

        public boolean removeFolder(float index) {
            return this.folderNames.removeIf(i -> i.getIndex() == index);
        }

        public ResourceLocation getLocation() {
            return new ResourceLocation(this.modid, this.getPath());
        }

        public String getPath() {
            String joinedFolder = String.join("/", IndexedObject.sortIndex(this.folderNames)) + "/";
            String joinedFile = String.join("_", IndexedObject.sortIndex(this.fileNames));
            return (this.folderNames.isEmpty() ? "" : joinedFolder) + joinedFile + this.suffix;
        }

        public ConfigurableLocation copy() {
            ConfigurableLocation location = new ConfigurableLocation(this.suffix);

            location.modid = this.modid;
            location.folderNames.addAll(this.folderNames);
            location.fileNames.addAll(this.fileNames);

            return location;
        }

        public void reset() {
            this.modid = "";
            this.folderNames.clear();
            this.fileNames.clear();
        }
    }
}
