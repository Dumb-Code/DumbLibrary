package net.dumbcode.dumblibrary.server.tabula;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public enum TabulaBufferHandler implements BiConsumer<ByteBuf, TabulaModelInformation>, Function<ByteBuf, TabulaModelInformation> {
    INSTANCE;

    public TabulaModelInformation deserialize(ByteBuf buf) {
        TabulaModelInformation info = new TabulaModelInformation(
            ByteBufUtils.readUTF8String(buf), //modelName
            ByteBufUtils.readUTF8String(buf), //authorName
            buf.readInt(), //projVersion
            getArrString(buf), //metadata
            buf.readInt(), //textureWidth
            buf.readInt(), //textureHeight
            getArr(buf), //scale
            buf.readInt() //cubeCount
        );

        TabulaModelInformation.CubeGroup rootGroup = info.group("@@ROOT@@", false, false, new String[0], "~~root~~");
        info.getGroups().add(rootGroup.setRoot(true));

        IntStream.range(0, buf.readShort()).forEach(i -> rootGroup.getCubeList().add(parseCube(buf, info))); //cubes
        IntStream.range(0, buf.readShort()).forEach(i -> info.getGroups().add(parseGroup(buf, info))); //cubeGroups

        return info;
    }


    private TabulaModelInformation.CubeGroup parseGroup(ByteBuf buf, TabulaModelInformation info) {
        TabulaModelInformation.CubeGroup group = info.group(
            ByteBufUtils.readUTF8String(buf), //name
            buf.readBoolean(), //txMirror
            buf.readBoolean(), //hidden
            getArrString(buf), //metadata
            ByteBufUtils.readUTF8String(buf) //identifier
        );
        IntStream.range(0, buf.readShort()).forEach(i -> group.getCubeList().add(parseCube(buf, info))); //cubes
        IntStream.range(0, buf.readShort()).forEach(i -> group.getChildGroups().add(parseGroup(buf, info))); //cubeGroups

        return group;
    }

    private TabulaModelInformation.Cube parseCube(ByteBuf buf, TabulaModelInformation info) {
        TabulaModelInformation.Cube cube = info.cube(
            ByteBufUtils.readUTF8String(buf), //name
            getArr(buf), //dimensions
            getArr(buf), //position
            getArr(buf), //offset
            getArrAngles(buf), //rotation
            getArr(buf), //scale
            getArr(buf), //txOffset
            buf.readBoolean(), //txMirror
            buf.readFloat(), //mcScale
            buf.readFloat(), //opacity
            buf.readBoolean(), //hidden
            getArrString(buf), //metadata
            buf.readBoolean() ? ByteBufUtils.readUTF8String(buf) : "null", //parentIdentifier
            ByteBufUtils.readUTF8String(buf) //identifier
        );
        info.getCubes().add(cube);
        IntStream.range(0, buf.readShort()).forEach(i -> {
            TabulaModelInformation.Cube childcube = parseCube(buf, info);
            cube.getChildren().add(childcube);
            childcube.setParent(cube);
        });
        return cube;
    }

    private String[] getArrString(ByteBuf buf) {
        String[] aString = new String[buf.readByte()];
        for (int i = 0; i < aString.length; i++) {
            aString[i] = ByteBufUtils.readUTF8String(buf);
        }
        return aString;
    }

    private float[] getArr(ByteBuf buf) {
        float[] aFloat = new float[buf.readByte()];
        for (int i = 0; i < aFloat.length; i++) {
            aFloat[i] = buf.readFloat();
        }
        return aFloat;
    }

    private float[] getArrAngles(ByteBuf buf) {
        float[] aFloat = new float[buf.readByte()];
        for (int i = 0; i < aFloat.length; i++) {
            aFloat[i] = (float) Math.toRadians(buf.readFloat());
        }
        return aFloat;
    }

    public void serialize(ByteBuf buf, TabulaModelInformation src) {
        ByteBufUtils.writeUTF8String(buf, src.getModelName());
        ByteBufUtils.writeUTF8String(buf, src.getAuthorName());
        buf.writeInt(src.getProjectVersion());
        toArr(buf, src.getMetadata());
        buf.writeInt(src.getTexWidth());
        buf.writeInt(src.getTexHeight());
        toArr(buf, src.getScale());
        buf.writeInt(src.getCubeCount());

        //Todo: move to method
        Predicate<TabulaModelInformation.CubeGroup> isRoot = group -> group.getName().equals("@@ROOT@@") && group.getIdentifier().equals("~~root~~");

        for (TabulaModelInformation.CubeGroup group : src.getGroups()) {
            if(isRoot.test(group)) {
                buf.writeShort(group.getCubeList().size());
                for (TabulaModelInformation.Cube cube : group.getCubeList()) {
                    this.serializeCube(buf, cube);
                }
                break;
            }
        }

        buf.writeShort(src.getGroups().size() - 1);
        for (TabulaModelInformation.CubeGroup group : src.getGroups()) {
            if(!isRoot.test(group)) {
                this.serializeGroup(buf, group);
                break;
            }
        }

    }

    private void serializeCube(ByteBuf buf, TabulaModelInformation.Cube cube) {
        ByteBufUtils.writeUTF8String(buf, cube.getName());
        toArr(buf, cube.getDimension());
        toArr(buf, cube.getRotationPoint());
        toArr(buf, cube.getOffset());
        toArrAngles(buf, cube.getRotation());
        toArr(buf, cube.getScale());
        toArr(buf, cube.getTexOffset());
        buf.writeBoolean(cube.isTextureMirror());
        buf.writeFloat(cube.getMcScale());
        buf.writeFloat(cube.getOpacity());
        buf.writeBoolean(cube.isHidden());
        toArr(buf, cube.getMetadata());
        if(cube.getParent() != null) {
            buf.writeBoolean(true);
            ByteBufUtils.writeUTF8String(buf, cube.getParentIdentifier());
        } else {
            buf.writeBoolean(false);
        }
        ByteBufUtils.writeUTF8String(buf, cube.getIdentifier());

        buf.writeShort(cube.getChildren().size());
        for (TabulaModelInformation.Cube child : cube.getChildren()) {
            this.serializeCube(buf, child);
        }
    }

    private void serializeGroup(ByteBuf buf, TabulaModelInformation.CubeGroup group) {
        ByteBufUtils.writeUTF8String(buf, group.getName());
        buf.writeBoolean(group.isTextureMirror());
        buf.writeBoolean(group.isHidden());
        toArr(buf, group.getMetadata());
        ByteBufUtils.writeUTF8String(buf, group.getIdentifier());

        buf.writeShort(group.getCubeList().size());
        for (TabulaModelInformation.Cube cube : group.getCubeList()) {
            this.serializeCube(buf, cube);
        }

        buf.writeShort(group.getChildGroups().size());
        for (TabulaModelInformation.CubeGroup childGroup : group.getChildGroups()) {
            this.serializeGroup(buf, childGroup);
        }
    }

    private void toArr(ByteBuf buf, float... arr) {
        buf.writeByte(arr.length);
        for (float v : arr) {
            buf.writeFloat(v);
        }
    }

    private void toArrAngles(ByteBuf buf, float... arr) {
        buf.writeByte(arr.length);
        for (float v : arr) {
            buf.writeFloat((float) Math.toDegrees(v));
        }
    }

    private void toArr(ByteBuf buf, String... arr) {
        buf.writeByte(arr.length);
        for (String s : arr) {
            ByteBufUtils.writeUTF8String(buf, s);
        }
    }

    @Override
    public void accept(ByteBuf buf, TabulaModelInformation modelInformation) {
        this.serialize(buf, modelInformation);
    }

    @Override
    public TabulaModelInformation apply(ByteBuf buf) {
        return this.deserialize(buf);
    }
}
