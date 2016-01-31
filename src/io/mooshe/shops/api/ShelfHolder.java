package io.mooshe.shops.api;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ShelfHolder implements InventoryHolder {

	
	private Shelf shelf;
	
	public ShelfHolder(Shelf shelf) {
		this.shelf = shelf;
	}
	
	public Inventory getInventory() {
		return shelf.getInventory();
	}
	
	public Shelf getShelf() {
		return shelf;
	}
	
	public boolean matches(Inventory inventory) {
		if(inventory.getHolder() instanceof ShelfHolder) {
			ShelfHolder holder = (ShelfHolder) inventory.getHolder();
			return holder.getShelf().getId() == shelf.getId();
		}
		return false;
	}
}
