package io.github.tavstaldev.openheads.managers;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openheads.HeadsConfiguration;
import io.github.tavstaldev.openheads.OpenHeads;
import io.github.tavstaldev.openheads.models.Favorite;
import io.github.tavstaldev.openheads.models.HeadData;
import io.github.tavstaldev.openheads.models.IDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages SQLite database operations for the OpenHeads plugin.
 * Implements the IDatabase interface to handle database-related tasks.
 */
public class SqlLiteManager implements IDatabase {
    private HeadsConfiguration _config;
    private final PluginLogger _logger = OpenHeads.Logger().WithModule(SqlLiteManager.class);

    /**
     * Loads the database manager. No operation is performed for SQLite.
     */
    @Override
    public void load() {
        _config = OpenHeads.Config();
    }

    /**
     * Unloads the database manager. No operation is performed for SQLite.
     */
    @Override
    public void unload() {}

    /**
     * Creates a connection to the SQLite database.
     * @return A Connection instance to the SQLite database, or null if an error occurs.
     */
    public Connection CreateConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(String.format("jdbc:sqlite:plugins/OpenHeads/%s.db", _config.storageFilename));
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while creating db connection...\n%s", ex.getMessage()));
            return null;
        }
    }

    /**
     * Ensures the database schema is up-to-date by creating necessary tables if they do not exist.
     */
    @Override
    public void checkSchema() {
        try (Connection connection = CreateConnection()) {
            // Favorites table
            String sql = String.format("CREATE TABLE IF NOT EXISTS %s_favorites (" +
                            "PlayerId VARCHAR(36), " +
                            "Category VARCHAR(200), " +
                            "HeadName VARCHAR(200));",
                    _config.storageTablePrefix
            );
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while creating tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Adds a favorite entry to the database.
     * @param owner The UUID of the player.
     * @param category The category of the favorite.
     * @param headName The name of the head to be added as a favorite.
     */
    @Override
    public void addFavorite(UUID owner, String category, String headName) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("INSERT INTO %s_favorites (PlayerId, Category, HeadName) " +
                            "VALUES (?, ?, ?);",
                    _config.storageTablePrefix);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, owner.toString());
                statement.setString(2, category);
                statement.setString(3, headName);
                statement.executeUpdate();
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while adding favorite...\n%s", ex.getMessage()));
        }
    }

    /**
     * Removes a favorite entry from the database.
     * @param owner The UUID of the player.
     * @param category The category of the favorite.
     * @param headName The name of the head to be removed from favorites.
     */
    @Override
    public void removeFavorite(UUID owner, String category, String headName) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("DELETE FROM %s_favorites WHERE PlayerId=? AND Category=? AND HeadName=?;",
                    _config.storageTablePrefix);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, owner.toString());
                statement.setString(2, category);
                statement.setString(3, headName);
                statement.executeUpdate();
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Checks if a specific head is marked as a favorite by a player.
     * @param owner The UUID of the player.
     * @param category The category of the favorite.
     * @param headName The name of the head to check.
     * @return True if the head is a favorite, false otherwise.
     */
    @Override
    public boolean isFavorite(UUID owner, String category, String headName) {
        boolean data = false;
        try (Connection connection = CreateConnection()) {
            String sql = String.format("SELECT * FROM %s_favorites WHERE PlayerId=? AND Category=? AND HeadName=? LIMIT 1;",
                    _config.storageTablePrefix);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, owner.toString());
                statement.setString(2, category);
                statement.setString(3, headName);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        data = true;
                    }
                }
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while finding favorite data...\n%s", ex.getMessage()));
            return false;
        }

        return data;
    }

    /**
     * Overloaded method to check if a specific head is marked as a favorite by a player.
     * @param owner The UUID of the player.
     * @param head A map entry containing the category and head data.
     * @return True if the head is a favorite, false otherwise.
     */
    @Override
    public boolean isFavorite(UUID owner, Map.Entry<String, HeadData> head) {
        return isFavorite(owner, head.getKey(), head.getValue().Name);
    }

    /**
     * Retrieves all favorite entries for a specific player.
     * @param owner The UUID of the player.
     * @return A list of Favorite objects representing the player's favorites.
     */
    @Override
    public List<Favorite> getFavorites(UUID owner) {
        List<Favorite> data = new ArrayList<>();
        try (Connection connection = CreateConnection()) {
            String sql = String.format("SELECT * FROM %s_favorites WHERE PlayerId=?;",
                    _config.storageTablePrefix);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, owner.toString());
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        data.add(new Favorite(
                                UUID.fromString(result.getString("PlayerId")),
                                result.getString("Category"),
                                result.getString("HeadName")
                        ));
                    }
                }
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while getting favorite data...\n%s", ex.getMessage()));
            return null;
        }
        return data;
    }
}