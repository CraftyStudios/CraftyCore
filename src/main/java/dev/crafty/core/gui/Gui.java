package dev.crafty.core.gui;

import dev.crafty.core.api.CraftyPlugin;
import dev.crafty.core.config.annotation.ConfigValue;
import dev.crafty.core.log.Logger;
import jakarta.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Gui implements InventoryHolder, Listener {
    @Inject
    private Logger logger;

    protected final Player player;
    private final String configName;
    private Inventory inventory;
    private final Map<String, Consumer<Player>> actionMethods = new HashMap<>();

    public record ConfigItem(String key, ItemStack item, String action, List<String> conditions) {}

    @ConfigValue(path = "title", colorize = true)
    protected String title = "Default Title";

    @ConfigValue(path = "layout.pattern", optional = true)
    protected List<String> layoutPattern = new ArrayList<>();

    @ConfigValue(path = "layout.items", optional = true)
    protected Map<String, ConfigItem> layoutItems = new HashMap<>();

    protected List<ConfigItem> mappedItems = new ArrayList<>();

    public Gui(Player player, String configName, String title) {
        this.player = player;
        this.configName = configName;
        this.title = title;

        int slots = 0;

        for (String row : layoutPattern) {
            slots += row.length();
        }

        if (slots > 54) {
            slots = 54;
        } else if (slots < 9) {
            slots = 9;
        }

        slots = (slots / 9) * 9;

        if (slots < 9) {
            slots = 9;
        }

        for (ConfigItem key : layoutItems.values()) {
            if (key.item() == null) {
                continue;
            }

            mappedItems.add(key);
        }

        // Create inventory
        this.inventory = Bukkit.createInventory(player, slots, title);
    }

    public void handleClick(Player player, int slot) {
        ConfigItem item = mappedItems.get(slot);

        if (item == null) {
            return;
        }

        if (item.action() != null) {
            Consumer<Player> action = actionMethods.get(item.action());

            if (action != null) {
                action.accept(player);
            } else {
                logger.warn("Action '" + item.action() + "' not found in GUI '" + configName + "'");
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() != this.inventory) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getSlot() < 0 || event.getSlot() >= mappedItems.size()) {
            return;
        }

        handleClick(player, event.getSlot());
    }

    public void register(CraftyPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
