package dev.crafty.core.config;

import com.fasterxml.jackson.core.type.TypeReference;
import org.bukkit.configuration.ConfigurationSection;

public interface ConfigSerializer<T> {
    void serialize(ConfigurationSection section, T value);
    T deserialize(ConfigurationSection section);
    TypeReference<T> getType();

    default <V> V getOrElse(ConfigurationSection section, String path, V defaultValue) {
        if (section == null) return defaultValue;
        V value = (V) section.get(path);
        if (value == null) return defaultValue;
        return value;
    }
}
