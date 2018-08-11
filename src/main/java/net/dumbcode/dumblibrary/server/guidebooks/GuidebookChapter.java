package net.dumbcode.dumblibrary.server.guidebooks;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class GuidebookChapter {

    @Getter
    @Setter
    private int chapterIndex;

    @Getter
    @Setter
    private String chapterUnlocalizedName;

    @Getter
    @Setter
    private List<String> pages;
}
