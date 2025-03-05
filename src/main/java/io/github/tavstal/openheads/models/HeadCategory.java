package io.github.tavstal.openheads.models;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

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

    private static Yaml createYaml() {
        // Create LoaderOptions
        LoaderOptions loaderOptions = new LoaderOptions();

        // Set up Constructor with LoaderOptions
        Constructor constructor = new Constructor(loaderOptions);
        TypeDescription dimensionDataDescription = new TypeDescription(HeadData.class);
        constructor.addTypeDescription(dimensionDataDescription);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);

        Representer representer = new Representer(options);
        representer.addClassTag(HeadData.class, Tag.MAP);

        return new Yaml(constructor, representer, options);
    }

    public boolean Load() {
        Path dirPath = Paths.get(OpenHeads.Instance.getDataFolder().getPath(), "heads");
        Path filePath = Paths.get(dirPath.toString(), _file);
        if (!Files.exists(filePath))
            return false;

        InputStream inputStream;
        _logger.Debug(String.format("Reading %s head file...", Name));
        try
        {
            inputStream = new FileInputStream(filePath.toString());
        }
        catch (FileNotFoundException ex)
        {
            _logger.Error(String.format("Failed to get file. Path: %s", filePath));
            return false;
        }
        catch (Exception ex)
        {
            _logger.Warn("Unknown error happened while reading the file.");
            _logger.Error(ex.getMessage());
            return false;
        }

        Yaml yaml = createYaml();
        Object yamlObject = yaml.load(inputStream);
        if (!(yamlObject instanceof List))
        {
            _logger.Error("Failed to cast the yamlObject after reading the file data.");
            return false;
        }

        //noinspection unchecked
        _heads = (List<HeadData>)yamlObject;
        return true;
    }

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
