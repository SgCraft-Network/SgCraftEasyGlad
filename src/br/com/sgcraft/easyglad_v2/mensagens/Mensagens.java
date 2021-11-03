package br.com.sgcraft.easyglad_v2.mensagens;

import org.bukkit.*;
import org.bukkit.configuration.file.*;
import java.io.*;
import org.bukkit.plugin.*;
import java.util.*;

public class Mensagens
{
    private static HashMap<String, String> mensagens;
    private static List<String> preparar;
    private static List<String> cancelar;
    private static List<String> continuar;
    private static List<String> iniciando;
    private static List<String> iniciar;
    private static List<String> finalizar;
    private static String tag;
    private static HashMap<String, String> vencedores;
    
    static {
        Mensagens.mensagens = new HashMap<String, String>();
        Mensagens.preparar = new ArrayList<String>();
        Mensagens.cancelar = new ArrayList<String>();
        Mensagens.continuar = new ArrayList<String>();
        Mensagens.iniciando = new ArrayList<String>();
        Mensagens.iniciar = new ArrayList<String>();
        Mensagens.finalizar = new ArrayList<String>();
        Mensagens.tag = "";
        Mensagens.vencedores = new HashMap<String, String>();
    }
    
    public static void loadMensagens() {
        Mensagens.mensagens.clear();
        Mensagens.preparar.clear();
        Mensagens.cancelar.clear();
        Mensagens.continuar.clear();
        Mensagens.iniciando.clear();
        Mensagens.iniciar.clear();
        Mensagens.finalizar.clear();
        Mensagens.tag = "";
        Mensagens.vencedores.clear();
        final Plugin g = Bukkit.getPluginManager().getPlugin("EasyGlad");
        final File f_preparar = new File(g.getDataFolder(), "preparar.txt");
        final File f_cancelar = new File(g.getDataFolder(), "cancelar.txt");
        final File f_continuar = new File(g.getDataFolder(), "continuar.txt");
        final File f_iniciando = new File(g.getDataFolder(), "iniciando.txt");
        final File f_iniciar = new File(g.getDataFolder(), "iniciar.txt");
        final File f_finalizar = new File(g.getDataFolder(), "finalizar.txt");
        final File f_msgs = new File(g.getDataFolder(), "mensagens.yml");
        final YamlConfiguration msgs = YamlConfiguration.loadConfiguration(f_msgs);
        for (final String n : msgs.getConfigurationSection("").getKeys(false)) {
            if (!Mensagens.mensagens.containsKey(n.toLowerCase())) {
                Mensagens.mensagens.put(n.toLowerCase(), msgs.getString(n));
            }
        }
        Mensagens.tag = g.getConfig().getString("Premios.Tag").replace("&", "§");
        for (final String n : g.getConfig().getStringList("Vencedores")) {
            Mensagens.vencedores.put(n.toLowerCase(), n);
        }
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(f_preparar));
            String line = null;
            while ((line = reader.readLine()) != null) {
                Mensagens.preparar.add(line);
            }
            reader.close();
        }
        catch (Exception ex) {}
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(f_cancelar));
            String line = null;
            while ((line = reader.readLine()) != null) {
                Mensagens.cancelar.add(line);
            }
            reader.close();
        }
        catch (Exception ex2) {}
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(f_continuar));
            String line = null;
            while ((line = reader.readLine()) != null) {
                Mensagens.continuar.add(line);
            }
            reader.close();
        }
        catch (Exception ex3) {}
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(f_iniciando));
            String line = null;
            while ((line = reader.readLine()) != null) {
                Mensagens.iniciando.add(line);
            }
            reader.close();
        }
        catch (Exception ex4) {}
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(f_iniciar));
            String line = null;
            while ((line = reader.readLine()) != null) {
                Mensagens.iniciar.add(line);
            }
            reader.close();
        }
        catch (Exception ex5) {}
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(f_finalizar));
            String line = null;
            while ((line = reader.readLine()) != null) {
                Mensagens.finalizar.add(line);
            }
            reader.close();
        }
        catch (Exception ex6) {}
    }
    
    public static List<String> getPreparar() {
        return Mensagens.preparar;
    }
    
    public static List<String> getCancelar() {
        return Mensagens.cancelar;
    }
    
    public static List<String> getContinuar() {
        return Mensagens.continuar;
    }
    
    public static List<String> getIniciando() {
        return Mensagens.iniciando;
    }
    
    public static List<String> getIniciar() {
        return Mensagens.iniciar;
    }
    
    public static List<String> getFinalizar() {
        return Mensagens.finalizar;
    }
    
    public static String getMensagem(final String n) {
        return Mensagens.mensagens.get(n.toLowerCase()).replaceAll("&", "§");
    }
    
    public static String getTag() {
        return Mensagens.tag;
    }
    
    public static boolean containsVencedor(final String n) {
        return Mensagens.vencedores.containsKey(n.toLowerCase());
    }
    
    public static List<String> getVencedores() {
        final List<String> n = new ArrayList<String>();
        n.addAll(Mensagens.vencedores.values());
        return n;
    }
    
    public static void changeVencedores(final List<String> l) {
        Mensagens.vencedores.clear();
        for (final String n : l) {
            Mensagens.vencedores.put(n.toLowerCase(), n);
        }
    }
}
