package net.dumbcode.dumblibrary.server.utils;

import com.google.common.collect.Maps;
import lombok.Value;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

@UtilityClass
public class MathUtils {

    private static final Random RANDOM = new Random();

    public static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    public static int getWeightedResult(double mean, double sd) {
        return (int) Math.abs(RANDOM.nextGaussian() * sd + mean);
    }

    //https://www.desmos.com/calculator/vczrdbi01s
    public static float bounce(float min, float max, float value) {
        float range = max - min;
        return max - Math.abs(mod(value, 2*range) - range);
    }
    public static float mod(float x, float k) {
        return ((x % k) + k) % k; //Respect the sign -> https://stackoverflow.com/a/4403631
    }

    public static double bounce(double min, double max, double value) {
        double range = max - min;
        return max - Math.abs(mod(value, 2*range) - range);
    }
    public static double mod(double x, double k) {
        return ((x % k) + k) % k; //Respect the sign -> https://stackoverflow.com/a/4403631
    }

    public static String ensureTrailingZeros(double num, int dp) {
        StringBuilder number = new StringBuilder(String.valueOf(num));
        int point = number.lastIndexOf(".");
        int zeros = point != -1 ? dp - (number.length()-point) : dp;
        if(point == -1) {
            point = number.length();
            number.append(".");
        }
        for (int i = 0; i <= zeros; i++) {
            number.append("0");
        }
        return number.toString().substring(0, point + dp + 1);

    }

    public static int floorToZero(double value) {
        return value > 0 ? MathHelper.floor(value) : MathHelper.ceil(value);
    }

    public static int ceilAwayZero(double value) {
        return value > 0 ? MathHelper.ceil(value) : MathHelper.floor(value);
    }

    public static int[] binChoose(int n) {
        n += 1;
        int[][] cache = new int[n + 1][n];

        for (int i = 0; i <= n; i++) {
            for (int j = 0; j < Math.min(i, n); j++) {
                if (j == 0 || j == i) {
                    cache[i][j] = 1;
                } else {
                    cache[i][j] = cache[i - 1][j - 1] + cache[i - 1][j];
                }
            }
        }

        return cache[n];
    }

    public static boolean inBetween(double test, double min, double max) {
        return test >= min && test <= max;
    }

    public static double binomialExp(double a, double b, int pow) {
        return binomialExp(a, b, binChoose(pow));
    }

    public static double binomialExp(double a, double b, int[] n) {
        double total = 0;
        for (int i = 0; i < n.length; i++) {
            total += n[i] * Math.pow(a, i) * Math.pow(b, n.length - i - 1D);
        }
        return total;
    }

    public static double mean(double... data) {
        double total = 0;
        for (double datum : data) {
            total += datum;
        }
        return total / data.length;
    }

    public static double meanDeviation(double... data) {
        double mean = mean(data);

        double divationTotal = 0;
        for (double datum : data) {
            divationTotal += Math.abs(datum - mean);
        }
        return divationTotal / data.length;
    }

    public static double meanDeviation(Collection<? extends Number> list) {
        return meanDeviation(list.stream().mapToDouble(Number::doubleValue).toArray());
    }

    private static final Map<TriVec, Vector3d> NORMAL_CACHE = Maps.newHashMap();

    /**
     * Calculate the normal of the given data
     *
     * @param data in the format [x1, y1, z1, x2, y2, z2, x3, y3, z3]
     * @return the Vec3d normal
     */
    public static Vector3d calculateNormal(double... data) {
        Vector3d pos1 = new Vector3d(data[0], data[1], data[2]);
        Vector3d pos2 = new Vector3d(data[3], data[4], data[5]);
        Vector3d pos3 = new Vector3d(data[6], data[7], data[8]);
        return NORMAL_CACHE.computeIfAbsent(new TriVec(pos1, pos2, pos3), v -> pos2.subtract(pos1).cross(pos3.subtract(pos1)).normalize());
    }

    public static Vector3f calculateNormalF(double... data) {
        Vector3d vec = calculateNormal(data);
        return new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);
    }


    public static Vector3f calculateNormalF(Vector3f p0, Vector3f p1, Vector3f p2) {
        return calculateNormalF(p0.x(), p0.y(), p0.z(), p1.x(), p1.y(), p1.z(), p2.x(), p2.y(), p2.z());
    }

    public static double horizontalDegree(double x, double z, boolean forward) {
        double angle = Math.atan(z / x);
        if (x < 0 == forward) {
            angle += Math.PI;
        }
        return angle * 180 / Math.PI;
    }

    @Value
    private static class TriVec {
        Vector3d pos1;
        Vector3d pos2;
        Vector3d pos3;
    }
}
