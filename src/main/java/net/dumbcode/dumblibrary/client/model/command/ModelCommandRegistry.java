package net.dumbcode.dumblibrary.client.model.command;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.Data;
import lombok.Getter;
import net.dumbcode.studio.model.RotationOrder;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.vector.Quaternion;
import org.joml.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The registry for the model commands
 *
 * @author Wyn Price
 * @see ModelCommandLoader
 */
public class ModelCommandRegistry {
    private static final Map<String, Function<JsonObject, Command>> commandMap = Maps.newHashMap();

    public static void register(String commandName, Function<JsonObject, Command> command) {
        if (commandMap.containsKey(commandName)) {
            throw new IllegalArgumentException("Command " + commandName + " is already registered. Current: " + commandMap.get(commandName).getClass().getSimpleName() + " New:" + command.getClass().getSimpleName());
        }
        commandMap.put(commandName, command);
    }

    public static Command get(String commandName, JsonObject object) {
        if (!commandMap.containsKey(commandName)) {
            throw new IllegalArgumentException("No command found: " + commandName);
        }
        return commandMap.get(commandName).apply(object);
    }

    public interface Command {
        void apply(BakedQuad quad, CommandDerived.ExpectedVariableResolver resolver);
    }

    public interface TransformCommand extends Command {
        default void apply(BakedQuad quad, CommandDerived.ExpectedVariableResolver resolver) {
            int[] vertices = quad.getVertices();

            GuiGraphics stack = new GuiGraphics();
            this.applyMatrix(stack, resolver);

            int size = DefaultVertexFormats.BLOCK.getIntegerSize();
            for (int v = 0; v < 4; v++) {
                Vector4f vec = new Vector4f(
                    Float.intBitsToFloat(vertices[v*size]),
                    Float.intBitsToFloat(vertices[v*size+1]),
                    Float.intBitsToFloat(vertices[v*size+2]),
                    1F
                );
                vec.transform(stack.last().pose());
                vertices[v*size] = Float.floatToRawIntBits(vec.x());
                vertices[v*size+1] = Float.floatToRawIntBits(vec.y());
                vertices[v*size+2] = Float.floatToRawIntBits(vec.z());
            }
        }
        void applyMatrix(GuiGraphics stack, CommandDerived.ExpectedVariableResolver valueGetter);
    }

    @Getter
    public static class VariableType<T> {
        private static final VariableType<String> STRING = new VariableType<>(String.class, JsonPrimitive::isString, JsonPrimitive::getAsString);
        private static final VariableType<Number> NUMBER = new VariableType<>(Number.class, JsonPrimitive::isNumber, JsonPrimitive::getAsNumber);
        private static final VariableType<Boolean> BOOLEAN = new VariableType<>(Boolean.class, JsonPrimitive::isBoolean, JsonPrimitive::getAsBoolean);

        private final Predicate<JsonPrimitive> predicate;
        private final Function<JsonPrimitive, T> function;
        private final Class<T> clazz;

        private VariableType(Class<T> clazz, Predicate<JsonPrimitive> predicate, Function<JsonPrimitive, T> function) {
            this.clazz = clazz;
            this.predicate = predicate;
            this.function = function;
        }
    }

    @Data
    public static class ExpectedVariable<T> {
        private final VariableType<T> type;
        private final String variableName;
        private final T foundValue;
        public static <T> ExpectedVariable<T> of(JsonElement element, VariableType<T> type) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            String variable = null;
            if(primitive.isString()) {
                String asString = primitive.getAsString();
                if(type != VariableType.STRING) {
                    variable = asString;
                } else if(asString.startsWith("$")) {
                    variable = asString.substring(1);
                }
            }

            T foundValue = null;
            if(variable == null) {
                foundValue = type.getFunction().apply(primitive);
            }
            return new ExpectedVariable<>(type, variable, foundValue);
        }

        public static <T> ExpectedVariable<T> ofDirect(T foundValue, VariableType<T> type) {
            return new ExpectedVariable<>(type, null, foundValue);
        }
    }

    private static class TranslateCommand implements TransformCommand {

        private final ExpectedVariable<Number> x, y, z;

        public TranslateCommand(JsonObject json) {
            JsonArray amount = JSONUtils.getAsJsonArray(json, "amount");
            this.x = ExpectedVariable.of(amount.get(0), VariableType.NUMBER);
            this.y = ExpectedVariable.of(amount.get(1), VariableType.NUMBER);
            this.z = ExpectedVariable.of(amount.get(2), VariableType.NUMBER);
        }


        @Override
        public void applyMatrix(GuiGraphics stack, CommandDerived.ExpectedVariableResolver resolver) {
            stack.translate(
                resolver.get(this.x).doubleValue(),
                resolver.get(this.y).doubleValue(),
                resolver.get(this.z).doubleValue()
            );
        }
    }

    private static class RotateCommand implements TransformCommand {

        private final RotationOrder order;
        private final boolean degrees;
        private final ExpectedVariable<Number> angleX, angleY, angleZ;
        private final ExpectedVariable<Number> originX, originY, originZ;

        public RotateCommand(JsonObject json) {
            String key;
            if(json.has("degrees")) {
                key = "degrees";
                this.degrees = true;
            } else if(json.has("radians")) {
                key = "radians";
                this.degrees = false;
            } else {
                throw new IllegalArgumentException("Json Needs to have degrees or radians");
            }

            RotationOrder order = RotationOrder.ZYX;
            if(json.has("rotation_order")) {
                String orderStr = JSONUtils.getAsString(json, "rotation_order");
                for (RotationOrder value : RotationOrder.values()) {
                    if(value.name().equalsIgnoreCase(orderStr)) {
                        order = value;
                        break;
                    }
                }
            }
            this.order = order;

            JsonArray amount = JSONUtils.getAsJsonArray(json, key);
            this.angleX = ExpectedVariable.of(amount.get(0), VariableType.NUMBER);
            this.angleY = ExpectedVariable.of(amount.get(1), VariableType.NUMBER);
            this.angleZ = ExpectedVariable.of(amount.get(2), VariableType.NUMBER);

            if(json.has("origin")) {
                JsonArray origin = JSONUtils.getAsJsonArray(json, "origin");
                this.originX = ExpectedVariable.of(origin.get(0), VariableType.NUMBER);
                this.originY = ExpectedVariable.of(origin.get(0), VariableType.NUMBER);
                this.originZ = ExpectedVariable.of(origin.get(0), VariableType.NUMBER);
            } else {
                this.originX = ExpectedVariable.ofDirect(8, VariableType.NUMBER);
                this.originY = ExpectedVariable.ofDirect(8, VariableType.NUMBER);
                this.originZ = ExpectedVariable.ofDirect(8, VariableType.NUMBER);
            }

        }


        @Override
        public void applyMatrix(GuiGraphics stack, CommandDerived.ExpectedVariableResolver resolver) {
            stack.translate(
                -resolver.get(this.originX).doubleValue()/16,
                -resolver.get(this.originY).doubleValue()/16,
                -resolver.get(this.originZ).doubleValue()/16
            );

            stack.mulPose(this.getQuaternion(this.order.getFirst(), resolver));
            stack.mulPose(this.getQuaternion(this.order.getSecond(), resolver));
            stack.mulPose(this.getQuaternion(this.order.getThird(), resolver));

            stack.translate(
                resolver.get(this.originX).doubleValue()/16,
                resolver.get(this.originY).doubleValue()/16,
                resolver.get(this.originZ).doubleValue()/16
            );
        }

        private Quaternion getQuaternion(int value, CommandDerived.ExpectedVariableResolver resolver) {
            float mul = this.degrees ? (float) (Math.PI / 180D) : 1F;
            switch (value) {
                default: case 0: return Vector3f.XP.rotation(resolver.get(this.angleX, 0).floatValue());
                case 1: return Vector3f.YP.rotation(resolver.get(this.angleY, 0).floatValue());
                case 2: return Vector3f.ZP.rotation(resolver.get(this.angleZ, 0).floatValue());
            }
        }
    }



//    static {
//
//        //Move to function ?
//        Function<IBakedModel, Iterable<BakedQuad>> modelToQuads = model -> {
//            Set<BakedQuad> quadSet = Sets.newLinkedHashSet(model.getQuads(Blocks.STONE.getDefaultState(), null, 0L));
//            for (EnumFacing facing : EnumFacing.values()) {
//                quadSet.addAll(model.getQuads(Blocks.STONE.getDefaultState(), facing, 0L));
//            }
//            return quadSet;
//        };
//
//        //The pattern used for all of the current commands. Not required.
//        Pattern norPat = Pattern.compile("(\\d*\\.?\\d*),(\\d*\\.?\\d*),(\\d*\\.?\\d*)@(\\d*\\.?\\d*),(\\d*\\.?\\d*),(\\d*\\.?\\d*)");
//
//        //Rotate around by an amount around an axis
//        register("rotate", (model, args) -> {
//            Matcher matcher = norPat.matcher(args);
//            if (!matcher.find()) {
//                throw new IllegalArgumentException("Could not find match");
//            }
//
//            double[] ds = new double[6];
//            for (int i = 0; i < 6; i++) {
//                ds[i] = Double.valueOf(matcher.group(i + 1));
//            }
//
//            Matrix4d mat = new Matrix4d();
//            mat.setIdentity();
//
//            Matrix4d m = new Matrix4d();
//
//            m.setIdentity();
//            m.rotZ(Math.toRadians(ds[2]));
//            mat.mul(m);
//
//            m.setIdentity();
//            m.rotY(Math.toRadians(ds[1]));
//            mat.mul(m);
//
//            m.setIdentity();
//            m.rotX(Math.toRadians(ds[0]));
//            mat.mul(m);
//
//
//            for (BakedQuad quad : modelToQuads.apply(model)) {
//                for (int v = 0; v < 4; v++) {
//                    if (quad instanceof UnpackedBakedQuad) {
//                        float[][][] datum = ReflectionHelper.getPrivateValue(UnpackedBakedQuad.class, (UnpackedBakedQuad) quad, "unpackedData");//todo: at
//
//                        Point3d pos = new Point3d(new Point3f(datum[v][0]));
//                        pos.sub(new Point3d(ds[3], ds[4], ds[5]));
//                        mat.transform(pos);
//                        datum[v][0][0] = (float) (pos.x + ds[3]);
//                        datum[v][0][1] = (float) (pos.y + ds[4]);
//                        datum[v][0][2] = (float) (pos.z + ds[5]);
//                    } else {
//                        int[] data = quad.getVertexData();
//
//                        int o = quad.getFormat().getIntegerSize() * v;
//                        Point3d pos = new Point3d(Float.intBitsToFloat(data[o]) - ds[3], Float.intBitsToFloat(data[o + 1]) - ds[4], Float.intBitsToFloat(data[o + 2]) - ds[5]);
//                        data[o] = Float.floatToRawIntBits((float) (pos.x + ds[3]));
//                        data[o + 1] = Float.floatToRawIntBits((float) (pos.y + ds[4]));
//                        data[o + 2] = Float.floatToRawIntBits((float) (pos.z + ds[5]));
//                    }
//                }
//            }
//
//
//        });
//
//        //Scale around a point
//        register("scale", (model, args) -> {
//            Matcher matcher = norPat.matcher(args);
//            if (!matcher.find()) {
//                throw new IllegalArgumentException("Could not find match");
//            }
//            double[] ds = new double[6];//((x-8)*s)+8
//            for (int i = 0; i < 6; i++) {
//                ds[i] = Double.valueOf(matcher.group(i + 1));
//            }
//            for (BakedQuad quad : modelToQuads.apply(model)) {
//                for (int v = 0; v < 4; v++) {
//                    if (quad instanceof UnpackedBakedQuad) {
//                        float[][][] datum = ReflectionHelper.getPrivateValue(UnpackedBakedQuad.class, (UnpackedBakedQuad) quad, "unpackedData");//todo: at
//
//                        datum[v][0][0] = (float) ((datum[v][0][0] - ds[3]) * ds[0] + ds[3]);
//                        datum[v][0][1] = (float) ((datum[v][0][1] - ds[4]) * ds[1] + ds[4]);
//                        datum[v][0][2] = (float) ((datum[v][0][2] - ds[5]) * ds[2] + ds[5]);
//                    } else {
//                        int[] data = quad.getVertexData();
//                        int o = quad.getFormat().getIntegerSize() * v;
//
//                        float x = (float) ((Float.intBitsToFloat(data[o]) - ds[3]) * ds[0] + ds[3]);
//                        float y = (float) ((Float.intBitsToFloat(data[o + 1]) - ds[4]) * ds[1] + ds[4]);
//                        float z = (float) ((Float.intBitsToFloat(data[o + 2]) - ds[5]) * ds[2] + ds[5]);
//
//                        data[o] = Float.floatToRawIntBits(x);
//                        data[o + 1] = Float.floatToRawIntBits(y);
//                        data[o + 2] = Float.floatToRawIntBits(z);
//                    }
//                }
//            }
//            for (BakedQuad quad : modelToQuads.apply(model)) {
//
//                int[] data = quad.getVertexData();
//                float[][][] datum = null;
//                if (quad instanceof UnpackedBakedQuad) {
//                    datum = ReflectionHelper.getPrivateValue(UnpackedBakedQuad.class, (UnpackedBakedQuad) quad, "unpackedData");
//                }
//
//                for (int v = 0; v < 4; v++) {
//                    int o = data.length / 4 * v;
//                    float x = (float) ((Float.intBitsToFloat(data[o]) - ds[3]) * ds[0] + ds[3]);
//                    float y = (float) ((Float.intBitsToFloat(data[o + 1]) - ds[4]) * ds[1] + ds[4]);
//                    float z = (float) ((Float.intBitsToFloat(data[o + 2]) - ds[5]) * ds[2] + ds[5]);
//                    data[o] = Float.floatToRawIntBits(x);
//                    data[o + 1] = Float.floatToRawIntBits(y);
//                    data[o + 2] = Float.floatToRawIntBits(z);
//
//                    if (datum != null) {
//                        datum[v][0][0] = x;
//                        datum[v][0][1] = y;
//                        datum[v][0][2] = z;
//                    }
//                }
//            }
//
//        });
//    }
//
}
