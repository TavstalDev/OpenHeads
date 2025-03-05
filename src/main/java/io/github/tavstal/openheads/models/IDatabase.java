package io.github.tavstal.openheads.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.List;
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
}
