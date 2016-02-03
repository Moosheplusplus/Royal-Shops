package io.mooshe.shops.api;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.*;
import org.bukkit.metadata.*;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ShopGUI implements Listener {
	
	private JavaPlugin plugin;
	public String title;
	public List<Long> uids = new ArrayList<Long>();
	public List<Player> players = new ArrayList<Player>();
	private Shelf shelf;
	
	/**
	 * Creates a Shop GUI frame.
	 * 
	 * @param plugin The plugin instance
	 * @param title The display name of the shop. Default name is 
	 * {@code NPC_NAME's Shop}.
	 */
	public ShopGUI(JavaPlugin plugin, String title) {
		this.plugin = plugin;
		this.title = title;
		this.shelf = new Shelf(title);
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	/**
	 * Displays the trading shop to the player.
	 * 
	 * @param player The player to display it to
	 */
	public void displayFrame(Player player) {
		if(player.hasMetadata("trade-uid")) {
			return;
		}
		long uid = new Random().nextLong();
		player.setMetadata("trade-uid", new FixedMetadataValue(
				plugin, uid));
		uids.add(uid);
		players.add(player);
		player.openInventory(shelf.getInventory());
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onInventoryClick(InventoryClickEvent event) {
		if(!shelf.matches(event.getInventory())) {
			return;
		}
		try {
			Player player = (Player) event.getWhoClicked();
			if(event.getSlotType() == SlotType.OUTSIDE) {
				close(player);
				return;
			}
			long uid = player.getMetadata("trade-uid").get(0).asLong();
			int slot = event.getRawSlot();
			if(uids.contains(uid)) {
				if(shelf.size() > slot) {
					ShopItem si = shelf.getItem(slot);
					if(slot < 54 && slot > -1)
						buy(event, player, si);
				}
				if(slot < 90 && slot > 53) {
					ItemStack selected = event.getCurrentItem();
					if(selected != null &&
							selected.getType() != Material.AIR) {
						ShopItem si = shelf.get(selected);
						if(si != null && si.getSell() > 0.0f)
							sell(event, player, si);
						else
							player.sendMessage("\u00A7cYou can only sell items"
									+ " already in the shop!");
					}
				}
			} else {
				plugin.getLogger().warning("Player "+player.getName()+
						" tried to access "
						+ "restricted shop! [Unique ID MISMATCH "+uid+"]");
				System.out.println(uids);
				close(player);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		event.setCancelled(true);
	}
	
	public abstract void buy(InventoryClickEvent event, Player player,
			ShopItem item);
	public abstract void sell(InventoryClickEvent event, Player player,
			ShopItem item);

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(!shelf.matches(event.getInventory())) {
			return;
		}
		close(event.getPlayer());
	}
	
	public Shelf getShelf() {
		return shelf;
	}
	
	public void closeAll() {
		for(Player p : players) {
			close(p);
		}
	}
	public void close(HumanEntity p) {
		if(!p.hasMetadata("trade-uid") || p.getInventory() == null) {
			return;
		}
		long uid = p.getMetadata("trade-uid").get(0).asLong();
		uids.remove(uid);
		p.removeMetadata("trade-uid", plugin);
		players.remove(p);
		p.closeInventory();
	}
	
	public ShopItem addItem(ItemStack item, float buy, float sell) {
		if(!item.hasItemMeta()) {
			item.setItemMeta(Bukkit.getItemFactory().getItemMeta(
					item.getType()));
		}
		ShopItem si = new ShopItem(item, buy, sell);
		shelf.putItem(si);
		return si;
	}
	
	public boolean removeItem(int slot) {
		if(slot > shelf.size() - 1)
			return false;
		return shelf.takeItem(shelf.getItem(slot));
	}
}
