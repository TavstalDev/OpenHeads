package io.github.tavstal.openheads.models;

import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstal.openheads.OpenHeads;
import io.github.tavstal.openheads.gui.HeadsGUI;
import io.github.tavstal.openheads.gui.MainGUI;
import io.github.tavstal.openheads.utils.HeadUtils;
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
    private SignMenuFactory.Menu _signMenu;
    private Map<String, List<HeadData>> _heads;

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
    public SignMenuFactory.Menu getSignMenu() {
        if (_signMenu == null) {
            _signMenu = OpenHeads.Instance.getSignMenuFactory().newMenu(
                    Arrays.asList(
                            OpenHeads.Instance.Localize(_player, "Sign.Menu.TopLine"),
                            OpenHeads.Instance.Localize(_player, "Sign.Menu.MiddleLine"),
                            "",
                            OpenHeads.Instance.Localize(_player, "Sign.Menu.BottomLine")
                    ))
                    .reopenIfFail(true)
                    .response((player, strings) -> {
                        _search = strings[2];
                        return true;
                    });
        }
        return _signMenu;
    }

    /**
     * Gets the map of heads for the player.
     *
     * @return a map where the key is the category name and the value is a list of HeadData objects
     */
    public Map<String, List<HeadData>> getHeads() {
        return _heads;
    }

    /**
     * Refreshes the heads for the player based on the current search and favorite status.
     */
    public void refreshHeads() {
        _heads = new HashMap<>();
        if (isFavorite()) {
            // TODO
            List<Favorite> favorites = new ArrayList<>();
            for (Favorite favorite : favorites) {
                HeadData head = HeadUtils.getHead(favorite.CategoryName, favorite.HeadName);
                if (head == null)
                    continue;

                if (!_heads.containsKey(favorite.CategoryName))
                    _heads.put(favorite.CategoryName, new ArrayList<>() {{
                        add(head);
                    }});
                else
                    _heads.get(favorite.CategoryName).add(head);
            }
        }
        if (!_search.isEmpty()) {
            for (var category : HeadUtils.getHeadCategories()) {
                List<HeadData> heads = new ArrayList<>();
                for (var head : category.getHeads()) {
                    if (head.Name.toLowerCase().contains(_search.toLowerCase()))
                        heads.add(head);
                }
                if (!heads.isEmpty())
                    _heads.put(category.Name, heads);
            }
        }
        if (_searchCategory != null) {
            _heads.put(_searchCategory.Name, _searchCategory.getHeads());
        }
    }

    /**
     * Frees the heads data by setting the heads map to null.
     */
    public void freeHeads() {
        _heads = null;
    }
}
