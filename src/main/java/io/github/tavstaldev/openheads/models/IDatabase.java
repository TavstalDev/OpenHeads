package io.github.tavstaldev.openheads.models;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Interface for database operations related to kits.
 */
public interface IDatabase {

    /**
     * Called when the database is loaded.
     */
    void Load();

    /**
     * Called when the database is unloaded.
     */
    void Unload();

    /**
     * Checks and updates the database schema if necessary.
     */
    void CheckSchema();

    /**
     * Adds a head to the user's favorites.
     *
     * @param owner the UUID of the owner
     * @param category the category of the head
     * @param headName the name of the head
     */
    void AddFavorite(UUID owner, String category, String headName);

    /**
     * Removes a head from the user's favorites.
     *
     * @param owner the UUID of the owner
     * @param category the category of the head
     * @param headName the name of the head
     */
    void RemoveFavorite(UUID owner, String category, String headName);

    /**
     * Checks if a head is in the user's favorites.
     *
     * @param owner the UUID of the owner
     * @param category the category of the head
     * @param headName the name of the head
     * @return true if the head is a favorite, false otherwise
     */
    boolean IsFavorite(UUID owner, String category, String headName);

    /**
     * Checks if a head is in the user's favorites.
     *
     * @param owner the UUID of the owner
     * @param head the entry containing the category and head data
     * @return true if the head is a favorite, false otherwise
     */
    boolean IsFavorite(UUID owner, Map.Entry<String, HeadData> head);

    /**
     * Gets the list of the user's favorite heads.
     *
     * @param owner the UUID of the owner
     * @return a list of the user's favorite heads
     */
    List<Favorite> GetFavorites(UUID owner);
}
