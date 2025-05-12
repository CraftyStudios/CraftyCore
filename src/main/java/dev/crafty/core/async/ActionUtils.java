package dev.crafty.core.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class ActionUtils {
    public static final ExecutorService executor;

    static {
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public static <T> CompletableFuture<T> runAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }
}