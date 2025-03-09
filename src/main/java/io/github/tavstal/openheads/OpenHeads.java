package io.github.tavstal.openheads;

import com.samjakob.spigui.SpiGUI;
import io.github.tavstal.minecorelib.PluginBase;
import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.minecorelib.core.PluginTranslator;
import io.github.tavstal.openheads.commands.CommandHeads;
import io.github.tavstal.openheads.managers.MySqlManager;
import io.github.tavstal.openheads.managers.SqlLiteManager;
import io.github.tavstal.openheads.models.IDatabase;
import io.github.tavstal.openheads.utils.EconomyUtils;
import io.github.tavstal.openheads.utils.HeadUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

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
    }

    /**
     * Called when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        Instance = this;
        getCustomLogger().Info(String.format("Loading %s...", getProjectName()));

        // Register Events
        EventListener.init();

        // Generate config file
        saveDefaultConfig();

        // Load Localizations
        if (!getTranslator().Load())
        {
            getCustomLogger().Error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register Economy
        getCustomLogger().Debug("Hooking into Vault...");
        if (EconomyUtils.setupEconomy())
            getCustomLogger().Info("Economy plugin found and hooked into Vault.");
        else
        {
            getCustomLogger().Warn("Economy plugin not found. Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register ProtocolLib
        getCustomLogger().Debug("Hooking into ProtocolLib...");
        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib"))
            getCustomLogger().Info("ProtocolLib found and hooked into it.");
        else
        {
            getCustomLogger().Warn("ProtocolLib not found. Unloading...");
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
        getCustomLogger().Debug("Loading config...");
        HeadUtils.Load();

        // Register GUI
        getCustomLogger().Debug("Loading GUI...");
        _spiGUI = new SpiGUI(this);

        // Register Commands
        getCustomLogger().Debug("Registering commands...");
        var command = getCommand("heads");
        if (command != null) {
            command.setExecutor(new CommandHeads());
        }

        getCustomLogger().Info(String.format("%s has been successfully loaded.", getProjectName()));
        if (!isUpToDate())
            getCustomLogger().Warn(String.format("A new version of %s is available! Download it at %s", getProjectName(), getDownloadUrl()));
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        getCustomLogger().Info(String.format("%s has been successfully unloaded.", getProjectName()));
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
        getCustomLogger().Info("Reloading OpenHeads...");
        getCustomLogger().Debug("Reloading localizations...");
        getTranslator().Load();
        getCustomLogger().Debug("Localizations reloaded.");
        getCustomLogger().Debug("Reloading configuration...");
        this.reloadConfig();
        getCustomLogger().Debug("Configuration reloaded.");
    }

    /**
     * Checks if the plugin is up to date by comparing the current version with the latest release version.
     * @return true if the plugin is up to date, false otherwise.
     */
    public boolean isUpToDate() {
        String version;
        getCustomLogger().Debug("Checking for updates...");
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            getCustomLogger().Debug("Sending request to GitHub...");
            HttpGet request = new HttpGet(getDownloadUrl());
            HttpResponse response = httpClient.execute(request);
            getCustomLogger().Debug("Received response from GitHub.");
            String jsonResponse = EntityUtils.toString(response.getEntity());
            getCustomLogger().Debug("Parsing response...");
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonResponse);
            getCustomLogger().Debug("Parsing release version...");
            version = jsonObject.get("tag_name").toString();
        } catch (IOException e) {
            getCustomLogger().Error("Failed to check for updates.");
            return false;
        } catch (ParseException e) {
            getCustomLogger().Error("Failed to parse release version.");
            return false;
        }

        getCustomLogger().Debug("Current version: " + getVersion());
        getCustomLogger().Debug("Latest version: " + version);
        return version.equalsIgnoreCase(getVersion());
    }
}
