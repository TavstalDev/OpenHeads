package io.github.tavstal.openheads.models;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
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
    public String[] Tags;

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
        Tags = tags.split(",");
        Texture = texture;
    }

    /**
     * Gets the icon representing the head data for the specified player and category.
     * If the texture is not set, a zombie head icon is returned.
     * If the texture is set, a player head icon with the specified texture is returned.
     *
     * @param player The player for whom the icon is being generated.
     * @param category The category of the head data.
     * @return The ItemStack representing the head icon.
     */
    public ItemStack GetIcon(Player player, HeadCategory category) {
        List<Component> loreList = new ArrayList<>() {{
            add(ChatUtils.translateColors(String.format("&8%s", OpenHeads.Instance.Localize(player, category.DisplayNameKey)), true));
        }};
        String displayName = OpenHeads.Instance.Localize(player, "GUI.HeadFormat")
                .replace("%head%", Name);

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
}
