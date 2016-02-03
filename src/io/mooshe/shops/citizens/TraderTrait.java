package io.mooshe.shops.citizens;

import io.mooshe.shops.MainPlugin;
import io.mooshe.shops.api.*;
import io.mooshe.shops.util.Util;

import java.util.*;

import net.citizensnpcs.api.event.*;
import net.citizensnpcs.api.trait.*;
import net.citizensnpcs.api.util.*;
import net.milkbowl.vault.economy.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TraderTrait extends Trait {

	private MainPlugin plugin;
	
	public String
		onShopOpen = "VILLAGER_HAGGLE",
		onShopUse  = "ORB_PICKUP",
		shop_title = "\u00a7aShop";
	public boolean 
		reqPerm,
		onClick,
		loaded = false,
		varyPitch = true;
	public float pitch = 1.0f;
	private ShopGUI gui = null;
	private static Economy economy = null;

	public TraderTrait() {
		super("shop");
		plugin = (MainPlugin) Bukkit.getServer().getPluginManager()
				.getPlugin("RoyalShops");
		if(economy == null)
			TraderTrait.economy = (plugin.getServer().getServicesManager()
			.getRegistration(Economy.class)).getProvider();
		shop_title = plugin.cfg.getString("default.title", shop_title).replace("&", "\u00A7");
		onShopOpen = plugin.cfg.getString("default.sounds.open", onShopOpen);
		onShopUse = plugin.cfg.getString("default.sounds.use", onShopUse);
		reqPerm = plugin.cfg.getBoolean("default.permission", false);
		onClick = plugin.cfg.getBoolean("default.right-click", true);
	}

	@Override
	public void load(DataKey key) {
		onShopOpen  = key.getString("sound_onShopOpen",onShopOpen);
		onShopUse   = key.getString("sound_onShopBuy", onShopUse);
		shop_title  = key.getString("shop_title", shop_title);
		reqPerm = key.getBoolean("require_permission", reqPerm);
		onClick = key.getBoolean("trade_on_click", onClick);
		varyPitch = key.getBoolean("vary_pitch", true);
		initGui();
		for(DataKey k : key.getIntegerSubKeys()) {
			ItemStack item = ItemStorage.loadItemStack(k);
			float b = (float) k.getDouble("worth");
			float s = (float) k.getDouble("sell");
			ItemMeta meta;
			if(item.hasItemMeta())
				meta = item.getItemMeta();
			else
				meta = Bukkit.getItemFactory().getItemMeta(item.getType());
			item.setItemMeta(meta);
			gui.addItem(item, b, s);
		}

		loaded = true;
	}
	
	@Override
	public void onAttach() {
		if(!loaded)
			initGui();
	}
	
	@Override
	public void save(DataKey key) {
		key.setString("sound_onShopOpen" , onShopOpen );
		key.setString("sound_onShopBuy"  , onShopUse  );
		key.setString("shop_title", shop_title);
		key.setBoolean("require_permission", reqPerm);
		key.setBoolean("trade_on_click", onClick);
		key.setBoolean("vary_pitch", varyPitch);
		List<ShopItem> stock = gui.getShelf().getStock();
		int p = 0;
		for(ShopItem i : stock) {
			ItemStack item = i.getCleanCopy();
			DataKey sk = key.getRelative(""+p);
			key.removeKey(""+p);
			ItemStorage.saveItem(sk, item);
			sk.setDouble("worth", i.getBuy());
			sk.setDouble("sell", i.getSell());
			p++;
		}
	}
	
	public void initGui() {
		if(gui != null)
			gui.closeAll();
		gui = new ShopGUI(plugin, shop_title) {

			@Override
			public void buy(InventoryClickEvent event, Player player,
					ShopItem item) {
				double d = getNPC().getStoredLocation()
						.distance(player.getLocation());
				if(d > 20d)
					return;
				ItemStack icon = item.getItem();
				int amt = event.getClick().isShiftClick() ? icon.getMaxStackSize() : 1;
				EconomyResponse response = economy
						.withdrawPlayer(player, 
								item.getBuy() * amt);
				if(response.transactionSuccess()) {
					playSound(player, onShopUse);
					player.sendMessage("\u00A76You bought \u00A7e"
							+amt+"\u00A76 of \u00A7a"
							+icon.getType()+"\u00A76.");
					ItemStack reward = item.getCleanCopy();
					reward.setAmount(amt);
					player.getInventory().addItem(reward);
					item.getItem().setAmount(1);
				} else {
					player.sendMessage("\u00A7cYou cannot buy this at the moment!");
				}
			}

			@Override
			public void sell(InventoryClickEvent event, Player player, ShopItem item) {
				double d = getNPC().getStoredLocation()
						.distance(player.getLocation());
				if(d > 20d)
					return;
				ItemStack product = event.getCurrentItem();
				int amt = event.getClick().isShiftClick() ? product.getAmount() : 1;
				EconomyResponse response = economy
						.depositPlayer(player, item.getSell() * amt);
				if(response.transactionSuccess()) {
					playSound(player, onShopUse);
					player.sendMessage("\u00A76You sold \u00A7e"
							+amt+"\u00A76 of \u00A7a"
							+product.getType()+"\u00A76.");
					if(product.getAmount() <= amt)
						player.getInventory().removeItem(product);
					else
						product.setAmount(product.getAmount() - amt);
				} else {
					player.sendMessage("\u00A7cYou cannot sell this at the moment!");
				}
			}
		};
	}
	
	private void playSound(Player player, String sound) {
		Util.playSound(player, sound,
				varyPitch ? 0.8f + (new Random().nextFloat() * 0.2f) : pitch);
	}
	
	@Override
	public void onDespawn() {
		if(gui != null)
			gui.closeAll();
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onRClick(NPCRightClickEvent event) {
		if(!event.getNPC().equals(getNPC()) || !onClick) {
			return;
		}
		Player buyer = event.getClicker();
		if(canTrade(buyer))
			trade(buyer);
		else {
			buyer.sendMessage("\u00A74You do not have permission to trade!");
			event.setCancelled(true);
		}
	}
	
	private void trade(Player player) {
		playSound(player, onShopOpen);
		gui.displayFrame(player);
	}

	public boolean canTrade(Player player) {
		return 
				!reqPerm || 
				(reqPerm && player.hasPermission(
						"citizens.shop.npc"+this.getNPC().getId())) ||
				(!reqPerm && player.hasPermission("citizens.shop.npc"));
	}
	
	public boolean canModify(Player player) {
		return 
				player.hasPermission("citizens.shop.admin") ||
				player.hasPermission("citizens.shop.npc"
						+getNPC().getId()+".modify");
	}
	
	public ShopGUI getGUI() {
		return gui;
	}

}
