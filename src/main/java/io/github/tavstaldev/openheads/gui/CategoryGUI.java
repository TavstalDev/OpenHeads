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
import io.github.tavstaldev.openheads.models.PlayerCache;
import io.github.tavstaldev.openheads.utils.HeadUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CategoryGUI extends MenuBase {

    public static String ID = "categories";

    public CategoryGUI() {
        super(OpenHeads.Instance, "categories.yml");
    }

    @Override
    protected void loadDefaults() {
        menuTitle = resolveGet("title", "GUI.MainTitle");
        isMenuTitleTranslated = resolveGet("title_translated", true);
        menuSize = resolveGet("size", 6);
        dynamicSlots = resolveDynamicSlots(new LinkedHashMap<>() {{
            put("category_slots", new ArrayList<>() {{
                add("10-16");
                add("19-25");
                add("28-34");
                add("37-43");
            }});
        }});
        menuButtons = resolveButtons(new LinkedHashSet<>() {{
            // Placeholder
            add(new MenuButton(Material.BLACK_STAINED_GLASS_PANE, null, 1, "§r", null, null, null, null, List.of("0-9", "17-18", "26-27", "35-36", "44-47", "51"), null));
            // Back button
            add(new MenuButton(Material.SPRUCE_DOOR, null, 1, null, "GUI.Close", null, null, 45, null,  List.of("[CLOSE]")));
            // Previous button
            add(new MenuButton(Material.ARROW, null, 1, null, "GUI.PreviousPage", null, null, 48, null, List.of("[PREV_PAGE]")));
            // Page button, NOTE: should be updated on refresh
            add(new MenuButton(Material.PAPER, null, 1, "{PAGE}", null, null, null, 49, null, null));
            // Next button
            add(new MenuButton(Material.ARROW, null, 1, null, "GUI.NextPage", null, null, 50, null, List.of("[NEXT_PAGE]")));
            // Favorites button
            add(new MenuButton(Material.NETHER_STAR, null, 1, null, "GUI.Favorites", null, null, 52, null, List.of("[FAVORITES]")));
            // Search button
            add(new MenuButton(Material.COMPASS, null, 1, null, "GUI.Search", null, null, 53, null, List.of("[SEARCH]")));
        }});
    }

    @Override
    public SGMenu create(@NotNull Player player) {
        MenuManager menuManager = OpenHeads.Instance.getMenuManager();
        if (menuManager == null)
            throw new RuntimeException("Menu manager was not initialized.");
        SGMenu menu = menuManager.getSpiGUI().create(isMenuTitleTranslated ? translator.localize(player, menuTitle) : menuTitle, menuSize);

        for (MenuButton button : menuButtons) {
            button.apply(player, translator, menu, this);
        }
        return menu;
    }

    @Override
    public void refresh(@NotNull Player player, @NotNull SGMenu sgMenu) {
        UUID playerId = player.getUniqueId();
        PlayerCache playerData = PlayerCacheManager.getPlayerData(playerId);

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
                    "page", String.valueOf(playerData.getMainPage()) // Localize the page number
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
        List<Integer> dynamicSlots = this.dynamicSlots.getOrDefault("category_slots", new ArrayList<>());
        int page = playerData.getMainPage();
        List<HeadCategory> heads = HeadUtils.getHeadCategories();
        for (int i = 0; i < dynamicSlots.size(); i++) {
            int index = i + (page - 1) * dynamicSlots.size();
            int slot = dynamicSlots.get(i);

            if (index >= heads.size()) {
                sgMenu.removeButton(0, slot);
                continue;
            }

            HeadCategory category = heads.get(index);
            if (category == null) {
                logger.warn("Failed to find category.");
                continue;
            }

            sgMenu.setButton(0, slot, new SGButton(category.getIcon(player)).withListener(event ->
            {
                PlayerCache data = PlayerCacheManager.getPlayerData(playerId);
                data.setHeadsPage(1);
                data.setSearchCategory(category);
                data.setFavorite(false);
                data.setSearch(null);
                MenuManager manager = OpenHeads.Instance.getMenuManager();
                if (manager == null)
                    return;
                manager.open(player, HeadsGUI.ID);
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
                int maxPage = 1 + (HeadUtils.getHeadCategories().size() / dynamicSlots.getOrDefault("category_slots", new ArrayList<>()).size());
                if (playerData.getMainPage() + 1 > maxPage)
                    return;
                playerData.setMainPage(playerData.getMainPage() + 1);

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
                if (playerData.getMainPage() - 1 <= 0)
                    return;
                playerData.setMainPage(playerData.getMainPage() - 1);

                MenuManager manager = OpenHeads.Instance.getMenuManager();
                if (manager == null)
                    break;
                SGMenu menu = manager.getMenu(player, ID);
                if (menu == null)
                    break;
                refresh(player, menu);
            }
            case "[close]" -> {
                MenuManager manager = OpenHeads.Instance.getMenuManager();
                if (manager != null)
                    manager.close(player, false);
            }
            case "[favorites]" -> {
                PlayerCache data = PlayerCacheManager.getPlayerData(player.getUniqueId());
                MenuManager manager = OpenHeads.Instance.getMenuManager();
                data.setHeadsPage(1);
                data.setSearchCategory(null);
                data.setFavorite(true);
                data.setSearch(null);
                if (manager != null)
                    manager.open(player, HeadsGUI.ID);
            }
            case "[search]" -> {
                PlayerCache data = PlayerCacheManager.getPlayerData(player.getUniqueId());
                MenuManager manager = OpenHeads.Instance.getMenuManager();
                if (manager != null)
                    manager.close(player, true);
                data.setHeadsPage(1);
                data.setSearchCategory(null);
                data.setFavorite(false);
                data.setSearch(null);
                data.getSignMenu().open(player);
            }
        }
    }

    @Override
    public void onOpen(@NotNull Player player) {
        PlayerCache playerData = PlayerCacheManager.getPlayerData(player.getUniqueId());
        playerData.setMainPage(1);

        MenuManager manager = OpenHeads.Instance.getMenuManager();
        if (manager != null) {
            SGMenu menu = manager.getMenu(player, ID);
            if (menu != null) {
                refresh(player, menu);
            }
        }
    }
}
