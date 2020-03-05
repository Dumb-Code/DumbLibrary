package net.dumbcode.dumblibrary.client.shader;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderManager;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlslSandboxShader {

    public static final String SHADER_NAME = "glsl_shader";

    private static final Pattern PATTERN = Pattern.compile("https?://glslsandbox\\.com/e#(\\d+.?\\d*)");

    private static final VertexFormat RENDER_FORMAT = new VertexFormat().addElement(DefaultVertexFormats.POSITION_3F);
    private static ShaderManager FLIP_SHADER;

    private final ShaderManager shaderManager;
    private final Framebuffer framebuffer;

    private long timeStarted = -1;

    private int screenWidth = 1;
    private int screenHeight = 1;


    private GlslSandboxShader(IResourceManager resourceManager, String programName) throws IOException {
        this.shaderManager = new ShaderManager(resourceManager, programName);
        this.framebuffer = new Framebuffer(1, 2, false);
    }

    public void init(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.framebuffer.createBindFramebuffer(screenWidth, screenHeight);
    }


    public void render(int relativeMouseX, int relativeMouseY) {
        if(this.timeStarted == -1) {
            this.timeStarted = System.currentTimeMillis();
        }
        this.shaderManager.getShaderUniformOrDefault("time").set((System.currentTimeMillis() - this.timeStarted) / 1000F);
        this.shaderManager.getShaderUniformOrDefault("mouse").set((float)relativeMouseX / this.screenWidth, (float)relativeMouseY / this.screenHeight);
        this.shaderManager.getShaderUniformOrDefault("resolution").set(this.screenWidth, this.screenHeight);
        this.shaderManager.getShaderUniformOrDefault("surfaceSize").set((float) this.screenWidth / this.screenHeight, 1);

        this.shaderManager.useShader();
        this.framebuffer.bindFramebuffer(true);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        buffer.begin(GL11.GL_QUADS, RENDER_FORMAT);

        buffer.pos(-1, -1, 0).endVertex();
        buffer.pos(1, -1, 0).endVertex();
        buffer.pos(1, 1, 0).endVertex();
        buffer.pos(-1, 1, 0).endVertex();

        Tessellator.getInstance().draw();

        this.shaderManager.endShader();
        this.framebuffer.unbindFramebuffer();

        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
    }

    public void startShader() {
        this.framebuffer.bindFramebufferTexture();
        if(FLIP_SHADER == null) {
            try {
                FLIP_SHADER = new ShaderManager(Minecraft.getMinecraft().getResourceManager(), DumbLibrary.MODID + ":flip");
            } catch (IOException e) {
                DumbLibrary.getLogger().error("Unable to load flip shader", e);
            }
        }
        if(FLIP_SHADER != null) {
            FLIP_SHADER.useShader();
            FLIP_SHADER.addSamplerTexture("sampler", this.framebuffer);
        }
    }

    public void endShader() {
        if(FLIP_SHADER != null) {
            FLIP_SHADER.endShader();
        }
        this.framebuffer.unbindFramebufferTexture();
    }

    public void dispose() {
        this.shaderManager.deleteShader();
        this.framebuffer.deleteFramebuffer();
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
