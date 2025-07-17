package io.github.tavstaldev.openheads;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openheads.managers.PlayerManager;
import io.github.tavstaldev.openheads.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Event listener for handling player-related events.
 */
public class EventListener implements Listener
{
    private static final PluginLogger _logger = OpenHeads.Logger().WithModule(EventListener.class);

    /**
     * Initializes and registers the event listener.
     */
    public static void init() {
        _logger.Debug("Registering event listener...");
        Bukkit.getPluginManager().registerEvents(new EventListener(), OpenHeads.Instance);
        _logger.Debug("Event listener registered.");
    }

    /**
     * Handles the event when a player joins the server for the first time.
     *
     * @param event The PlayerJoinEvent triggered when a player joins the server.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = new PlayerData(player);
        PlayerManager.addPlayerData(player.getUniqueId(), playerData);
    }
}
