package io.github.tavstaldev.openheads;

import com.samjakob.spigui.SpiGUI;
import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.VersionUtils;
import io.github.tavstaldev.openheads.commands.CommandHeads;
import io.github.tavstaldev.openheads.managers.MySqlManager;
import io.github.tavstaldev.openheads.managers.SqlLiteManager;
import io.github.tavstaldev.openheads.models.IDatabase;
import io.github.tavstaldev.openheads.utils.EconomyUtils;
import io.github.tavstaldev.openheads.utils.HeadUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * The main class for the OpenHeads plugin.
 */
public class OpenHeads extends PluginBase {
    public static OpenHeads Instance;
    private final PluginTranslator _translator;
    /**
     * Gets the custom logger for the plugin.
     *
     * @return The PluginLogger instance.
     */
    public static PluginLogger Logger() {
        return Instance.getCustomLogger();
    }
    /**
     * Gets the translator for the plugin.
     *
     * @return The PluginTranslator instance.
     */
    public static PluginTranslator Translator() {
        return Instance.getTranslator();
    }
    private static SpiGUI _spiGUI;
    /**
     * Gets the SpiGUI instance.
     *
     * @return The SpiGUI instance.
     */
    public static SpiGUI GetGUI() {
        return _spiGUI;
    }

    /**
     * Gets the plugin configuration.
     * @return The FileConfiguration object.
     */
    public static FileConfiguration GetConfig(){
        return Instance.getConfig();
    }
    public static IDatabase Database;

    /**
     * Constructor for the OpenHeads plugin.
     */
    public OpenHeads() {
        super("OpenHeads",
                "1.0.0",
                "Tavstal",
                "https://github.com/TavstalDev/OpenHeads/releases/latest",
                new String[]{"eng", "hun"}
        );
        _translator = getTranslator();
    }

    /**
     * Called when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        Instance = this;
        _logger.Info(String.format("Loading %s...", getProjectName()));

        if (VersionUtils.isLegacy()) {
            _logger.Error("The plugin is not compatible with legacy versions of Minecraft. Please use a newer version of the game.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register Events
        EventListener.init();

        // Generate config file
        saveDefaultConfig();

        // Load Localizations
        if (!_translator.Load())
        {
            _logger.Error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register Economy
        _logger.Debug("Hooking into Vault...");
        if (EconomyUtils.setupEconomy())
            _logger.Info("Economy plugin found and hooked into Vault.");
        else
        {
            _logger.Warn("Economy plugin not found. Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register ProtocolLib
        _logger.Debug("Hooking into ProtocolLib...");
        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib"))
            _logger.Info("ProtocolLib found and hooked into it.");
        else
        {
            _logger.Warn("ProtocolLib not found. Unloading...");
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
        Database.CheckSchema();

        // Register Head Config
        _logger.Debug("Loading config...");
        HeadUtils.Load();

        // Register GUI
        _logger.Debug("Loading GUI...");
        _spiGUI = new SpiGUI(this);

        // Register Commands
        _logger.Debug("Registering commands...");
        var command = getCommand("heads");
        if (command != null) {
            command.setExecutor(new CommandHeads());
        }

        _logger.Ok(String.format("%s has been successfully loaded.", getProjectName()));
        isUpToDate().thenAccept(upToDate -> {
            if (upToDate) {
                _logger.Ok("Plugin is up to date!");
            } else {
                _logger.Warn("A new version of the plugin is available: " + getDownloadUrl());
            }
        }).exceptionally(e -> {
            _logger.Error("Failed to determine update status: " + e.getMessage());
            return null;
        });
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        _logger.Info(String.format("%s has been successfully unloaded.", getProjectName()));
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
            result = result.replace("%currency_singular%", currencySingular == null ? Localize("General.CurrencySingular") : currencySingular);
        }

        if (result.contains("%currency_plural%")) {
            String currencyPlural = EconomyUtils.currencyNamePlural();
            result = result.replace("%currency_plural%", currencyPlural == null ? Localize("General.CurrencyPlural") : currencyPlural);
        }
        return result;
    }

    /**
     * Reloads the plugin configuration and localizations.
     */
    public void reload() {
        _logger.Info("Reloading OpenHeads...");
        _logger.Debug("Reloading localizations...");
        _translator.Load();
        _logger.Debug("Localizations reloaded.");
        _logger.Debug("Reloading configuration...");
        this.reloadConfig();
        _logger.Debug("Configuration reloaded.");
    }
}
