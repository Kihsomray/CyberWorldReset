package net.zerotoil.cyberworldreset.old.cache;

import net.zerotoil.cyberworldreset.old.CyberWorldReset;
import net.zerotoil.cyberworldreset.old.objects.SavedFile;
import org.bukkit.configuration.Configuration;

import java.io.File;
import java.util.HashMap;

public class Files {

    private CyberWorldReset main;
    private HashMap<String, SavedFile> files = new HashMap<>();
    private int ymls = 0;

    public Files(CyberWorldReset main) {

        this.main = main;

    }

    public void loadFiles() {
        if (!files.isEmpty()) files.clear();
        main.logger("&bLoading YAML files...");
        long startTime = System.currentTimeMillis();

        // front end
        addFile("config");
        addFile("lang");
        addFile("worlds");

        // back end
        File savedWorlds = new File(main.getDataFolder(),"saved_worlds");
        if (!savedWorlds.exists()) savedWorlds.mkdirs();

        main.logger("&7Loaded &e" + ymls + "&7 files in &a" + (System.currentTimeMillis() - startTime) + "ms&7.");
        main.logger("");
    }

    private void addFile(String file) {
        ymls++;
        files.put(file, new SavedFile(main, file + ".yml"));
        files.get(file).reloadConfig();
        main.logger("&7Loaded file &e" + file + ".yml&7.");
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
