package net.dumbcode.dumblibrary.server.utils;

import net.dumbcode.studio.model.CubeInfo;
import net.dumbcode.studio.model.ModelInfo;
import net.dumbcode.studio.model.RotationOrder;
import net.minecraft.network.PacketBuffer;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public enum DCMBufferHandler implements BiConsumer<PacketBuffer, ModelInfo>, Function<PacketBuffer, ModelInfo> {
    INSTANCE;

    @Override
    public void accept(PacketBuffer buffer, ModelInfo model) {
        buffer.writeUtf(model.getAuthor());
        buffer.writeInt(model.getTextureWidth());
        buffer.writeInt(model.getTextureHeight());
        writeCubes(model.getRoots(), buffer);
    }
    public static void writeCubes(List<CubeInfo> cubes, PacketBuffer buffer) {
        buffer.writeShort(cubes.size());
        for (CubeInfo cube : cubes) {
            buffer.writeUtf(cube.getName());
            writeIntArray(buffer, cube.getDimensions(), 3);
            writeFloatArray(buffer, cube.getRotationPoint(), 3);
            writeFloatArray(buffer, cube.getOffset(), 3);
            writeFloatArray(buffer, cube.getRotation(), 3);
            writeIntArray(buffer, cube.getTextureOffset(), 2);
            buffer.writeBoolean(cube.isTextureMirrored());
            writeFloatArray(buffer, cube.getCubeGrow(), 3);
            writeCubes(cube.getChildren(), buffer);
        }
    }

    private static void writeFloatArray(PacketBuffer buf, float[] arr, int size) {
        for (int i = 0; i < size; i++) {
            buf.writeFloat(arr[i]);
        }
    }
    private static void writeIntArray(PacketBuffer buf, int[] arr, int size) {
        for (int i = 0; i < size; i++) {
            buf.writeInt(arr[i]);
        }
    }


    @Override
    public ModelInfo apply(PacketBuffer buffer) {
        ModelInfo info = new ModelInfo(
            buffer.readUtf(32767),
            buffer.readInt(), buffer.readInt(),
            RotationOrder.ZYX
        );
        readCubes(info, null, buffer, info.getRoots());
        return info;
    }
    private static void readCubes(ModelInfo info, CubeInfo parent, PacketBuffer buf, List<CubeInfo> out) {
        short cubes = buf.readShort();
        for (int i = 0; i < cubes; i++) {
            CubeInfo cube = new CubeInfo(
                info, parent,
                buf.readUtf(32767),
                readIntArray(buf, 3),
                readFloatArray(buf, 3),
                readFloatArray(buf, 3),
                readFloatArray(buf, 3),
                readIntArray(buf, 2),
                buf.readBoolean(),
                readFloatArray(buf, 3)
            );
            readCubes(info, cube, buf, cube.getChildren());
            out.add(cube);
        }
    }
    private static float[] readFloatArray(PacketBuffer buf, int size) {
        float[] out = new float[size];
        for (int i = 0; i < size; i++) {
            out[i] = buf.readFloat();
        }
        return out;
    }
    private static int[] readIntArray(PacketBuffer buf, int size) {
        int[] out = new int[size];
        for (int i = 0; i < size; i++) {
            out[i] = buf.readInt();
        }
        return out;
    }
}
