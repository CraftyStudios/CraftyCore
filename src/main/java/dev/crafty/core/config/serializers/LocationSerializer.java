package dev.crafty.core.config.serializers;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.crafty.core.config.ConfigHandler;
import dev.crafty.core.config.ConfigSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import javax.inject.Inject;

public class LocationSerializer implements ConfigSerializer<Location> {
    @Inject
    private ConfigHandler handler;

    {
        handler.registerSerializer(this);
    }

    @Override
    public void serialize(ConfigurationSection section, Location value) {
        section.set("world", value.getWorld().getName());
        section.set("x", value.getX());
        section.set("y", value.getY());
        section.set("z", value.getZ());
        section.set("yaw", value.getYaw());
        section.set("pitch", value.getPitch());
    }

    @Override
    public Location deserialize(ConfigurationSection section) {
        return new Location(
                Bukkit.getWorld(getOrElse(section, "world", "world")),
                getOrElse(section, "x", 0.0d),
                getOrElse(section, "y", 0.0d),
                getOrElse(section, "z", 0.0d),
                getOrElse(section, "yaw", 0.0f),
                getOrElse(section, "pitch", 0.0f)
        );
    }

    @Override
    public TypeReference<Location> getType() {
        return new TypeReference<>() {
        };
    }
}
