package dev.crafty.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.crafty.core.api.CraftyPlugin;
import dev.crafty.core.config.ConfigHandler;
import dev.crafty.core.lang.Lang;
import dev.crafty.core.log.Logger;
import dev.crafty.core.log.LoggerProvider;
import lombok.Getter;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CraftyCore extends CraftyPlugin {
    public static final List<CraftyPlugin> REGISTERED_PLUGINS = new ArrayList<>();

    private Injector injector;

    private Logger logger;
    private ConfigHandler configHandler;
    private Lang lang;

    @Getter private CommentedConfigurationNode globalConfig;
    @Getter private CommentedConfigurationNode databaseConfig;

    private YamlConfigurationLoader globalConfigLoader;
    private YamlConfigurationLoader databaseConfigLoader;

    @Override
    protected String minimumCoreVersion() {
        return "1.0.0";
    }

    @Override
    protected void handleEnable() {
        REGISTERED_PLUGINS.add(this);

        this.logger = initLogger();
        this.configHandler = new ConfigHandler();
        this.lang = new Lang();

        injector = Guice.createInjector(new CoreModule(this, logger, configHandler, lang));

        globalConfigLoader = createLoader("config.yml");
        databaseConfigLoader = createLoader("database.yml");
    }

    @Override
    protected void handleDisable() {

    }

    @Override
    protected void handleReload() {
        try {
            globalConfig = globalConfigLoader.load();
            databaseConfig = databaseConfigLoader.load();
        } catch (ConfigurateException e) {
            logger.logAndDigest(e);
        }
    }

    private YamlConfigurationLoader createLoader(String file) {
        return YamlConfigurationLoader.builder()
                .path(getDataPath().resolve(file))
                .build();
    }
    
    private Logger initLogger() {
        File logFolder = getDataPath().resolve("logs").toFile();
        if (!logFolder.exists()) logFolder.mkdirs();
        String newFileName = "log_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".log";
        File logFile = logFolder.toPath().resolve(newFileName).toFile();

        return new LoggerProvider(logFile);
    }
}
