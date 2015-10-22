package me.geocraft.shops.api;

import java.util.*;

import org.bukkit.*;
import org.bukkit.inventory.*;

public class Shelf {

	private Inventory inventory;
	private List<ShopItem> shop = new ArrayList<ShopItem>();
	private String title;
	private long id;
	
	public Shelf(String title) {
		this.title = title;
		inventory = Bukkit.createInventory(new ShelfHolder(this), 54, title
				.replace("&", "\u00A7"));
		this.id = new Random().nextLong();
	}
	
	public void putItem(ShopItem item) {
		if(!shop.contains(item))
			shop.add(item);
		inventory.addItem(item.getItem());
	}
	
	public boolean takeItem(ShopItem item) {
		return shop.remove(item);
	}
	
	public String getName() {
		return inventory.getName();
	}
	
	public ShopItem getItem(int slot) {
		return shop.get(slot);
	}
	
	public int size() {
		return shop.size();
	}
	
	public boolean matches(Inventory inventory) {
		return ((ShelfHolder) this.inventory.getHolder()).matches(inventory);
	}
	
	public long getId() {
		return id;
	}
	
	public List<ShopItem> getStock() {
		return shop;
	}
	
	protected Inventory getInventory() {
		return inventory;
	}
	
	public void reload() {
		inventory = Bukkit.createInventory(new ShelfHolder(this), 54, title);
		for(ShopItem i : shop)
			inventory.addItem(i.getItem());
	}
	
	public void setName(String title) {
		this.title = title;
		reload();
	}
	
	public ShopItem get(ItemStack item) {
		for(ShopItem i : shop)
			if(i.getItem().getType().equals(item.getType()))
				if(i.getCleanCopy().isSimilar(item))
					return i;
		return null;
	}
	
}
