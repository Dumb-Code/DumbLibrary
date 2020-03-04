package net.dumbcode.dumblibrary.client.shader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.SneakyThrows;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class DummyGLSLResourceManager implements IResourceManager {

    private static final String DUMMY_PACK_NAME = "dumblibrary_glsl_dummy_pack";
    private static final String DUMMY_VERTEX_SHADER = "attribute vec3 position; attribute vec2 surfacePosAttrib; varying vec2 surfacePosition; void main() { surfacePosition = surfacePosAttrib; gl_Position = vec4( position, 1.0 ); }";

    private static final ResourceLocation FAKE_JSON_LOCATION = new ResourceLocation(DumbLibrary.MODID, "shaders/glslshader.json");

    private final ResourceLocation jsonLocation;
    private final ResourceLocation fragmentLocation;
    private final ResourceLocation vertexLocation;

    private final String shaderName;

    private final URL shaderApiUrl;

    public DummyGLSLResourceManager(String shaderName, String shaderApiUrl) throws MalformedURLException {
        this.jsonLocation = new ResourceLocation(DumbLibrary.MODID, "shaders/program/" + shaderName + ".json");
        this.fragmentLocation = new ResourceLocation(DumbLibrary.MODID, "shaders/program/" + shaderName + ".fsh");
        this.vertexLocation = new ResourceLocation(DumbLibrary.MODID, "shaders/program/" + shaderName + ".vsh");

        this.shaderName = shaderName;

        this.shaderApiUrl = new URL(shaderApiUrl);
    }

    @Override
    public Set<String> getResourceDomains() {
        throw new IllegalStateException("Should not be called");
    }

    @Override
    public IResource getResource(ResourceLocation location) throws IOException {
        if(location.equals(this.jsonLocation)) {
            String string = IOUtils.toString(Minecraft.getMinecraft().getResourceManager().getResource(FAKE_JSON_LOCATION).getInputStream(), StandardCharsets.UTF_8);
            return new SimpleResource(DUMMY_PACK_NAME, location, new ByteArrayInputStream(string.replaceAll("\\Q$$shadername\\E", this.shaderName).getBytes()), null, null);
        } else if (location.equals(this.vertexLocation)) {
            return new SimpleResource(DUMMY_PACK_NAME, location, new ByteArrayInputStream(DUMMY_VERTEX_SHADER.getBytes()), null, null);
        } else if(location.equals(this.fragmentLocation)) {
            URLConnection connection = this.shaderApiUrl.openConnection();
            connection.connect();

            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(new InputStreamReader((InputStream) connection.getContent())).getAsJsonObject();
            String code = JsonUtils.getString(root, "code");
            return new SimpleResource(DUMMY_PACK_NAME, location, new ByteArrayInputStream(code.getBytes()), null, null);
        }
        throw new IllegalStateException("Unable to get " + location + " on dummb glsl resource manager");
    }

    @Override
    public List<IResource> getAllResources(ResourceLocation location) {
        throw new IllegalStateException("Should not be called");
    }

}
