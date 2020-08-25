package org.trentech.gameover.events;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.trentech.gameover.Main;
import org.trentech.gameover.player.PlayerService;

import com.earth2me.essentials.Essentials;

public class PlayerListener implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLoginEvent(PlayerJoinEvent event){
		Player player = event.getPlayer();
		PlayerService playerService = PlayerService.instance();

		playerService.updatePlayer(player, playerService.getWorldGroup(player.getWorld()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuitEvent(PlayerQuitEvent event){
		Player player = event.getPlayer();
		PlayerService playerService = PlayerService.instance();
		
		playerService.savePlayer(player, playerService.getWorldGroup(player.getWorld()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChangeWorldEvent(PlayerChangedWorldEvent event){
		Player player = event.getPlayer();
		PlayerService playerService = PlayerService.instance();
		
		String currentGroup = playerService.getWorldGroup(player.getWorld());
		String previousGroup = playerService.getWorldGroup(event.getFrom());
		if(!currentGroup.equalsIgnoreCase(previousGroup)){
			playerService.savePlayer(player, previousGroup);

			playerService.updatePlayer(player, currentGroup);		
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldSaveEvent(WorldSaveEvent event){
		PlayerService playerService = PlayerService.instance();
		
		Collection<? extends Player> players = Main.getPlugin().getServer().getOnlinePlayers();
		
		for(Object pl : players){
			Player player = (Player) pl;
			playerService.savePlayer(player, playerService.getWorldGroup(player.getWorld()));
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogoutEvent(PlayerQuitEvent event){
		Player player = event.getPlayer();
		PlayerService playerService = PlayerService.instance();
		
		playerService.savePlayer(player, playerService.getWorldGroup(player.getWorld()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityPickupItemEvent(EntityPickupItemEvent event){
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			PlayerService playerService = PlayerService.instance();
			
			playerService.savePlayer(player, playerService.getWorldGroup(player.getWorld()));
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerItemDropEvent(PlayerDropItemEvent event){
		Player player = event.getPlayer();
		PlayerService playerService = PlayerService.instance();
		
		playerService.savePlayer(player, playerService.getWorldGroup(player.getWorld()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerExpChangeEvent(PlayerExpChangeEvent event){
		Player player = event.getPlayer();
		PlayerService playerService = PlayerService.instance();
		
		playerService.savePlayer(player, playerService.getWorldGroup(player.getWorld()));
	}
	 
	@EventHandler(priority = EventPriority.MONITOR)
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent event){	
		if(event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			PlayerService playerService = PlayerService.instance();
			
			playerService.savePlayer(player, playerService.getWorldGroup(player.getWorld()));
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityRegainHealthEvent (EntityRegainHealthEvent event) {
		if(event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			PlayerService playerService = PlayerService.instance();
			
			playerService.savePlayer(player, playerService.getWorldGroup(player.getWorld()));
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamageEvent (EntityDamageEvent event) {
		if(event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			boolean b = true;
			
			if(Main.getPlugin().getServer().getPluginManager().getPlugin("Essentials") != null){
				Essentials essentials = (Essentials) Main.getPlugin().getServer().getPluginManager().getPlugin("Essentials");
				
				if(essentials.getUser(player).isGodModeEnabled()){
					b = false;
				}
			}

			if(b){
				PlayerService playerService = PlayerService.instance();
				playerService.savePlayer(player, playerService.getWorldGroup(player.getWorld()));
			}
		}
	}
	 
}
