package io.github.tavstal.openheads.models;

import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstal.openheads.gui.HeadsGUI;
import io.github.tavstal.openheads.gui.MainGUI;
import org.bukkit.entity.Player;

public class PlayerData {
    private final Player _player;
    private boolean _isGUIOpened;
    private SGMenu _mainMenu;
    private int _mainPage;
    private SGMenu _headsMenu;
    private int _headsPage;

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
}
