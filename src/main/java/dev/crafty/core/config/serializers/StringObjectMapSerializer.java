package dev.crafty.core.config.serializers;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.crafty.core.config.ConfigHandler;
import dev.crafty.core.config.ConfigSerializer;
import org.bukkit.configuration.ConfigurationSection;

import javax.inject.Inject;
import java.util.Map;

public class StringObjectMapSerializer implements ConfigSerializer<Map<String, Object>> {
    @Inject
    private ConfigHandler handler;

    {
        handler.registerSerializer(this);
    }

    @Override
    public void serialize(ConfigurationSection section, Map<String, Object> value) {
        for (String key : value.keySet()) {
            Object obj = value.get(key);
            Class<?> clazz = obj.getClass();
            ConfigSerializer serializer = handler.getSerializer(clazz);

            if (serializer != null) {
                serializer.serialize(section.createSection(key), obj);
            } else {
                section.set(key, obj);
            }
        }
    }

    @Override
    public Map<String, Object> deserialize(ConfigurationSection section) {
        Map<String, Object> map = section.getValues(false);
        Map<String, Object> result = new java.util.HashMap<>();

        for (String key : map.keySet()) {
            Object obj = map.get(key);
            Class<?> clazz = obj.getClass();
            ConfigSerializer serializer = handler.getSerializer(clazz);

            if (serializer != null) {
                result.put(key, serializer.deserialize(section.getConfigurationSection(key)));
            } else {
                result.put(key, obj);
            }
        }

        return result;
    }

    @Override
    public TypeReference<Map<String, Object>> getType() {
        return new TypeReference<>() {};
    }
}
