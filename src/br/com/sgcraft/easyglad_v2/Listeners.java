package br.com.sgcraft.easyglad_v2;

import org.bukkit.event.*;
import org.bukkit.entity.*;
import java.util.*;
import org.bukkit.*;
import org.bukkit.scoreboard.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

public class Listeners implements Listener
{
    private Main plugin;
    
    public Listeners(final Main main) {
        this.plugin = main;
    }
    
    @EventHandler
    private void onDeath(final PlayerDeathEvent e) {
        final Player killed = e.getEntity().getPlayer();
        if (e.getEntity().getKiller() instanceof Player) {
            final Player killer = e.getEntity().getKiller();

            if (this.plugin.participantes.contains(killer.getName()) && this.plugin.participantes.contains(e.getEntity().getName())) {
                final int k = this.plugin.totalParticipantes.get(killer.getName());
                this.plugin.totalParticipantes.remove(killer.getName());
                this.plugin.totalParticipantes.put(killer.getName(), k + 1);
                killer.sendMessage("§f[§3EasyGlad§f] §eVocê tem §6 " + (k + 1) + "§e kill!");
                Main.pl.getSqlite().addKillerPoint(killer.getName().toString());
            }
        }
        this.plugin.removePlayer(killed.getPlayer(), 1);
        this.plugin.checkEasyGladEnd_core1();
    }
    
    @EventHandler
    private void onQuit(final PlayerQuitEvent e) {
        if (!this.plugin.specs.isEmpty()) {
            this.plugin.removeSpectator(e.getPlayer());
        }
        this.plugin.removePlayer(e.getPlayer(), 2);
        if (this.plugin.sb.getTeam(e.getPlayer().getName().toLowerCase()) != null) {
            this.plugin.sb.getTeam(e.getPlayer().getName().toLowerCase()).unregister();
        }
    }
    
    @EventHandler
    private void onKick(final PlayerKickEvent e) {
        if (!this.plugin.specs.isEmpty()) {
            this.plugin.removeSpectator(e.getPlayer());
            Main.pl.limparInvPlayer(e.getPlayer());
        }
        this.plugin.removePlayer(e.getPlayer(), 2);
        if (this.plugin.sb.getTeam(e.getPlayer().getName().toLowerCase()) != null) {
            this.plugin.sb.getTeam(e.getPlayer().getName().toLowerCase()).unregister();
            Main.pl.limparInvPlayer(e.getPlayer());
        }
    }
    
   @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onDamage(final EntityDamageByEntityEvent e) {
        if (this.plugin.getEasyGladEtapa() != 0 && e.getEntity() instanceof Player && (e.getDamager() instanceof Player || e.getDamager() instanceof Projectile)) {
            final Player ent = (Player)e.getEntity();
            Player dam = null;
            if (e.getDamager() instanceof Player) {
                dam = (Player)e.getDamager();
            }
            else {
                final Projectile a = (Projectile)e.getDamager();
                if (a.getShooter() instanceof Player) {
                    dam = (Player)a.getShooter();
                }
            }
            if (!this.plugin.specs.isEmpty() && this.plugin.specs.contains(dam.getName())) {
                e.setCancelled(true);
                if (dam != null) {
                    dam.sendMessage("§f[§3EasyGlad§f] §cVoc\u00ea nao pode hitar no modo espectador!");
                }
            }
            if (this.plugin.participantes.contains(ent.getName()) && this.plugin.getEasyGladEtapa() != 3) {
                e.setCancelled(true);
                if (dam != null) {
                    dam.sendMessage("§f[§3EasyGlad§f] §cPvP desativado no momento!");
                }
            }
            if (dam != null && this.plugin.getEasyGladEtapa() == 3 && this.plugin.participantes.contains(ent.getName()) && this.plugin.participantes.contains(dam.getName())) {
                if (this.plugin.sameClan(ent, dam)) {
                    e.setCancelled(true);
                }
                else if (!this.plugin.sameClan(ent, dam)) {
                    e.setCancelled(false);
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onDamageP(final PotionSplashEvent e) {
        for (final Entity ent2 : e.getAffectedEntities()) {
            if (ent2 instanceof Player && this.plugin.getEasyGladEtapa() != 0) {
                final Player ent3 = (Player)ent2;
                Player dam = null;
                if (e.getPotion().getShooter() instanceof Player) {
                    dam = (Player)e.getEntity().getShooter();
                }
                if (this.plugin.participantes.contains(ent3.getName()) && this.plugin.getEasyGladEtapa() != 3) {
                    e.setCancelled(true);
                    if (dam != null) {
                        dam.sendMessage("§f[§3EasyGlad§f] §cPvP desativado no momento!");
                    }
                }
                if (dam == null || this.plugin.getEasyGladEtapa() != 3) {
                    continue;
                }
                if (!this.plugin.participantes.contains(ent3.getName())) {
                    continue;
                }
                if (!this.plugin.participantes.contains(dam.getName())) {
                    continue;
                }
                if (!this.plugin.sameClan(ent3, dam)) {
                    continue;
                }
                e.getAffectedEntities().remove(ent2);
            }
        }
    }
    
    @EventHandler
    private void onPCmd(final PlayerCommandPreprocessEvent e) {
        if (!this.plugin.specs.isEmpty() && this.plugin.specs.contains(e.getPlayer().getName()) && e.getMessage().toLowerCase().startsWith("/") && !e.getMessage().startsWith("/camarote sair")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(new String[] { "§f[§3EasyGlad§f] §cVoc\u00ea nao pode usar comandos no modo espectador!", "§f[§3EasyGlad§f] §ePara sair clique na cama do seu invent\u00e1rio!" });
        }
        if((this.plugin.getEasyGladEtapa() == 3 || this.plugin.getEasyGladEtapa() == 2 || this.plugin.getEasyGladEtapa() == 1) && (e.getMessage().toLowerCase().startsWith("/x9") || e.getMessage().toLowerCase().startsWith("/sgcraft:x9")|| e.getMessage().toLowerCase().startsWith("/fim") || e.getMessage().toLowerCase().startsWith("/bau") && e.getMessage().toLowerCase().startsWith("/enderchest") || e.getMessage().toLowerCase().startsWith("/ec"))) {
            e.getPlayer().sendMessage("§f[§3EasyGlad§f] §cComando bloqueado no evento EasyGlad!");
            e.setCancelled(true);
        }
        if (this.plugin.getEasyGladEtapa() == 3 && e.getMessage().toLowerCase().startsWith("/clan") && !e.getMessage().toLowerCase().startsWith("/clan resign") && !e.getMessage().toLowerCase().startsWith("/clan kick") && !e.getMessage().toLowerCase().startsWith("/clan invite") && !e.getMessage().toLowerCase().startsWith("/clan trust") && !e.getMessage().toLowerCase().startsWith("/clan profile")) {
            e.getPlayer().sendMessage("§f[§3EasyGlad§f] §cComando bloqueado no evento EasyGlad!");
            e.setCancelled(true);
        }
        else if (this.plugin.getEasyGladEtapa() != 0 && e.getMessage().toLowerCase().startsWith("/clan modtag")) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void durp(final PlayerInteractEvent e) {
        if (!this.plugin.specs.isEmpty() && this.plugin.specs.contains(e.getPlayer().getName()) && ((e.getPlayer().getItemInHand().getTypeId() == 355 && e.getAction() == Action.RIGHT_CLICK_AIR) || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            this.plugin.removeSpectator(e.getPlayer());
        }
    }
    
    @EventHandler
    public void aaa(final EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player p = (Player)e.getEntity();
            if (!this.plugin.specs.isEmpty() && this.plugin.specs.contains(p.getName())) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void hurp(final PlayerDropItemEvent e) {
        if (!this.plugin.specs.isEmpty() && this.plugin.specs.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onReceive(final PlayerPickupItemEvent e) {
        if (!this.plugin.specs.isEmpty() && this.plugin.specs.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
        }
    }
}
