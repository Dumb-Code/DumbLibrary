package net.dumbcode.dumblibrary.client;

import lombok.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;

import java.util.HashMap;
import java.util.Map;

public class FramebufferCache {
    private static final Map<Size, Framebuffer> CACHE = new HashMap<>();

    public static Framebuffer getFrameBuffer(int width, int height) {
        return CACHE.computeIfAbsent(new Size(width, height), s -> new Framebuffer(width, height, true, Minecraft.ON_OSX));
    }

    @Value
    private static class Size {
        int width;
        int height;
    }
}
