package br.com.sgcraft.easyglad_v2.chat;

import br.com.devpaulo.legendchat.api.events.*;
import br.com.sgcraft.easyglad_v2.mensagens.*;

import org.bukkit.event.*;

public class LegendChat implements Listener
{
    @EventHandler
    private void onChat(final ChatMessageEvent e) {
        if (Mensagens.containsVencedor(e.getSender().getName()) && e.getTags().contains("easyglad")) {
            e.setTagValue("easyglad", Mensagens.getTag());
        }
    }
}
