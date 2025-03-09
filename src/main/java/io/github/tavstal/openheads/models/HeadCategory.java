package io.github.tavstal.openheads.models;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.minecorelib.utils.ChatUtils;
import io.github.tavstal.openheads.OpenHeads;
import io.github.tavstal.openheads.helpers.GUIHelper;
import io.github.tavstal.openheads.utils.EconomyUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HeadCategory {
    /**
     * The name of the head category.
     */
    public final String Name;

    /**
     * The key for the display name of the head category.
     */
    public final String DisplayNameKey;

    /**
     * The key for the description of the head category.
     */
    public final String DescriptionKey;

    /**
     * Indicates if permission is required to access the head category.
     */
    public final boolean RequirePermission;

    /**
     * The permission string required to access the head category.
     */
    public final String Permission;

    /**
     * The price of the head category.
     */
    public final Double Price;

    /**
     * The file associated with the head category.
     */
    private final String _file;

    /**
     * The texture associated with the head category.
     */
    public final String Texture;

    /**
     * A list of head data associated with the head category.
     */
    private List<HeadData> _heads;

    /**
     * Gets the list of head data associated with the head category.
     *
     * @return A list of HeadData objects.
     */
    public List<HeadData> getHeads() {
        return _heads;
    }

    /**
     * The logger for the head category.
     */
    private final PluginLogger _logger;

    /**
     * The icon representing the head category.
     */
    private ItemStack _icon;

    /**
     * Constructs a new HeadCategory object with the specified parameters.
     *
     * @param name The name of the head category.
     * @param displayNameKey The key for the display name of the head category.
     * @param descriptionKey The key for the description of the head category.
     * @param requirePermission Indicates if permission is required to access the head category.
     * @param permission The permission string required to access the head category.
     * @param price The price of the head category.
     * @param file The file associated with the head category.
     * @param texture The texture associated with the head category.
     */
    public HeadCategory(String name, String displayNameKey, String descriptionKey, boolean requirePermission, String permission, Double price, String file, String texture) {
        Name = name;
        DisplayNameKey = displayNameKey;
        DescriptionKey = descriptionKey;
        RequirePermission = requirePermission;
        Permission = permission;
        Price = price;
        _file = file;
        Texture = texture;
        _heads = new ArrayList<>();
        _logger = OpenHeads.Instance.getCustomLogger().WithModule(String.format("%s Category", Name));
    }

    /**
     * Copies the head data file from the plugin's resources to the plugin's data folder.
     *
     * @return true if the file was successfully copied or already exists, false otherwise
     */
    public boolean CopyFromResource() {
        InputStream inputStream;
        Path dirPath = Paths.get(OpenHeads.Instance.getDataFolder().getPath(), "heads");
        Path filePath = Paths.get(dirPath.toString(), _file);
        if (!Files.exists(dirPath)) {
            try {
                _logger.Debug("Creating heads directory...");
                Files.createDirectory(dirPath);
            } catch (IOException ex) {
                _logger.Warn("Failed to create heads directory.");
                _logger.Error(ex.getMessage());
                return false;
            }
        }

        if (!Files.exists(filePath)) {
            _logger.Debug(String.format("Copying %s head file...", Name));
            try {
                inputStream = OpenHeads.Instance.getResource("heads/" + _file);
                if (inputStream == null) {
                    _logger.Debug(String.format("Failed to get head data file for category '%s'.", Name));
                } else
                    Files.copy(inputStream, filePath);
            } catch (IOException ex) {
                _logger.Warn(String.format("Failed to copy head data file for category '%s'.", Name));
                _logger.Error(ex.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * Loads the head data from the file associated with the head category.
     *
     * @return true if the data was successfully loaded, false otherwise
     */
    public boolean Load() {
        Path dirPath = Paths.get(OpenHeads.Instance.getDataFolder().getPath(), "heads");
        Path filePath = Paths.get(dirPath.toString(), _file);
        if (!Files.exists(filePath))
            return false;

        _logger.Debug(String.format("Reading %s head file...", Name));
        Gson gson = new Gson();
        try (FileReader fileReader = new FileReader(filePath.toFile())) {
            _heads = gson.fromJson(fileReader, new TypeToken<List<HeadData>>() {}.getType());
        } catch (IOException ex) {
            _logger.Error(String.format("Failed to read or parse the file. Path: %s", filePath));
            _logger.Error(ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Gets the icon representing the head category for the specified player.
     *
     * @param player the player for whom the icon is being retrieved
     * @return the ItemStack representing the icon
     */
    public ItemStack GetIcon(Player player) {
        if (_icon != null)
            return _icon;

        List<Component> loreList = new ArrayList<>();
        String displayName = OpenHeads.Instance.Localize(player, "GUI.CategoryName").replace("%category%", OpenHeads.Instance.Localize(player, DisplayNameKey));
        String description = OpenHeads.Instance.Localize(player, DescriptionKey);
        String freeText = OpenHeads.Instance.Localize(player, "GUI.Free");
        for (String rawLore : OpenHeads.Instance.LocalizeList(player, "GUI.CategoryLore")) {
            String lore = rawLore
                    .replace("%price%", Price == 0 ? freeText : String.format("%.2f", Price))
                    .replace("%description%", description)
                    .replace("%count%", String.valueOf(_heads.size()));

            if (lore.contains("%currency_singular%")) {
                String currencySingular = EconomyUtils.currencyNameSingular();
                lore = lore.replace("%currency_singular%", Price == 0 ? "" : currencySingular == null ? OpenHeads.Instance.Localize("General.CurrencySingular") : currencySingular);
            }
            if (lore.contains("%currency_plural%")) {
                String currencyPlural = EconomyUtils.currencyNamePlural();
                lore = lore.replace("%currency_plural%", Price == 0 ? "" : currencyPlural == null ? OpenHeads.Instance.Localize("General.CurrencyPlural") : currencyPlural);
            }
            loreList.add(ChatUtils.translateColors(lore, true));
        }

        try
        {
            if (Texture == null)
                return GUIHelper.createItem(Material.ZOMBIE_HEAD, displayName, loreList);
            var result = GUIHelper.createItem(Material.PLAYER_HEAD, displayName, loreList);
            var meta = result.getItemMeta();
            if (meta instanceof SkullMeta skullMeta) {
                PlayerProfile profile = Bukkit.createProfile(UUID.fromString("bbde04e7-ccb9-49a8-8ad8-08d11b6540d4"));
                profile.setProperty(new ProfileProperty("textures", Texture));
                skullMeta.setPlayerProfile(profile);
                result.setItemMeta(meta);
            }
            _icon = result;
            return result;
        }
        catch (Exception ex) {
            OpenHeads.Logger().Error("Failed to get category icon.");
            OpenHeads.Logger().Error(ex.getMessage());
            return GUIHelper.createItem(Material.ZOMBIE_HEAD, displayName, loreList);
        }
    }
}
