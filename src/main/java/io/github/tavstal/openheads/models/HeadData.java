package io.github.tavstal.openheads.models;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.github.tavstal.minecorelib.utils.ChatUtils;
import io.github.tavstal.openheads.OpenHeads;
import io.github.tavstal.openheads.helpers.GUIHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HeadData {
    /**
     * The name of the head data.
     */
    public String Name;

    /**
     * The tags associated with the head data.
     */
    public String Tags;

    /**
     * The texture associated with the head data.
     */
    public String Texture;

    /**
     * Constructs a new HeadData object with the specified parameters.
     *
     * @param name The name of the head data.
     * @param tags The tags associated with the head data.
     * @param texture The texture associated with the head data.
     */
    public HeadData(String name, String tags, String texture) {
        Name = name;
        Tags = tags;
        Texture = texture;
    }

    /**
     * Splits the tags associated with the head data into an array of individual tags.
     *
     * @return an array of strings, each representing a tag
     */
    public String[] GetTags() {
        return Tags.split(",");
    }

    /**
     * Creates an ItemStack representing the head icon for the player.
     *
     * @param player the player for whom the icon is being created
     * @param categoryDisplayNameKey the key for the category display name localization
     * @return the ItemStack representing the head icon
     */
    public ItemStack GetIcon(Player player, String category, String categoryDisplayNameKey) {
        List<Component> loreList = new ArrayList<>();
        boolean isFavorite = OpenHeads.Database.IsFavorite(player.getUniqueId(), category, Name);
        String favoriteTxt = OpenHeads.Instance.Localize(player, isFavorite ? "GUI.Favorite.Remove" : "GUI.Favorite.Add");
        String categoryTxt = OpenHeads.Instance.Localize(player, categoryDisplayNameKey);
        for (String lore : OpenHeads.Instance.LocalizeList(player, "GUI.HeadLore"))
            loreList.add(ChatUtils.translateColors(lore
                    .replace("%category%", categoryTxt)
                    .replace("%favorite%", favoriteTxt),
                    true)
            );

        String displayName = OpenHeads.Instance.Localize(player, "GUI.HeadFormat")
                .replace("%head%", Name)
                .replace("%favorite%", OpenHeads.Instance.Localize(player, isFavorite ? "GUI.Favorite.Yes" : "GUI.Favorite.No"));

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
            return result;
        }
        catch (Exception ex) {
            OpenHeads.Logger().Error("Failed to get category icon.");
            OpenHeads.Logger().Error(ex.getMessage());
            return GUIHelper.createItem(Material.ZOMBIE_HEAD, displayName, loreList);
        }
    }

    /**
     * Creates an ItemStack representing the head item for the player.
     *
     * @param player the player for whom the item is being created
     * @param categoryDisplayNameKey the key for the category display name localization
     * @return the ItemStack representing the head item
     */
    public ItemStack GetItem(Player player, String categoryDisplayNameKey) {
        List<Component> loreList = new ArrayList<>() {{
            add(ChatUtils.translateColors(String.format("&8%s", OpenHeads.Instance.Localize(player, categoryDisplayNameKey)), true));
        }};
        String displayName = OpenHeads.Instance.Localize(player, "GUI.HeadFormat")
                .replace("%head%", Name);

        try
        {
            if (Texture == null) {
                ItemStack item = new ItemStack(Material.ZOMBIE_HEAD, 1);
                ItemMeta meta = item.getItemMeta();
                // Set display name
                meta.displayName(ChatUtils.translateColors(displayName, true));
                // Set lore
                meta.lore(loreList);
                item.setItemMeta(meta);
                return item;
            }
            ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
            ItemMeta meta = item.getItemMeta();
            // Set display name
            meta.displayName(ChatUtils.translateColors(displayName, true));
            // Set lore
            meta.lore(loreList);
            if (meta instanceof SkullMeta skullMeta) {
                PlayerProfile profile = Bukkit.createProfile(UUID.fromString("bbde04e7-ccb9-49a8-8ad8-08d11b6540d4"));
                profile.setProperty(new ProfileProperty("textures", Texture));
                skullMeta.setPlayerProfile(profile);
                item.setItemMeta(skullMeta);
            }
            else
                item.setItemMeta(meta);
            return item;
        }
        catch (Exception ex) {
            OpenHeads.Logger().Error("Failed to get category icon.");
            OpenHeads.Logger().Error(ex.getMessage());

            ItemStack item = new ItemStack(Material.ZOMBIE_HEAD, 1);
            ItemMeta meta = item.getItemMeta();
            // Set display name
            meta.displayName(ChatUtils.translateColors(displayName, true));
            // Set lore
            meta.lore(loreList);
            item.setItemMeta(meta);
            return item;
        }
    }
}
