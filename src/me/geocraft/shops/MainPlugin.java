package me.geocraft.shops;


import me.geocraft.shops.citizens.TraderTrait;
import me.geocraft.shops.util.Util;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.*;

public class MainPlugin extends JavaPlugin {
	
	public static final String PLUGIN_VERSION = "0.3-beta";
	
	public boolean freeTrade = true;
	
	@Override
	public void onEnable() {
		load();
		getLogger().info("RoyalShops v"+PLUGIN_VERSION+" enabled!");
	}
	
	@Override
	public void onDisable() {
		getLogger().info("RoyalShops v"+PLUGIN_VERSION+" disabled!");
	}

	
	public void load() {
		FileConfiguration cfg = getConfig();
		if(!cfg.contains("free-trade")) {
			Util.loadYamlConfig(this, "config.yml");
			reloadConfig();
			cfg = getConfig();
		}
		freeTrade = cfg.getBoolean("free-trade");
		CitizensAPI.getTraitFactory().registerTrait(
				TraitInfo.create(TraderTrait.class).withName("shop"));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl,
			String[] args) {
		//TODO: Add WAY cleaner command handling.
		NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
		if(npc == null) {
			sender.sendMessage("\u00A7cYou must select an NPC!");
			return true;
		}
		TraderTrait trait = npc.getTrait(TraderTrait.class);
		if(trait == null) {
			sender.sendMessage("\u00A7cThis NPC does not have the Trait"
					+ " 'shop'!");
			return true;
		}
		if((sender instanceof Player && trait.canModify((Player) sender)) ||
				sender.isOp()) {
			if(args.length < 1) {
				sender.sendMessage("\u00A7btype /"+lbl+" help for information");
				return true;
			}
			switch(args[0].toLowerCase()) {
				case "remitem":
				case "delitem":
					if(args.length < 2) {
						sender.sendMessage("\u00A7bUsage: /"+lbl+" "+args[0]
									+ "<slot[0-53]>");
						return true;
					}
					int slot = -1;
					try {
						slot = Integer.parseInt(args[1]);
					} catch(Exception e) {
						sender.sendMessage("\u00A7bUsage: /"+lbl+" "+args[0]
								+ "<slot[0-53]>");
						return true;
					}
					if(slot < 0 || slot > 53) {
						sender.sendMessage("\u00A7cInvalid Slot Number! Only 0-53 accepted.");
						return true;
					}
					String msg = "\u00A7bSuccessfully removed item in slot "
								+slot;
					if(!trait.getGUI().removeItem(slot))
						msg = "\u00A7cItem was not found in slot "+slot;
					sender.sendMessage(msg);
					return true;
				
				case "trade":
					sender.sendMessage("\u00A7bOpening up "+trait.shop_title);
					trait.getGUI().displayFrame((Player) sender);
					return true;
				
				case "additem":
					if(sender instanceof Player) {
						if(args.length < 3) {
							sender.sendMessage("\u00A7bUsage: /"+lbl+" "+args[0]
									+ " <buy> <sell>");
							return true;
						}
						float b = 0, s = 0;
						try {
							System.out.println("Parsing Variables");
							b = Float.parseFloat(args[1]);
							s = Float.parseFloat(args[2]);
						} catch (Exception e) {
							sender.sendMessage("\u00A7bUsage: /"+lbl+" "+args[0]
									+ " <buy> <sell>");
							return true;
						}
						ItemStack hand = ((Player) sender)
								.getInventory().getItemInHand();
						if(hand != null && !hand.getType().equals(Material.AIR)) {
							System.out.println("Adding Item");
							trait.getGUI().addItem(hand, b, s);
						} else {
							sender.sendMessage("\u00A7cYou must have the item you want to sell in your hands.");
							return true;
						}
						((Player) sender).setItemInHand(new ItemStack(Material.AIR));
						sender.sendMessage("\u00A7bSuccessfully Added "
									+hand+" to Shop!");
					}
					return true;
				
				case "sound":
					if(args.length < 3) {
						sender.sendMessage("\u00A7bUsage: /"+lbl+" "+args[0]
								+ "<open|use> <sound> [pitch(?|0.0-1.0)]");
						return true;
					}
					if(args.length >= 3) {
						if(args[3].contains("?"))
							trait.varyPitch = true;
						else
							try {
								trait.pitch = Float.parseFloat(args[3]);
							} catch(Exception e) {
								sender.sendMessage("\u00A74Unknown "
										+ "value "+args[3]);
								return true;
							}
					}
					switch(args[1].toLowerCase()) {
						case "open":
							trait.sound_onShopOpen = args[2].toUpperCase();
							break;
						case "use":
							trait.sound_onShopUse = args[2].toUpperCase();
							break;
						default:
							sender.sendMessage("\u00A74Unknown sound "+args[1]);
							return true;
					}
					
					sender.sendMessage("\u00A76Set sound to \u00A7c"
								+args[2].toUpperCase());
					return true;
				
				case "permissions":
				case "perm":
					trait.reqPerm = !trait.reqPerm;
					sender.sendMessage("\u00A76Set require_permission to \u00A7"+
							(trait.reqPerm ? "b" : "c")+trait.reqPerm);
					if(trait.reqPerm)
						sender.sendMessage("\u00A76Give any users you want "
								+ "to use this shop the permisssion "
								+ "'citizens.shop.npc"
								+trait.getNPC().getId()+"'");
					return true;
				
				case "click":
				case "onclick":
					trait.onClick = !trait.onClick;
					sender.sendMessage("\u00A76Set trade_on_click to \u00A7"+
							(trait.onClick ? "b" : "c")+trait.onClick);
					return true;
					
				case "title":
				case "name":
					if(args.length < 2) {
						sender.sendMessage("\u00A7bUsage: /"+lbl
								+" "+args[0]+" <title>");
						return true;
					}
					String s = args[1];
					for(int i = 2; i < args.length; i++) {
						s += " "+args[i];
					}
					trait.shop_title = s.replace("&", "\u00A7");
					trait.getGUI().getShelf().setName(trait.shop_title);
					sender.sendMessage("\u00A76Set NPC shop title to \u00A7a"
							+trait.shop_title);
					return true;
				case "infinite":
				case "inf":
					trait.shop_infinite = !trait.shop_infinite;
					sender.sendMessage("\u00A76Set shop_infinite to "
							+ "\u00A7"+(trait.shop_infinite ? "b" : "c")
							+trait.shop_infinite);
					return true;
			}
		} else {
			sender.sendMessage("\u00A74You do not have permission "
					+ "to use this command!");
			return true;
		}
		return false;
	}
}
