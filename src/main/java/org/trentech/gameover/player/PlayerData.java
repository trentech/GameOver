package org.trentech.gameover.player;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;

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

	public UUID getPlayerUUID() {
		return playerUUID;
	}

	public ItemStack[] getInventory(){
        return inventory;
	}
	
	public ItemStack[] getEquipment(){
        return equipment;
	}
	
	public int getFoodLevel() {
		return foodLevel;
	}

	public int getExpLevel() {
		return expLevel;
	}

	public float getExperience() {
		return experience;
	}

	public float getSaturation() {
		return saturation;
	}
	
	public void setPlayerUUID(UUID playerUUID) {
		this.playerUUID = playerUUID;
	}
	
	public void setInventory(ItemStack[] inventory){	
		this.inventory = inventory;
	}
	
	public void setEquipment(ItemStack[] equipment){	
		this.equipment = equipment;
	}
	
	public void setFoodLevel(int foodLevel) {
		this.foodLevel = foodLevel;
	}
	public void setExpLevel(int expLevel) {
		this.expLevel = expLevel;
	}
	public void setExperience(float experience) {
		this.experience = experience;
	}

	public void setHealth(double health) {
		this.health = health;
	}

	public void setSaturation(float saturation) {
		this.saturation = saturation;
	}

	public double getHealth() {
		return health;
	}
}
