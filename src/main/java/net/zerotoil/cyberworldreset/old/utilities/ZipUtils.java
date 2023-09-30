package net.zerotoil.cyberworldreset.old.utilities;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ExcludeFileFilter;
import net.lingala.zip4j.model.ZipParameters;
import net.zerotoil.cyberworldreset.old.CyberWorldReset;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ZipUtils {

    private final CyberWorldReset main;

    public ZipUtils(CyberWorldReset main) {
        this.main = main;
    }

    public File getLastModified(String world) {
        File directory = new File(main.getDataFolder(),"saved_worlds");
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) return null;

        List<File> targetFiles = new ArrayList<>();
        for (File file : files)
            if (file.getName().substring(0, file.getName().length() - 29).equalsIgnoreCase(world))
                targetFiles.add(file);

        if (targetFiles.isEmpty()) return null;
        File lastModifiedFile = targetFiles.get(0);
        for (int i = 1; i < targetFiles.size(); i++)
            if (lastModifiedFile.lastModified() < targetFiles.get(i).lastModified())
                lastModifiedFile = targetFiles.get(i);

        return lastModifiedFile;
    }

    public void zip(String world) throws IOException {
        final List<File> filesToExclude = new ArrayList<>();
        String worldLocation = Bukkit.getWorldContainer() + File.separator + world;

        filesToExclude.add(new File(worldLocation + File.separator + "session.lock"));
        filesToExclude.add(new File(worldLocation + File.separator + "uid.dat"));

        ExcludeFileFilter exclude = filesToExclude::contains;
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setExcludeFileFilter(exclude);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        String date = world + "_save_" + dtf.format(now).replace(" ", "_")
                .replace("/", "-").replace(":", "-");
        ZipFile zipFile = new ZipFile(main.getDataFolder() + File.separator + "saved_worlds" + File.separator + date + ".zip");
        zipFile.addFolder(new File(worldLocation), zipParameters);
    }

    public void unZip(File zipFile) throws IOException {
        new ZipFile(zipFile).extractAll(String.valueOf(Bukkit.getWorldContainer()));
    }
}
