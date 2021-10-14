package net.zerotoil.cyberworldreset.cache;

import net.zerotoil.cyberworldreset.CyberWorldReset;
import net.zerotoil.cyberworldreset.objects.SavedFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;

import java.util.HashMap;

public class Files {

    private CyberWorldReset main;
    private HashMap<String, SavedFile> files = new HashMap<>();

    public Files(CyberWorldReset main) {

        this.main = main;

    }

    public void loadFiles() {
        if (!files.isEmpty()) files.clear();
        Bukkit.getLogger().info("[CyberWorldReset] Loading Files...");

        // front end
        addFile("config");
        addFile("lang");
        addFile("worlds");

        // back end
        java.io.File savedWorlds = new java.io.File(main.getDataFolder(),"saved_worlds");
        if (!savedWorlds.exists()) savedWorlds.mkdirs();

    }

    private void addFile(String file) {
        files.put(file, new SavedFile(main, file + ".yml"));
        Bukkit.getLogger().info("[CyberWorldReset] Loaded file \"" + file + ".yml\".");
    }

    public HashMap<String, SavedFile> getFiles() {
        return this.files;
    }
    public SavedFile get(String file){
        return files.get(file);
    }
    public Configuration getConfig(String file) {
        return files.get(file).getConfig();
    }

}
