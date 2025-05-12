package dev.crafty.core.config;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.crafty.core.LangUtils;
import dev.crafty.core.Utils;
import dev.crafty.core.async.ActionUtils;
import dev.crafty.core.config.annotation.ConfigClassFile;
import dev.crafty.core.config.annotation.ConfigValue;
import dev.crafty.core.config.annotation.Validator;
import dev.crafty.core.log.Logger;
import dev.crafty.core.log.LoggingUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConfigHandler {
    private final List<ConfigSerializer<?>> serializers = new ArrayList<>();
    private final Map<Field, Supplier<OfflinePlayer>> playerSuppliers = new HashMap<>();

    @Inject
    private Logger logger;

    private record FieldValue(Field field, ConfigValue annotation) {
    }

    public void registerSerializer(ConfigSerializer<?> serializer) {
        serializers.add(serializer);
    }

    @SuppressWarnings("unchecked")
    public <T> ConfigSerializer<T> getSerializer(TypeReference<T> target) {
        return (ConfigSerializer<T>) serializers.stream()
                .filter((s) -> s.getType().getType().equals(target))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No serializer found for type: " + target.getType().getTypeName()));
    }

    @SuppressWarnings("unchecked")
    public <T> ConfigSerializer<T> getSerializer(Class<T> target) {
        return (ConfigSerializer<T>) serializers.stream()
                .filter((s) -> s.getType().getType().equals(target))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No serializer found for type: " + target.getName()));
    }

    public void registerPlayerSupplier(Field field, Supplier<OfflinePlayer> supplier) {
        playerSuppliers.put(field, supplier);
    }

    public void reloadAll() {
        Plugin callingPlugin = Utils.getCallingPluginInstance();

        if (callingPlugin == null) {
            logger.error("Plugin instance is null when reloading config... This means the config was not reloaded");
            return;
        }

        Set<FieldValue> fields = getAllFields(getAllClasses(callingPlugin));
        
        fields.forEach(fieldValue -> {
            // ensures we aren't processing service objects here
            if (ServiceObject.class.isAssignableFrom(fieldValue.field().getDeclaringClass())) return;

            String file = fieldValue.annotation().file().isEmpty() ? getParentFile(fieldValue.annotation()) : fieldValue.annotation().file();
            if (file == null) {
                logger.error("Failed to refresh config value with path: " + fieldValue.annotation().path() + ". No file specified!");
                return;
            }

            YamlConfiguration config = LoggingUtils.tryOrLog(() -> {
                File resolved = callingPlugin.getDataPath().resolve(file).toFile();
                if (!resolved.exists()) {
                    resolved.mkdirs();
                }

                return YamlConfiguration.loadConfiguration(resolved);
            }, logger);

            if (config == null) {
                logger.error("Failed to refresh config value with path: " + fieldValue.annotation().path() + ". Could not load config!");
                return;
            }

            updateField(config, fieldValue);
        });
        
        handleServiceObjects(callingPlugin);
    }

    private Set<Class<?>> getAllClasses(Plugin plugin) {
        return new HashSet<>(getClasses(plugin.getClass().getPackage().getName()));
    }

    private Set<Class<?>> getClasses(@NotNull String packageName) {
        Set<Class<?>> classes = new HashSet<>();

        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        if (stream == null) return classes;

        BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .collect(Collectors.toSet());
    }

    private @Nullable Class<?> getClass(String packageName, @NotNull String className) {
        try {
            return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private @NotNull Set<FieldValue> getAllFields(@NotNull Set<Class<?>> classes) {
        Set<FieldValue> fields = new HashSet<>();
        for (Class<?> clazz : classes) {
            fields.addAll(getAnnotatedFields(clazz));
        }
        return fields;
    }

    private @NotNull Set<FieldValue> getAnnotatedFields(@NotNull Class<?> target) {
        Set<FieldValue> annotatedFields = new HashSet<>();

        Field[] fields = target.getDeclaredFields();
        for (Field field : fields) {
            ConfigValue annotatedField = field.getAnnotation(ConfigValue.class);
            if (annotatedField != null) {
                annotatedFields.add(new FieldValue(field, annotatedField));
            }
        }

        return annotatedFields;
    }

    private void updateField(@NotNull YamlConfiguration config, @NotNull FieldValue fieldValue) {
        TypeReference<?> fieldType = new TypeReference<>() {
            @Override
            public Type getType() {
                return fieldValue.field().getGenericType();
            }
        };

        Object value;

        Optional<ConfigSerializer<?>> serializer = serializers.stream()
                .filter(s -> s.getType().equals(fieldType.getType()))
                .findFirst();

        if (serializer.isPresent()) {
            ConfigurationSection section = config.getConfigurationSection(fieldValue.annotation().path());
            value = serializer.get().deserialize(section);
        } else {
            value = config.get(fieldValue.annotation().path());
        }

        if (value != null) {
            if (fieldValue.annotation().expressionable()) {
                value = parseExpression(value, fieldValue);
            }

            if (fieldValue.annotation().colorize()) {
                if (value instanceof String) {
                    value = LangUtils.colorize((String) value);
                } else if (serializer.isPresent() && serializer.get() instanceof ColorizedConfigSerializer<?>) {
                    value = ((ColorizedConfigSerializer) serializer.get()).colorize(value);
                }
            }

            if (!validateField(fieldValue, value)) {
                logger.warn("The value " + value + " is not valid for config key " + fieldValue.annotation.path() + "!");
            } else {
                // valid, lets set it
                try {
                    fieldValue.field().setAccessible(true);
                    Class<?> rawType = fieldValue.field().getType();
                    fieldValue.field().set(null, rawType.cast(value));
                } catch (IllegalAccessException e) {
                    logger.error("Failed to set field value for path: " + fieldValue.annotation().path());
                    logger.logAndDigest(e);
                }
            }
        } else {
            Object setValue = LoggingUtils.tryOrLog(() -> fieldValue.field().get(null), logger);

            ConfigurationSection section;
            if (config.isConfigurationSection(fieldValue.annotation().path())) {
                section = config.getConfigurationSection(fieldValue.annotation().path());
            } else {
                section = config.createSection(fieldValue.annotation().path());
            }

            if (serializer.isPresent() && setValue != null) {
                ConfigSerializer<?> foundSerializer = serializer.get();
                serializeValue(foundSerializer, section, setValue);
            } else if (setValue != null) {
                config.set(fieldValue.annotation().path(), setValue);
            }

            if (setValue == null && !fieldValue.annotation().optional()) {
                logger.warn("Field is empty for required option");
            }
        }
    }

    private boolean validateField(FieldValue fieldValue, Object setValue) {
        if (fieldValue.field.isAnnotationPresent(Validator.class)) {
            Annotation validatorAnnotation = fieldValue.field.getAnnotation(Validator.class);
            Class<? extends Validator.ValueValidator<?>> validatorClass =
                    fieldValue.field.getAnnotation(Validator.class).value();

            try {
                Validator.ValueValidator validator = validatorClass.getDeclaredConstructor().newInstance();

                if (!validator.validate(setValue, validatorAnnotation)) {
                    logger.warn("Validation failed for field " + fieldValue.field.getName() +
                            " with validator " + validatorClass.getSimpleName());
                    return false;
                }

            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                logger.logAndDigest(e);
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private <T> void serializeValue(ConfigSerializer<T> serializer, ConfigurationSection section, Object value) {
        serializer.serialize(section, (T) value);
    }

    private @Nullable String getParentFile(@NotNull ConfigValue field) {
        Class<?> parentClass = field.getClass().getDeclaringClass();
        ConfigClassFile parentFile = parentClass.getAnnotation(ConfigClassFile.class);
        if (parentFile == null) return null;
        return parentFile.file();
    }

    private Object parseExpression(@NotNull Object value, @NotNull FieldValue fieldValue) {
        String expression = value.toString();
        Field field = fieldValue.field();

        Supplier<OfflinePlayer> supplier = playerSuppliers.get(field);
        OfflinePlayer player = supplier == null ? null : supplier.get();

        expression = PlaceholderAPI.setPlaceholders(player, expression);

        Expression exp = new ExpressionBuilder(expression)
                .build();

        try {
            return exp.evaluateAsync(ActionUtils.executor).get();
        } catch (ExecutionException | InterruptedException e) {
            logger.logAndDigest("Expression failed to evaluate", e);
            return value;
        }
    }

    private record ServiceRecord<T>(Class<T> clazz, Set<FieldValue> fields, List<String> servicePaths) {
    }

    private Set<ServiceRecord<?>> getServiceRecords(Plugin callingPlugin) {
        Set<ServiceRecord<?>> serviceObjects = new HashSet<>();
        Set<Class<?>> classes = getAllClasses(callingPlugin);

        for (Class<?> clazz : classes) {
            List<String> servicePaths = LoggingUtils.tryOrLog(() -> (List<String>) clazz.getField("servicePaths").get(null), logger);
            Set<FieldValue> fields = getAnnotatedFields(clazz);
            if (!fields.isEmpty()) {
                @SuppressWarnings("unchecked")
                ServiceRecord<?> serviceObject = new ServiceRecord<>((Class<Object>) clazz, fields, servicePaths);
                serviceObjects.add(serviceObject);
            }
        }

        return serviceObjects;
    }

    private void handleServiceObjects(Plugin callingPlugin) {
        Set<ServiceRecord<?>> serviceRecords = getServiceRecords(callingPlugin);

        for (ServiceRecord<?> serviceObject : serviceRecords) {
            List<String> servicePaths = serviceObject.servicePaths();

            List<File> searchableDirectories = new ArrayList<>();

            for (String path : servicePaths) {
                File directory = new File(callingPlugin.getDataFolder(), path);

                if (!directory.exists()) {
                    directory.mkdirs();
                }

                searchableDirectories.add(directory);
            }

            for (File directory : searchableDirectories) {
                File[] files = directory.listFiles();

                if (files == null) continue;
                for (File file : files) {
                    LoggingUtils.tryOrLog(() -> createServiceRecord(file, serviceObject), logger);
                }
            }
        }
    }

    private void createServiceRecord(File file, @NotNull ServiceRecord<?> target) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ServiceObject<?> newInstance = (ServiceObject<?>) target.clazz.getConstructor().newInstance();
        Set<FieldValue> fields = getAnnotatedFields(newInstance.getClass());

        YamlConfiguration config = LoggingUtils.tryOrLog(() -> YamlConfiguration.loadConfiguration(file), logger);
        if (config == null) {
            logger.error("Config is null when loading service object " + target.clazz.getSimpleName());
            return;
        }

        fields.forEach(field -> updateField(config, field));

        ServiceObjectMetadata metadata = new ServiceObjectMetadata<>(file, newInstance);
        newInstance.onLoad(metadata);
    }
}