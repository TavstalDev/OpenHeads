package io.github.tavstal.openheads.managers;

import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.openheads.OpenHeads;
import io.github.tavstal.openheads.models.Favorite;
import io.github.tavstal.openheads.models.HeadData;
import io.github.tavstal.openheads.models.IDatabase;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SqlLiteManager implements IDatabase {
    private static FileConfiguration getConfig() { return OpenHeads.Instance.getConfig(); }
    private static final PluginLogger _logger = OpenHeads.Logger().WithModule(SqlLiteManager.class);

    @Override
    public void Load() {
        // Do nothing
    }

    @Override
    public void Unload() {
        // Do nothing
    }

    public Connection CreateConnection() {
        try
        {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(String.format("jdbc:sqlite:plugins/OpenHeads/%s.db", getConfig().getString("storage.filename")));
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while creating db connection...\n%s", ex.getMessage()));
            return null;
        }
    }

    @Override
    public void CheckSchema() {
        try (Connection connection = CreateConnection())
        {
            // Favorites
            String sql = String.format("CREATE TABLE IF NOT EXISTS %s_favorites (" +
                            "PlayerId VARCHAR(36), " +
                            "Category VARCHAR(200), " +
                            "HeadName VARCHAR(200));",
                    getConfig().getString("storage.tablePrefix")
            );
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while creating tables...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void AddFavorite(UUID owner, String category, String headName) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("INSERT INTO %s_favorites (PlayerId, Category, HeadName) " +
                            "VALUES (?, ?, ?);",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, owner.toString());
                statement.setString(2, category);
                statement.setString(3, headName);
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while adding favorite...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void RemoveFavorite(UUID owner, String category, String headName) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("DELETE FROM %s_favorites WHERE PlayerId=? AND Category=? AND HeadName=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, owner.toString());
                statement.setString(2, category);
                statement.setString(3, headName);
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    @Override
    public boolean IsFavorite(UUID owner, String category, String headName) {
        boolean data = false;
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("SELECT * FROM %s_favorites WHERE PlayerId=? AND Category=? AND HeadName=? LIMIT 1;",
                    getConfig().getString("storage.tablePrefix"));
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
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while finding favorite data...\n%s", ex.getMessage()));
            return false;
        }

        return data;
    }

    @Override
    public boolean IsFavorite(UUID owner, Map.Entry<String, HeadData> head) {
        return IsFavorite(owner, head.getKey(), head.getValue().Name);
    }

    @Override
    public List<Favorite> GetFavorites(UUID owner) {
        List<Favorite> data = new ArrayList<>();
        try (Connection connection = CreateConnection()) {
            String sql = String.format("SELECT * FROM %s_favorites WHERE PlayerId=?;",
                    getConfig().getString("storage.tablePrefix"));
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
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while getting favorite data...\n%s", ex.getMessage()));
            return null;
        }
        return data;
    }
}
