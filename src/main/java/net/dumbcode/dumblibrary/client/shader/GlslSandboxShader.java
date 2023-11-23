package net.dumbcode.dumblibrary.client.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderInstance;
import net.minecraft.resources.IResourceManager;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class GlslSandboxShader {

    public static final String SHADER_NAME = "glsl_shader";

    private static final Pattern PATTERN = Pattern.compile("https?://glslsandbox\\.com/e#(\\d+.?\\d*)");

    private static final VertexFormat RENDER_FORMAT = DefaultVertexFormats.POSITION;
    private static ShaderInstance FLIP_SHADER;

    private final ShaderInstance shaderManager;
    private final Framebuffer framebuffer;

    private long timeStarted = -1;

    private int screenWidth = 1;
    private int screenHeight = 1;

    private GlslSandboxShader(IResourceManager resourceManager, String programName) throws IOException {
        this.shaderManager = new ShaderInstance(resourceManager, programName);
        this.framebuffer = new Framebuffer(1, 2, false, Minecraft.ON_OSX);
    }

    public void init(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.framebuffer.createBuffers(screenWidth, screenHeight, Minecraft.ON_OSX);
    }

    public void render(int relativeMouseX, int relativeMouseY) {
        if(this.timeStarted == -1) {
            this.timeStarted = System.currentTimeMillis();
        }
        this.shaderManager.safeGetUniform("time").set((System.currentTimeMillis() - this.timeStarted) / 1000F);
        this.shaderManager.safeGetUniform("mouse").set((float)relativeMouseX / this.screenWidth, 1F - (float)relativeMouseY / this.screenHeight);
        this.shaderManager.safeGetUniform("resolution").set(this.screenWidth, this.screenHeight);
        this.shaderManager.safeGetUniform("surfaceSize").set((float) this.screenWidth / this.screenHeight, 1);

        this.shaderManager.apply();
        this.framebuffer.bindWrite(Minecraft.ON_OSX);

        RenderSystem.clearColor(0, 0, 0, 0);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        buffer.begin(GL11.GL_QUADS, RENDER_FORMAT);

        buffer.vertex(-1, -1, 0).endVertex();
        buffer.vertex(1, -1, 0).endVertex();
        buffer.vertex(1, 1, 0).endVertex();
        buffer.vertex(-1, 1, 0).endVertex();

        Tesselator.getInstance().end();

        this.shaderManager.clear();
        this.framebuffer.unbindWrite();

        Minecraft.getInstance().getMainRenderTarget().bindWrite(Minecraft.ON_OSX);
    }

    public void startShader() {
        this.framebuffer.bindRead();
        if(FLIP_SHADER == null) {
            try {
                FLIP_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), DumbLibrary.MODID + ":flip");
            } catch (IOException e) {
                DumbLibrary.getLogger().error("Unable to load flip shader", e);
            }
        }
        if(FLIP_SHADER != null) {
            FLIP_SHADER.setSampler("sampler", this.framebuffer::getColorTextureId);
            FLIP_SHADER.apply();
        }
    }

    public void endShader() {
        if(FLIP_SHADER != null) {
            FLIP_SHADER.clear();
        }
        this.framebuffer.unbindRead();
    }

    public void dispose() {
        this.shaderManager.close();
        this.framebuffer.destroyBuffers();
    }

    public static GlslSandboxShader createShader(String url) {
        Matcher matcher = PATTERN.matcher(url);
        if(matcher.find()) {
            String id = matcher.group(1);
            String name = SHADER_NAME + "_" + id;
            try {
                return new GlslSandboxShader(new DummyGLSLResourceManager(name, "http://glslsandbox.com/item/" + id), DumbLibrary.MODID + ":" + name);
            } catch (IOException e) {
                DumbLibrary.getLogger().error("Unable to create glsl shader", e);
            }
        }
        return null;
    }
}
