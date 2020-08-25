package org.trentech.gameover;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.trentech.gameover.inventory.PlayerService;

import de.diddiz.LogBlock.QueryParams;

public class DataSource {

	public static DataSource get() {
		return new DataSource();
	}
	
	public void deletePlayerData(Player player){
		player.kickPlayer("Game Over!");
		deleteMoney(player);
		doRollback(player);
		deleteEssenHomes(player);
		PlayerService.instance().deletePlayerData(player.getUniqueId().toString(), "active");
		setBan(player);
	}
	
	public void deleteMoney(Player player){
		if(Main.getPlugin().econSupport){
			if(Main.getPlugin().getConfig().getBoolean("Remove-Money")){
				Main.getPlugin().economy.withdrawPlayer(player, Main.getPlugin().economy.getBalance(player));
			}
		}
	}
	
	public void deleteEssenHomes(Player player){
		if(Main.getPlugin().essSupport){
			if(Main.getPlugin().getConfig().getBoolean("Delete-Essentials-Homes")){
				List<String> homes = Main.getPlugin().essentials.getUser(player).getHomes();
				Main.getPlugin().essentials.getUser(player).save();
				Main.getPlugin().essentials.getUser(player).reloadConfig();
				for(String home : homes){
					try {
						Location homeLoc = Main.getPlugin().essentials.getUser(player).getHome(home);
						World world = homeLoc.getWorld();
						if(Main.getPlugin().getConfig().getStringList("Worlds").contains(world.getName())){
							Main.getPlugin().essentials.getUser(player).delHome(home);
							Main.getPlugin().essentials.getUser(player).save();
							Main.getPlugin().essentials.getUser(player).reloadConfig();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void setBan(Player player){
		File file = new File(Main.getPlugin().getDataFolder() + "/players/", player.getUniqueId().toString() + ".yml");
		YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
		if(Main.getPlugin().getConfig().getBoolean("Temp-Ban.Enable")){
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			String strDate = dateFormat.format(date).toString();
			playerConfig.set("Banned", true);
			playerConfig.set("Time", strDate);
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
	
	public void doRollback(Player player){
		if(Main.getPlugin().lbSupport){
			if(Main.getPlugin().getConfig().getBoolean("Rollback")){
				List<String> worlds = Main.getPlugin().getConfig().getStringList("Worlds");
				for(String worldName : worlds){
					World world = Main.getPlugin().getServer().getWorld(worldName);
					QueryParams params = new QueryParams(Main.getPlugin().logBlock);
					params.setPlayer(player.getName());
					params.world = world;
					params.silent = true;
					try {
					    Main.getPlugin().logBlock.getCommandsHandler().new CommandRollback(Main.getPlugin().getServer().getConsoleSender(), params, true);
					} catch (Exception ex) {
						Main.getPlugin().getLogger().severe(String.format("[%s] Failed to rollback blocks", new Object[] {Main.getPlugin().getDescription().getName()}));
					}
				}
			}
		}
	}
	
	public long getTime(){
		long time = 0;
		String[] strTime = Main.getPlugin().getConfig().getString("Temp-Ban.Time").split(" ");
		for(String t : strTime){
			if(t.matches("(^\\d*)(?i)[s]$")){
				time = time + Integer.parseInt(t.replace("s", ""));
			}else if(t.matches("(^\\d*)(?i)[m]$")){
				time = time + (Integer.parseInt(t.replace("m", "")) * 60);
			}else if(t.matches("(^\\d*)(?i)[h]$")){
				time = time + (Integer.parseInt(t.replace("h", "")) * 3600);
			}else if(t.matches("(^\\d*)(?i)[d]$")){
				time = time + (Integer.parseInt(t.replace("d", "")) * 86400);
			}else if(t.matches("(^\\d*)(?i)[w]$")){
				time = time + (Integer.parseInt(t.replace("w", "")) * 604800);
			}else if(t.matches("(^\\d*)(?i)[mo]$")){
				time = time + (Integer.parseInt(t.replace("mo", "")) * 2592000);
			}else if(t.matches("(^\\d*)(?i)[y]$")){
				time = time + (Integer.parseInt(t.replace("y", "")) * 31557600);
			}
		}
		return time;
	}
	
	public String tempBanCheck(Player player){
		String remaining = null;
		File file = new File(Main.getPlugin().getDataFolder() + "/players/", player.getUniqueId().toString() + ".yml");
		YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);		
		String playerDateStr = playerConfig.getString("Time");
		Date date = new Date();
		Date playerDate = null;
		try {
			playerDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(playerDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		long compare = TimeUnit.MILLISECONDS.toSeconds(date.getTime() - playerDate.getTime());
		long time = getTime();
		if(!(time - compare <= 0)){
			remaining = formatTime(time - compare);
		}else{
			playerConfig.set("Banned", false);
			playerConfig.set("Time", 0);
			try {
				playerConfig.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return remaining;
	}
	
	static String formatTime(long timeInSec) {
		long days = timeInSec / 86400;
		long dRemainder = timeInSec % 86400;
		long hours = dRemainder / 3600;
		long hRemainder = dRemainder % 3600;
		long minutes = hRemainder / 60;
		long seconds = hRemainder % 60;
		String time = null;
		if(days > 0){
			String dys = " Days";
			if(days == 1){
				dys = " Day";
			}
			time = days + dys;	
		}		
		if((hours > 0) || (hours == 0 && days > 0)){
			String hrs = " Hours";
			if(hours == 1){
				hrs = " Hour";
			}
			if(time != null){
				time = time + ", " + hours + hrs;
			}else{
				time = hours + hrs;
			}		
		}
		if((minutes > 0) || (minutes == 0 && days > 0) || (minutes == 0 && hours > 0)){
			String min = " Minutes";
			if(minutes == 1){
				min = " Minute";
			}
			if(time != null){
				time = time + ", " + minutes + min;	
			}else{
				time = minutes + min;
			}			
		}
		if(seconds > 0){
			String sec = " Seconds";
			if(seconds == 1){
				sec = " Second";
			}
			if(time != null){
				time = time + ", " + seconds + sec;
			}else{
				time = seconds + sec;
			}			
		}
		return time;
	}
	
	public UUID getUUID(String playerName){
		UUID uuid = null;
		HashMap<UUID, String> players = Main.getPlugin().players;
		Set<Map.Entry<UUID, String>> keys = players.entrySet();
		for(Entry<UUID, String> key : keys){
			if(key.getValue().equalsIgnoreCase(playerName)){
				uuid = key.getKey();
				break;
			}
		}
		return uuid;
	}
}
