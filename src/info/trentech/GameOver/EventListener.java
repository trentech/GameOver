package info.trentech.GameOver;

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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EventListener implements Listener{
	
	private GameOver plugin;
	public EventListener(GameOver plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoinEvent(PlayerJoinEvent event){
		if(plugin.getConfig().getString("Ban-Type").equalsIgnoreCase("global")){
			Player player = event.getPlayer();
			String uuid = player.getUniqueId().toString();
			plugin.players.put(event.getPlayer().getUniqueId(), event.getPlayer().getName());	
			File file = new File(this.plugin.getDataFolder() + "/players/", uuid + ".yml");
			YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
			if(playerConfig.getString("Lives") == null){
				playerConfig.set("Lives", plugin.getConfig().getInt("Lives"));
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
					String message = plugin.getConfig().getString("Perm-Ban.Kick-Message");
					event.setJoinMessage("");
					player.kickPlayer(message);
				}else{
					DataSource.instance.tempBanCheck(player, null, "global");
				}
			}
		}
	}
		
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTeleportEvent(PlayerTeleportEvent event){
		System.out.println("TEST");
		Player player = event.getPlayer();
		if(plugin.getConfig().getString("Ban-Type").equalsIgnoreCase("world")){
			System.out.println(event.getTo().getWorld().getName());		
			List<String> worlds = plugin.getConfig().getStringList("Worlds");
			System.out.println(worlds);
			if(worlds.contains(event.getTo().getWorld().getName())){
				System.out.println("TEST3");
				String uuid = player.getUniqueId().toString();
				plugin.players.put(event.getPlayer().getUniqueId(), event.getPlayer().getName());	
				File file = new File(this.plugin.getDataFolder() + "/players/", uuid + ".yml");
				YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
				if(playerConfig.getString("Lives") == null){
					playerConfig.set("Lives", plugin.getConfig().getInt("Lives"));
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
						String message = plugin.getConfig().getString("Perm-Ban.Kick-Message");
						event.setCancelled(true);
						event.getPlayer().sendMessage(ChatColor.RED + message);
					}else{
						DataSource.instance.tempBanCheck(player, event.getFrom(), "world");
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeathEvent(EntityDeathEvent event){
		if(event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			String uuid = player.getUniqueId().toString();
			List<String> worlds = plugin.getConfig().getStringList("Worlds");
			if(worlds.contains(player.getWorld().getName())){
				if(player.hasPermission("GameOver.use")){
					File file = new File(this.plugin.getDataFolder() + "/players/", uuid + ".yml");
					YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);	
					int lives = playerConfig.getInt("Lives");
					if((playerConfig.getInt("Lives") - 1) <= 0){
						event.getDrops().clear();
						playerConfig.set("Lives", plugin.getConfig().getInt("Lives"));
						try {
							playerConfig.save(file);
						} catch (IOException e) {
							e.printStackTrace();
						}
						DataSource.instance.deletePlayerData(player);					
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
