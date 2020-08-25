package org.trentech.gameover.sql;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.trentech.gameover.player.PlayerData;

public abstract class SQLMethods extends SQLUtils {

	public boolean loaded = false;
    private static Object lock = new Object();
	
	public static void createGroupTable(String group) {
		synchronized (lock) {
			try {
				Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS" + group + "( id INTEGER PRIMARY KEY, Player TEXT, Inventory BLOB, Armor BLOB, Health INTEGER, Experience FLOAT, Level INTEGER, Food INTEGER, Saturation FLOAT)");
				
				statement.executeUpdate();
				
				statement.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}			
	}

	public static void create(Player player, String group) {
		synchronized (lock) {
			ItemStack[] inv = player.getInventory().getContents();
			ItemStack[] armor = player.getInventory().getArmorContents();
			
			try {
				Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement("INSERT into " + group + " (Player, Inventory, Armor, Health, Experience, Level, Food, Saturation) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
				
				statement.setString(1, "`" + player.getUniqueId().toString() + "`");
				statement.setBytes(2, serialize(inv));
				statement.setBytes(3, serialize(armor));
				statement.setDouble(4, player.getHealth());
				statement.setFloat(5, player.getExp());
				statement.setInt(6, player.getExpToLevel());
				statement.setInt(7, player.getFoodLevel());
				statement.setFloat(8, player.getSaturation());
				
				statement.executeUpdate();
				
				statement.close();
				connection.close();
			} catch (SQLException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void update(Player player, String group) {
		synchronized (lock) {
			ItemStack[] inv = player.getInventory().getContents();
			ItemStack[] armor = player.getInventory().getArmorContents();

			try {
				Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement("UPDATE " + group + " SET Inventory = ?, Armor = ?, Health = ?, Experience = ?, Level = ?, Food = ?, Saturation = ? WHERE Player = ?");
	
				statement.setBytes(1, serialize(inv));
				statement.setBytes(2, serialize(armor));
				statement.setDouble(3, player.getHealth());
				statement.setFloat(4, player.getExp());
				statement.setInt(5, player.getExpToLevel());
				statement.setInt(6, player.getFoodLevel());
				statement.setFloat(7, player.getSaturation());
				statement.setString(2, "`" + player.getUniqueId().toString() + "`");
				statement.executeUpdate();
				
				statement.close();
				connection.close();
			} catch (SQLException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Optional<PlayerData> getPlayerData(Player player, String group) {
		Optional<PlayerData> optional = Optional.empty();
		
		try {
			Connection connection = getConnection();
			ResultSet result = connection.createStatement().executeQuery("SELECT * FROM " + group);

			while (result.next()){
				if (result.getString("Player").equalsIgnoreCase("`" + player.getUniqueId().toString() + "`")) {
					optional = Optional.of(new PlayerData(player.getUniqueId(), deserialize(result.getBytes("Inventory")), deserialize(result.getBytes("Armor")), result.getDouble("Health"), result.getFloat("Experience"), result.getInt("Level"), result.getInt("Food"), result.getFloat("Saturation")));
					break;
				}
			}
			
			result.close();
			connection.close();
		} catch (SQLException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		
		return optional;
	}

	public static void deletePlayerData(String uuid, String group){
		try {
			Connection connection = getConnection();
			PreparedStatement statement = connection.prepareStatement("DELETE from " + group + " WHERE Player = ?");
			
			statement.setString(1, "`" + uuid + "`");
			statement.executeUpdate();
			
			statement.close();
			connection.close();
		}catch (SQLException e) {
			e.printStackTrace();
		} 
	}

	private static ItemStack[] deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ItemStack[] contents = null;

		ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
        BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(arrayInputStream);
        
        contents =  (ItemStack[]) objectInputStream.readObject();
        
        arrayInputStream.close();
        objectInputStream.close();

		return contents;
	}
	
	private static byte[] serialize(ItemStack[] contents) throws IOException {
		ByteArrayOutputStream arrayOutputStream= new ByteArrayOutputStream();

    	BukkitObjectOutputStream objectOutputStream = new BukkitObjectOutputStream(arrayOutputStream);
    	objectOutputStream.writeObject(contents);
    	
    	arrayOutputStream.close();
    	objectOutputStream.close();
    	
    	return arrayOutputStream.toByteArray();
	}	
}
