package org.trentech.gameover.player;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.trentech.gameover.Main;
import org.trentech.gameover.Utils;
import org.trentech.gameover.sql.SQLMethods;

import com.earth2me.essentials.Essentials;

import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;

public class PlayerService extends SQLMethods {

	private static PlayerService service = new PlayerService();

	public static PlayerService instance() {
		return service;
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
		Optional<PlayerData> optional = SQLMethods.getPlayerData(player, group);
		
		if(optional.isPresent()) {
			PlayerData playerData = optional.get();
			
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			
			player.getInventory().setContents(playerData.getInventory());
			player.getInventory().setArmorContents(playerData.getEquipment());
			
			player.setExp(playerData.getExperience());
			player.setLevel(playerData.getExpLevel());
			player.setFoodLevel(playerData.getFoodLevel());
			player.setSaturation(playerData.getSaturation());
			player.setHealth(playerData.getHealth());
		}
	}
	
	public void savePlayer(Player player, String group){
		if (SQLMethods.getPlayerData(player, group).isPresent()) {
			SQLMethods.update(player, group);
		} else {
			SQLMethods.create(player, group);
		}
	}
	
	public void deletePlayerData(Player player){
		player.kickPlayer("Game Over!");
		deleteMoney(player);
		doRollback(player);
		deleteEssenHomes(player);
		SQLMethods.deletePlayerData(player.getUniqueId().toString(), "active");
		setBan(player);
	}
	
	public String tempBanCheck(Player player){
		File file = new File(Main.getPlugin().getDataFolder() + "/players/", player.getUniqueId().toString() + ".yml");
		YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);		

		Date date = new Date();
		
		Date playerDate = null;
		try {
			playerDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(playerConfig.getString("Time"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		long compare = TimeUnit.MILLISECONDS.toSeconds(date.getTime() - playerDate.getTime());
		long time = Utils.getTime();
		
		if(!(time - compare <= 0)){
			return Utils.formatTime(time - compare);
		}else{
			playerConfig.set("Banned", false);
			playerConfig.set("Time", 0);
			
			try {
				playerConfig.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	private void deleteMoney(Player player){
		if(Main.getPlugin().economy == null){
			return;
		}
		
		if(Main.getPlugin().getConfig().getBoolean("Remove-Money")){
			Main.getPlugin().economy.withdrawPlayer(player, Main.getPlugin().economy.getBalance(player));
		}
	}
	
	private void deleteEssenHomes(Player player){
		if(Main.getPlugin().getServer().getPluginManager().getPlugin("Essentials") == null){
			return;
		}
		
		Essentials essentials = (Essentials) Main.getPlugin().getServer().getPluginManager().getPlugin("Essentials");
		
		if(!Main.getPlugin().getConfig().getBoolean("Delete-Essentials-Homes")){
			return;
		}

		essentials.getUser(player).save();
		essentials.getUser(player).reloadConfig();
		
		List<String> homes = essentials.getUser(player).getHomes();
		
		for(String home : homes){
			try {
				Location homeLoc = essentials.getUser(player).getHome(home);
				World world = homeLoc.getWorld();
				
				if(Main.getPlugin().getConfig().getStringList("Worlds").contains(world.getName())){
					essentials.getUser(player).delHome(home);
					essentials.getUser(player).save();
					essentials.getUser(player).reloadConfig();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setBan(Player player){
		File file = new File(Main.getPlugin().getDataFolder() + "/players/", player.getUniqueId().toString() + ".yml");
		YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
		
		if(Main.getPlugin().getConfig().getBoolean("Temp-Ban.Enable")){
			playerConfig.set("Banned", true);
			playerConfig.set("Time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).toString());
		}
		
		if(Main.getPlugin().getConfig().getBoolean("Perm-Ban.Enable")){
			playerConfig.set("Banned", true);
			playerConfig.set("Time", -1);	
		}
		
		try {			
			playerConfig.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void doRollback(Player player){
		if(Main.getPlugin().getServer().getPluginManager().getPlugin("LogBlock") == null){
			return;
		}
		
		if(!Main.getPlugin().getConfig().getBoolean("Rollback")){
			return;
		}
		
		LogBlock logBlock = (LogBlock) Main.getPlugin().getServer().getPluginManager().getPlugin("LogBlock");
		
		List<String> worlds = Main.getPlugin().getConfig().getStringList("Worlds");
		
		for(String worldName : worlds){
			World world = Main.getPlugin().getServer().getWorld(worldName);
			
			QueryParams params = new QueryParams(logBlock);
			params.setPlayer(player.getName());
			params.world = world;
			params.silent = true;
			
			try {
			    logBlock.getCommandsHandler().new CommandRollback(Main.getPlugin().getServer().getConsoleSender(), params, true);
			} catch (Exception ex) {
				Main.getPlugin().getLogger().severe(String.format("[%s] Failed to rollback blocks", new Object[] {Main.getPlugin().getDescription().getName()}));
			}
		}
	}
}
