package net.dumbcode.dumblibrary.server.guidebooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Data
public class Guidebook extends IForgeRegistryEntry.Impl<Guidebook> {

    public static final Guidebook MISSING = new Guidebook() {
        {
            setRegistryName(DumbLibrary.MODID,"missing");
            setTitleKey("missing");
            setCover(GuidebookPage.MISSING_PAGE);
            setPageWidth(500);
        }
    };

    @Getter
    @Expose(deserialize = false, serialize = false)
    private final ModContainer modContainer;

    @Getter
    @Setter
    private String titleKey;

    @Getter
    @Setter
    private GuidebookPage cover;

    @Getter
    @Setter
    private int pageWidth;

    @Getter
    @Setter
    private int pageMargins;

    @Getter(lazy = true)
    @Expose(deserialize = false, serialize = false)
    private final Map<String, GuidebookPage> allPages = createPageList();

    @Getter(lazy = true)
    @Expose(deserialize = false, serialize = false)
    private final Map<String, GuidebookChapter> chapters = createChapterList();

    @Getter
    @Expose(deserialize = false, serialize = false)
    private final List<GuidebookPage> compiledPages = new LinkedList<>();

    public Guidebook() {
        modContainer = Loader.instance().activeModContainer();
    }

    public String getInternalID() {
        return getRegistryName().getResourcePath();
    }

    private Map<String, GuidebookPage> createPageList() {
        return createMap(GuidebookPage.class, "pages");
    }

    private Map<String, GuidebookChapter> createChapterList() {
        return createMap(GuidebookChapter.class, "chapters");
    }

    private <T> Map<String, T> createMap(Class<T> contentType, String pathSuffix) {
        Map<String, T> map = new HashMap<>();
        Gson gson = GuidebooksManager.prepareGsonBuilderForGuidebooks(new GsonBuilder()).create();
        CraftingHelper.findFiles(modContainer, "assets/" + modContainer.getModId() + "/" + DumbLibrary.MODID + "/guidebook_contents/" + getInternalID()+"_"+pathSuffix,
                null,
                (root, file) -> {
                    if (!"json".equals(FilenameUtils.getExtension(file.toString()))) {
                        return true;
                    }
                    try(BufferedReader reader = Files.newBufferedReader(file)) {
                        String relative = root.relativize(file).toString();
                        String key = FilenameUtils.removeExtension(relative).replaceAll("\\\\", "/");
                        T value = JsonUtils.fromJson(gson, reader, contentType);
                        if (value == null) {
                            return false;
                        } else {
                            map.put(key, value);
                        }
                    } catch (JsonParseException e) {
                        DumbLibrary.getLogger().error("Parsing error loading json in " + file.getFileName().toString(), e);
                        return false;
                    } catch (IOException e) {
                        DumbLibrary.getLogger().error("Couldn't read from " + file, e);
                        return false;
                    }
                    return true;
                }, true, true);
        return map;
    }

    public int getAvailableWidth() {
        return pageWidth-pageMargins*2;
    }

    public float getAspectRatio() {
        return 5f/8f; // corresponds to the aspect ratio of page in the book model from vanilla
    }

    public int getAvailableHeight() {
        return (int) (getPageWidth() / getAspectRatio());
    }

    /**
     * Returns a flat list of all pages inside the book (cover included)
     * @return
     */
    public List<GuidebookPage> compilePages() {
        compiledPages.clear();
        compiledPages.add(cover);
        compiledPages.add(new GuidebookPage()); // blank page
        // TODO: Add summary here if requested in book

        List<GuidebookChapter> sortedChapters = new LinkedList<>(getChapters().values());
        sortedChapters.sort(Comparator.comparingInt(GuidebookChapter::getChapterIndex));
        sortedChapters.forEach(chapter -> {
            chapter.getPages().forEach(pageID -> {
                GuidebookPage page = getAllPages().get(pageID);
                if(page == null) {
                    page = GuidebookPage.MISSING_PAGE;
                }
                compiledPages.add(page);
            });
        });
        return compiledPages;
    }

    public int hashCode() {
        return modContainer.hashCode();
    }

    public void recompile() {
        compilePages();
        cover.recompile(this);
        for(GuidebookPage page : compiledPages) {
            page.recompile(this);
        }
    }
}
