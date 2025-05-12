package dev.crafty.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

public class LangUtils {
    public static String colorize(String uncolored) {
        return ((TextComponent) MiniMessage.miniMessage().deserialize(uncolored).decoration(TextDecoration.ITALIC, false)).content();
    }
    
    public static String getContents(Component component) {
        return ((TextComponent) component).content();
    }
    
    public static List<String> getContents(List<Component> components) {
        return components.stream()
                .map(LangUtils::getContents)
                .toList();
    }
}
