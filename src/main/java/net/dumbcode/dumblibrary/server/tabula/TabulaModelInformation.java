package net.dumbcode.dumblibrary.server.tabula;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.NonFinal;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class TabulaModelInformation {

    String modelName;
    String authorName;
    int projectVersion;
    String[] metadata;
    int texWidth;
    int texHeight;
    float[] scale;
    List<CubeGroup> groups = Lists.newArrayList();
    int cubeCount;

    List<Cube> cubes = Lists.newArrayList();

    public TabulaModel createModel() {
        TabulaModel model = new TabulaModel(this);
        for (CubeGroup group : this.groups) {
            group.addRenderers(model);
        }
        return model;
    }

    public List<Cube> getAllCubes() {
        List<Cube> cube = Lists.newArrayList();
        for (CubeGroup group : this.groups) {
            group.addCubes(cube);
        }
        return cube;
    }

    public List<String> getAllCubeNames() {//todo cache
        return this.getAllCubes().stream().map(Cube::getName).collect(Collectors.toList());
    }

    public CubeGroup group(String name, boolean mirrorTexture, boolean hidden, String[] metadata, String identifier) {
        return new CubeGroup(name, mirrorTexture, hidden, metadata, identifier);
    }

    public Cube cube(String name, float[] dimensions, float[] postion, float[] offset,
                     float[] rotation, float[] scale, float[] texOffset, boolean textureMirror,
                     float mcScale, float opacity, boolean hidden, String[] metadata,
                     String parentIdentifier, String identifier) {

        rotation[0] = (float) Math.toRadians(rotation[0]);
        rotation[1] = (float) Math.toRadians(rotation[1]);
        rotation[2] = (float) Math.toRadians(rotation[2]);

        return new Cube(name, dimensions, postion, offset, rotation, scale, texOffset, true, mcScale, opacity, hidden, metadata, parentIdentifier, identifier);
    }

    @Nullable
    public Cube getCube(String cubeName) {
        for (Cube cube : this.getAllCubes()) {
            if(cube.getName().equals(cubeName)) {
                return cube;
            }
        }
        return null;
    }

    @Value
    @Accessors(chain = true)
    @RequiredArgsConstructor
    public class CubeGroup {
        @NonFinal @Setter private boolean isRoot = true;
        List<Cube> cubeList = Lists.newArrayList();
        List<CubeGroup> childGroups = Lists.newArrayList();
        String name;
        boolean textureMirror;
        boolean hidden;
        String[] metadata;
        String identifier;

        public void addRenderers(TabulaModel base) {
            for (Cube cube : this.cubeList) {
                cube.createRenderer(base, true);
            }
            for (CubeGroup childGroup : this.childGroups) {
                childGroup.addRenderers(base);
            }
        }

        public void addCubes(List<Cube> cubes) {
            for (Cube cube : this.cubeList) {
                cube.addCubes(cubes);
            }
            for (CubeGroup childGroup : this.childGroups) {
                childGroup.addCubes(cubes);
            }
        }

        public TabulaModelInformation getInfo() {
            return TabulaModelInformation.this;
        }
    }

    @Value
    @RequiredArgsConstructor
    public class Cube {
        String name;
        float[] dimension;
        float[] rotationPoint;
        float[] offset;
        float[] rotation;
        float[] scale;
        float[] texOffset;
        boolean textureMirror;
        float mcScale;
        float opacity;
        boolean hidden;
        String[] metadata;
        List<Cube> children = Lists.newArrayList();
        String parentIdentifier;
        String identifier;
        @NonFinal @Nullable @Setter private Cube parent;

        public TabulaModelRenderer createRenderer(TabulaModel base, boolean root) {
            TabulaModelRenderer cube = new TabulaModelRenderer(base, this);
            cube.setTextureOffset((int)this.texOffset[0], (int)this.texOffset[1]);
            cube.mirror = this.textureMirror;
            cube.setRotationPoint(this.rotationPoint[0], this.rotationPoint[1], this.rotationPoint[2]);
            cube.addBox(this.offset[0], this.offset[1], this.offset[2], (int)this.dimension[0], (int)this.dimension[1], (int)this.dimension[2], this.mcScale);
            cube.rotateAngleX = this.rotation[0];
            cube.rotateAngleY = this.rotation[1];
            cube.rotateAngleZ = this.rotation[2];

            for (Cube child : this.children) {
                cube.addChild(child.createRenderer(base, false));
            }
            if(root) {
                base.getRoots().add(cube);
            }
            base.getCubeNameMap().put(this.name, cube);

            cube.reset();
            return cube;
        }

        public void addCubes(List<Cube> cubes) {
            cubes.add(this);
            for (Cube child : this.children) {
                child.addCubes(cubes);
            }
        }

        public TabulaModelInformation getInfo() {
            return TabulaModelInformation.this;
        }
    }
}
