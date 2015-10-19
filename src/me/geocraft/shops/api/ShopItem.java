package me.geocraft.shops.api;

import java.util.*;

import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

public class ShopItem {

	private ItemStack item;
	private float worth;
	private ItemMeta meta;
	
	public ShopItem(ItemStack item, float worth) {
		this.item = item;
		meta = item.getItemMeta();
		if(!meta.hasLore())
			meta.setLore(new ArrayList<String>());
		setWorth(worth);
		item.setItemMeta(meta);
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public ItemStack getCleanCopy() {
		ItemStack clean = item.clone();
		ItemMeta meta = clean.getItemMeta();
		List<String> str = meta.getLore();
		str.remove(str.size() - 1);
		meta.setLore(str);
		clean.setItemMeta(meta);
		return clean;
	}
	
	public float getWorth() {
		return worth;
	}
	
	public void setWorth(float worth) {
		String desc = "\u00A7bBuy: \u00A7o$"+worth;
		List<String> str = meta.getLore();
		if(str.size() > 0)
			str.remove(str.size() - 1);
		str.add(desc);
		meta.setLore(str);
		item.setItemMeta(meta);
		this.worth = worth;
	}
	
	public ItemMeta getItemMeta() {
		return meta;
	}
	
	public void addLore(String... str) {
		List<String> lore = new ArrayList<String>();
		if(meta.hasLore())
			lore.addAll(meta.getLore());
		lore.addAll(Arrays.asList(str));
		meta.setLore(lore);
		item.setItemMeta(meta);
	}
}
