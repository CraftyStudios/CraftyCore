package dev.crafty.core;

import com.google.inject.AbstractModule;
import dev.crafty.core.config.ConfigHandler;
import dev.crafty.core.lang.Lang;
import dev.crafty.core.log.Logger;

public class CoreModule extends AbstractModule {
    private final CraftyCore craftyCore;
    private final Logger logger;
    private final ConfigHandler configHandler;
    private final Lang lang;

    public CoreModule(CraftyCore craftyCore, Logger logger, ConfigHandler configHandler, Lang lang) {
        this.craftyCore = craftyCore;
        this.logger = logger;
        this.configHandler = configHandler;
        this.lang = lang;
    }

    @Override
    protected void configure() {
        bind(CraftyCore.class).toInstance(craftyCore);
        bind(Logger.class).toInstance(logger);
        bind(ConfigHandler.class).toInstance(configHandler);
        bind(Lang.class).toInstance(lang);
    }
}