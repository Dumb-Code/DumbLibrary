package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import com.google.common.collect.Lists;
import lombok.Setter;
import lombok.Value;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.minecraft.util.ResourceLocation;

import java.util.Comparator;
import java.util.List;

public interface RenderLocationComponent {
    void editLocations(ConfigurableLocation texture, ConfigurableLocation fileLocation);

    class ConfigurableLocation {

        //Suffix is used for the suffix for the file name. For example, could be a file extension
        private final String suffix;

        @Setter private String modid;

        //List of folder names. Sorted by index
        private final List<IndexedString> folderNames = Lists.newArrayList();

        //List of file names. Sorted by index, then joined with '_'
        private final List<IndexedString> fileNames = Lists.newArrayList();

        public ConfigurableLocation() {
            this("");
        }

        public ConfigurableLocation(String suffix) {
            this.suffix = suffix;
        }

        public void addFolderName(String name, float index) {
            this.folderNames.add(new IndexedString(name, index));
        }

        public void addFileName(String name, float index) {
            this.fileNames.add(new IndexedString(name, index));
        }

        public void addName(String name, float index) {
            this.addFolderName(name, index);
            this.addFileName(name, index);
        }

        public ResourceLocation getLocation() {
            String joinedFolder = String.join("/", () -> this.folderNames.stream()
                    .sorted(Comparator.comparing(IndexedString::getIndex))
                    .map(IndexedString::getStr)
                    .iterator()) + "/";
            String joinedFile = String.join("_", () -> this.fileNames.stream()
                    .sorted(Comparator.comparing(IndexedString::getIndex))
                    .map(IndexedString::getStr)
                    .iterator());
            return new ResourceLocation(this.modid, (this.folderNames.isEmpty() ? "" : joinedFolder) + joinedFile + this.suffix);
        }


        public void reset() {
            this.modid = "";
            this.folderNames.clear();
            this.fileNames.clear();
        }
    }

    @Value
    class IndexedString {
        CharSequence str; float index;
    }
}
