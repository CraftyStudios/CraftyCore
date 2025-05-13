package dev.crafty.core.config.conditions;

import java.util.Set;

public interface Condition<T> {
    String getName();
    boolean evaluate(ConditionContext context, T value);
    Set<String> contextRequirements();
}

