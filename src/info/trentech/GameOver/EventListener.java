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
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class EventListener implements Listener{
	
	private GameOver plugin;
	public EventListener(GameOver plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLoginEvent(PlayerLoginEvent event){
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();
		plugin.players.put(player.getUniqueId(), player.getName());
		File uuidFile = new File(plugin.getDataFolder(), "uuid.yml");  
		YamlConfiguration uuidConfig = YamlConfiguration.loadConfiguration(uuidFile);
		List<String> uuids = uuidConfig.getStringList("uuid");
		boolean b = false;
		for(String uuidStr : uuids){
			String[] str = uuidStr.split(";");
			if(str[0].equals(uuid)){
				if(!str[1].equalsIgnoreCase(player.getName())){
					uuids.remove(uuidStr);	
				}else{
					b = true;
				}
				break;
			}
		}
		if(!b){
			uuids.add(uuid + ";" + player.getName());
			uuidConfig.set("uuid", uuids);
			try {
				uuidConfig.save(uuidFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		
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
				event.setKickMessage(message);
				event.setResult(Result.KICK_BANNED);			
			}else{
				String remaining = DataSource.instance.tempBanCheck(player);
				if(remaining != null){
					String message = plugin.getConfig().getString("Temp-Ban.Kick-Message").replace("%T", remaining);
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
