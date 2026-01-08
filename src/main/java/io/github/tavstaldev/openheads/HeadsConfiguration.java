package io.github.tavstaldev.openheads;

import io.github.tavstaldev.minecorelib.config.ConfigurationBase;
import io.github.tavstaldev.openheads.utils.IconUtils;
import org.bukkit.Material;

public class HeadsConfiguration extends ConfigurationBase {

    public HeadsConfiguration() {
        super(OpenHeads.Instance, "config.yml", null);
    }

    public String prefix;
    public boolean checkForUpdates, debug;

    public String storageType, storageFilename, storageHost, storageDatabase, storageUsername, storagePassword, storageTablePrefix;
    public int storagePort;

    @Override
    protected void loadDefaults() {
        // General
        resolve("locale", "eng");
        resolve("usePlayerLocale", true);
        checkForUpdates = resolveGet("checkForUpdates", true);
        debug = resolveGet("debug", false);
        prefix = resolveGet("prefix", "&bOpen&3Heads &8»");

        // Storage
        storageType = resolveGet("storage.type", "sqlite");
        storageFilename = resolveGet("storage.filename", "database");
        storageHost = resolveGet("storage.host", "localhost");
        storagePort = resolveGet("storage.port", 3306);
        storageDatabase = resolveGet("storage.database", "minecraft");
        storageUsername = resolveGet("storage.username", "root");
        storagePassword = resolveGet("storage.password", "ascent");
        storageTablePrefix = resolveGet("storage.tablePrefix", "openheads");
    }
}
