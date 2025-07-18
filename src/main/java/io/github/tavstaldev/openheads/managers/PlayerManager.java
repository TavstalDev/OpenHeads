package io.github.tavstaldev.openheads.managers;

import io.github.tavstaldev.openheads.models.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private static final Map<UUID, PlayerData> _playerData = new HashMap<>();

    /**
     * Adds player data to the manager.
     *
     * @param playerId   the UUID of the player
     * @param playerData the data associated with the player
     */
    public static void addPlayerData(UUID playerId, PlayerData playerData) {
        _playerData.put(playerId, playerData);
    }

    /**
     * Removes player data from the manager.
     *
     * @param playerId the UUID of the player
     */
    public static void removePlayerData(UUID playerId) {
        _playerData.remove(playerId);
    }

    /**
     * Retrieves player data from the manager.
     *
     * @param playerId the UUID of the player
     * @return the data associated with the player, or null if not found
     */
    public static PlayerData getPlayerData(UUID playerId) {
        return _playerData.get(playerId);
    }
}
