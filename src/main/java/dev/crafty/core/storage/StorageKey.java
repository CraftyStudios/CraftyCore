package dev.crafty.core.storage;

import dev.crafty.core.CraftyCore;
import lombok.Data;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

public class StorageKey<T> {
    @Inject
    private CraftyCore craftyCore;

    @Getter
    private final NamespacedKey key;
    @Getter
    private final T defaultValue;

    public StorageKey(OfflinePlayer player, String key, T defaultValue) {
        this.defaultValue = defaultValue;
        this.key = new NamespacedKey(craftyCore, key + "_" + player.getUniqueId());
    }

    public StorageKey(String key, T defaultValue) {
        this.defaultValue = defaultValue;
        this.key = new NamespacedKey(craftyCore, key);
    }

    public StorageKey(NamespacedKey key, T defaultValue) {
        this.defaultValue = defaultValue;
        this.key = key;
    }

    public Class<?> getType() {
        return defaultValue.getClass();
    }
}
