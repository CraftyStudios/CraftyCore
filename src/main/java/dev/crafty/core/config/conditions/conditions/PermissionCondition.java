package dev.crafty.core.config.conditions.conditions;

import dev.crafty.core.config.ConfigHandler;
import dev.crafty.core.config.conditions.Condition;
import dev.crafty.core.config.conditions.ConditionContext;
import jakarta.inject.Inject;

import java.util.Set;

public class PermissionCondition implements Condition<String> {
    @Inject
    private ConfigHandler handler;

    {
        handler.registerCondition(this);
    }

    @Override
    public String getName() {
        return "permission";
    }

    @Override
    public boolean evaluate(ConditionContext context, String permission) {
        return context.player().hasPermission(permission);
    }

    @Override
    public Set<String> contextRequirements() {
        return Set.of("player");
    }
}
