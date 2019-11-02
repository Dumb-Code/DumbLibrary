package net.dumbcode.dumblibrary.server.utils;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public class JavaUtils {
    @Nullable
    public static <I, O> O nullOr(@Nullable I in, Function<I, O> mapper) {
        return in == null ? null : mapper.apply(in);
    }

    @Nullable
    public static <I> I nullApply(@Nullable I in, Consumer<I> applier) {
        if(in != null) {
            applier.accept(in);
        }
        return in;
    }
}
