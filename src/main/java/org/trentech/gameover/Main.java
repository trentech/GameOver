package org.trentech.gameover;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.trentech.gameover.events.EventListener;
import org.trentech.gameover.events.PlayerListener;
import org.trentech.gameover.inventory.PlayerService;

import com.earth2me.essentials.Essentials;

import de.diddiz.LogBlock.LogBlock;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

	public static Main plugin;

	public Economy economy;
	public LogBlock logBlock;
	public Essentials essentials;
	public boolean econSupport = true;
	public boolean lbSupport = true;
	public boolean essSupport = true;
	public HashMap<UUID, String> players = new HashMap<UUID, String>();
    
	@Override
	public void onEnable(){
		registerEvents(this, new EventListener(), new PlayerListener());

		File configFile = new File(getDataFolder(), "config.yml");   
		if(!configFile.exists()){
		    configFile.getParentFile().mkdirs();
		    copy(getResource("config.yml"), configFile);
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		if(config.getStringList("Worlds").size() == 0){
			List<String> worlds = new ArrayList<String>();
			worlds.add("world");
			worlds.add("world_nether");
			worlds.add("world_the_end");
			config.set("Worlds", worlds);
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(config.getString("Delete-Essentials-UserData") != null){
			config.set("Delete-Essentials-UserData", null);
			config.set("Delete-Essentials-Homes", true);
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		File uuidFile = new File(getDataFolder(), "uuid.yml");   
		if(!uuidFile.exists()){
		    try {
				uuidFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		YamlConfiguration uuidConfig = YamlConfiguration.loadConfiguration(uuidFile);
		List<String> uuids = uuidConfig.getStringList("uuid");
		for(String uuid : uuids){
			String[] str = uuid.split(";");
			players.put(UUID.fromString(str[0]), str[1]);
		}
		
    	setupEconomy();	
		setupLogBlock();
		setupEssentials();
		
		PlayerService playerService = PlayerService.instance();
        try{
        	playerService.connect();
        }catch(Exception e){
        	getLogger().severe(String.format("[%s] Disabled! Unable to connect to database!", new Object[] { getDescription().getName() }));
			getServer().getPluginManager().disablePlugin(this);
			return;
        }
        
		if(!playerService.tableExist("active")){
			playerService.createGroupTable("active");
			getLogger().warning(String.format("[%s] Creating table!", new Object[] {getDescription().getName()}));
		}
		
		if(!playerService.tableExist("inactive")){
			playerService.createGroupTable("inactive");
			getLogger().warning(String.format("[%s] Creating database!", new Object[] {getDescription().getName()}));
		}
	}
	
	private static void registerEvents(Plugin plugin, Listener... listeners) {
		for (Listener listener : listeners) {
			Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
		}
	}
	
	private void setupEconomy() {
		Plugin plugin = getServer().getPluginManager().getPlugin("Vault");
		if(plugin != null){
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
			economy = (Economy) economyProvider.getProvider();
			getLogger().info(String.format("[%s] Vault found! Economy support enabled!", new Object[] { getDescription().getName() }));
		}else{
			getLogger().warning(String.format("[%s] Vault not found! Economy support disabled!", new Object[] {getDescription().getName()}));
        	econSupport = false;
		}
	}
	
	private void setupLogBlock() {
		Plugin plugin = getServer().getPluginManager().getPlugin("LogBlock");
		if(plugin != null){
			logBlock = (LogBlock) plugin;
			getLogger().info(String.format("[%s] LogBlock found! Rollback support enabled!", new Object[] {getDescription().getName()}));
		}else{
			getLogger().warning(String.format("[%s] LogBlock not found! Rollback support disabled!", new Object[] {getDescription().getName()}));
			lbSupport =  false;
		}
	}
	
	private void setupEssentials() {
		Plugin plugin = getServer().getPluginManager().getPlugin("Essentials");
		if(plugin != null){
			essentials = (Essentials) plugin;
			getLogger().info(String.format("[%s] Essentials found!", new Object[] {getDescription().getName()}));
		}else{
			getLogger().warning(String.format("[%s] Essentials not found!", new Object[] {getDescription().getName()}));
			essSupport =  false;
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		
		if (label.equalsIgnoreCase("go") || label.equalsIgnoreCase("gameover")){
			if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
				if(sender.hasPermission("GameOver.reload")){
					PlayerService playerService = PlayerService.instance();
					reloadConfig();
	                File folder = new File(getDataFolder() + "/players/");
	                if(folder.exists()){
	                	File[] files = folder.listFiles();
					    for(File file : files){
					    	YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
							try {
								playerConfig.save(file);
							} catch (IOException e) {
								e.printStackTrace();
							}
					    }
	                }
	                playerService.dispose();
					try {
						playerService.connect();
					} catch (Exception e) {
						getLogger().severe(String.format("[%s] - Unable to connect to database!", new Object[] { getDescription().getName() }));
					}
					sender.sendMessage(ChatColor.DARK_GREEN + "Reloaded!");
				}else{
					sender.sendMessage(ChatColor.DARK_RED + "You do not have permission!");
				}
			}else if(args.length == 2 && args[0].equalsIgnoreCase("reset")){
				UUID uuid = DataSource.get().getUUID(args[1]);
				File file = new File(getDataFolder() + "/players/" + uuid + ".yml");
				if(file.exists()){
					file.delete();
					sender.sendMessage(ChatColor.DARK_GREEN + args[1] + " reset!");
				}else{
					sender.sendMessage(ChatColor.DARK_RED + args[1] + " does not exist!");
				}
			}else{
				sender.sendMessage(ChatColor.YELLOW + "/go reset [player]");
				sender.sendMessage(ChatColor.YELLOW + "/go reload");
			}
			return true;
		}
		return false;
	}
	
	private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public static Main getPlugin() {
		return plugin;
	}
}
