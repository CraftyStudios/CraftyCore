package dev.crafty.core.storage.providers;

import dev.crafty.core.storage.StorageProvider;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;

public abstract class InternalStorageProvider<ConfigSchema> implements StorageProvider {
    private final String providerName;
    protected final ConfigSchema config;

    public InternalStorageProvider(String providerName, ConfigSchema configSchema) {
        this.providerName = providerName;
        this.config = configSchema;
    }

    public void generateConfigSection(FileConfiguration config) {
        Field[] fields = config.getClass().getDeclaredFields();
        if (fields.length == 0) return;
        ConfigurationSection section = config.createSection(providerName.toLowerCase());

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                section.set(field.getName(), field.get(config));
            } catch (IllegalAccessException e) {
                // Ignore inaccessible fields
            }
        }
    }

    public void reloadConfig(FileConfiguration config) {
        //TODO update the config
    }

    public void init() {}
    public void shutdown() {}
}
