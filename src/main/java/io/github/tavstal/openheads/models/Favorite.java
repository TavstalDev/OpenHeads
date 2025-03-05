package io.github.tavstal.openheads.models;

import java.util.UUID;

public class Favorite {
    /**
     * The unique identifier for the player.
     */
    public UUID PlayerId;

    /**
     * The unique identifier for the category.
     */
    public long CategoryId;

    /**
     * The unique identifier for the head.
     */
    public long HeadId;

    /**
     * Constructs a new Favorite object with the specified parameters.
     *
     * @param playerId The unique identifier for the player.
     * @param categoryId The unique identifier for the category.
     * @param headId The unique identifier for the head.
     */
    public Favorite(UUID playerId, long categoryId, long headId) {
        PlayerId = playerId;
        CategoryId = categoryId;
        HeadId = headId;
    }
}
