package dev.crafty.core.config;

import java.io.File;

public record ServiceObjectMetadata<T>(File file, T value) {}