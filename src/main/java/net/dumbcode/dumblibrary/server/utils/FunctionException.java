package net.dumbcode.dumblibrary.server.utils;

@FunctionalInterface
public interface FunctionException<T, R, X extends Throwable> {
    R accept(T t) throws X;
}
