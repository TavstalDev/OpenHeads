package io.github.tavstaldev.openheads.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.GuiUtils;
import io.github.tavstaldev.openheads.OpenHeads;
import io.github.tavstaldev.openheads.managers.PlayerManager;
import io.github.tavstaldev.openheads.models.HeadData;
import io.github.tavstaldev.openheads.models.PlayerData;
import io.github.tavstaldev.openheads.utils.EconomyUtils;
import io.github.tavstaldev.openheads.utils.HeadUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class HeadsGUI {
    private static final PluginLogger _logger = OpenHeads.Logger().WithModule(HeadsGUI.class);
    private static final OpenHeads _plugin = OpenHeads.Instance;

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
            // Create a new GUI menu with 6 rows and a default title
            SGMenu menu = OpenHeads.GetGUI().create("...", 6);

            // Create placeholder buttons to fill specific slots in the GUI
            SGButton placeholderButton = new SGButton(GuiUtils.createItem(OpenHeads.Instance, Material.BLACK_STAINED_GLASS_PANE, " "));
            for (Integer slot : SlotPlaceholders) {
                // Set the placeholder button in the specified slots
                menu.setButton(0, slot, placeholderButton);
            }

            // Create a back button to return to the main GUI
            SGButton closeButton = new SGButton(
                    GuiUtils.createItem(OpenHeads.Instance, Material.SPRUCE_DOOR, _plugin.Localize(player, "GUI.Back")))
                    .withListener((InventoryClickEvent event) -> {
                        // Close the current GUI and open the main GUI
                        close(player);
                        MainGUI.open(player);
                    });
            // Set the back button in the bottom-left corner of the GUI
            menu.setButton(0, 45, closeButton);

            // Create a button to navigate to the previous page
            SGButton prevPageButton = new SGButton(
                    GuiUtils.createItem(OpenHeads.Instance, Material.ARROW, _plugin.Localize(player, "GUI.PreviousPage")))
                    .withListener((InventoryClickEvent event) -> {
                        // Retrieve the player's data
                        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
                        // Check if the current page is the first page
                        if (playerData.getHeadsPage() - 1 <= 0)
                            return;
                        // Decrement the page number and refresh the GUI
                        playerData.setHeadsPage(playerData.getHeadsPage() - 1);
                        refresh(player);
                    });
            // Set the previous page button in the bottom-left center of the GUI
            menu.setButton(0, 48, prevPageButton);

            // Create a page indicator button to display the current page number
            SGButton pageButton = new SGButton(
                    GuiUtils.createItem(OpenHeads.Instance, Material.PAPER, _plugin.Localize(player, "GUI.Page").replace("%page%", "1"))
            );
            // Set the page indicator button in the center of the bottom row
            menu.setButton(0, 49, pageButton);

            // Create a button to navigate to the next page
            SGButton nextPageButton = new SGButton(
                    GuiUtils.createItem(OpenHeads.Instance, Material.ARROW, _plugin.Localize(player, "GUI.NextPage")))
                    .withListener((InventoryClickEvent event) -> {
                        // Retrieve the player's data
                        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
                        // Calculate the maximum number of pages
                        int maxPage = 1 + (playerData.getHeads().size() / 45);
                        // Check if the current page is the last page
                        if (playerData.getHeadsPage() + 1 > maxPage)
                            return;
                        // Increment the page number and refresh the GUI
                        playerData.setHeadsPage(playerData.getHeadsPage() + 1);
                        refresh(player);
                    });
            // Set the next page button in the bottom-right center of the GUI
            menu.setButton(0, 50, nextPageButton);

            // Return the created menu
            return menu;
        } catch (Exception ex) {
            // Log an error if an exception occurs during the GUI creation process
            _logger.Error("An error occurred while creating the main GUI.");
            _logger.Error(ex);
            // Return null if the menu creation fails
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

        String menuName = null;
        var playerSearch = playerData.getSearch();

        // Determine the menu name based on the player's current state
        if (playerData.isFavorite()) {
            // If the player is viewing their favorites, set the menu name to the localized "FavoriteTitle"
            menuName = _plugin.Localize(player, "GUI.FavoriteTitle");
        } else if (playerSearch != null && !playerSearch.isBlank()) {
            // If the player has performed a search, set the menu name to the localized "SearchTitle"
            // and include the search term in the localization
            menuName = _plugin.Localize(player, "GUI.SearchTitle",
                    Map.of("search", playerSearch)
            );
        } else if (playerData.getSearchCategory() != null) {
            // If the player is viewing a specific category, set the menu name to the localized "CategoryTitle"
            // and include the category's display name in the localization
            var category = _plugin.Localize(player, playerData.getSearchCategory().DisplayNameKey);
            menuName = _plugin.Localize(player, "GUI.CategoryTitle",
                    Map.of("category", category)
            );
        }

        // If a menu name was determined, update the GUI's name
        if (menuName != null) {
            playerData.getHeadsMenu().setName(menuName);
        }
        
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
            // Retrieve the player's data
            PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());

            // Create a page indicator button displaying the current page number
            SGButton pageButton = new SGButton(
                    GuiUtils.createItem(
                            OpenHeads.Instance,
                            Material.PAPER,
                            _plugin.Localize(player, "GUI.Page", Map.of(
                                    "page", String.valueOf(playerData.getHeadsPage()) // Localize the page number
                            ))
                    )
            );
            // Set the page indicator button in the GUI
            playerData.getHeadsMenu().setButton(0, 49, pageButton);

            // Get the current page number
            int page = playerData.getHeadsPage();
            for (int i = 0; i < 45; i++) {
                // Calculate the index of the head based on the current page and slot
                int index = i + (page - 1) * 45;
                List<Map.Entry<String, HeadData>> heads = playerData.getHeads();

                // If the index is out of bounds, remove the button from the slot
                if (index >= heads.size()) {
                    playerData.getHeadsMenu().removeButton(0, i);
                    continue;
                }

                // Retrieve the head data and its category
                Map.Entry<String, HeadData> head = heads.get(index);
                var category = HeadUtils.getCategory(head.getKey());

                // Log a warning if the category is not found and skip this head
                if (category == null) {
                    _logger.Warn("Failed to find category for head data.");
                    continue;
                }

                int finalSlot = i;
                // Set a button in the specified slot with the head's icon and listener
                playerData.getHeadsMenu().setButton(0, i, new SGButton(head.getValue().GetIcon(player, category.Name, category.DisplayNameKey))
                        .withListener(event -> {
                            // Handle left-click events: Buy or receive the head
                            if (event.isLeftClick()) {
                                var price = category.Price;
                                var headValue = head.getValue();

                                // Check if the player has enough money to buy the head
                                if (price > 0) {
                                    if (!EconomyUtils.has(player, price)) {
                                        _plugin.sendLocalizedMsg(player, "General.NotEnoughMoney");
                                        return;
                                    }

                                    // Deduct the price and add the head to the player's inventory
                                    EconomyUtils.withdraw(player, price);
                                    player.getInventory().addItem(headValue.GetItem(player, category.DisplayNameKey));
                                    _plugin.sendLocalizedMsg(player, "General.BoughtHead",  Map.of(
                                            "price", String.format("%.2f", price), // Format the price to two decimal places
                                            "head", headValue.Name
                                    ));
                                    return;
                                }

                                // Add the head to the player's inventory for free
                                player.getInventory().addItem(headValue.GetItem(player, category.DisplayNameKey));
                                _plugin.sendLocalizedMsg(player, "General.ReceivedHead",  Map.of(
                                        "head", headValue.Name
                                ));
                                return;
                            }

                            // Handle right-click events: Add or remove the head from favorites
                            if (event.isRightClick()) {
                                var headValue = head.getValue();
                                var headKey = head.getKey();
                                var playerId = player.getUniqueId();

                                // Toggle the favorite status of the head
                                if (OpenHeads.Database.IsFavorite(playerId, headKey, headValue.Name)) {
                                    OpenHeads.Database.RemoveFavorite(playerId, headKey, headValue.Name);
                                } else {
                                    OpenHeads.Database.AddFavorite(playerId, headKey, headValue.Name);
                                }

                                // Refresh the slot to reflect the changes
                                refreshSlot(player, finalSlot);
                            }
                        }));
            }

            // Open the updated inventory for the player
            player.openInventory(playerData.getHeadsMenu().getInventory());
        } catch (Exception ex) {
            // Log any errors that occur during the GUI refresh process
            _logger.Error("An error occurred while refreshing the heads GUI.");
            _logger.Error(ex);
        }
    }

    /**
     * Refreshes a specific slot in the heads GUI for the specified player.
     *
     * @param player The player for whom the slot is being refreshed.
     * @param slot   The slot index to be refreshed.
     */
    public static void refreshSlot(@NotNull Player player, int slot) {
        try {
            // Retrieve the player's data
            PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
            int page = playerData.getHeadsPage();

            // Calculate the index of the head based on the current page and slot
            int index = slot + (page - 1) * 45;
            List<Map.Entry<String, HeadData>> heads = playerData.getHeads();

            // If the index is out of bounds, remove the button from the slot
            if (index >= heads.size()) {
                playerData.getHeadsMenu().removeButton(0, slot);
                return;
            }

            // Retrieve the head data and its category
            Map.Entry<String, HeadData> head = heads.get(index);
            var category = HeadUtils.getCategory(head.getKey());

            // Log a warning if the category is not found and exit
            if (category == null) {
                _logger.Warn("Failed to find category for head data.");
                return;
            }

            // Set a button in the specified slot with the head's icon and listener
            playerData.getHeadsMenu().setButton(0, slot, new SGButton(head.getValue().GetIcon(player, category.Name, category.DisplayNameKey))
                    .withListener(event -> {
                        // Handle left-click events: Buy or receive the head
                        if (event.isLeftClick()) {
                            var price = category.Price;
                            var headValue = head.getValue();

                            // Check if the player has enough money to buy the head
                            if (price > 0) {
                                if (!EconomyUtils.has(player, price)) {
                                    _plugin.sendLocalizedMsg(player, "General.NotEnoughMoney");
                                    return;
                                }

                                // Deduct the price and add the head to the player's inventory
                                EconomyUtils.withdraw(player, price);
                                player.getInventory().addItem(headValue.GetItem(player, category.DisplayNameKey));
                                _plugin.sendLocalizedMsg(player, "General.BoughtHead", Map.of(
                                        "price", String.format("%.2f", price),
                                        "head", headValue.Name
                                ));
                                return;
                            }

                            // Add the head to the player's inventory for free
                            player.getInventory().addItem(headValue.GetItem(player, category.DisplayNameKey));
                            _plugin.sendLocalizedMsg(player, "General.ReceivedHead", Map.of(
                                    "head", headValue.Name
                            ));
                            return;
                        }

                        // Handle right-click events: Add or remove the head from favorites
                        if (event.isRightClick()) {
                            var headValue = head.getValue();
                            var headKey = head.getKey();
                            var playerId = player.getUniqueId();

                            // Toggle the favorite status of the head
                            if (OpenHeads.Database.IsFavorite(playerId, headKey, headValue.Name)) {
                                OpenHeads.Database.RemoveFavorite(playerId, headKey, headValue.Name);
                            } else {
                                OpenHeads.Database.AddFavorite(playerId, headKey, headValue.Name);
                            }

                            // Refresh the slot to reflect the changes
                            refreshSlot(player, slot);
                        }
                    })
            );

            // Open the updated inventory for the player
            player.openInventory(playerData.getHeadsMenu().getInventory());
        } catch (Exception ex) {
            // Log any errors that occur during the slot refresh process
            _logger.Error("An error occurred while refreshing one of the slots of the heads GUI.");
            _logger.Error(ex);
        }
    }
}
