package net.zerotoil.cyberworldreset.old.objects;

import net.zerotoil.cyberworldreset.old.CyberWorldReset;
import net.zerotoil.cyberworldreset.old.addons.configupdater.ConfigUpdater;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.Collections;

public class SavedFile {

    private CyberWorldReset main;
    private java.io.File configFile;
    private FileConfiguration dataConfig;
    private String location;
    private String name;

    public SavedFile(CyberWorldReset main, String location) {
        this.main = main;
        this.location = location;
        this.name = location.replace(".yml", "");
        saveDefaultConfig();
        dataConfig = YamlConfiguration.loadConfiguration(getFile());
    }

    private java.io.File getFile() {
        return new java.io.File(main.getDataFolder(), location);
    }

    public FileConfiguration getConfig() {
        return dataConfig;
    }

    public void saveConfig() throws IOException {
        if (!((this.dataConfig == null) || (this.configFile == null))) {
            this.getConfig().save(this.configFile);
        }
    }

    public void updateConfig() {
        try {
            ConfigUpdater.update(main, location, getFile(), Collections.emptyList());
            if (main.getVersion() < 13) ConfigUpdater.update(main, location, getFile(), Collections.emptyList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();
    }

    public void reloadConfig() {
        dataConfig = YamlConfiguration.loadConfiguration(getFile());
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = getFile();
        }

        if (configFile.exists()) {
            return;
        }
        main.saveResource(location, false);
    }

}
