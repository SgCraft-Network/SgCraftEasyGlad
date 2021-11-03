package br.com.sgcraft.easyglad_v2.utils;

import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItensUtils {
	
	static Files files;
	
	static {
		ItensUtils.files = Files.getInstance();
	}

	 public static void saveInventory(final Player p) {
	        ItensUtils.files.getDataFile().set("EasyGlad.Inventory", (Object)p.getInventory().getContents());
	        ItensUtils.files.getDataFile().set("EasyGlad.Armor", (Object)p.getInventory().getArmorContents());
	        ItensUtils.files.saveDataFile();
	        p.getInventory().clear();
	        p.getInventory().setArmorContents((ItemStack[])null);
	        p.sendMessage("§5[EasyGlad] §cItens do evento EasyGlad salvos com sucesso!");
	    }
	    
	    public static void loadInventory(final Player p) {
	        final Object a = ItensUtils.files.getDataFile().get("EasyGlad.Inventory");
	        final Object b = ItensUtils.files.getDataFile().get("EasyGlad.Armor");
	        if (a == null || b == null) {
	            p.sendMessage("§5[EasyGlad] §cERRO: O Inventario do EasyGlad nao foi salvo!");
	            return;
	        }
	        ItemStack[] inventory = null;
	        ItemStack[] armor = null;
	        if (a instanceof ItemStack[]) {
	            inventory = (ItemStack[])a;
	        }
	        else if (a instanceof List) {
	            final List<?> lista = (List<?>)a;
	            inventory = lista.toArray(new ItemStack[0]);
	        }
	        if (b instanceof ItemStack[]) {
	            armor = (ItemStack[])b;
	        }
	        else if (b instanceof List) {
	            final List<?> listb = (List<?>)b;
	            armor = listb.toArray(new ItemStack[0]);
	        }
	        p.getInventory().setArmorContents((ItemStack[])null);
	        p.getInventory().clear();
	        p.getInventory().setContents(inventory);
	        p.getInventory().setArmorContents(armor);
	    }
	
}
