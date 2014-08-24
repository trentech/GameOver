package info.trentech.GameOver;

import info.trentech.GameOver.InvManager.InvListener;
import info.trentech.GameOver.InvManager.InvSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;

import de.diddiz.LogBlock.LogBlock;

public class GameOver extends JavaPlugin {

	public static GameOver plugin;
	private EventListener eventlistener;
	private InvListener invlistener;
	public final Logger log = Logger.getLogger("Minecraft");
	public Economy economy;
	public LogBlock logBlock;
	public Essentials essentials;
	public boolean econSupport = true;
	public boolean lbSupport = true;
	public boolean essSupport = true;
	public HashMap<UUID, String> players = new HashMap<UUID, String>();
    
	@Override
	public void onEnable(){
		
		new DataSource(this);
		new InvSource(this);
		
		this.eventlistener = new EventListener(this);
		getServer().getPluginManager().registerEvents(this.eventlistener, this);
		this.invlistener = new InvListener(this);
		getServer().getPluginManager().registerEvents(this.invlistener, this);
		
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
		
        try{
        	InvSource.instance.connect();
        }catch(Exception e){
        	log.severe(String.format("[%s] Disabled! Unable to connect to database!", new Object[] { getDescription().getName() }));
			getServer().getPluginManager().disablePlugin(this);
			return;
        }
        
		if(!InvSource.instance.tableExist("active")){
			InvSource.instance.createGroupTable("active");
			log.warning(String.format("[%s] Creating table!", new Object[] {getDescription().getName()}));
		}
		
		if(!InvSource.instance.tableExist("inactive")){
			InvSource.instance.createGroupTable("inactive");
			log.warning(String.format("[%s] Creating database!", new Object[] {getDescription().getName()}));
		}
	}
	
	private void setupEconomy() {
		Plugin plugin = getServer().getPluginManager().getPlugin("Vault");
		if(plugin != null){
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
			economy = (Economy) economyProvider.getProvider();
			log.info(String.format("[%s] Vault found! Economy support enabled!", new Object[] { getDescription().getName() }));
		}else{
        	log.warning(String.format("[%s] Vault not found! Economy support disabled!", new Object[] {getDescription().getName()}));
        	econSupport = false;
		}
	}
	
	private void setupLogBlock() {
		Plugin plugin = getServer().getPluginManager().getPlugin("LogBlock");
		if(plugin != null){
			logBlock = (LogBlock) plugin;
        	log.info(String.format("[%s] LogBlock found! Rollback support enabled!", new Object[] {getDescription().getName()}));
		}else{
			log.warning(String.format("[%s] LogBlock not found! Rollback support disabled!", new Object[] {getDescription().getName()}));
			lbSupport =  false;
		}
	}
	
	private void setupEssentials() {
		Plugin plugin = getServer().getPluginManager().getPlugin("Essentials");
		if(plugin != null){
			essentials = (Essentials) plugin;
        	log.info(String.format("[%s] Essentials found!", new Object[] {getDescription().getName()}));
		}else{
			log.warning(String.format("[%s] Essentials not found!", new Object[] {getDescription().getName()}));
			essSupport =  false;
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("go") || label.equalsIgnoreCase("gameover")){
			if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
				if(sender.hasPermission("GameOver.reload")){
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
					InvSource.instance.dispose();
					try {
						InvSource.instance.connect();
					} catch (Exception e) {
						log.severe(String.format("[%s] - Unable to connect to database!", new Object[] { getDescription().getName() }));
					}
					sender.sendMessage(ChatColor.DARK_GREEN + "Reloaded!");
				}else{
					sender.sendMessage(ChatColor.DARK_RED + "You do not have permission!");
				}
			}else if(args.length == 2 && args[0].equalsIgnoreCase("reset")){
				UUID uuid = DataSource.instance.getUUID(args[1]);
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
}
