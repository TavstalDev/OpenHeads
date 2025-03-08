package io.github.tavstal.openheads.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import de.rapha149.signgui.SignGUI;
import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.openheads.OpenHeads;
import io.github.tavstal.openheads.helpers.GUIHelper;
import io.github.tavstal.openheads.managers.PlayerManager;
import io.github.tavstal.openheads.models.HeadCategory;
import io.github.tavstal.openheads.models.PlayerData;
import io.github.tavstal.openheads.utils.HeadUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class MainGUI {
    private static final PluginLogger _logger = OpenHeads.Logger().WithModule(MainGUI.class);

    private static final Integer[] SlotPlaceholders = {
            0,  1,  2,  3,  4,  5,  6,  7,  8,
            9,                              17,
            18,                             26,
            27,                             35,
            36,                             44,
                46, 47,             51,
    };

    /**
     * Creates the GUI for the specified player.
     *
     * @param player The player for whom the GUI is being created.
     * @return The created SGMenu instance.
     */
    public static SGMenu create(@NotNull Player player) {
        try {
            SGMenu menu = OpenHeads.GetGUI().create(OpenHeads.Instance.Localize(player, "GUI.MainTitle"), 6);

            // Create Placeholders
            SGButton placeholderButton = new SGButton(GUIHelper.createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
            for (Integer slot : SlotPlaceholders) {
                menu.setButton(0, slot, placeholderButton);
            }

            // Close Button
            SGButton closeButton = new SGButton(
                    GUIHelper.createItem(Material.BARRIER, OpenHeads.Instance.Localize(player, "GUI.Close")))
                    .withListener((InventoryClickEvent event) -> close(player));
            menu.setButton(0, 45, closeButton);

            // Previous Page Button
            SGButton prevPageButton = new SGButton(
                    GUIHelper.createItem(Material.ARROW, OpenHeads.Instance.Localize(player, "GUI.PreviousPage")))
                    .withListener((InventoryClickEvent event) -> {
                        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
                        if (playerData.getMainPage() - 1 <= 0)
                            return;
                        playerData.setMainPage(playerData.getMainPage() - 1);
                        refresh(player);
                    });
            menu.setButton(0, 48, prevPageButton);

            // Page Indicator
            SGButton pageButton = new SGButton(
                    GUIHelper.createItem(Material.PAPER, OpenHeads.Instance.Localize(player, "GUI.Page").replace("%page%", "1"))
            );
            menu.setButton(0, 49, pageButton);

            // Next Page Button
            SGButton nextPageButton = new SGButton(
                    GUIHelper.createItem(Material.ARROW, OpenHeads.Instance.Localize(player, "GUI.NextPage")))
                    .withListener((InventoryClickEvent event) -> {
                        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
                        int maxPage = 1 + (HeadUtils.getHeadCategories().size() / 28);
                        if (playerData.getMainPage() + 1 > maxPage)
                            return;
                        playerData.setMainPage(playerData.getMainPage() + 1);
                        refresh(player);
                    });
            menu.setButton(0, 50, nextPageButton);

            // Favorites Button
            SGButton favoriteButton = new SGButton(
                    GUIHelper.createItem(Material.NETHER_STAR, OpenHeads.Instance.Localize(player, "GUI.Favorites")))
                    .withListener((InventoryClickEvent event) -> {
                        PlayerData data = PlayerManager.getPlayerData(player.getUniqueId());
                        close(player);
                        data.setHeadsPage(1);
                        data.setSearchCategory(null);
                        data.setFavorite(true);
                        data.setSearch(null);
                        HeadsGUI.open(player);
                    });
            menu.setButton(0, 52, favoriteButton);

            // Search Button
            SGButton searchButton = new SGButton(
                    GUIHelper.createItem(Material.COMPASS, OpenHeads.Instance.Localize(player, "GUI.Search")))
                    .withListener((InventoryClickEvent event) -> {
                        PlayerData data = PlayerManager.getPlayerData(player.getUniqueId());
                        close(player);
                        data.setHeadsPage(1);
                        data.setSearchCategory(null);
                        data.setFavorite(false);
                        SignGUI gui = data.getSignMenu();
                        // open the sign
                        gui.open(player);
                    });
            menu.setButton(0, 53, searchButton);
            return menu;
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while creating the main GUI.");
            _logger.Error(ex);
            return null;
        }
    }

    /**
     * Opens the GUI for the specified player.
     *
     * @param player The player for whom the GUI is being opened.
     */
    public static void open(@NotNull Player player) {
        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
        // Show the GUI
        playerData.setGUIOpened(true);
        playerData.setMainPage(1);
        player.openInventory(playerData.getMainMenu().getInventory());
        refresh(player);
    }

    /**
     * Closes the GUI for the specified player.
     *
     * @param player The player for whom the GUI is being closed.
     */
    public static void close(@NotNull Player player) {
        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
        player.closeInventory();
        playerData.setGUIOpened(false);
    }

    /**
     * Refreshes the GUI for the specified player.
     *
     * @param player The player for whom the GUI is being refreshed.
     */
    public static void refresh(@NotNull Player player) {
        try {
            PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());

            // Page Indicator
            SGButton pageButton = new SGButton(
                    GUIHelper.createItem(Material.PAPER, OpenHeads.Instance.Localize(player, "GUI.Page")
                            .replace("%page%", String.valueOf(playerData.getMainPage())))
            );
            playerData.getMainMenu().setButton(0, 49, pageButton);

            var heads = HeadUtils.getHeadCategories();
            int page = playerData.getMainPage();

            for (int i = 0; i < 28; i++) {
                int index = i + (page - 1) * 28;
                int slot = i + 10 + (2 * (i / 7));
                if (index >= heads.size()) {
                    playerData.getMainMenu().removeButton(0, slot);
                    continue;
                }

                HeadCategory category = heads.get(index);
                playerData.getMainMenu().setButton(0, slot, new SGButton(category.GetIcon(player)).withListener((InventoryClickEvent event) -> {
                    PlayerData data = PlayerManager.getPlayerData(player.getUniqueId());
                    close(player);
                    data.setHeadsPage(1);
                    data.setSearchCategory(category);
                    data.setFavorite(false);
                    data.setSearch(null);
                    HeadsGUI.open(player);
                }));
            }
            player.openInventory(playerData.getMainMenu().getInventory());
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while refreshing the main GUI.");
            _logger.Error(ex);
        }
    }
}
