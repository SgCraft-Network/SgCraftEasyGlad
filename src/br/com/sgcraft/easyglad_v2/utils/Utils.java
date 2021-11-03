package br.com.sgcraft.easyglad_v2.utils;

import br.com.sgcraft.easyglad_v2.*;

public class Utils
{
    private static Main plugin;
    static int delay;
    
    public Utils(final Main main) {
        this.plugin = main;
    }
    
    public static void addClan(final String tag) {
        Main.pl.getConfig().set("ClanVenceu", (Object)tag);
        Main.pl.saveConfig();
    }
    
}
