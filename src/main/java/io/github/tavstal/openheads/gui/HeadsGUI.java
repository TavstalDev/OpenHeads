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

public class HeadsGUI {
    private static final PluginLogger _logger = OpenHeads.Logger().WithModule(HeadsGUI.class);

    private static final Integer[] SlotPlaceholders = {
               46, 47,              51, 52, 53,
    };

    public static SGMenu create(@NotNull Player player) {
        try {
            SGMenu menu = OpenHeads.GetGUI().create("...", 6);

            // Create Placeholders
            SGButton placeholderButton = new SGButton(GUIHelper.createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
            for (Integer slot : SlotPlaceholders) {
                menu.setButton(0, slot, placeholderButton);
            }

            // Close Button
            SGButton closeButton = new SGButton(
                    GUIHelper.createItem(Material.BARRIER, OpenHeads.Instance.Localize(player, "GUI.Close")))
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
                        int maxPage = 1 + (HeadUtils.getHeadCategories().size() / 28);
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
        if (!playerData.getSearch().isEmpty())
            playerData.getHeadsMenu().setName(OpenHeads.Instance.Localize(player, "GUI.SearchTitle")
                    .replace("%search%", playerData.getSearch())
            );
        if (playerData.getSearchCategory() != null)
            playerData.getHeadsMenu().setName(OpenHeads.Instance.Localize(player, "GUI.CategoryTitle")
                    .replace("%category%", OpenHeads.Instance.Localize(player,playerData.getSearchCategory().DisplayNameKey))
            );
        playerData.refreshHeads();
        player.openInventory(playerData.getHeadsMenu().getInventory());
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
            int mapIndex = 0;
            var keyArray = playerData.getHeads().keySet().stream().toList();
            for (int i = 0; i < 45; i++) {
                int index = i + (page - 1) * 45;
                String key = keyArray.get(mapIndex);
                List<HeadData> heads = playerData.getHeads().get(key);
                if (index >= heads.size()) {
                    if (mapIndex + 1 < keyArray.size()) {
                        mapIndex++;
                        --index;
                        continue;
                    }
                    playerData.getHeadsMenu().removeButton(0, i);
                    continue;
                }

                HeadData head = heads.get(index);
                var category = HeadUtils.getCategory(key);
                if (category == null)
                {
                    _logger.Warn("Failed to find category for head data.");
                    continue;
                }

                playerData.getHeadsMenu().setButton(0, i, new SGButton(head.GetIcon(player, category.DisplayNameKey)).withListener((InventoryClickEvent event) -> {
                    if (category.Price > 0 && !EconomyUtils.has(player, category.Price)) {
                        OpenHeads.Instance.sendLocalizedMsg(player, "General.NotEnoughMoney");
                        return;
                    }

                    player.getInventory().addItem(head.GetItem(player, category.DisplayNameKey));
                    if (category.Price > 0) {
                        EconomyUtils.withdraw(player, category.Price);
                        OpenHeads.Instance.sendLocalizedMsg(player, "General.BoughtHead", new HashMap<>() {{
                            put("price", String.format("%.2f", category.Price));
                            put("head", head.Name);
                        }});
                    }
                    else
                        OpenHeads.Instance.sendLocalizedMsg(player, "General.ReceivedHead", new HashMap<>() {{
                            put("head", head.Name);
                        }});
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
