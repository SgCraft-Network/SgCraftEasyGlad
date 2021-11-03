package br.com.sgcraft.easyglad_v2.utils;

import java.io.*;
import org.bukkit.plugin.*;
import org.bukkit.*;
import org.bukkit.configuration.file.*;

public class Files
{
    private static Files instance;
    private FileConfiguration data;
    private File dfile;
    
    static {
        Files.instance = new Files();
    }
    
    public static Files getInstance() {
        return Files.instance;
    }
    
    public void setupFiles(final Plugin p) {
        if (!p.getDataFolder().exists()) {
            p.getDataFolder().mkdir();
        }
        this.dfile = new File(p.getDataFolder(), "Data.yml");
        if (!this.dfile.exists()) {
            try {
                this.dfile.createNewFile();
                Bukkit.getConsoleSender().sendMessage(" §5Files: §cData.yml criado com sucesso.");
            }
            catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§cOcorreu um erro ao criar o arquivo data.yml, Erro:");
                e.printStackTrace();
            }
        }
        this.data = (FileConfiguration)YamlConfiguration.loadConfiguration(this.dfile);
      
    }
    
    public FileConfiguration getDataFile() {
        return this.data;
    }
    
    
    public void saveDataFile() {
        try {
            this.data.save(this.dfile);
        }
        catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§cOcorreu um erro ao salvar o arquivo data.yml, Erro:");
            e.printStackTrace();
        }
    }
    
}
