package dev.crafty.core.gui;

import dev.crafty.core.config.annotation.ConfigValue;
import dev.crafty.core.config.validators.MultipleOf;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class Gui implements InventoryHolder {
    protected final Player player;
    private final String configName;
    private final Inventory inventory;

    @ConfigValue(path = "title", colorize = true)
    protected String title = "Default Title";

    @ConfigValue(path = "height")
    @MultipleOf(multiple = 9)
    protected int slots = 27;

    public Gui(Player player, String configName, String title) {
        this.player = player;
        this.configName = configName;
        this.title = title;
        this.inventory = Bukkit.createInventory(player, slots, title);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
