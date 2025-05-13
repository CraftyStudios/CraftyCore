package dev.crafty.core.api;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import com.vdurmont.semver4j.Semver;
import dev.crafty.core.CraftyCore;
import dev.crafty.core.config.ConfigHandler;
import dev.crafty.core.gui.Gui;
import dev.crafty.core.lang.Lang;
import dev.crafty.core.log.Logger;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public abstract class CraftyPlugin extends JavaPlugin {
    @Inject
    protected Logger logger;
    @Inject
    protected ConfigHandler configHandler;
    @Inject
    protected Lang lang;

    protected CraftyCore craftyCore = CraftyCore.getPlugin(CraftyCore.class);
    protected PaperCommandManager commandManager;

    protected abstract String minimumCoreVersion();
    protected abstract void handleEnable();
    protected abstract void handleDisable();
    protected abstract void handleReload();

    List<BaseCommand> getCommands() {return new ArrayList<>();}
    List<String> getRequiredPlugins() {return new ArrayList<>();}
    List<Listener> getListeners() {return new ArrayList<>();}
    List<Gui> getGuis() { return new ArrayList<>();}

    @Override
    public void onEnable() {
        CraftyCore.REGISTERED_PLUGINS.add(this);

        logger.info("Checking requirements...");
        if (!(getRequiredPlugins().isEmpty())) {
            for (String plugin : getRequiredPlugins()) {
                if (getServer().getPluginManager().getPlugin(plugin) == null) {
                    logger.error("Plugin %s not found... disabling!".formatted(plugin));
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }
            }
        }

        Plugin runningCore = getServer().getPluginManager().getPlugin(craftyCore.getName());
        PluginMeta coreMeta = runningCore.getPluginMeta();

        Semver runningVersion = new Semver(coreMeta.getVersion(), Semver.SemverType.LOOSE);
        Semver requiredVersion = new Semver(minimumCoreVersion(), Semver.SemverType.LOOSE);

        if (runningVersion.isLowerThan(requiredVersion)) {
            logger.error("Core version too low! Found %s but need at least %s.".formatted(runningVersion.getValue(), requiredVersion.getValue()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        logger.info("Requirements met. Starting initialization...");

        this.lang.loadLang();

        this.reloadConfig();

        this.handleEnable();

        logger.info("Starting command registration...");

        commandManager = new PaperCommandManager(this);

        for (BaseCommand command : getCommands()) {
            commandManager.registerCommand(command);
        }

        logger.info("Successfully registered %s commands!".formatted(getCommands().size()));

        logger.info("Registering listeners...");

        for (Listener listener : getListeners()) {
            getServer().getPluginManager().registerEvents(listener, this);
        }

        logger.info("Successfully registered %s listeners!".formatted(getListeners().size()));

        logger.info("Registering GUIs...");

        for (Gui gui : getGuis()) {
            gui.register(this);
        }

        logger.info("Successfully registered %s GUIs!".formatted(getGuis().size()));
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.configHandler.reloadAll();
    }

    public String getTranslated(String key) {
        NamespacedKey namespacedKey = new NamespacedKey(this, key);
        return lang.getTranslated(namespacedKey);
    }
}
