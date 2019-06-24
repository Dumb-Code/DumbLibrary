package net.dumbcode.dumblibrary.client.component;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Value;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Comparator;
import java.util.List;

@Getter
@SideOnly(Side.CLIENT)
public class RenderComponentContext {

    private final List<Callback> preRenderCallbacks = Lists.newArrayList();
    private final List<Callback> renderCallbacks = Lists.newArrayList();
    private final List<Callback> postRenderCallback = Lists.newArrayList();

    //TODO: This is kinda strict configuration stuff. No ideas how to really expand it.
    // Maybe an array of ConfigurableLocation, with each index being a specif thing,
    // Like index 0 is texture, index 1 is file location ect ect.
    private final ConfigurableLocation texture = new ConfigurableLocation();
    private final ConfigurableLocation fileLocation = new ConfigurableLocation(); //Optional


    public interface Callback {
        void invoke(Entity entity, double x, double y, double z, float entityYaw, float partialTicks);
    }


    public static class ConfigurableLocation {

        private String modid;

        //List of folder names. Sorted by index
        private final List<IndexedString> folderNames = Lists.newArrayList();

        //List of file names. Sorted by index, then joined with '_'
        private final List<IndexedString> fileNames = Lists.newArrayList();

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
                    .sorted(Comparator.comparing(IndexedString::getIndex).reversed())
                    .map(IndexedString::getStr)
                    .iterator());
            return new ResourceLocation(this.modid, (this.folderNames.isEmpty() ? "" : joinedFolder) + joinedFile);
        }


    }
    @Value
    private static class IndexedString {
        CharSequence str; float index;
    }

}
