package dev.crafty.core.config.conditions.conditions;

import dev.crafty.core.config.ConfigHandler;
import dev.crafty.core.config.conditions.Condition;
import dev.crafty.core.config.conditions.ConditionContext;
import jakarta.inject.Inject;
import org.bukkit.World;

import java.util.Set;

public class WorldCondition implements Condition<World> {
    @Inject
    private ConfigHandler handler;

    {
        handler.registerCondition(this);
    }

    @Override
    public String getName() {
        return "world";
    }

    @Override
    public boolean evaluate(ConditionContext context, World target) {
        return context.world().getUID().equals(target.getUID());
    }

    @Override
    public Set<String> contextRequirements() {
        return Set.of("world");
    }
}
