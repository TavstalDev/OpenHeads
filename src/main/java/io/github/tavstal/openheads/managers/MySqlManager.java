package io.github.tavstal.openheads.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.openheads.OpenHeads;
import io.github.tavstal.openheads.models.Favorite;
import io.github.tavstal.openheads.models.HeadData;
import io.github.tavstal.openheads.models.IDatabase;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MySqlManager implements IDatabase {
    private static HikariDataSource _dataSource;
    private static FileConfiguration getConfig() { return OpenHeads.Instance.getConfig(); }
    private static final PluginLogger _logger = OpenHeads.Logger().WithModule(MySqlManager.class);

    @Override
    public void Load() {
        _dataSource = CreateDataSource();
    }

    @Override
    public void Unload() {
        if (_dataSource != null) {
            if (!_dataSource.isClosed())
                _dataSource.close();
        }
    }

    public HikariDataSource CreateDataSource() {
        try
        {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s", getConfig().getString("storage.host"), getConfig().getString("storage.port"), getConfig().getString("storage.database"))); // Address of your running MySQL database
            config.setUsername(getConfig().getString("storage.username")); // Username
            config.setPassword(getConfig().getString("storage.password")); // Password
            config.setMaximumPoolSize(10); // Pool size defaults to 10
            config.setMaxLifetime(30000);
            return new HikariDataSource(config);
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened during the creation of database connection...\n%s", ex.getMessage()));
            return null;
        }
    }

    @Override
    public void CheckSchema() {
        try (Connection connection = _dataSource.getConnection())
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
        try (Connection connection = _dataSource.getConnection())
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
        try (Connection connection = _dataSource.getConnection())
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
        try (Connection connection = _dataSource.getConnection())
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
        try (Connection connection = _dataSource.getConnection()) {
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
