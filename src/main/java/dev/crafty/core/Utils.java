package dev.crafty.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final Pattern PLUGIN_NAME_PATTERN = Pattern.compile("plugin=([^,]+)");

    public static Field getFieldForClass(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public static Plugin getCallingPluginInstance() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stackTrace.length; i++) {
            try {
                Class<?> callingClass = Class.forName(stackTrace[i].getClassName());
                ClassLoader classLoader = callingClass.getClassLoader();
                if (
                        classLoader != null &&
                                classLoader.toString().contains("PluginClassLoader")
                ) {
                    String pluginName = extractPluginName(classLoader.toString());
                    if (pluginName == null) continue;
                    return Bukkit.getPluginManager().getPlugin(pluginName);
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        return null;
    }

    private static String extractPluginName(String classLoaderString) {
        Matcher matcher = PLUGIN_NAME_PATTERN.matcher(classLoaderString);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
