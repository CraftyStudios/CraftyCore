package dev.crafty.core.lang;

import dev.crafty.core.CraftyCore;
import dev.crafty.core.LangUtils;
import dev.crafty.core.api.CraftyPlugin;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Lang {
    @Getter
    private final Map<NamespacedKey, String> langCache = new HashMap<>();

    public String getTranslated(NamespacedKey key) {
        String translation = langCache.get(key);

        if (translation == null) {
            return key.getKey();
        }

        return translation;
    }

    public void loadLang() {
        for (NamespacedKey key : langCache.keySet()) {
            langCache.remove(key);
        }

        for (CraftyPlugin plugin : CraftyCore.REGISTERED_PLUGINS) {
            File langFile = new File(plugin.getDataFolder(), "lang.yml");
            YamlConfiguration lang = YamlConfiguration.loadConfiguration(langFile);

            for (String key : lang.getKeys(true)) {
                NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
                String value = lang.getString(key);
                langCache.put(namespacedKey, LangUtils.colorize(value));
            }
        }
    }
}
