package io.github.tavstaldev.openheads.gui;

import io.github.tavstaldev.minecorelib.managers.MenuManager;
import io.github.tavstaldev.minecorelib.models.gui.MenuBase;
import io.github.tavstaldev.minecorelib.models.gui.MenuButton;
import io.github.tavstaldev.minecorelib.shadow.spigui.buttons.SGButton;
import io.github.tavstaldev.minecorelib.shadow.spigui.menu.SGMenu;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openheads.OpenHeads;
import io.github.tavstaldev.openheads.managers.PlayerCacheManager;
import io.github.tavstaldev.openheads.models.HeadCategory;
import io.github.tavstaldev.openheads.models.HeadData;
import io.github.tavstaldev.openheads.models.PlayerCache;
import io.github.tavstaldev.openheads.utils.EconomyUtils;
import io.github.tavstaldev.openheads.utils.HeadUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class HeadsGUI extends MenuBase {

    public static String ID = "heads";

    public HeadsGUI() {
        super(OpenHeads.Instance, "heads.yml");
    }

    @Override
    protected void loadDefaults() {
        menuTitle = "";
        isMenuTitleTranslated = false; // disable it
        menuSize = resolveGet("size", 6);
        dynamicSlots = resolveDynamicSlots(new LinkedHashMap<>() {{
            put("head_slots", new ArrayList<>() {{
                add("0-44");
            }});
        }});
        menuButtons = resolveButtons(new LinkedHashSet<>() {{
            // Placeholder
            add(new MenuButton(Material.BLACK_STAINED_GLASS_PANE, null, 1, "§r", null, null, null, null, List.of("46", "47", "51", "52", "53"), null));
            // Back button
            add(new MenuButton(Material.SPRUCE_DOOR, null, 1, null, "GUI.Back", null, null, 45, null,  List.of("[OPEN] " + CategoryGUI.ID)));
            // Previous button
            add(new MenuButton(Material.ARROW, null, 1, null, "GUI.PreviousPage", null, null, 48, null,List.of("[PREV_PAGE]")));
            // Page button, NOTE: should be updated on refresh
            add(new MenuButton(Material.PAPER, null, 1, "{PAGE}", null, null, null, 49, null, null));
            // Next button
            add(new MenuButton(Material.ARROW, null, 1, null, "GUI.NextPage", null, null, 50, null, List.of("[NEXT_PAGE]")));
        }});
    }

    @Override
    public SGMenu create(@NotNull Player player) {
         MenuManager menuManager = OpenHeads.Instance.getMenuManager();
         if (menuManager == null)
             throw new RuntimeException("Menu manager was not initialized.");
         SGMenu menu = menuManager.getSpiGUI().create("...", menuSize);

         for (MenuButton button : menuButtons) {
             button.apply(player, translator, menu, this);
         }

         return menu;
    }

    @Override
    public void refresh(@NotNull Player player, @NotNull SGMenu sgMenu) {
        PlayerCache playerData = PlayerCacheManager.getPlayerData(player.getUniqueId());

        // 1. Find page button
        MenuButton pageButton = null;
        for (MenuButton btn : menuButtons) {
            if (btn.getTitle() != null && btn.getTitle().equalsIgnoreCase("{PAGE}")) {
                pageButton = btn;
                break;
            }
        }

        // 2. Update page button
        if (pageButton != null) {
            String pageText = translator.localize(player,  "GUI.Page", Map.of(
                    "page", String.valueOf(playerData.getHeadsPage()) // Localize the page number
            ));
            Component pageComp = ChatUtils.translateColors(pageText, true);

            for (Integer slot : pageButton.getSlots()) {
                SGButton btn = sgMenu.getButton(0, slot);
                if (btn == null)
                    continue;

                ItemStack icon = btn.getIcon();
                ItemMeta meta = icon.getItemMeta();
                if (meta != null) {
                    meta.displayName(pageComp);
                    icon.setItemMeta(meta);
                }
                btn.setIcon(icon);
            }
        }

        // 3. Handle dynamic slots
        List<Integer> dynamicSlots = this.dynamicSlots.getOrDefault("head_slots", new ArrayList<>());
        int page = playerData.getHeadsPage();
        List<Map.Entry<String, HeadData>> heads = playerData.getHeads();
        for (int i = 0; i < dynamicSlots.size(); i++) {
            int index = i + (page - 1) * dynamicSlots.size();
            int slot = dynamicSlots.get(i);

            if (index >= heads.size()) {
                sgMenu.removeButton(0, slot);
                continue;
            }

            Map.Entry<String, HeadData> head = heads.get(index);
            HeadCategory category = HeadUtils.getCategory(head.getKey());
            if (category == null) {
                logger.warn("Failed to find category for head data.");
                continue;
            }

            sgMenu.setButton(0, slot, new SGButton(head.getValue().getIcon(player, category.Name, category.DisplayNameKey)).withListener(event ->
            {
                // Handle left-click events: Buy or receive the head
                if (event.isLeftClick()) {
                    var price = category.Price;
                    var headValue = head.getValue();

                    // Check if the player has enough money to buy the head
                    if (price > 0) {
                        if (!EconomyUtils.has(player, price)) {
                            plugin.sendLocalizedMsg(player, "General.NotEnoughMoney");
                            return;
                        }

                        // Deduct the price and add the head to the player's inventory
                        EconomyUtils.withdraw(player, price);
                        player.getInventory().addItem(headValue.getItem(player, category.DisplayNameKey));
                        plugin.sendLocalizedMsg(player, "General.BoughtHead", Map.of(
                                "price", String.format("%.2f", price), // Format the price to two decimal places
                                "head", headValue.Name
                        ));
                        return;
                    }

                    // Add the head to the player's inventory for free
                    player.getInventory().addItem(headValue.getItem(player, category.DisplayNameKey));
                    plugin.sendLocalizedMsg(player, "General.ReceivedHead", Map.of(
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
                    if (OpenHeads.Database.isFavorite(playerId, headKey, headValue.Name)) {
                        OpenHeads.Database.removeFavorite(playerId, headKey, headValue.Name);
                    } else {
                        OpenHeads.Database.addFavorite(playerId, headKey, headValue.Name);
                    }

                    // Refresh the slot to reflect the changes
                    refreshSlot(player, slot);
                }
            }));
        }
        player.openInventory(sgMenu.getInventory());
    }

    @Override
    public void executeCommand(@NotNull Player player, @NotNull String command) {
        String[] parts = command.split("\\s+");
        switch (parts[0].toLowerCase()) {
            case "[next_page]" -> {
                PlayerCache playerData = PlayerCacheManager.getPlayerData(player.getUniqueId());
                int maxPage = 1 + (playerData.getHeads().size() / dynamicSlots.getOrDefault("head_slots", new ArrayList<>()).size());
                if (playerData.getHeadsPage() + 1 > maxPage)
                    return;
                playerData.setHeadsPage(playerData.getHeadsPage() + 1);

                MenuManager manager = OpenHeads.Instance.getMenuManager();
                if (manager == null)
                    break;
                SGMenu menu = manager.getMenu(player, ID);
                if (menu == null)
                    break;
                refresh(player, menu);
            }
            case "[prev_page]" -> {
                PlayerCache playerData = PlayerCacheManager.getPlayerData(player.getUniqueId());
                if (playerData.getHeadsPage() - 1 <= 0)
                    return;
                playerData.setHeadsPage(playerData.getHeadsPage() - 1);

                MenuManager manager = OpenHeads.Instance.getMenuManager();
                if (manager == null)
                    break;
                SGMenu menu = manager.getMenu(player, ID);
                if (menu == null)
                    break;
                refresh(player, menu);
            }
            case "[open]" -> {
                if (parts.length < 2)
                    return;
                String menuId = parts[1];
                MenuManager manager = OpenHeads.Instance.getMenuManager();
                if (manager != null)
                    manager.open(player, menuId);
            }
        }
    }

    @Override
    public void onOpen(@NotNull Player player) {
        PlayerCache playerData = PlayerCacheManager.getPlayerData(player.getUniqueId());
        playerData.setHeadsPage(1);

        String menuName = null;
        String playerSearch = playerData.getSearch();

        MenuManager manager = OpenHeads.Instance.getMenuManager();
        if (manager != null) {
            SGMenu menu = manager.getMenu(player, ID);
            if (menu != null) {
                // Determine the menu name based on the player's current state
                if (playerData.isFavorite()) {
                    // If the player is viewing their favorites, set the menu name to the localized "FavoriteTitle"
                    menuName = plugin.localize(player, "GUI.FavoriteTitle");
                } else if (playerSearch != null && !playerSearch.isBlank()) {
                    // If the player has performed a search, set the menu name to the localized "SearchTitle"
                    // and include the search term in the localization
                    menuName = plugin.localize(player, "GUI.SearchTitle",
                            Map.of("search", playerSearch)
                    );
                } else if (playerData.getSearchCategory() != null) {
                    // If the player is viewing a specific category, set the menu name to the localized "CategoryTitle"
                    // and include the category's display name in the localization
                    String category = plugin.localize(player, playerData.getSearchCategory().DisplayNameKey);
                    menuName = plugin.localize(player, "GUI.CategoryTitle",
                            Map.of("category", category)
                    );
                }

                if (menuName != null)
                    menu.setName(menuName);

                playerData.refreshHeads();
                refresh(player, menu);
            }
        }
    }

    @Override
    public void onClose(@NotNull Player player) {
        PlayerCache playerData = PlayerCacheManager.getPlayerData(player.getUniqueId());
        playerData.freeHeads();
        playerData.setSearchCategory(null);
    }

    private void refreshSlot(@NotNull Player player, int slot) {
        try {
            // Retrieve the player's data
            PlayerCache playerData = PlayerCacheManager.getPlayerData(player.getUniqueId());
            int page = playerData.getHeadsPage();

            // Calculate the index of the head based on the current page and slot
            int index = slot + (page - 1) * dynamicSlots.getOrDefault("head_slots", new ArrayList<>()).size();
            List<Map.Entry<String, HeadData>> heads = playerData.getHeads();

            MenuManager manager = OpenHeads.Instance.getMenuManager();
            if (manager == null)
                return;

            SGMenu menu = manager.getMenu(player, ID);
            if (menu == null)
                return;

            // If the index is out of bounds, remove the button from the slot
            if (index >= heads.size()) {
                menu.removeButton(0, slot);
                return;
            }

            // Retrieve the head data and its category
            Map.Entry<String, HeadData> head = heads.get(index);
            var category = HeadUtils.getCategory(head.getKey());

            // Log a warning if the category is not found and exit
            if (category == null) {
                logger.warn("Failed to find category for head data.");
                return;
            }

            // Set a button in the specified slot with the head's icon and listener
            menu.setButton(0, slot, new SGButton(head.getValue().getIcon(player, category.Name, category.DisplayNameKey)).withListener(event ->
                    {
                        // Handle left-click events: Buy or receive the head
                        if (event.isLeftClick()) {
                            var price = category.Price;
                            var headValue = head.getValue();

                            // Check if the player has enough money to buy the head
                            if (price > 0) {
                                if (!EconomyUtils.has(player, price)) {
                                    plugin.sendLocalizedMsg(player, "General.NotEnoughMoney");
                                    return;
                                }

                                // Deduct the price and add the head to the player's inventory
                                EconomyUtils.withdraw(player, price);
                                player.getInventory().addItem(headValue.getItem(player, category.DisplayNameKey));
                                plugin.sendLocalizedMsg(player, "General.BoughtHead", Map.of(
                                        "price", String.format("%.2f", price),
                                        "head", headValue.Name
                                ));
                                return;
                            }

                            // Add the head to the player's inventory for free
                            player.getInventory().addItem(headValue.getItem(player, category.DisplayNameKey));
                            plugin.sendLocalizedMsg(player, "General.ReceivedHead", Map.of(
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
                            if (OpenHeads.Database.isFavorite(playerId, headKey, headValue.Name)) {
                                OpenHeads.Database.removeFavorite(playerId, headKey, headValue.Name);
                            } else {
                                OpenHeads.Database.addFavorite(playerId, headKey, headValue.Name);
                            }

                            // Refresh the slot to reflect the changes
                            refreshSlot(player, slot);
                        }
                    })
            );

            // Open the updated inventory for the player
            player.openInventory(menu.getInventory());
        } catch (Exception ex) {
            // Log any errors that occur during the slot refresh process
            logger.error("An error occurred while refreshing one of the slots of the heads GUI.");
            logger.error(ex);
        }
    }
}
