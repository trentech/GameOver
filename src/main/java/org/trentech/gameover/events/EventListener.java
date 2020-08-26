package org.trentech.gameover.events;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.trentech.gameover.Main;
import org.trentech.gameover.player.PlayerService;

public class EventListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLoginEvent(PlayerLoginEvent event){
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();

		File file = new File(Main.getPlugin().getDataFolder() + "/players/", uuid + ".yml");
		YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
		
		if(playerConfig.getString("Lives") == null){
			playerConfig.set("Lives", Main.getPlugin().getConfig().getInt("Lives"));
		}
		if(playerConfig.getString("Banned") == null){
			playerConfig.set("Banned", false);
		}
		if(playerConfig.getString("Time") == null){
			playerConfig.set("Time", 0);
		}
		
		try {
			playerConfig.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(playerConfig.getBoolean("Banned")){
			if(playerConfig.getLong("Time") == -1){
				event.setKickMessage(Main.getPlugin().getConfig().getString("Perm-Ban.Kick-Message"));
				event.setResult(Result.KICK_BANNED);			
			}else{
				String remaining = PlayerService.instance().tempBanCheck(player);
				
				if(remaining != null){
					String message = Main.getPlugin().getConfig().getString("Temp-Ban.Kick-Message").replace("%T", remaining);
					event.setKickMessage(message);
					event.setResult(Result.KICK_BANNED);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeathEvent(EntityDeathEvent event){
		if(event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			
			String uuid = player.getUniqueId().toString();
			
			List<String> worlds = Main.getPlugin().getConfig().getStringList("Worlds");
			
			if(worlds.contains(player.getWorld().getName())){
				if(player.hasPermission("GameOver.use")){
					File file = new File(Main.getPlugin().getDataFolder() + "/players/", uuid + ".yml");
					YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
					
					int lives = playerConfig.getInt("Lives");
					
					if((playerConfig.getInt("Lives") - 1) <= 0){
						event.getDrops().clear();
						
						playerConfig.set("Lives", Main.getPlugin().getConfig().getInt("Lives"));
						
						try {
							playerConfig.save(file);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						PlayerService.instance().deletePlayerData(player);					
					}else{
						playerConfig.set("Lives", lives - 1);
						player.sendMessage(ChatColor.YELLOW + "Lives: " + (lives - 1));
						
						try {
							playerConfig.save(file);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
