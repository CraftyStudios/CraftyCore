package dev.crafty.core.config.serializers;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.crafty.core.LangUtils;
import dev.crafty.core.config.ColorizedConfigSerializer;
import dev.crafty.core.config.ConfigHandler;
import dev.crafty.core.config.ConfigSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.crafty.core.LangUtils.getContents;

public class ItemSerializer implements ColorizedConfigSerializer<ItemStack> {
    @Inject
    private ConfigHandler config;

    {
        config.registerSerializer(this);
    }
    
    @Override
    public ItemStack colorize(ItemStack value) {
        ItemStack clone = value.clone();

        clone.editMeta((meta) -> {
            if (meta.displayName() != null) {
                meta.displayName(
                        Component.text(
                                LangUtils.colorize(
                                        getContents(meta.displayName())
                                )
                        )
                );

            }
            
            if (meta.lore() != null) {
                List<Component> newLore = new ArrayList<>();

                meta.lore().forEach((lore) -> {
                    Component translated = Component.text(
                            LangUtils.colorize(
                                    getContents(lore)
                            )
                    );

                    newLore.add(translated);
                });

                meta.lore(newLore);
            }
        });

        return clone;
    }

    @Override
    public void serialize(ConfigurationSection section, ItemStack value) {
        section.set("name", getContents(value.displayName()));
        
        if (value.lore() == null) {
            section.set("lore", new ArrayList<>());
        } else {
            section.set("lore", getContents(value.lore()));
        }

        section.set("material", value.getType().toString().toLowerCase());
        
        if (!value.getEnchantments().isEmpty()) {
            ConfigurationSection enchantSection = section.createSection("enchantments");
            ConfigSerializer<Map<Enchantment, Integer>> serializer = config.getSerializer(new TypeReference<>() {});
            serializer.serialize(enchantSection, value.getEnchantments());
        }

        section.set("amount", value.getAmount());

        // item flags
    }

    @Override
    public ItemStack deserialize(ConfigurationSection section) {
        return null;
    }

    @Override
    public TypeReference<ItemStack> getType() {
        return new TypeReference<>() {
        };
    }
}
