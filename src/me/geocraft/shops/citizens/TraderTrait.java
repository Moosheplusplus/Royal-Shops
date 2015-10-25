package me.geocraft.shops.citizens;

import java.util.*;

import me.geocraft.shops.MainPlugin;
import me.geocraft.shops.api.*;
import me.geocraft.shops.util.Util;
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
		sound_onShopOpen  = null,
		sound_onShopUse   = "ORB_PICKUP",
		shop_title        = "Shop";
	public boolean 
		reqPerm = false,
		onClick = true,
		shop_infinite = false,
		loaded = false,
		varyPitch = false;
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
	}

	@Override
	public void load(DataKey key) {
		sound_onShopOpen  = key.getString("sound_onShopOpen" );
		sound_onShopUse   = key.getString("sound_onShopUse"  );
		shop_title        = key.getString("shop_title"       );
		shop_infinite     = key.getBoolean("shop_infinite");
		reqPerm = key.getBoolean("require_permission");
		onClick = key.getBoolean("trade_on_click"    );
		varyPitch = key.getBoolean("vary_pitch");
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
		key.setString("sound_onShopOpen" , sound_onShopOpen );
		key.setString("sound_onShopBuy"  , sound_onShopUse  );
		key.setString("shop_title", shop_title);
		key.setBoolean("shop_infinite", shop_infinite);
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
				ItemStack icon = item.getItem();
				int amt = event.getClick().isShiftClick() ? 64 : 1;
				if(!shop_infinite && icon.getAmount() < amt)
					amt = icon.getAmount();
				EconomyResponse response = economy
						.withdrawPlayer(player, 
								item.getBuy() * amt);
				if(response.transactionSuccess()) {
					playSound(player, sound_onShopUse);
					player.sendMessage("\u00A76You bought \u00A7e"
							+amt+"\u00A76 of \u00A7a"
							+icon.getType()+"\u00A76.");
					ItemStack reward = item.getCleanCopy();
					reward.setAmount(amt);
					player.getInventory().addItem(reward);
					if(!shop_infinite)
						item.getItem().setAmount(amt);
				} else {
					player.sendMessage("\u00A7cYou cannot buy this at the moment!");
				}
			}

			@Override
			public void sell(InventoryClickEvent event, Player player, ShopItem item) {
				ItemStack product = event.getCurrentItem();
				int amt = event.getClick().isShiftClick() ? product.getAmount() : 1;
				EconomyResponse response = economy
						.depositPlayer(player, item.getSell() * amt);
				if(response.transactionSuccess()) {
					playSound(player, sound_onShopUse);
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
				varyPitch ? new Random().nextFloat() : pitch);
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
		playSound(player, sound_onShopOpen);
		gui.displayFrame(player);
	}

	public boolean canTrade(Player player) {
		return 
				(plugin.freeTrade && !reqPerm) || 
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
