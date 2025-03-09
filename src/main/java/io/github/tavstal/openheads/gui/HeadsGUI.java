package io.github.tavstal.openheads.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.openheads.OpenHeads;
import io.github.tavstal.openheads.helpers.GUIHelper;
import io.github.tavstal.openheads.managers.PlayerManager;
import io.github.tavstal.openheads.models.HeadData;
import io.github.tavstal.openheads.models.PlayerData;
import io.github.tavstal.openheads.utils.EconomyUtils;
import io.github.tavstal.openheads.utils.HeadUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeadsGUI {
    private static final PluginLogger _logger = OpenHeads.Logger().WithModule(HeadsGUI.class);

    private static final Integer[] SlotPlaceholders = {
               46, 47,              51, 52, 53,
    };

    /**
     * Creates the heads GUI for the specified player.
     *
     * @param player The player for whom the GUI is being created.
     * @return The created SGMenu instance.
     */
    public static SGMenu create(@NotNull Player player) {
        try {
            SGMenu menu = OpenHeads.GetGUI().create("...", 6);

            // Create Placeholders
            SGButton placeholderButton = new SGButton(GUIHelper.createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
            for (Integer slot : SlotPlaceholders) {
                menu.setButton(0, slot, placeholderButton);
            }

            // Back Button
            SGButton closeButton = new SGButton(
                    GUIHelper.createItem(Material.SPRUCE_DOOR, OpenHeads.Instance.Localize(player, "GUI.Back")))
                    .withListener((InventoryClickEvent event) -> {
                        close(player);
                        MainGUI.open(player);
                    });
            menu.setButton(0, 45, closeButton);

            // Previous Page Button
            SGButton prevPageButton = new SGButton(
                    GUIHelper.createItem(Material.ARROW, OpenHeads.Instance.Localize(player, "GUI.PreviousPage")))
                    .withListener((InventoryClickEvent event) -> {
                        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
                        if (playerData.getHeadsPage() - 1 <= 0)
                            return;
                        playerData.setHeadsPage(playerData.getHeadsPage() - 1);
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
                        int maxPage = 1 + (playerData.getHeads().size() / 45);
                        if (playerData.getHeadsPage() + 1 > maxPage)
                            return;
                        playerData.setHeadsPage(playerData.getHeadsPage() + 1);
                        refresh(player);
                    });
            menu.setButton(0, 50, nextPageButton);
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
        playerData.setHeadsPage(1);
        if (playerData.isFavorite())
            playerData.getHeadsMenu().setName(OpenHeads.Instance.Localize(player, "GUI.FavoriteTitle"));
        if (playerData.getSearch() != null && !playerData.getSearch().isBlank())
            playerData.getHeadsMenu().setName(OpenHeads.Instance.Localize(player, "GUI.SearchTitle")
                    .replace("%search%", playerData.getSearch())
            );
        if (playerData.getSearchCategory() != null)
            playerData.getHeadsMenu().setName(OpenHeads.Instance.Localize(player, "GUI.CategoryTitle")
                    .replace("%category%", OpenHeads.Instance.Localize(player,playerData.getSearchCategory().DisplayNameKey))
            );
        playerData.refreshHeads();
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
        playerData.freeHeads();
        playerData.setSearchCategory(null);
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
                            .replace("%page%", String.valueOf(playerData.getHeadsPage())))
            );
            playerData.getHeadsMenu().setButton(0, 49, pageButton);

            int page = playerData.getHeadsPage();
            for (int i = 0; i < 45; i++) {
                int index = i + (page - 1) * 45;
                List<Map.Entry<String, HeadData>> heads = playerData.getHeads();
                if (index >= heads.size()) {
                    playerData.getHeadsMenu().removeButton(0, i);
                    continue;
                }

                Map.Entry<String, HeadData> head = heads.get(index);
                var category = HeadUtils.getCategory(head.getKey());
                if (category == null)
                {
                    _logger.Warn("Failed to find category for head data.");
                    continue;
                }

                playerData.getHeadsMenu().setButton(0, i, new SGButton(head.getValue().GetIcon(player, category.Name, category.DisplayNameKey)).withListener((InventoryClickEvent event) -> {
                    if (event.isLeftClick()) {
                        if (category.Price > 0 && !EconomyUtils.has(player, category.Price)) {
                            OpenHeads.Instance.sendLocalizedMsg(player, "General.NotEnoughMoney");
                            return;
                        }

                        player.getInventory().addItem(head.getValue().GetItem(player, category.DisplayNameKey));
                        if (category.Price > 0) {
                            EconomyUtils.withdraw(player, category.Price);
                            OpenHeads.Instance.sendLocalizedMsg(player, "General.BoughtHead", new HashMap<>() {{
                                put("price", String.format("%.2f", category.Price));
                                put("head", head.getValue().Name);
                            }});
                        } else
                            OpenHeads.Instance.sendLocalizedMsg(player, "General.ReceivedHead", new HashMap<>() {{
                                put("head", head.getValue().Name);
                            }});
                    }
                    if (event.isRightClick()) {
                        if (OpenHeads.Database.IsFavorite(player.getUniqueId(), head.getKey(), head.getValue().Name)) {
                            OpenHeads.Database.RemoveFavorite(player.getUniqueId(), head.getKey(), head.getValue().Name);
                        }
                        else {
                            OpenHeads.Database.AddFavorite(player.getUniqueId(), head.getKey(), head.getValue().Name);
                        }
                        refresh(player);
                    }
                }));
            }
            player.openInventory(playerData.getHeadsMenu().getInventory());
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while refreshing the main GUI.");
            _logger.Error(ex);
        }
    }
}
