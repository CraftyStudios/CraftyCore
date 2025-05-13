package dev.crafty.core.config.conditions;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConditionContext {
    private final Player player;
    private final Location location;
    private final World world;

    private final Map<String, Object> extras = new HashMap<>();

    private ConditionContext(Builder builder) {
        this.player = builder.player;
        this.location = builder.location != null ? builder.location : (player != null ? player.getLocation() : null);
        this.world = builder.world != null ? builder.world : (location != null ? location.getWorld() : null);
        this.extras.putAll(builder.extras);
    }

    public Player player() { return player; }
    public Location location() { return location; }
    public World world() { return world; }

    @SuppressWarnings("unchecked")
    public <T> T extra(String key, Class<T> clazz) {
        Object val = extras.get(key);
        if (clazz.isInstance(val)) return (T) val;
        return null;
    }

    public boolean hasExtra(String key) {
        return extras.containsKey(key);
    }

    public Set<String> extraKeys() {
        return extras.keySet();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Player player;
        private Location location;
        private World world;
        private final Map<String, Object> extras = new HashMap<>();

        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        public Builder location(Location location) {
            this.location = location;
            return this;
        }

        public Builder world(World world) {
            this.world = world;
            return this;
        }

        public Builder withExtra(String key, Object value) {
            extras.put(key, value);
            return this;
        }

        public ConditionContext build() {
            return new ConditionContext(this);
        }
    }
}

