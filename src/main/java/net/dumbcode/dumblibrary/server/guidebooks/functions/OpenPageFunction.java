package net.dumbcode.dumblibrary.server.guidebooks.functions;

import net.dumbcode.dumblibrary.client.gui.GuiGuidebook;
import net.dumbcode.dumblibrary.server.guidebooks.GuidebookPage;

public class OpenPageFunction implements GuidebookFunction {

    private final String target;

    public OpenPageFunction(String pageTarget) {
        this.target = pageTarget;
    }

    @Override
    public void onClick(GuiGuidebook guiGuidebook, int localPageX, int localPageY, int mouseX, int mouseY) {
        GuidebookPage page = guiGuidebook.getBook().getAllPages().get(target);
        System.out.println(">> " + page);
        guiGuidebook.showPage(page);
    }
}
