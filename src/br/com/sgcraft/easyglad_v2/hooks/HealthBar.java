package br.com.sgcraft.easyglad_v2.hooks;

import com.gmail.filoghost.healthbar.api.*;

import br.com.sgcraft.easyglad_v2.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scoreboard.*;
import org.bukkit.event.*;

public class HealthBar implements Listener
{
    private Main plugin;
    
    public HealthBar(final Main main) {
        this.plugin = null;
        this.plugin = main;
    }
    
    @EventHandler
    private void onBhide(final BarHideEvent e) {
        if (this.plugin.sb.getTeam(e.getOfflinePlayer().getName().toLowerCase()) != null && !e.getOfflinePlayer().isOnline()) {
            this.plugin.sb.getTeam(e.getOfflinePlayer().getName().toLowerCase()).unregister();
        }
        if (e.getOfflinePlayer().isOnline()) {
            final Player p = e.getOfflinePlayer().getPlayer();
            Team t = null;
            if (this.plugin.sb.getPlayerTeam((OfflinePlayer)p) == null) {
                if ((t = this.plugin.sb.getTeam(p.getName().toLowerCase())) == null) {
                    t = this.plugin.sb.registerNewTeam(p.getName().toLowerCase());
                }
                t.addPlayer((OfflinePlayer)p);
            }
            if (t == null) {
                t = this.plugin.sb.getPlayerTeam((OfflinePlayer)p);
            }
            t.setPrefix(this.plugin.formatTag(p));
        }
    }
}
