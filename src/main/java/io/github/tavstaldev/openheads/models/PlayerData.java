package io.github.tavstaldev.openheads.models;

import com.samjakob.spigui.menu.SGMenu;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import de.rapha149.signgui.exception.SignGUIVersionException;
import io.github.tavstaldev.openheads.OpenHeads;
import io.github.tavstaldev.openheads.gui.HeadsGUI;
import io.github.tavstaldev.openheads.gui.MainGUI;
import io.github.tavstaldev.openheads.utils.HeadUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerData {
    private final Player _player;
    private boolean _isGUIOpened;
    private SGMenu _mainMenu;
    private int _mainPage;
    private SGMenu _headsMenu;
    private int _headsPage;
    private String _search;
    private HeadCategory _searchCategory;
    private boolean _isFavorite;
    private SignGUI _signMenu;
    private List<Map.Entry<String, HeadData>> _heads;

    /**
     * Constructs a new PlayerData object for the specified player.
     *
     * @param player the player associated with this data
     */
    public PlayerData(Player player) {
        _player = player;
        _isGUIOpened = false;
        _mainMenu = null;
        _mainPage = 0;
        _headsMenu = null;
        _headsPage = 0;
    }

    /**
     * Checks if the GUI is opened for the player.
     *
     * @return true if the GUI is opened, false otherwise
     */
    public boolean isGUIOpened() {
        return _isGUIOpened;
    }

    /**
     * Sets the GUI opened status for the player.
     *
     * @param isGUIOpened the new GUI opened status
     */
    public void setGUIOpened(boolean isGUIOpened) {
        _isGUIOpened = isGUIOpened;
    }

    /**
     * Gets the main menu for the player. If the main menu is not created yet, it creates a new one.
     *
     * @return the main menu for the player
     */
    public SGMenu getMainMenu() {
        if (_mainMenu == null) {
            _mainMenu = MainGUI.create(_player);
        }
        return _mainMenu;
    }

    /**
     * Gets the heads menu for the player. If the heads menu is not created yet, it creates a new one.
     *
     * @return the heads menu for the player
     */
    public SGMenu getHeadsMenu() {
        if (_headsMenu == null) {
            _headsMenu = HeadsGUI.create(_player);
        }
        return _headsMenu;
    }

    /**
     * Gets the current main page number for the player.
     *
     * @return the current main page number
     */
    public int getMainPage() {
        return _mainPage;
    }

    /**
     * Sets the current main page number for the player.
     *
     * @param kitsPage the new main page number
     */
    public void setMainPage(int kitsPage) {
        _mainPage = kitsPage;
    }

    /**
     * Gets the current heads page number for the player.
     *
     * @return the current heads page number
     */
    public int getHeadsPage() {
        return _headsPage;
    }

    /**
     * Sets the current heads page number for the player.
     *
     * @param page the new heads page number
     */
    public void setHeadsPage(int page) {
        _headsPage = page;
    }

    /**
     * Gets the current search string.
     *
     * @return the current search string
     */
    public String getSearch() {
        return _search;
    }

    /**
     * Sets the search string.
     *
     * @param search the new search string
     */
    public void setSearch(String search) {
        _search = search;
    }

    /**
     * Gets the current search category.
     *
     * @return the current search category
     */
    public HeadCategory getSearchCategory() {
        return _searchCategory;
    }

    /**
     * Sets the search category.
     *
     * @param searchCategory the new search category
     */
    public void setSearchCategory(HeadCategory searchCategory) {
        _searchCategory = searchCategory;
    }

    /**
     * Checks if the player is marked as favorite.
     *
     * @return true if the player is marked as favorite, false otherwise
     */
    public boolean isFavorite() {
        return _isFavorite;
    }

    /**
     * Sets the favorite status for the player.
     *
     * @param isFavorite the new favorite status
     */
    public void setFavorite(boolean isFavorite) {
        _isFavorite = isFavorite;
    }

    /**
     * Gets the sign menu for the player. If the sign menu is not created yet, it creates a new one.
     *
     * @return the sign menu for the player
     */
    public SignGUI getSignMenu() {
        if (_signMenu == null) {
            try {
            _signMenu = SignGUI.builder()
                    // set lines
                    .setLines(
                            OpenHeads.Instance.Localize(_player, "Sign.Menu.TopLine").replaceAll("&", "§"),
                            OpenHeads.Instance.Localize(_player, "Sign.Menu.MiddleLine").replaceAll("&", "§"),
                            "",
                            OpenHeads.Instance.Localize(_player, "Sign.Menu.BottomLine").replaceAll("&", "§")
                            )
                    // set the sign type
                    .setType(Material.OAK_SIGN)
                    // set the sign color
                    .setColor(DyeColor.BLACK)
                    .setLine(2, "")
                    // set the handler/listener (called when the player finishes editing)
                    .setHandler((p, result) -> {
                        String line = result.getLineWithoutColor(2);

                        if (line.isEmpty()) {
                            // The user has not entered anything on line 2, so we open the sign again
                            return List.of(SignGUIAction.displayNewLines(
                                    OpenHeads.Instance.Localize(_player, "Sign.Menu.TopLine").replaceAll("&", "§"),
                                    OpenHeads.Instance.Localize(_player, "Sign.Menu.MiddleLine").replaceAll("&", "§"),
                                    "",
                                    OpenHeads.Instance.Localize(_player, "Sign.Menu.BottomLine").replaceAll("&", "§")
                            ));
                        }
                        _search = line;
                        Bukkit.getScheduler().runTask(OpenHeads.Instance, () -> HeadsGUI.open(_player));

                        // Just close the sign by not returning any actions
                        return Collections.emptyList();
                    })
                    // build the SignGUI
                    .build();
            } catch (SignGUIVersionException e) {
                OpenHeads.Logger().Warn("SignGUI does not support this server version.");
            }
        }
        return _signMenu;
    }

    /**
     * Gets the map of heads for the player.
     *
     * @return a map where the key is the category name and the value is a list of HeadData objects
     */
    public List<Map.Entry<String, HeadData>> getHeads() {
        return _heads;
    }

    /**
     * Refreshes the heads for the player based on the current search and favorite status.
     */
    public void refreshHeads() {
        _heads = new ArrayList<>();
        if (isFavorite()) {
            List<Favorite> favorites = OpenHeads.Database.GetFavorites(_player.getUniqueId());
            for (Favorite favorite : favorites) {
                HeadData head = HeadUtils.getHead(favorite.CategoryName, favorite.HeadName);
                if (head == null)
                    continue;

                _heads.add(new AbstractMap.SimpleEntry<>(favorite.CategoryName, head));
            }
        }
        if (_search != null && !_search.isBlank()) {
            for (var category : HeadUtils.getHeadCategories()) {
                for (var head : category.getHeads()) {
                    if (head.Name.toLowerCase().contains(_search.toLowerCase())) {
                        _heads.add(new AbstractMap.SimpleEntry<>(category.Name, head));
                        continue;
                    }

                    if (head.Tags.toLowerCase().contains(_search.toLowerCase())) {
                        _heads.add(new AbstractMap.SimpleEntry<>(category.Name, head));
                    }
                }
            }
        }
        if (_searchCategory != null) {
            for (var headData : _searchCategory.getHeads()) {
                _heads.add(new AbstractMap.SimpleEntry<>(_searchCategory.Name, headData));
            }
        }
    }

    /**
     * Frees the heads data by setting the heads map to null.
     */
    public void freeHeads() {
        _heads = null;
    }
}
