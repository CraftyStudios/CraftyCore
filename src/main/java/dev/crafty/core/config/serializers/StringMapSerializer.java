package dev.crafty.core.config.serializers;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.crafty.core.LangUtils;
import dev.crafty.core.config.ColorizedConfigSerializer;
import dev.crafty.core.config.ConfigHandler;
import org.bukkit.configuration.ConfigurationSection;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class StringMapSerializer implements ColorizedConfigSerializer<Map<String, String>> {
    @Inject
    private ConfigHandler handler;

    {
        handler.registerSerializer(this);
    }

    @Override
    public Map<String, String> colorize(Map<String, String> value) {
        Map<String, String> map = new HashMap<>();

        value.forEach((key, val) -> map.put(key, LangUtils.colorize(val)));
        return map;
    }

    @Override
    public void serialize(ConfigurationSection section, Map<String, String> value) {
        value.forEach(section::set);
    }

    @Override
    public Map<String, String> deserialize(ConfigurationSection section) {
        Map<String, String> map = new HashMap<>();

        for (String key : section.getKeys(true)) {
            map.put(key, section.getString(key));
        }

        return map;
    }

    @Override
    public TypeReference<Map<String, String>> getType() {
        return new TypeReference<>() {};
    }
}
