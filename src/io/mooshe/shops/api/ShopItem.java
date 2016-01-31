package io.mooshe.shops.api;

import java.util.*;

import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

public class ShopItem {

	private ItemStack item;
	private float buy = 0.0f, sell = 0.0f;
	private ItemMeta meta;
	
	public ShopItem(ItemStack item, float buy, float sell) {
		this.item = item;
		meta = item.getItemMeta();
		if(!meta.hasLore())
			meta.setLore(new ArrayList<String>());
		setBuy(buy);
		setSell(sell);
		item.setItemMeta(meta);
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public ItemStack getCleanCopy() {
		ItemStack clean = item.clone();
		ItemMeta meta = clean.getItemMeta();
		List<String> str = meta.getLore();
		if(sell > 0.0f)
			str.remove(str.size() - 1);
		str.remove(str.size() - 1);
		meta.setLore(str);
		clean.setItemMeta(meta);
		return clean;
	}
	
	public float getBuy() {
		return buy;
	}
	
	public float getSell() {
		return sell;
	}
	
	public void setBuy(float worth) {
		String desc = String.format("\u00A7aBuy: \u00A7o$%1.2f", worth);
		List<String> str = meta.getLore();
		int i = str.size() - (sell > 0.0f ? 2 : 1);
		if(str.size() > 0 && str.get(i).contains("\u00A7aBuy")) {
			str.remove(i);
			str.add(i, desc);
		}
		else
			str.add(desc);
		meta.setLore(str);
		item.setItemMeta(meta);
		this.buy = worth;
	}
	
	public void setSell(float worth) {
		String desc = String.format("\u00A7bSell: \u00A7o$%1.2f", worth);
		List<String> str = meta.getLore();
		int i = str.size() - 1;
		if(str.size() > 0 && str.get(i).contains("\u00A7bSell"))
			str.remove(i);
		if(worth > 0.0f)
			str.add(desc);
		meta.setLore(str);
		item.setItemMeta(meta);
		this.sell = worth;
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
