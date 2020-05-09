package net.dumbcode.dumblibrary.client.shader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DummyGLSLResourceManager implements IResourceManager {

    private static final String DUMMY_PACK_NAME = "dumblibrary_glsl_dummy_pack";

    private static final Pattern PRECISION_PATTERN = Pattern.compile("(precision .+ .+;)");

    private static final ResourceLocation FAKE_GLSL_FRAGMENT = new ResourceLocation(DumbLibrary.MODID, "shaders/glslshader.fsh");

    private final ResourceLocation jsonLocation;
    private final ResourceLocation fragmentLocation;
    private final ResourceLocation vertexLocation;

    private final String shaderName;

    private final URL shaderApiUrl;

    private String codeCache;

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
            String code = this.getOrDownloadCode();
            //The following is to only include uniforms that are actually used by the shader. Declaring it as `uniform float x` doesn't count.
            //Note, yes this does mean that commands aren't excluded, but considering this is only to prevent minor log spam it's okay
            String string = this.createShaderJson((name, type) -> Pattern.compile("(?<!uniform " + type + " )" + name).matcher(code).find());
            return new SimpleResource(DUMMY_PACK_NAME, location, new ByteArrayInputStream(string.replaceAll("\\Q$$shadername\\E", this.shaderName).getBytes()), null, null);
        } else if (location.equals(this.vertexLocation)) {
            return Minecraft.getMinecraft().getResourceManager().getResource(FAKE_GLSL_FRAGMENT);
        } else if(location.equals(this.fragmentLocation)) {
            return new SimpleResource(DUMMY_PACK_NAME, location, new ByteArrayInputStream(this.getOrDownloadCode().getBytes()), null, null);
        }
        throw new IllegalStateException("Unable to get " + location + " on dummb glsl resource manager");
    }

    private String getOrDownloadCode() throws IOException {
        if(this.codeCache == null) {
            URLConnection connection = this.shaderApiUrl.openConnection();
            connection.connect();

            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(new InputStreamReader((InputStream) connection.getContent())).getAsJsonObject();
            String code = JsonUtils.getString(root, "code");

            //Ensure the precision stuff has a ifdef GL_ES statement
            Matcher matcher = PRECISION_PATTERN.matcher(code);
            StringBuilder joined = new StringBuilder();
            while (matcher.find()) {
                joined.append(matcher.group(0));
                joined.append('\n');
            }

            if(joined.length() > 0) {
                code = "#ifdef GL_ES\n" + joined + "#endif\n" + matcher.replaceAll("").replaceAll("#ifdef GL_ES\\s+#endif", "");
            }
            this.codeCache = code;
        }
        return this.codeCache;
    }

    private String createShaderJson(BiPredicate<String, String> uniformPredicate) {
        JsonObject object = new JsonObject();

        object.addProperty("vertex", "dumblibrary:" + this.shaderName);
        object.addProperty("fragment", "dumblibrary:" + this.shaderName);

        JsonArray attributes = new JsonArray();
        attributes.add("position");
        object.add("attributes", attributes);

        JsonArray uniforms = new JsonArray();

        this.createUniform("time", 1, uniformPredicate, uniforms);
        this.createUniform("mouse", 2, uniformPredicate, uniforms);
        this.createUniform("resolution", 2, uniformPredicate, uniforms);
        this.createUniform("backbuffer", 1, uniformPredicate, uniforms);
        this.createUniform("surfaceSize", 2, (s, s2) -> true, uniforms);

        object.add("uniforms", uniforms);

        return object.toString();
    }

    private void createUniform(String uniform, int count, BiPredicate<String, String> uniformPredicate, JsonArray uniforms) {
        if(!uniformPredicate.test(uniform, count == 1 ? "float" : "vec2")) {
            return;
        }

        JsonObject json = new JsonObject();

        json.addProperty("name", uniform);
        json.addProperty("type", "float");
        json.addProperty("count", count);

        JsonArray arr = new JsonArray();
        for (int i = 0; i < count; i++) {
            arr.add(0);
        }

        json.add("values", arr);

        uniforms.add(json);
    }

    @Override
    public List<IResource> getAllResources(ResourceLocation location) {
        throw new IllegalStateException("Should not be called");
    }

}
