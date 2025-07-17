package io.github.tavstaldev.openheads.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.GuiUtils;
import io.github.tavstaldev.openheads.OpenHeads;
import io.github.tavstaldev.openheads.managers.PlayerManager;
import io.github.tavstaldev.openheads.models.HeadCategory;
import io.github.tavstaldev.openheads.models.PlayerData;
import io.github.tavstaldev.openheads.utils.HeadUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MainGUI {
    private static final PluginLogger _logger = OpenHeads.Logger().WithModule(MainGUI.class);
    private static final OpenHeads _plugin = OpenHeads.Instance;

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
            var playerId = player.getUniqueId();
            SGMenu menu = OpenHeads.GetGUI().create(_plugin.Localize(player, "GUI.MainTitle"), 6);

            // Create Placeholders
            SGButton placeholderButton = new SGButton(GuiUtils.createItem(OpenHeads.Instance, Material.BLACK_STAINED_GLASS_PANE, " "));
            for (Integer slot : SlotPlaceholders) {
                menu.setButton(0, slot, placeholderButton);
            }

            // Close Button
            SGButton closeButton = new SGButton(
                    GuiUtils.createItem(OpenHeads.Instance, Material.BARRIER, _plugin.Localize(player, "GUI.Close"))
            ).withListener(event -> close(player));
            menu.setButton(0, 45, closeButton);

            // Previous Page Button
            SGButton prevPageButton = new SGButton(
                    GuiUtils.createItem(OpenHeads.Instance, Material.ARROW, _plugin.Localize(player, "GUI.PreviousPage"))
            ).withListener(event -> {
                PlayerData playerData = PlayerManager.getPlayerData(playerId);
                if (playerData.getMainPage() > 1) {
                    playerData.setMainPage(playerData.getMainPage() - 1);
                    refresh(player);
                }
            });
            menu.setButton(0, 48, prevPageButton);

            // Page Indicator
            SGButton pageButton = new SGButton(
                    GuiUtils.createItem(
                            OpenHeads.Instance,
                            Material.PAPER, 
                            _plugin.Localize(player, "GUI.Page", Map.of("page", "1"))
                    )
            );
            menu.setButton(0, 49, pageButton);

            // Next Page Button
            SGButton nextPageButton = new SGButton(
                    GuiUtils.createItem(OpenHeads.Instance, Material.ARROW, _plugin.Localize(player, "GUI.NextPage"))
            ).withListener(event -> {
                PlayerData playerData = PlayerManager.getPlayerData(playerId);
                int maxPage = 1 + HeadUtils.getHeadCategories().size() / 28;
                if (playerData.getMainPage() < maxPage) {
                    playerData.setMainPage(playerData.getMainPage() + 1);
                    refresh(player);
                }
            });
            menu.setButton(0, 50, nextPageButton);

            // Favorites Button
            SGButton favoriteButton = new SGButton(
                    GuiUtils.createItem(OpenHeads.Instance,Material.NETHER_STAR, _plugin.Localize(player, "GUI.Favorites"))
            ).withListener(event -> {
                PlayerData data = PlayerManager.getPlayerData(playerId);
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
                    GuiUtils.createItem(OpenHeads.Instance, Material.COMPASS, _plugin.Localize(player, "GUI.Search"))
            ).withListener(event -> {
                PlayerData data = PlayerManager.getPlayerData(playerId);
                close(player);
                data.setHeadsPage(1);
                data.setSearchCategory(null);
                data.setFavorite(false);
                data.getSignMenu().open(player);
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
            var playerId = player.getUniqueId();
            PlayerData playerData = PlayerManager.getPlayerData(playerId);

            // Page Indicator
            SGButton pageButton = new SGButton(
                    GuiUtils.createItem(OpenHeads.Instance, Material.PAPER, _plugin.Localize(player, "GUI.Page", Map.of(
                            "page", String.valueOf(playerData.getMainPage())))
                    )
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
                    PlayerData data = PlayerManager.getPlayerData(playerId);
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
