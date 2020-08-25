package org.trentech.gameover.inventory;

import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.trentech.easykits.Main;

public class PlayerData {

	private UUID playerUUID;
	private int foodLevel;
	private int expLevel;
	private float experience;
	private float saturation;
	private double health;
	private ItemStack[] inventory;
	private ItemStack[] equipment;

	public PlayerData(UUID playerUUID, ItemStack[] inventory, ItemStack[] equipment, double health, float experience, int expLevel, int foodLevel, float saturation) {
		this.playerUUID = playerUUID;
		this.inventory = inventory;
		this.equipment = equipment;
		this.health = health;
		this.experience = experience;
		this.expLevel = expLevel;
		this.foodLevel = foodLevel;
		this.saturation = saturation;
	}
	
	public Kit(String name, Player player, long cooldown, double price, int limit) {
		this.name = name;
		this.inventory = player.getInventory().getContents();
		this.equipment = player.getInventory().getArmorContents();
		this.cooldown = cooldown;
		this.price = price;
		this.limit = limit;
	}

	public String getName() {
		return name;
	}

	public ItemStack[] getInventory(){
        return inventory;
	}
	
	public ItemStack[] getEquipment(){
        return equipment;
	}

	public int getLimit(){
		return limit;	
	}

	public double getPrice(){
		return price;
	}

	public long getCooldown(){
		return cooldown;
	}

	public void setInventory(ItemStack[] inventory){	
		this.inventory = inventory;
	}
	
	public void setEquipment(ItemStack[] equipment){	
		this.equipment = equipment;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
}
