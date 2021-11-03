package br.com.sgcraft.easyglad_v2;

import org.bukkit.plugin.java.*;
import net.milkbowl.vault.economy.*;
import org.bukkit.event.*;

import java.io.*;
import org.bukkit.entity.*;
import org.bukkit.scoreboard.*;

import br.com.sgcraft.easyglad_v2.utils.ItensUtils;
import br.com.sgcraft.easyglad_v2.utils.Files;
import br.com.sgcraft.easyglad_v2.chat.*;
import br.com.sgcraft.easyglad_v2.hooks.*;
import br.com.sgcraft.easyglad_v2.mensagens.*;
import br.com.sgcraft.easyglad_v2.utils.*;

import org.bukkit.plugin.*;
import org.bukkit.inventory.*;
import java.util.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.command.*;
import net.sacredlabyrinth.phaed.simpleclans.*;
import org.bukkit.*;

public class Main extends JavaPlugin
{
    public static Main pl;
    public SimpleClans core1;
    protected static Economy econ;
    private SQLite sqlite;
    private MySQL mysql;
    public Scoreboard sb;
    private int EasyGladEtapa;
    public int PremioTotal;
    protected boolean canStart;
    private boolean formatEnabled;
    protected Location spawn;
    protected Location saida;
    protected Location camarote;
    protected HashMap<String, Integer> totalParticipantes;
    protected HashMap<String, Integer> clann;
    protected List<String> participantes;
    protected List<String> specs;
    protected boolean mito;
    protected int id;
    
    static {
        Main.econ = null;
    }
    
    public Main() {
        this.EasyGladEtapa = 0;
        this.canStart = true;
        this.formatEnabled = false;
        this.totalParticipantes = new HashMap<String, Integer>();
        this.clann = new HashMap<String, Integer>();
        this.participantes = new ArrayList<String>();
        this.specs = new ArrayList<String>();
        this.mito = false;
    }
    
    public void onEnable() {
        Main.pl = this;
        Files.getInstance().setupFiles((Plugin)this);
        this.getLogger().info("Ativando EasyGlad (V" + this.getDescription().getVersion() + ") - Autor: Sardinhagamer_HD");
        if (this.getServer().getPluginManager().getPlugin("SimpleClans") != null) {
            this.getLogger().info("Hooked to SimpleClans!");
            this.core1 = (SimpleClans)this.getServer().getPluginManager().getPlugin("SimpleClans");
        }
        else {
            this.getLogger().info("ERRO: SimpleClans ou SimpleClans2 nao encontrado!");
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
        }
        if (!this.setupEconomy()) {
            this.getLogger().warning("ERRO: Vault (Economia) nao encontrado!");
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
        }
        else {
            this.getLogger().info("Hooked to Vault (Economia)!");
        }
        if (this.getServer().getPluginManager().getPlugin("Legendchat") != null) {
            this.getServer().getPluginManager().registerEvents((Listener)new LegendChat(), (Plugin)this);
            this.getLogger().info("Hooked to Legendchat.");
        }
        else {
            this.getServer().getPluginManager().registerEvents((Listener)new NormalChat(), (Plugin)this);
        }
        if (this.getServer().getPluginManager().getPlugin("HealthBar") != null) {
            this.getServer().getPluginManager().registerEvents((Listener)new HealthBar(this), (Plugin)this);
            this.getLogger().info("Hooked to HealthBar.");
        }
        this.getServer().getPluginManager().registerEvents((Listener)new Listeners(this), (Plugin)this);
        this.getServer().getPluginCommand("easyglad").setExecutor((CommandExecutor)new Comando(this));
        this.getServer().getPluginCommand("vencedores").setExecutor((CommandExecutor)new Comando(this));
        final File file = new File(this.getDataFolder(), "config.yml");
        if (!file.exists()) {
            try {
                this.saveResource("config_template.yml", false);
                final File file2 = new File(this.getDataFolder(), "config_template.yml");
                file2.renameTo(new File(this.getDataFolder(), "config.yml"));
            }
            catch (Exception ex) {}
        }
        final File database = new File(this.getDataFolder() + File.separator + "database.db");
        if (!database.exists()) {
            try {
                database.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.getConfig().getBoolean("MySQL.Ativado")) {
            this.mysql = new MySQL(this.getConfig().getString("MySQL.Usuario"), this.getConfig().getString("MySQL.Senha"), this.getConfig().getString("MySQL.Database"), this.getConfig().getString("MySQL.Host"));
            this.getLogger().info("MySQL Habilitado!");
        }
        else {
            this.sqlite = new SQLite();
        }
        if (this.getConfig().getBoolean("AutoStart.Ativo")) {
            this.getLogger().info("AutoStart Habilitado!");
            this.getServer().getScheduler().runTaskTimer((Plugin)this, (Runnable)new Runnable() {
                @Override
                public void run() {
                    Main.this.checkAutoStart();
                }
            }, 0L, 1000L);
        }
        else {
            this.getLogger().info("AutoStart Desabilitado!");
        }
        final String[] ent = this.getConfig().getString("Arena.Entrada").split(";");
        this.spawn = new Location(this.getServer().getWorld(ent[0]), Double.parseDouble(ent[1]), Double.parseDouble(ent[2]), Double.parseDouble(ent[3]), Float.parseFloat(ent[4]), Float.parseFloat(ent[5]));
        final String[] sai = this.getConfig().getString("Arena.Saida").split(";");
        this.saida = new Location(this.getServer().getWorld(sai[0]), Double.parseDouble(sai[1]), Double.parseDouble(sai[2]), Double.parseDouble(sai[3]), Float.parseFloat(sai[4]), Float.parseFloat(sai[5]));
        final String[] cam = this.getConfig().getString("Arena.Camarote").split(";");
        this.camarote = new Location(this.getServer().getWorld(cam[0]), Double.parseDouble(cam[1]), Double.parseDouble(cam[2]), Double.parseDouble(cam[3]), Float.parseFloat(cam[4]), Float.parseFloat(cam[5]));
        this.sb = this.getServer().getScoreboardManager().getMainScoreboard();
        this.formatEnabled = this.getConfig().getBoolean("Format.Ativar");
        if (this.formatEnabled) {
            this.getServer().getScheduler().runTaskTimer((Plugin)this, (Runnable)new Runnable() {
                @Override
                public void run() {
                    Player[] onlinePlayers;
                    for (int length = (onlinePlayers = Main.this.getServer().getOnlinePlayers()).length, i = 0; i < length; ++i) {
                        final Player p = onlinePlayers[i];
                        Team t = null;
                        if (Main.this.sb.getPlayerTeam((OfflinePlayer)p) == null) {
                            if ((t = Main.this.sb.getTeam(p.getName().toLowerCase())) == null) {
                                t = Main.this.sb.registerNewTeam(p.getName().toLowerCase());
                            }
                            t.addPlayer((OfflinePlayer)p);
                        }
                        if (t == null) {
                            t = Main.this.sb.getPlayerTeam((OfflinePlayer)p);
                        }
                        t.setPrefix(Main.this.formatTag(p));
                    }
                }
            }, (long)(this.getConfig().getInt("Update") * 20), (long)(this.getConfig().getInt("Update") * 20));
        }
        else {
            this.getServer().getScheduler().runTaskTimer((Plugin)this, (Runnable)new Runnable() {
                @Override
                public void run() {
                    Player[] onlinePlayers;
                    for (int length = (onlinePlayers = Main.this.getServer().getOnlinePlayers()).length, i = 0; i < length; ++i) {
                        final Player p = onlinePlayers[i];
                        if (Main.this.playerHasClan(p) && Main.this.sb.getTeam(p.getName().toLowerCase()) == null) {
                            final Team t = Main.this.sb.registerNewTeam(p.getName().toLowerCase());
                            t.setPrefix(Main.this.formatTag(p));
                            t.addPlayer((OfflinePlayer)p);
                        }
                        else if (Main.this.playerHasClan(p) && Main.this.sb.getTeam(p.getName().toLowerCase()) != null) {
                            final Team t = Main.this.sb.getPlayerTeam((OfflinePlayer)p);
                            t.setPrefix(Main.this.formatTag(p));
                        }
                        else if (!Main.this.playerHasClan(p) && Main.this.sb.getTeam(p.getName().toLowerCase()) != null) {
                            Main.this.sb.getTeam(p.getName().toLowerCase()).unregister();
                        }
                    }
                }
            }, (long)(this.getConfig().getInt("Update") * 20), (long)(this.getConfig().getInt("Update") * 20));
        }
        try {
            final File file3 = new File(this.getDataFolder(), "mensagens.yml");
            if (!file3.exists()) {
                this.saveResource("mensagens.yml", false);
                this.getLogger().info("Salvo mensagens.yml");
            }
        }
        catch (Exception ex2) {}
        try {
            final File file3 = new File(this.getDataFolder(), "preparar.txt");
            if (!file3.exists()) {
                this.saveResource("preparar.txt", false);
                this.getLogger().info("Salvo preparar.txt");
            }
        }
        catch (Exception ex3) {}
        try {
            final File file3 = new File(this.getDataFolder(), "cancelar.txt");
            if (!file3.exists()) {
                this.saveResource("cancelar.txt", false);
                this.getLogger().info("Salvo cancelar.txt");
            }
        }
        catch (Exception ex4) {}
        try {
            final File file3 = new File(this.getDataFolder(), "continuar.txt");
            if (!file3.exists()) {
                this.saveResource("continuar.txt", false);
                this.getLogger().info("Salvo continuar.txt");
            }
        }
        catch (Exception ex5) {}
        try {
            final File file3 = new File(this.getDataFolder(), "iniciando.txt");
            if (!file3.exists()) {
                this.saveResource("iniciando.txt", false);
                this.getLogger().info("Salvo iniciando.txt");
            }
        }
        catch (Exception ex6) {}
        try {
            final File file3 = new File(this.getDataFolder(), "iniciar.txt");
            if (!file3.exists()) {
                this.saveResource("iniciar.txt", false);
                this.getLogger().info("Salvo iniciar.txt");
            }
        }
        catch (Exception ex7) {}
        try {
            final File file3 = new File(this.getDataFolder(), "finalizar.txt");
            if (!file3.exists()) {
                this.saveResource("finalizar.txt", false);
                this.getLogger().info("Salvo finalizar.txt");
            }
        }
        catch (Exception ex8) {}
        Mensagens.loadMensagens();
        this.mito = this.getConfig().getBoolean("Premios.Mito.Ativar");
    }
    
    public void onDisable() {
        this.getLogger().info("Desativando EasyGlad - Autor: Sardinhagamer_HD");
    }
    
    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        final RegisteredServiceProvider<Economy> rsp = (RegisteredServiceProvider<Economy>)this.getServer().getServicesManager().getRegistration((Class)Economy.class);
        if (rsp == null) {
            return false;
        }
        Main.econ = (Economy)rsp.getProvider();
        return Main.econ != null;
    }
    
    public boolean checkAutoStart() {
        final Calendar c = Calendar.getInstance();
        final Date data = c.getTime();
        final List<String> dias = (List<String>)this.getConfig().getStringList("AutoStart.Dias");
        final String iniciarhora = this.getConfig().getString("AutoStart.Hora");
        final String[] ih = iniciarhora.split(":");
        final int hora = Integer.parseInt(ih[0]);
        final int minutos = Integer.parseInt(ih[1]);
        switch (c.get(7)) {
            case 1: {
                if (dias.contains("Domingo") && data.getHours() == hora && data.getMinutes() == minutos) {
                    this.prepareEasyGlad();
                    break;
                }
                break;
            }
            case 2: {
                if (dias.contains("Segunda") && data.getHours() == hora && data.getMinutes() == minutos) {
                    this.prepareEasyGlad();
                    break;
                }
                break;
            }
            case 3: {
                if (dias.contains("Terca") && data.getHours() == hora && data.getMinutes() == minutos) {
                    this.prepareEasyGlad();
                    break;
                }
                break;
            }
            case 4: {
                if (dias.contains("Quarta") && data.getHours() == hora && data.getMinutes() == minutos) {
                    this.prepareEasyGlad();
                    break;
                }
                break;
            }
            case 5: {
                if (dias.contains("Quinta") && data.getHours() == hora && data.getMinutes() == minutos) {
                    this.prepareEasyGlad();
                    break;
                }
                break;
            }
            case 6: {
                if (dias.contains("Sexta") && data.getHours() == hora && data.getMinutes() == minutos) {
                    this.prepareEasyGlad();
                    break;
                }
                break;
            }
            case 7: {
                if (dias.contains("Sabado") && data.getHours() == hora && data.getMinutes() == minutos) {
                    this.prepareEasyGlad();
                    break;
                }
                break;
            }
        }
        return false;
    }
    
    public void addSpectator(final Player p) {
        for (final String a : this.participantes) {
            final Player all = Bukkit.getPlayer(a);
            all.hidePlayer(p);
        }
        this.specs.add(p.getName());
        final ItemStack exit = new ItemStack(Material.BED);
        final ItemMeta exitm = exit.getItemMeta();
        exitm.setDisplayName("§c§lSair");
        final List<String> lore = new ArrayList<String>();
        lore.add("§cClique aqui para");
        lore.add("§csair do espectador");
        exitm.setLore((List)lore);
        exit.setItemMeta(exitm);
        p.getInventory().setItem(4, exit);
        p.setAllowFlight(true);
        p.setFlying(true);
        p.setFlySpeed(0.1f);
        p.teleport(this.camarote);
    }
    
    public void removeSpectator(final Player p) {
        if (this.specs.contains(p.getName())) {
            Player[] onlinePlayers;
            for (int length = (onlinePlayers = Bukkit.getOnlinePlayers()).length, i = 0; i < length; ++i) {
                final Player a = onlinePlayers[i];
                if (this.specs.contains(p.getName())) {
                    a.showPlayer(p);
                }
            }
            p.getInventory().clear();
            this.specs.remove(p.getName());
            p.teleport(this.saida);
            p.setAllowFlight(false);
            p.setFlying(false);
        }
    }
    
    protected void prepareEasyGlad() {
        if (this.EasyGladEtapa != 0) {
            return;
        }
        final String[] ent = this.getConfig().getString("Arena.Entrada").split(";");
        this.spawn = new Location(this.getServer().getWorld(ent[0]), Double.parseDouble(ent[1]), Double.parseDouble(ent[2]), Double.parseDouble(ent[3]), Float.parseFloat(ent[4]), Float.parseFloat(ent[5]));
        final String[] sai = this.getConfig().getString("Arena.Saida").split(";");
        this.saida = new Location(this.getServer().getWorld(sai[0]), Double.parseDouble(sai[1]), Double.parseDouble(sai[2]), Double.parseDouble(sai[3]), Float.parseFloat(sai[4]), Float.parseFloat(sai[5]));
        final String[] cam = this.getConfig().getString("Arena.Camarote").split(";");
        this.camarote = new Location(this.getServer().getWorld(cam[0]), Double.parseDouble(cam[1]), Double.parseDouble(cam[2]), Double.parseDouble(cam[3]), Float.parseFloat(cam[4]), Float.parseFloat(cam[5]));
        this.getServer().dispatchCommand((CommandSender)this.getServer().getConsoleSender(), "clan globalff auto");
        this.EasyGladEtapa = 1;
        this.tirarTagsAntigas();
        this.messagePrepare(this.getConfig().getInt("Timers.Preparar.Avisos"));
    }
    
    private void messagePrepare(final int vezes) {
        this.canStart = true;
        if (this.EasyGladEtapa != 1) {
            return;
        }
        this.canStart = false;
        if (vezes == 0) {
            this.preparedEasyGlad();
        }
        else {
            for (final String n : Mensagens.getPreparar()) {
                if ((this.mito || !n.startsWith("(mito)")) && (!this.mito || !n.startsWith("(gladiador)"))) {
                    if (n.startsWith("(mito)")) {
                        this.getServer().broadcastMessage(n.substring(6).replace("&", "§").replace("@dinheiro", Double.toString(PremioTotal)).replace("@tempo", Integer.toString(vezes * this.getConfig().getInt("Timers.Preparar.TempoEntre"))).replace("@clans", Integer.toString(this.getClansEGSize())).replace("@jogadores", Integer.toString(this.participantes.size()).replace("@limite", Integer.toString(this.getConfig().getInt("Config.Limite")))));
                    }
                    else if (n.startsWith("(gladiador)")) {
                        this.getServer().broadcastMessage(n.substring(11).replace("&", "§").replace("@dinheiro", Double.toString(PremioTotal)).replace("@tempo", Integer.toString(vezes * this.getConfig().getInt("Timers.Preparar.TempoEntre"))).replace("@clans", Integer.toString(this.getClansEGSize())).replace("@jogadores", Integer.toString(this.participantes.size()).replace("@limite", Integer.toString(this.getConfig().getInt("Config.Limite")))));
                    }
                    else {
                        this.getServer().broadcastMessage(n.replace("&", "§").replace("@dinheiro", Double.toString(PremioTotal)).replace("@tempo", Integer.toString(vezes * this.getConfig().getInt("Timers.Preparar.TempoEntre"))).replace("@clans", Integer.toString(this.getClansEGSize())).replace("@jogadores", Integer.toString(this.participantes.size()).replace("@limite", Integer.toString(this.getConfig().getInt("Config.Limite")))));
                    }
                }
            }
        }
        this.getServer().getScheduler().runTaskLater((Plugin)this, (Runnable)new Runnable() {
            @Override
            public void run() {
                Main.this.canStart = true;
                if (Main.this.EasyGladEtapa != 1) {
                    return;
                }
                Main.this.canStart = false;
                Main.this.messagePrepare(vezes - 1);
            }
        }, (long)(20 * this.getConfig().getInt("Timers.Preparar.TempoEntre")));
    }
    
    public int getClansEGSize() {
        return this.getClansEG_core1().size();
    }
    
    protected void preparedEasyGlad() {
        if (this.getClansEGSize() < 2) {
            this.cancelEasyGlad();
            for (final String n : Mensagens.getCancelar()) {
                this.getServer().broadcastMessage(n.replace("&", "§"));
            }
            return;
        }
        this.EasyGladEtapa = 2;
        for (final String n : Mensagens.getContinuar()) {
        	this.giverItensInicio();
            this.getServer().broadcastMessage(n.replace("&", "§"));
        }
        this.canStart = false;
        this.messageIniciando(this.getConfig().getInt("Timers.Iniciando.Avisos"));
    }
    
    private void messageIniciando(final int vezes) {
        this.canStart = true;
        if (this.EasyGladEtapa != 2) {
            return;
        }
        this.canStart = false;
        if (vezes == 0) {
            this.startEasyGlad();
        }
        else {
            for (final String n : Mensagens.getIniciando()) {
                this.getServer().broadcastMessage(n.replace("&", "§").replace("@tempo", Integer.toString(vezes * this.getConfig().getInt("Timers.Iniciando.TempoEntre"))));
                this.getServer().dispatchCommand((CommandSender)this.getServer().getConsoleSender(), "tm bc &3&lEasyGlad<nl>&bPrepare-se");
            }
        }
        this.getServer().getScheduler().runTaskLater((Plugin)this, (Runnable)new Runnable() {
            @Override
            public void run() {
                Main.this.canStart = true;
                if (Main.this.EasyGladEtapa != 2) {
                    return;
                }
                Main.this.canStart = false;
                Main.this.messageIniciando(vezes - 1);
            }
        }, (long)(20 * this.getConfig().getInt("Timers.Iniciando.TempoEntre")));
    }
    
    protected void startEasyGlad() {
        if (this.getClansEGSize() < 2) {
            this.cancelEasyGlad();
            for (final String n : Mensagens.getCancelar()) {
                this.getServer().broadcastMessage(n.replace("&", "§"));
                this.getServer().dispatchCommand((CommandSender)this.getServer().getConsoleSender(), "tm bc &3&lEasyGlad<nl>&cEvento Cancelado!");
            }
            return;
        }
        this.canStart = true;
        this.EasyGladEtapa = 3;
        for (final String n : Mensagens.getIniciar()) {
            this.getServer().broadcastMessage(n.replace("&", "§"));
        }
        this.taskL();
    }
    
	protected void checkEasyGladEnd_core1() {
        final List<Clan> lista = this.getClansEG_core1();
        if (lista.size() == 1 && this.EasyGladEtapa == 3) {
            this.EasyGladEtapa = 4;
            final Clan vencedor = lista.get(0);
            final double premio = this.PremioTotal * 1.0 / vencedor.getLeaders().size();
            for (final ClanPlayer cp : vencedor.getLeaders()) {
                Main.econ.depositPlayer(cp.getName(), premio);
            }
            if (this.getConfig().getBoolean("MySQL.Ativado")) {
                this.getMysql().addWinnerPoint(vencedor.getColorTag().replace("&", "§"));
                Utils.addClan(vencedor.getTag());
            }
            else {
                this.getSqlite().addWinnerPoint(vencedor.getTag(), vencedor.getColorTag().replace("&", "§"));
                Utils.addClan(vencedor.getTag());
            }
            for (final String n : Mensagens.getFinalizar()) {
            	 this.getServer().dispatchCommand((CommandSender)this.getServer().getConsoleSender(), "tm bc &3&lEasyGlad<nl>&7Parab\u00e9ns " + vencedor.getColorTag());
                if ((this.mito || !n.startsWith("(mito)")) && (!this.mito || !n.startsWith("(gladiador)"))) {
                    if (n.startsWith("(gladiador)")) {
                        this.getServer().broadcastMessage(n.replace("&", "§").substring(11).replace("@clan", vencedor.getColorTag()).replace("@dinheiro", Double.toString(PremioTotal)));
                    }
                    else if (n.startsWith("(mito)")) {
                        this.getServer().broadcastMessage(n.replace("&", "§").substring(6).replace("@clan", vencedor.getColorTag()).replace("@dinheiro", Double.toString(PremioTotal)));
                    }
                    else if (!this.mito) {
                        this.getServer().broadcastMessage(n.replace("&", "§").replace("@clan", vencedor.getColorTag()).replace("@dinheiro", Double.toString(PremioTotal)));
                    }
                    else {
                        this.getServer().broadcastMessage(n.replace("&", "§").replace("@clan", vencedor.getColorTag()).replace("@dinheiro", Double.toString(PremioTotal)));
                    }
                }
            }
            this.sendMessageEasyGlad(Mensagens.getMensagem("Final1").replace("@tempo", Integer.toString(this.getConfig().getInt("Timers.Finalizando"))));
            this.getServer().dispatchCommand((CommandSender)this.getServer().getConsoleSender(), "clan mod globalff auto");
            this.getServer().getScheduler().runTaskLater((Plugin)this, (Runnable)new Runnable() {
                @Override
                public void run() {
                    Main.this.finalizarEasyGlad();
                }
            }, (long)(20 * this.getConfig().getInt("Timers.Finalizando")));
        }
        else {
        }
    }
    
    protected void finalizarEasyGlad() {
        this.sendMessageEasyGlad(Mensagens.getMensagem("Final2"));
        this.limparInv();
        this.cancelEasyGlad();
    }
    
    protected void darMito(final String p) {
        this.getServer().dispatchCommand((CommandSender)this.getServer().getConsoleSender(), this.getConfig().getString("Premios.Mito.Cmd").replace("@player", p));
    }
    
    protected void darTagsNovas(final String p1, final String p2) {
        final List<String> l = new ArrayList<String>();
        if (p1 != null) {
            l.add(p1);
        }
        if (p2 != null) {
            l.add(p2);
        }
        this.getConfig().set("Vencedores", (Object)l);
        this.saveConfig();
        Mensagens.changeVencedores(l);
    }
    
    protected void tirarTagsAntigas() {
        this.getConfig().set("Vencedores", (Object)new ArrayList());
        this.saveConfig();
        final List<String> l = new ArrayList<String>();
        Mensagens.changeVencedores(l);
    }
    
    protected void cancelEasyGlad() {
        if (this.EasyGladEtapa == 0) {
            return;
        }
        if (this.getEasyGladEtapa() > 2) {
            Bukkit.getScheduler().cancelTask(this.id);
        }
        this.getServer().dispatchCommand((CommandSender)this.getServer().getConsoleSender(), "clan globalff auto");
        this.EasyGladEtapa = 0;
        this.limparInv();
        for (final String n : this.participantes) {
            this.getServer().getPlayer(n).teleport(this.saida);
        }
        this.participantes.clear();
        this.totalParticipantes.clear();
        this.clann.clear();
        this.PremioTotal = 0;
    }
    
    public int getEasyGladEtapa() {
        return this.EasyGladEtapa;
    }
    
    protected void addPlayer(final Player p) {
        this.totalParticipantes.put(p.getName(), 0);
        this.participantes.add(p.getName());
        final ClanPlayer cp = this.core1.getClanManager().getClanPlayer(this.getServer().getPlayer(p.getName()));
        if (!this.clann.containsKey(cp.getClan().getColorTag())) {
            this.clann.put(cp.getClan().getColorTag(), 1);
        }
        else {
            this.clann.put(cp.getClan().getColorTag(), this.clann.get(cp.getClan().getColorTag()) + 1);
        }
        p.teleport(this.spawn);
        p.sendMessage(Mensagens.getMensagem("Entrou1"));
        p.sendMessage(Mensagens.getMensagem("Entrou2"));
        p.sendMessage(Mensagens.getMensagem("Entrou3"));
    }
    
    protected void removePlayer(final Player p, final int motive) {
        if (!this.participantes.contains(p.getName())) {
            return;
        }
        this.participantes.remove(p.getName());
        final ClanPlayer cp = this.core1.getClanManager().getClanPlayer(this.getServer().getPlayer(p.getName()));
        if (this.clann.containsKey(cp.getClan().getColorTag())) {
            if (this.clann.get(cp.getClan().getColorTag()) > 0) {
                this.clann.put(cp.getClan().getColorTag(), this.clann.get(cp.getClan().getColorTag()) - 1);
            }
            else {
                this.clann.remove(cp.getClan().getTag());
            }
        }
        if (this.EasyGladEtapa < 2) {
            this.totalParticipantes.remove(p.getName());
        }
        else if (this.EasyGladEtapa == 3) {
            this.checkEasyGladEnd_core1();
        }
        this.limparInvPlayer(p);
        p.teleport(this.saida);
        if (this.EasyGladEtapa == 1) {
            this.totalParticipantes.remove(p.getName());
            if (motive != 3) {
                p.sendMessage(Mensagens.getMensagem("Saiu1"));
            }
            else {
                p.sendMessage(Mensagens.getMensagem("Saiu2"));
            }
        }
        else if (motive == 0) {
            p.sendMessage(Mensagens.getMensagem("Saiu3"));
        }
        else if (motive == 1) {
            p.sendMessage(Mensagens.getMensagem("Saiu4"));
        }
        else if (motive == 3) {
            p.sendMessage(Mensagens.getMensagem("Saiu2"));
        }
    }
    
    protected List<Clan> getClansEG_core1() {
        final List<Clan> clans = new ArrayList<Clan>();
        for (final String n : this.participantes) {
            final ClanPlayer cp = this.core1.getClanManager().getClanPlayer(this.getServer().getPlayer(n));
            if (cp != null && !clans.contains(cp.getClan())) {
                clans.add(cp.getClan());
            }
        }
        return clans;
    }
    
    protected List<String> getClansEG_core2() {
        final List<String> clanst = new ArrayList<String>();
        for (final String n : this.participantes) {
            final ClanPlayer cp = this.core1.getClanManager().getClanPlayer(this.getServer().getPlayer(n));
            final Clan clan = cp.getClan();
            if (cp != null && !clanst.contains(clan.getColorTag())) {
                clanst.add(clan.getColorTag());
            }
        }
        return clanst;
    }
    
    protected void sendMessageEasyGlad(final String msg) {
        for (final String n : this.participantes) {
            this.getServer().getPlayer(n).sendMessage(msg);
        }
    }
    
    protected void giverItensInicio() {
        for (final String n : this.participantes) {
            ItensUtils.loadInventory(this.getServer().getPlayer(n));
        }
    }
    
    protected void limparInv() {
        for (final String n : this.participantes) {
            this.getServer().getPlayer(n).getInventory().clear();
            this.getServer().getPlayer(n).getInventory().setArmorContents((ItemStack[])null);
        }
    }  
    
    protected void limparInvPlayer(Player p) {
            this.getServer().getPlayer(p.getName()).getInventory().clear();
            this.getServer().getPlayer(p.getName()).getInventory().setArmorContents((ItemStack[])null);
    }
    
    
    public String formatTag(final Player p) {
        if (this.formatEnabled) {
            String s = this.getConfig().getString("Format.default");
            for (final String n : this.getConfig().getConfigurationSection("Format").getKeys(false)) {
                if (!n.equals("default") && !n.equals("Ativar") && p.hasPermission("gladiador.format." + n)) {
                    s = this.getConfig().getString("Format." + n);
                    break;
                }
            }
            if (this.playerHasClan(p)) {
                String final_tag = "";
                final ClanPlayer cp = this.core1.getClanManager().getClanPlayer(p);
                final String ctag = cp.getTagLabel();
                final String ntag = cp.getTag();
                String lastcor = "";
                int parte = 0;
                for (int i = 0; i < ctag.length(); ++i) {
                    final char c = cp.getTagLabel().charAt(i);
                    if (Character.compare(Character.toLowerCase(c), Character.toLowerCase(ntag.charAt(parte))) == 0 && !this.lastchar(ctag, i)) {
                        if (lastcor.equals(ChatColor.getLastColors(ctag.substring(0, i)))) {
                            final_tag = String.valueOf(String.valueOf(String.valueOf(String.valueOf(final_tag)))) + c;
                        }
                        else {
                            final_tag = String.valueOf(String.valueOf(String.valueOf(String.valueOf(final_tag)))) + ChatColor.getLastColors(ctag.substring(0, i)) + c;
                            lastcor = ChatColor.getLastColors(ctag.substring(0, i));
                        }
                        ++parte;
                        if (ntag.length() - 1 < parte) {
                            break;
                        }
                    }
                }
                final String pronto = this.getConfig().getString("ClanTag");
                final int max = 16 - pronto.replace("%tag%", "").length();
                if (final_tag.length() > max) {
                    final_tag = final_tag.substring(0, max - 1);
                }
                if (Character.compare(final_tag.charAt(final_tag.length() - 1), '§') == 0) {
                    final_tag = final_tag.substring(0, final_tag.length() - 1);
                }
                s = s.replaceAll("%clan%", pronto.replace("%tag%", final_tag).replaceAll("&", "§"));
            }
            else {
                s = s.replaceAll("%clan%", "");
            }
            s = s.replaceAll("&", "§");
            if (s.length() > 16) {
                s = s.substring(0, 15);
            }
            return s;
        }
        String final_tag2 = "";
        final ClanPlayer cp2 = this.core1.getClanManager().getClanPlayer(p);
        final String ctag2 = cp2.getTagLabel();
        final String ntag2 = cp2.getTag();
        String lastcor2 = "";
        int parte2 = 0;
        for (int j = 0; j < ctag2.length(); ++j) {
            final char c2 = cp2.getTagLabel().charAt(j);
            if (Character.compare(Character.toLowerCase(c2), Character.toLowerCase(ntag2.charAt(parte2))) == 0 && !this.lastchar(ctag2, j)) {
                if (lastcor2.equals(ChatColor.getLastColors(ctag2.substring(0, j)))) {
                    final_tag2 = String.valueOf(String.valueOf(String.valueOf(String.valueOf(final_tag2)))) + c2;
                }
                else {
                    final_tag2 = String.valueOf(String.valueOf(String.valueOf(String.valueOf(final_tag2)))) + ChatColor.getLastColors(ctag2.substring(0, j)) + c2;
                    lastcor2 = ChatColor.getLastColors(ctag2.substring(0, j));
                }
                ++parte2;
                if (ntag2.length() - 1 < parte2) {
                    break;
                }
            }
        }
        final String pronto2 = this.getConfig().getString("ClanTag");
        final int max2 = 16 - pronto2.replace("%tag%", "").length();
        if (final_tag2.length() > max2) {
            final_tag2 = final_tag2.substring(0, max2 - 1);
        }
        if (Character.compare(final_tag2.charAt(final_tag2.length() - 1), '§') == 0) {
            final_tag2 = final_tag2.substring(0, final_tag2.length() - 1);
        }
        return pronto2.replace("%tag%", final_tag2).replaceAll("&", "§");
    }
    
    private boolean lastchar(final String str, final int pos) {
        return pos != 0 && Character.compare(str.charAt(pos - 1), '§') == 0;
    }
    
    public boolean playerHasClan(final Player p) {
        return this.core1.getClanManager().getClanPlayer(p) != null;
    }
    
    public boolean sameClan(final Player p1, final Player p2) {
        return this.core1.getClanManager().getClanPlayer(p1).getClan() == this.core1.getClanManager().getClanPlayer(p2).getClan();
    }
    
    
    public boolean isInventoryEmpty(final Player p) {
        ItemStack[] contents;
        for (int length = (contents = p.getInventory().getContents()).length, j = 0; j < length; ++j) {
            final ItemStack i = contents[j];
            ItemStack[] armorContents;
            for (int length2 = (armorContents = p.getInventory().getArmorContents()).length, k = 0; k < length2; ++k) {
                final ItemStack a = armorContents[k];
                if (a.getType() != Material.AIR) {
                    return false;
                }
            }
            if (i != null && i.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }  
    
    public void taskL() {
        this.id = Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)this, (Runnable)new Runnable() {
            @Override
            public void run() {
                final StringBuilder sb = new StringBuilder();
                for (final String xx : Main.this.clann.keySet()) {
                    if (Main.this.clann.get(xx) != 0 && Main.this.getClansEGSize() > 1) {
                        sb.append(String.valueOf(String.valueOf(String.valueOf(String.valueOf(xx.replace("[", "").replace("]", ""))))) + " §e[" + Main.this.clann.get(xx) + "], ");
                    }
                }
                Bukkit.getServer().broadcastMessage(Mensagens.getMensagem("Restam").replace("@clans", sb.toString()));
            }
        }, 0L, 1200L);
    }
    
    public SQLite getSqlite() {
        return this.sqlite;
    }
    
    public MySQL getMysql() {
        return this.mysql;
    }
}
