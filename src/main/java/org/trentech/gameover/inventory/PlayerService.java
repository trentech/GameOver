package org.trentech.gameover.inventory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.trentech.easykits.kits.Kit;
import org.trentech.easykits.sql.SQLKits;
import org.trentech.gameover.Main;
import org.trentech.gameover.inventory.sql.SQLMethods;

public class PlayerService extends SQLMethods {


	private static PlayerService service = new PlayerService();

	public static PlayerService instance() {
		return service;
	}
	
	public void saveInventory(Player player, String group) {
		if (SQLMethods.getInventory(player, group).isPresent()) {
			SQLMethods.update(player, group);
		} else {
			SQLMethods.create(player, group);
		}
	}
	
	public void savePlayerInventory(Player player, String group, boolean empty){
		savePlayerInv(player.getUniqueId().toString(), group);
	}
//	public void savePlayerInventory(Player player, String group, ItemStack[] invArray, ItemStack[] armorArray){
//		ByteArrayOutputStream inv = new ByteArrayOutputStream();
//		ByteArrayOutputStream arm = new ByteArrayOutputStream();
//	    try {
//	        BukkitObjectOutputStream invObjOS = new BukkitObjectOutputStream(inv);
//	        invObjOS.writeObject(invArray);
//	        invObjOS.close();
//	        BukkitObjectOutputStream armorObjOS = new BukkitObjectOutputStream(arm);
//	        armorObjOS.writeObject(armorArray);
//	        armorObjOS.close();
//	    } catch (IOException ioexception) {
//	        ioexception.printStackTrace();
//	    }
//	    
//	}
	
	public ItemStack[] getPlayerInventory(Player player, String group){
		Object inv = null;
        byte[] byteInv =  getPlayerInv(player.getUniqueId().toString(), group);
        if(byteInv != null){
    		ByteArrayInputStream ByteArIS = new ByteArrayInputStream(byteInv);         
            try {
                BukkitObjectInputStream invObjIS = new BukkitObjectInputStream(ByteArIS);
                inv = invObjIS.readObject();
                invObjIS.close();
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            }   		
        }
        return (ItemStack[]) inv;
	}
	
	public ItemStack[] getPlayerArmor(Player player, String group){
		Object armor = null;
        byte[] byteArm =  getPlayerArm(player.getUniqueId().toString(), group);
        if(byteArm != null){
    		ByteArrayInputStream ByteArIS = new ByteArrayInputStream(byteArm);
            try {
                BukkitObjectInputStream invObjIS = new BukkitObjectInputStream(ByteArIS);
                armor = invObjIS.readObject();
                invObjIS.close();
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            }
        }
		return (ItemStack[]) armor;
	}
	
	public float getPlayerExperience(Player player, String group){				
		return getPlayerExp(player.getUniqueId().toString(), group);
	}
	
	public int getPlayerExperienceLevel(Player player, String group){				
		return getPlayerExpLevel(player.getUniqueId().toString(), group);
	}
	
	public void savePlayerExperience(Player player, String group){
		int expLevel = player.getLevel();
		float exp = player.getExp();
		savePlayerExp(player.getUniqueId().toString(), group, exp);
		savePlayerExpLevel(player.getUniqueId().toString(), group, expLevel);
	}
	
	public float getPlayerSaturation(Player player, String group){				
		return getPlayerSat(player.getUniqueId().toString(), group);
	}
	
	public int getPlayerFoodLevel(Player player, String group){				
		return getPlayerFoodLev(player.getUniqueId().toString(), group);
	}
	
	public void savePlayerFood(Player player, String group){
		int foodLevel = player.getFoodLevel();
		float sat = player.getSaturation();
		savePlayerFoodLev(player.getUniqueId().toString(), group, foodLevel);
		savePlayerSat(player.getUniqueId().toString(), group, sat);
	}
	
	public double getPlayerHealth(Player player, String group){
		return getPlayerHlth(player.getUniqueId().toString(), group);
	}
	
	public void savePlayerHealth(Player player, String group){
		Damageable pl = (Damageable) player;
		double health = pl.getHealth();
		savePlayerHlth(player.getUniqueId().toString(), group, health);
	}
	
	public String getWorldGroup(World world){
		List<String> list = Main.getPlugin().getConfig().getStringList("Worlds");
		if(list.contains(world.getName())){
			return "active";
		}else{
			return "inactive";
		}
	}

	public void updatePlayer(Player player, String group){
		ItemStack[] invArray = getPlayerInventory(player, group);
		ItemStack[] armorArray = getPlayerArmor(player, group);			
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		if(invArray != null){
			player.getInventory().setContents(invArray);
		}
		if(armorArray != null){
			player.getInventory().setArmorContents(armorArray);
		}	
		player.setExp(getPlayerExperience(player, group));
		player.setLevel(getPlayerExperienceLevel(player, group));
		player.setFoodLevel(getPlayerFoodLevel(player, group));
		player.setSaturation(getPlayerSaturation(player, group));
		player.setHealth(getPlayerHealth(player, group));
	}
	
	public void savePlayer(Player player, String group){
		savePlayerInventory(player, group, false);
		savePlayerExperience(player, group);
		savePlayerFood(player, group);
		savePlayerHealth(player, group);
	}
	
	public void createPlayerInventory(Player player, String group){
		createPlayerInv(player.getUniqueId().toString(), group);
	}
}
