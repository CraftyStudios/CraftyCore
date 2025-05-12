package dev.crafty.core.config;

public interface ColorizedConfigSerializer<T> extends ConfigSerializer<T> {
    T colorize(T value);
}
