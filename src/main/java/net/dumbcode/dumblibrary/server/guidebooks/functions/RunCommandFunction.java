package net.dumbcode.dumblibrary.server.guidebooks.functions;

import net.dumbcode.dumblibrary.client.gui.GuiGuidebook;
import net.dumbcode.dumblibrary.server.guidebooks.GuidebookPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class RunCommandFunction implements GuidebookFunction {

    private final String command;

    public RunCommandFunction(String command) {
        this.command = command;
    }

    @Override
    public void onClick(GuiGuidebook guiGuidebook, int localPageX, int localPageY, int mouseX, int mouseY) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        player.sendChatMessage(command);
    }
}
