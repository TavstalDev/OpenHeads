package io.github.tavstaldev.openheads;

import com.samjakob.spigui.SpiGUI;
import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.VersionUtils;
import io.github.tavstaldev.openheads.commands.CommandHeads;
import io.github.tavstaldev.openheads.events.PlayerEventListener;
import io.github.tavstaldev.openheads.managers.MySqlManager;
import io.github.tavstaldev.openheads.managers.SqlLiteManager;
import io.github.tavstaldev.openheads.metrics.Metrics;
import io.github.tavstaldev.openheads.models.IDatabase;
import io.github.tavstaldev.openheads.utils.EconomyUtils;
import io.github.tavstaldev.openheads.utils.HeadUtils;
import org.bukkit.Bukkit;

/**
 * The main class for the OpenHeads plugin.
 */
public class OpenHeads extends PluginBase {
    public static OpenHeads Instance;
    /**
     * Gets the custom logger for the plugin.
     *
     * @return The PluginLogger instance.
     */
    public static PluginLogger logger() {
        return Instance.getCustomLogger();
    }
    /**
     * Gets the translator for the plugin.
     *
     * @return The PluginTranslator instance.
     */
    public static PluginTranslator translator() {
        return Instance.getTranslator();
    }
    private static SpiGUI _spiGUI;
    /**
     * Gets the SpiGUI instance.
     *
     * @return The SpiGUI instance.
     */
    public static SpiGUI gui() {
        return _spiGUI;
    }

    /**
     * Gets the plugin configuration.
     * @return The FileConfiguration object.
     */
    public static HeadsConfiguration config(){
        return (HeadsConfiguration) Instance.getConfig();
    }
    public static IDatabase Database;

    /**
     * Constructor for the OpenHeads plugin.
     */
    public OpenHeads() {
        super(true, "https://github.com/TavstalDev/OpenHeads/releases/latest");
    }

    /**
     * Called when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        Instance = this;
        _config = new HeadsConfiguration();
        _config.load();
        _translator = new PluginTranslator(this, new String[]{"eng", "hun"});
        _logger.info(String.format("Loading %s...", getProjectName()));

        if (VersionUtils.isLegacy()) {
            _logger.error("The plugin is not compatible with legacy versions of Minecraft. Please use a newer version of the game.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register Events
        PlayerEventListener.init();

        // Load Localizations
        if (!_translator.load())
        {
            _logger.error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register Economy
        _logger.debug("Hooking into Vault...");
        if (EconomyUtils.setupEconomy())
            _logger.info("Economy plugin found and hooked into Vault.");
        else
        {
            _logger.warn("Economy plugin not found. Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Create Database
        String databaseType = this.getConfig().getString("storage.type");
        if (databaseType == null)
            databaseType = "sqlite";
        switch (databaseType.toLowerCase()) {
            case "mysql":
            {
                Database = new MySqlManager();
                break;
            }
            case "sqlite":
            default:
            {
                Database = new SqlLiteManager();
                break;
            }
        }
        Database.load();
        Database.checkSchema();

        // Register Head Config
        _logger.debug("Loading config...");
        HeadUtils.load();

        // Register GUI
        _logger.debug("Loading GUI...");
        _spiGUI = new SpiGUI(this);

        // Register Commands
        _logger.debug("Registering commands...");
        var command = getCommand("heads");
        if (command != null) {
            command.setExecutor(new CommandHeads());
        }

        // Metrics
        try {
            @SuppressWarnings("unused") Metrics metrics = new Metrics(this, 27765);
        }
        catch (Exception ex)
        {
            _logger.error("Failed to start Metrics: " + ex.getMessage());
        }

        _logger.ok(String.format("%s has been successfully loaded.", getProjectName()));
        if (config().checkForUpdates) {
            isUpToDate().thenAccept(upToDate -> {
                if (upToDate) {
                    _logger.ok("Plugin is up to date!");
                } else {
                    _logger.warn("A new version of the plugin is available: " + getDownloadUrl());
                }
            }).exceptionally(e -> {
                _logger.error("Failed to determine update status: " + e.getMessage());
                return null;
            });
        }
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        super.onDisable();
        if (Database != null)
            Database.unload();
        _logger.info(String.format("%s has been successfully unloaded.", getProjectName()));
    }

    /**
     * Replaces placeholders in the given message with actual values.
     *
     * @param message The message containing placeholders.
     * @return The message with placeholders replaced.
     */
    @Override
    protected String replacePlaceholders(String message) {
        String result = super.replacePlaceholders(message);

        if (result.contains("%currency_singular%")) {
            String currencySingular = EconomyUtils.currencyNameSingular();
            result = result.replace("%currency_singular%", currencySingular == null ? localize("General.CurrencySingular") : currencySingular);
        }

        if (result.contains("%currency_plural%")) {
            String currencyPlural = EconomyUtils.currencyNamePlural();
            result = result.replace("%currency_plural%", currencyPlural == null ? localize("General.CurrencyPlural") : currencyPlural);
        }
        return result;
    }

    /**
     * Reloads the plugin configuration and localizations.
     */
    public void reload() {
        _logger.info("Reloading OpenHeads...");
        _logger.debug("Reloading localizations...");
        _translator.load();
        _logger.debug("Localizations reloaded.");
        _logger.debug("Reloading configuration...");
        _config.load();
        _logger.debug("Configuration reloaded.");
    }
}
