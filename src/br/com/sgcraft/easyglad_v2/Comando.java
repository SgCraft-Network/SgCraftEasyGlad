package br.com.sgcraft.easyglad_v2;

import org.bukkit.command.*;
import org.bukkit.entity.*;
import java.text.*;
import org.bukkit.inventory.*;
import br.com.sgcraft.easyglad_v2.mensagens.Mensagens;
import br.com.sgcraft.easyglad_v2.utils.ItensUtils;
import br.com.sgcraft.easyglad_v2.utils.SQLite;

import java.util.*;
import org.bukkit.*;

public class Comando implements CommandExecutor
{
    private Main plugin;
    public static SQLite sqlite;
    
    static {
        Comando.sqlite = new SQLite();
    }
    
    public Comando(final Main main) {
        this.plugin = main;
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (cmd.getName().equalsIgnoreCase("vencedores")) {
            if (args.length == 0) {
                final List<String> l = (List<String>)this.plugin.getConfig().getStringList("Vencedores");
                if (l.size() == 0) {
                    sender.sendMessage(Mensagens.getMensagem("vencedores1"));
                    return true;
                }
                if (l.size() == 1) {
                    sender.sendMessage(Mensagens.getMensagem("vencedores2").replace("@nome", l.get(0)));
                }
                else {
                    sender.sendMessage(Mensagens.getMensagem("vencedores3").replace("@nome1", l.get(0)).replace("@nome2", l.get(1)));
                }
            }
            return true;
        }
        if (!cmd.getName().equalsIgnoreCase("easyglad")) {
            return false;
        }
        Player p = (Player) sender;
        if (args.length == 0) {
            if (sender == this.plugin.getServer().getConsoleSender()) {
                sender.sendMessage("§f[§3EasyGlad§f] §cConsole bloqueado de executar o comando!");
                return true;
            }
            if (this.plugin.getEasyGladEtapa() == 0) {
                sender.sendMessage(Mensagens.getMensagem("Erro1"));
                return true;
            }
            if (this.plugin.getEasyGladEtapa() > 1) {
                sender.sendMessage(Mensagens.getMensagem("Erro2"));
                return true;
            }
            if (this.plugin.participantes.contains(sender.getName())) {
                sender.sendMessage(Mensagens.getMensagem("Erro3"));
                return true;
            }
            if (this.plugin.getConfig().contains("Bans." + sender.getName().toLowerCase())) {
                sender.sendMessage(Mensagens.getMensagem("Erro4_1"));
                sender.sendMessage(Mensagens.getMensagem("Erro4_2").replace("@nome", this.plugin.getConfig().getString("Bans." + sender.getName().toLowerCase() + ".Por")).replace("@data", this.plugin.getConfig().getString("Bans." + sender.getName().toLowerCase() + ".Data")));
                return true;
            }
            if (this.plugin.core1.getClanManager().getClanPlayer((Player)sender) == null) {
                sender.sendMessage(Mensagens.getMensagem("Erro5"));
                return true;
            }
            if (((Player)sender).isInsideVehicle()) {
                sender.sendMessage(Mensagens.getMensagem("Erro6"));
                return true;
            }
            if (!Main.pl.isInventoryEmpty(p)) {
                p.sendMessage("§f[§3EasyGlad§f] §cEzvazie seu Inventário para entrar nesse Evento!");
                return true;
            }
            final String tag = this.plugin.core1.getClanManager().getClanPlayerName(((Player)sender).getName()).getTag();
            if (this.plugin.clann.containsKey(tag) && this.plugin.clann.get(tag) >= this.plugin.getConfig().getInt("Config.Limite")) {
                sender.sendMessage("§f[§3EasyGlad§f] §cO seu clan j\u00e1 atingiu o m\u00e1ximo de membros no EasyGlad!");
                return true;
            }
            final double moneyQtd = VaultAPI.getEconomy().getBalance(p);
			if (!(moneyQtd >= plugin.getConfig().getDouble("PrecoParticipar"))) {
                p.sendMessage("§f[§3EasyGlad§f] §cParar participar do evento você deve ter no minimo: §fR$" +  plugin.getConfig().getDouble("PrecoParticipar") + "0§c, e será cobrado uma taxa de §fR$" + plugin.getConfig().getDouble("PrecoEntrar") + "0.");
                return true;
            }
			else {
				VaultAPI.getEconomy().withdrawPlayer(p.getName(), plugin.getConfig().getInt("PrecoEntrar"));
				plugin.PremioTotal += plugin.getConfig().getInt("PrecoEntrar");
				this.plugin.addPlayer((Player)sender);
				p.sendMessage("§f[§3EasyGlad§f] §aVoce pagou §f" + plugin.getConfig().getDouble("PrecoEntrar") + "0, §aparar entrar no EasyGlad!");
			}
            return true;
        }
        else if (args[0].equalsIgnoreCase("sair")) {
            if (this.plugin.getEasyGladEtapa() == 0) {
                sender.sendMessage(Mensagens.getMensagem("Erro1"));
                return true;
            }
            if (this.plugin.getEasyGladEtapa() != 1) {
                sender.sendMessage(Mensagens.getMensagem("Erro7"));
                return true;
            }
            sender.sendMessage("§f[§3EasyGlad§f] §cComando n\u00e3o dispon\u00edvel no momento!");
            return true;
        }
        else {
            if (args[0].equalsIgnoreCase("top")) {
                if (this.plugin.getConfig().getBoolean("MySQL.Ativado")) {
                    this.plugin.getMysql().getTOPWins(p);
                }
                else {
                    this.plugin.getSqlite().getTOPWins(p);
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("topkills")) {
                if (!this.plugin.getConfig().getBoolean("MySQL.Ativado")) {
                    this.plugin.getSqlite().getTOPKills(p);
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("camarote")) {
                if (!sender.hasPermission("easyglad.camarote")) {
                    sender.sendMessage(Mensagens.getMensagem("Erro8"));
                    return true;
                }
                if (this.plugin.getEasyGladEtapa() < 2) {
                    sender.sendMessage(Mensagens.getMensagem("Erro1"));
                    return true;
                }
                if (this.plugin.participantes.contains(sender.getName())) {
                    sender.sendMessage(Mensagens.getMensagem("Erro9"));
                    return true;
                }
                final ItemStack[] armors = ((Player)sender).getInventory().getArmorContents();
                final ItemStack[] contents = ((Player)sender).getInventory().getContents();
                if (this.checkItemStacks(armors) || this.checkItemStacks(contents)) {
                    ((Player)sender).sendMessage("§f[§3EasyGlad§f] §cRemova todos items de seu invet\u00e1rio para entrar no camarote!");
                    return true;
                }
                this.plugin.addSpectator((Player)sender);
                sender.sendMessage(Mensagens.getMensagem("Msg1"));
                return true;
            }
            else {
                if (!sender.hasPermission("easyglad.admin")) {
                    sender.sendMessage(Mensagens.getMensagem("Erro10"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("forcestart")) {
                    if (this.plugin.getEasyGladEtapa() != 0) {
                        sender.sendMessage("§f[§3EasyGlad§f] §cJa existe um evento EasyGlad sendo executado!");
                        return true;
                    }
                    if (this.plugin.getEasyGladEtapa() == 0 && !this.plugin.canStart) {
                        sender.sendMessage("§f[§3EasyGlad§f] §eUm evento EasyGlad esta sendo finalizado!");
                        return true;
                    }
                    sender.sendMessage("§f[§3EasyGlad§f] §aEvento EasyGlad sendo iniciado!");
                    this.plugin.prepareEasyGlad();
                    return true;
                }
                else if (args[0].equalsIgnoreCase("reset")) {
                    if (args.length < 2 || args[1] == null) {
                        sender.sendMessage("§f[§3EasyGlad§f] §aForne\u00e7a um argumento: 'Wins' ou 'Kills'!");
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("wins")) {
                        if (sender.hasPermission("easyglad.admin")) {
                            if (this.plugin.getConfig().getBoolean("MySQL.Ativado")) {
                                this.plugin.getMysql().purgeRows();
                            }
                            else {
                                this.plugin.getSqlite().resetGladTop();
                            }
                            sender.sendMessage("§f[§3EasyGlad§f] §aOs TOPs ganhadores foram resetados!");
                            return true;
                        }
                        sender.sendMessage(Mensagens.getMensagem("Erro10"));
                        return true;
                    }
                    else {
                        if (!args[1].equalsIgnoreCase("kills")) {
                            return false;
                        }
                        if (sender.hasPermission("easyglad.admin")) {
                            if (this.plugin.getConfig().getBoolean("MySQL.Ativado")) {
                                this.plugin.getMysql().purgeRows();
                            }
                            else {
                                this.plugin.getSqlite().resetTopKills();
                            }
                            sender.sendMessage("§f[§3EasyGlad§f] §aO TOP Kills foi resetado!");
                            return true;
                        }
                        sender.sendMessage(Mensagens.getMensagem("Erro10"));
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase("forcestop")) {
                    if (this.plugin.getEasyGladEtapa() == 0) {
                        sender.sendMessage("§f[§3EasyGlad§f] §c Nao existe nenhum evento EasyGlad sendo executado!");
                        return true;
                    }
                    this.plugin.cancelEasyGlad();
                    sender.sendMessage("§f[§3EasyGlad§f] §eEvento EasyGlad sendo parado!");
                    return true;
                }
                else if (args[0].equalsIgnoreCase("kick")) {
                    if (args.length < 2) {
                        sender.sendMessage("§f[§3EasyGlad§f] §c/easyglad kick <nome>");
                        return true;
                    }
                    final String nome = args[1].toLowerCase();
                    final Player p2 = this.plugin.getServer().getPlayer(nome);
                    if (p2 == null) {
                        sender.sendMessage("§f[§3EasyGlad§f] §cJogador nao encontrado!");
                        return true;
                    }
                    this.plugin.removePlayer(p2, 3);
                    sender.sendMessage("§f[§3EasyGlad§f] " + ChatColor.GREEN + nome + " foi kickado do evento EasyGlad!");
                    return true;
                }
                else if (args[0].equalsIgnoreCase("info")) {
                    if (this.plugin.getEasyGladEtapa() != 3) {
                        sender.sendMessage("§f[§3EasyGlad§f] §cO evento EasyGlad n\u00e3o comecou!");
                        return true;
                    }
                    final StringBuilder sb = new StringBuilder();
                    for (final String xx : this.plugin.clann.keySet()) {
                        sb.append(String.valueOf(String.valueOf(String.valueOf(String.valueOf(xx.replace("[", "").replace("]", ""))))) + " §e[" + this.plugin.clann.get(xx) + "], ");
                    }
                    sender.sendMessage("§f[§3EasyGlad§f] §eRestam os clans " + sb.toString());
                    return true;
                }
                else if (args[0].equalsIgnoreCase("ban")) {
                    if (args.length < 2) {
                        sender.sendMessage("§f[§3EasyGlad§f] §c/easyglad ban <nome>");
                        return true;
                    }
                    final String nome = args[1].toLowerCase();
                    this.plugin.getConfig().set("Bans." + nome + ".Por", (Object)sender.getName());
                    this.plugin.getConfig().set("Bans." + nome + ".Data", (Object)new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
                    this.plugin.saveConfig();
                    final Player p2 = this.plugin.getServer().getPlayerExact(nome);
                    if (p2 != null) {
                        this.plugin.removePlayer(p2, 3);
                    }
                    sender.sendMessage("§f[§3EasyGlad§f] " + ChatColor.GREEN + nome + " foi banido dos eventos EasyGlad!");
                    return true;
                }
                else if (args[0].equalsIgnoreCase("unban")) {
                    if (args.length < 2) {
                        sender.sendMessage("§f[§3EasyGlad§f] §c/easyglad unban <nome>");
                        return true;
                    }
                    final String nome = args[1].toLowerCase();
                    if (!this.plugin.getConfig().contains("Bans." + nome)) {
                        sender.sendMessage("§f[§3EasyGlad§f] §cNome n\u00e3o encontrado!");
                        return true;
                    }
                    this.plugin.getConfig().set("Bans." + nome, (Object)null);
                    this.plugin.saveConfig();
                    sender.sendMessage("§f[§3EasyGlad§f] " + ChatColor.GREEN + nome + " foi desbanido dos eventos EasyGlad!");
                    return true;
                }
                else if (args[0].equalsIgnoreCase("setinv")) {
                	Player p2 = (Player) sender;
                	ItensUtils.saveInventory(p2);
                	return true;
                }
                else if (args[0].equalsIgnoreCase("setspawn")) {
                    if (sender == this.plugin.getServer().getConsoleSender()) {
                        sender.sendMessage("§f[§3EasyGlad§f] §cConsole bloqueado de executar o comando!");
                        return true;
                    }
                    this.plugin.spawn = p.getLocation();
                    this.plugin.getConfig().set("Arena.Entrada", (Object)(String.valueOf(String.valueOf(String.valueOf(String.valueOf(this.plugin.spawn.getWorld().getName())))) + ";" + this.plugin.spawn.getX() + ";" + this.plugin.spawn.getY() + ";" + this.plugin.spawn.getZ() + ";" + this.plugin.spawn.getYaw() + ";" + this.plugin.spawn.getPitch()));
                    this.plugin.saveConfig();
                    sender.sendMessage("§f[§3EasyGlad§f] §aSpawn marcado!");
                    return true;
                }
                else if (args[0].equalsIgnoreCase("setsaida")) {
                    if (sender == this.plugin.getServer().getConsoleSender()) {
                        sender.sendMessage("§f[§3EasyGlad§f] §cConsole bloqueado de executar o comando!");
                        return true;
                    }
                    this.plugin.saida = p.getLocation();
                    this.plugin.getConfig().set("Arena.Saida", (Object)(String.valueOf(String.valueOf(String.valueOf(String.valueOf(this.plugin.saida.getWorld().getName())))) + ";" + this.plugin.saida.getX() + ";" + this.plugin.saida.getY() + ";" + this.plugin.saida.getZ() + ";" + this.plugin.saida.getYaw() + ";" + this.plugin.saida.getPitch()));
                    this.plugin.saveConfig();
                    sender.sendMessage("§f[§3EasyGlad§f] §aSaida marcada!");
                    return true;
                }
                else if (args[0].equalsIgnoreCase("setcamarote")) {
                    if (sender == this.plugin.getServer().getConsoleSender()) {
                        sender.sendMessage("§f[§3EasyGlad§f] §aConsole bloqueado de executar o comando!");
                        return true;
                    }
                    this.plugin.camarote = p.getLocation();
                    this.plugin.getConfig().set("Arena.Camarote", (Object)(String.valueOf(String.valueOf(String.valueOf(String.valueOf(this.plugin.camarote.getWorld().getName())))) + ";" + this.plugin.camarote.getX() + ";" + this.plugin.camarote.getY() + ";" + this.plugin.camarote.getZ() + ";" + this.plugin.camarote.getYaw() + ";" + this.plugin.camarote.getPitch()));
                    this.plugin.saveConfig();
                    sender.sendMessage("§f[§3EasyGlad§f] §cCamarote marcado!");
                    return true;
                }
                else if (args[0].equalsIgnoreCase("setartags")) {
                    if (sender == this.plugin.getServer().getConsoleSender()) {
                        sender.sendMessage("§f[§3EasyGlad§f] §cConsole bloqueado de executar o comando!");
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage("§f[§3EasyGlad§f] §cDigite 2 Nicks para Setar as Tags! ");
                        return true;
                    }
                    final String tag2 = args[1];
                    final String tag3 = args[2];
                    Main.pl.darTagsNovas(tag2, tag3);
                    sender.sendMessage("§f[§3EasyGlad§f] §aTags setadas com sucesso!");
                    Bukkit.broadcastMessage("§r");
                    Bukkit.broadcastMessage("§b§m--------------------------------------------------");
                    Bukkit.broadcastMessage("  §fOs Seguintes Jogadores receberam a Tag §f[§3EasyGlad§f]§7:");
                    Bukkit.broadcastMessage("   §9- §f" + tag2);
                    Bukkit.broadcastMessage("   §9- §f" + tag3);
                    Bukkit.broadcastMessage("§b§m--------------------------------------------------");
                    Bukkit.broadcastMessage("§r");
                    return true;
                }
                else {
                    if (!args[0].equalsIgnoreCase("reload")) {
                        this.sendHelp((Player)sender);
                        return true;
                    }
                    if (this.plugin.getEasyGladEtapa() != 0) {
                        sender.sendMessage("§f[§3EasyGlad§f] §cNao existe um evento EasyGlad acontecendo!");
                        return true;
                    }
                    this.plugin.reloadConfig();
                    Mensagens.loadMensagens();
                    final String[] ent = this.plugin.getConfig().getString("Arena.Entrada").split(";");
                    this.plugin.spawn = new Location(this.plugin.getServer().getWorld(ent[0]), Double.parseDouble(ent[1]), Double.parseDouble(ent[2]), Double.parseDouble(ent[3]), Float.parseFloat(ent[4]), Float.parseFloat(ent[5]));
                    final String[] sai = this.plugin.getConfig().getString("Arena.Saida").split(";");
                    this.plugin.saida = new Location(this.plugin.getServer().getWorld(sai[0]), Double.parseDouble(sai[1]), Double.parseDouble(sai[2]), Double.parseDouble(sai[3]), Float.parseFloat(sai[4]), Float.parseFloat(sai[5]));
                    final String[] cam = this.plugin.getConfig().getString("Arena.Camarote").split(";");
                    this.plugin.camarote = new Location(this.plugin.getServer().getWorld(cam[0]), Double.parseDouble(cam[1]), Double.parseDouble(cam[2]), Double.parseDouble(cam[3]), Float.parseFloat(cam[4]), Float.parseFloat(cam[5]));
                    sender.sendMessage("§f[§3EasyGlad§f] §aConfiguracao recarregada!");
                    return true;
                }
            }
        }
    }
    
    public boolean checkItemStacks(final ItemStack[] ises) {
        for (final ItemStack is : ises) {
            if (is != null && is.getType() != Material.AIR) {
                return true;
            }
        }
        return false;
    }
    
    private void sendHelp(final Player p) {
        p.sendMessage("§f[§3EasyGlad§f] §fComandos do plugin:");
        p.sendMessage(ChatColor.AQUA + "/easyglad ? " + ChatColor.WHITE + "- Lista de comandos");
        p.sendMessage(ChatColor.AQUA + "/easyglad forcestart " + ChatColor.WHITE + "- Inicia o evento EasyGlad");
        p.sendMessage(ChatColor.AQUA + "/easyglad forcestop " + ChatColor.WHITE + "- Para o evento EasyGlad");
        p.sendMessage(ChatColor.AQUA + "/easyglad kick <nome> " + ChatColor.WHITE + "- Kicka um jogador do evento EasyGlad");
        p.sendMessage(ChatColor.AQUA + "/easyglad reset " + ChatColor.WHITE + "- Zera as vit\u00f3rias do EasyGlad TOP");
        p.sendMessage(ChatColor.AQUA + "/easyglad ban <nome> " + ChatColor.WHITE + "- Bane um jogador do evento EasyGlad");
        p.sendMessage(ChatColor.AQUA + "/easyglad unban <nome> " + ChatColor.WHITE + "- Desbane um jogador do evento EasyGlad");
        p.sendMessage(ChatColor.AQUA + "/easyglad setspawn " + ChatColor.WHITE + "- Marca local de spawn do evento EasyGlad");
        p.sendMessage(ChatColor.AQUA + "/easyglad setsaida " + ChatColor.WHITE + "- Marca local de saida do evento EasyGlad");
        p.sendMessage(ChatColor.AQUA + "/easyglad setcamarote " + ChatColor.WHITE + "- Marca local do camarote do evento EasyGlad");
        p.sendMessage(ChatColor.AQUA + "/easyglad setartags " + ChatColor.WHITE + "- Setar as Tags do EasyGlad");
        p.sendMessage(ChatColor.AQUA + "/easyglad setinv " + ChatColor.WHITE + "- Setar o Inventário do EasyGlad");
        p.sendMessage(ChatColor.AQUA + "/easyglad top " + ChatColor.WHITE + "- Top Clans Vencedores do EasyGlad");
        p.sendMessage(ChatColor.AQUA + "/easyglad topkills " + ChatColor.WHITE + "- Top Kills do EasyGlad");
        p.sendMessage(ChatColor.AQUA + "/easyglad info " + ChatColor.WHITE + "- Mostra quantos jogadores estao dentro do evento EasyGlad");
        p.sendMessage(ChatColor.AQUA + "/easyglad reload " + ChatColor.WHITE + "- Recarrega a configuracao");
    }
}
