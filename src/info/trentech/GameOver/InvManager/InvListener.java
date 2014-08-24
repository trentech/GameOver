package info.trentech.GameOver.InvManager;

import java.util.Collection;

import info.trentech.GameOver.GameOver;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;

public class InvListener implements Listener{
	 
	private GameOver plugin;
	public InvListener(GameOver plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLoginEvent(PlayerJoinEvent event){
		Player player = event.getPlayer();
		if(!InvSource.instance.playerExist(player.getUniqueId().toString(), InvSource.instance.getWorldGroup(player.getWorld()))){
			InvSource.instance.createPlayerInventory(player, InvSource.instance.getWorldGroup(player.getWorld()));
		}		
		InvSource.instance.updatePlayer(player, InvSource.instance.getWorldGroup(player.getWorld()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuitEvent(PlayerQuitEvent event){
		Player player = event.getPlayer();
		InvSource.instance.savePlayer(player, InvSource.instance.getWorldGroup(player.getWorld()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChangeWorldEvent(PlayerChangedWorldEvent event){
		Player player = event.getPlayer();
		String currentGroup = InvSource.instance.getWorldGroup(player.getWorld());
		String previousGroup = InvSource.instance.getWorldGroup(event.getFrom());
		if(!currentGroup.equalsIgnoreCase(previousGroup)){
			InvSource.instance.savePlayer(player, previousGroup);
			if(!InvSource.instance.playerExist(player.getUniqueId().toString(), currentGroup)){
				InvSource.instance.createPlayerInventory(player, currentGroup);
			}
			InvSource.instance.updatePlayer(player, currentGroup);		
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldSaveEvent(WorldSaveEvent event){
		Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
		for(Object pl : players){
			Player player = (Player) pl;
			InvSource.instance.savePlayer(player, InvSource.instance.getWorldGroup(player.getWorld()));
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogoutEvent(PlayerQuitEvent event){
		Player player = event.getPlayer();
		InvSource.instance.savePlayer(player, InvSource.instance.getWorldGroup(player.getWorld()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerItemPickupEvent(PlayerPickupItemEvent event){
		Player player = event.getPlayer();
		InvSource.instance.savePlayer(player, InvSource.instance.getWorldGroup(player.getWorld()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerItemDropEvent(PlayerDropItemEvent event){
		Player player = event.getPlayer();
		InvSource.instance.savePlayer(player, InvSource.instance.getWorldGroup(player.getWorld()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerExpChangeEvent(PlayerExpChangeEvent event){
		Player player = event.getPlayer();
		InvSource.instance.savePlayer(player, InvSource.instance.getWorldGroup(player.getWorld()));
	}
	 
	@EventHandler(priority = EventPriority.MONITOR)
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent event){	
		if(event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			InvSource.instance.savePlayer(player, InvSource.instance.getWorldGroup(player.getWorld()));
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityRegainHealthEvent (EntityRegainHealthEvent event) {
		if(event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			InvSource.instance.savePlayer(player, InvSource.instance.getWorldGroup(player.getWorld()));
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamageEvent (EntityDamageEvent event) {
		if(event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			InvSource.instance.savePlayer(player, InvSource.instance.getWorldGroup(player.getWorld()));
		}
	}
	 
}
