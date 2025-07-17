package io.github.tavstaldev.openheads.models;

import java.util.UUID;

public class Favorite {
    /**
     * The unique identifier for the player.
     */
    public UUID PlayerId;

    /**
     * The unique identifier for the category.
     */
    public String CategoryName;

    /**
     * The unique identifier for the head.
     */
    public String HeadName;

    /**
     * Constructs a new Favorite object with the specified parameters.
     *
     * @param playerId The unique identifier for the player.
     * @param category The unique identifier for the category.
     * @param headName The unique identifier for the head.
     */
    public Favorite(UUID playerId, String category, String headName) {
        PlayerId = playerId;
        CategoryName = category;
        HeadName = headName;
    }
}
