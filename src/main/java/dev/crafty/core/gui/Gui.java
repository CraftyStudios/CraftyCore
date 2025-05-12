package dev.crafty.core.gui;

import dev.crafty.core.config.annotation.ConfigValue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Gui implements InventoryHolder {
    protected final Player player;
    private final String configName;
    private Inventory inventory;

    @ConfigValue(path = "title", colorize = true)
    protected String title = "Default Title";

    @ConfigValue(path = "layout.pattern", optional = true)
    protected List<String> layoutPattern = new ArrayList<>();

    @ConfigValue(path = "layout.items", optional = true)
    protected Map<String, ItemStack> layoutItems = new HashMap<>();

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

        // Create inventory
        this.inventory = Bukkit.createInventory(player, slots, title);
    }

//    /**
//     * Applies the layout pattern to the inventory
//     */
//    protected void applyLayout() {
//        if (layoutPattern.isEmpty() || layoutItems.isEmpty()) {
//            return;
//        }
//
//        int slot = 0;
//        for (String row : layoutPattern) {
//            for (char c : row.toCharArray()) {
//                if (slot >= inventory.getSize()) {
//                    break; // Prevent index out of bounds
//                }
//
//                String key = String.valueOf(c);
//                if (layoutItems.containsKey(key)) {
//                    inventory.setItem(slot, layoutItems.get(key));
//                }
//
//                slot++;
//            }
//        }
//    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
