package dev.crafty.core.storage;

import java.util.concurrent.CompletableFuture;

public interface StorageProvider {
    <T> CompletableFuture<T> fetch(StorageKey<T> key);
    <T> void store(StorageKey<T> key, T value);
}
