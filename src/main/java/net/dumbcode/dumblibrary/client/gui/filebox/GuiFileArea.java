package net.dumbcode.dumblibrary.client.gui.filebox;

import lombok.Getter;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

import java.io.File;
import java.io.FilenameFilter;

public class GuiFileArea extends Button {

    private final String dropboxTitle;
    private final FilenameFilter filter;

    @Getter
    private File file;


    public GuiFileArea(int p_i232256_1_, int p_i232256_2_, int p_i232256_3_, int p_i232256_4_, ITextComponent p_i232256_5_, IPressable p_i232256_6_, ITooltip p_i232256_7_, String dropboxTitle, FilenameFilter filter) {
        super(p_i232256_1_, p_i232256_2_, p_i232256_3_, p_i232256_4_, p_i232256_5_, p_i232256_6_, p_i232256_7_);
        this.dropboxTitle = dropboxTitle;
        this.filter = filter;
    }
}
