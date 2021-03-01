package org.trentech.gameover;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
import org.trentech.gameover.sql.SQLMethods;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

	public static Main plugin;

	public Economy economy;

	@Override
	public void onEnable(){
		plugin = this;
		
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
		
    	setupEconomy();	

		SQLMethods.createGroupTable("active");
		SQLMethods.createGroupTable("inactive");
	}
	
	private static void registerEvents(Plugin plugin, Listener... listeners) {
		for (Listener listener : listeners) {
			Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
		}
	}
	
	private void setupEconomy() {
		if(Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("go") || label.equalsIgnoreCase("gameover")){
			if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
				if(sender.hasPermission("gameover.reload")){
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

					sender.sendMessage(ChatColor.DARK_GREEN + "Reloaded!");
				}else{
					sender.sendMessage(ChatColor.DARK_RED + "You do not have permission!");
				}
			}else if(args.length == 2 && args[0].equalsIgnoreCase("reset")){
				File file = new File(getDataFolder() + "/players/" + getServer().getOfflinePlayer(UUID.fromString(args[1])) + ".yml");
				
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
