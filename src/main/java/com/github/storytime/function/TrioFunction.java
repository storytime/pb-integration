package com.github.storytime.function;

@FunctionalInterface
public interface TrioFunction<T, U, I, R> {
    R calculate(T var1, U var2, I var3);
}