package io.github.tavstaldev.openheads.events;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openheads.OpenHeads;
import io.github.tavstaldev.openheads.managers.PlayerCacheManager;
import io.github.tavstaldev.openheads.models.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Event listener for handling player-related events.
 */
public class PlayerEventListener implements Listener
{
    private static final PluginLogger _logger = OpenHeads.logger().withModule(PlayerEventListener.class);

    /**
     * Initializes and registers the event listener.
     */
    public static void init() {
        _logger.debug("Registering event listener...");
        Bukkit.getPluginManager().registerEvents(new PlayerEventListener(), OpenHeads.Instance);
        _logger.debug("Event listener registered.");
    }

    /**
     * Handles the event when a player joins the server for the first time.
     *
     * @param event The PlayerJoinEvent triggered when a player joins the server.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerCache playerData = new PlayerCache(player);
        PlayerCacheManager.addPlayerData(player.getUniqueId(), playerData);
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerCacheManager.removePlayerData(player.getUniqueId());
    }
}
