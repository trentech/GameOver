package info.trentech.GameOver.InvManager.SQL;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class SQLMethods extends SQLUtils{

	public boolean loaded = false;
    private Object lock = new Object();
	
	public boolean tableExist(String group) {
		boolean b = false;
		try {
			Statement statement = getConnection().createStatement();
			DatabaseMetaData md = statement.getConnection().getMetaData();
			ResultSet rs = md.getTables(null, null, group , null);
			if (rs.next()){
				b = true;	
			}		
		} catch (SQLException ex) { }
		return b;
	}
		
	public void createGroupTable(String group) {
		synchronized (lock) {
			try {
				PreparedStatement statement;	
					statement = prepare("CREATE TABLE " + group + "( id INTEGER PRIMARY KEY, Player TEXT, Inventory BLOB, Armor BLOB, Health INTEGER, Experience FLOAT, Level INTEGER, Food INTEGER, Saturation FLOAT)");
					statement.executeUpdate();
			} catch (SQLException e) {
				System.out.println("Unable to connect to Database!");
				System.out.println(e.getMessage());
			}
		}			
	}

	public boolean playerExist(String uuid, String group) {
		boolean b = false;
		try {
			PreparedStatement statement = prepare("SELECT * FROM " + group);
			ResultSet rs = statement.executeQuery();
			while (rs.next()){
				if (rs.getString("Player").equalsIgnoreCase("`" + uuid + "`")) {
					b = true;
					break;
				}
			}
		} catch (SQLException ex) {
			System.out.println("Unable to connect to Database!");
			System.out.println(ex.getMessage());
		}
		return b;
	}
	
	public void createPlayerInv(String uuid, String group) {
		synchronized (lock) {
			try {
				PreparedStatement statement = prepare("INSERT into " + group + " (Player, Inventory, Armor, Health, Experience, Level, Food, Saturation) VALUES (?, null, null, 20, 0, 0, 20, 20)");	
				statement.setString(1, "`" + uuid + "`");
				statement.executeUpdate();
			} catch (SQLException e) {
				System.out.println("Unable to connect to Database!");
				System.out.println(e.getMessage());
			}
		}
	}
	
	public void savePlayerInv(String uuid, String group, byte[] inv, byte[] armor) {
		synchronized (lock) {
			try {
				if(inv != null){
					PreparedStatement statement = prepare("UPDATE " + group + " SET Inventory = ? WHERE Player = ?");				
					statement.setBytes(1, inv);
					statement.setString(2, "`" + uuid + "`");
					statement.executeUpdate();
				}
				if(armor != null){
					PreparedStatement statement = prepare("UPDATE " + group + " SET Armor = ? WHERE Player = ?");
					statement.setBytes(1, armor);
					statement.setString(2, "`" + uuid + "`");
					statement.executeUpdate();
				}
			} catch (SQLException e) {
				System.out.println("Unable to connect to Database!");
				System.out.println(e.getMessage());
			}
		}
	}
	
	public byte[] getPlayerInv(String uuid, String group) {
		byte[] inv = null;
		try {
			PreparedStatement statement = prepare("SELECT * FROM " + group);
			ResultSet rs = statement.executeQuery();
			while (rs.next()){
				if (rs.getString("Player").equalsIgnoreCase("`" + uuid + "`")) {
					inv = rs.getBytes("Inventory");
					break;
				}
			}
		} catch (SQLException ex) {
			System.out.println("Unable to connect to Database!");
			System.out.println(ex.getMessage());
		}
		return inv;
	}
	
	public byte[] getPlayerArm(String uuid, String group) {
		byte[] armor = null;
		try {
			PreparedStatement statement = prepare("SELECT * FROM " + group);
			ResultSet rs = statement.executeQuery();
			while (rs.next()){
				if (rs.getString("Player").equalsIgnoreCase("`" + uuid + "`")) {
					armor = rs.getBytes("Armor");
					break;
				}
			}	
		} catch (SQLException ex) {
			System.out.println("Unable to connect to Database!");
			System.out.println(ex.getMessage());
		}
		return armor;
	}
	
	public float getPlayerExp(String uuid, String group){
		float xp = 0;
		try {
			PreparedStatement statement = prepare("SELECT * FROM " + group);
			ResultSet rs = statement.executeQuery();
			while (rs.next()){
				if (rs.getString("Player").equalsIgnoreCase("`" + uuid + "`")) {
					xp = rs.getFloat("Experience");
					break;
				}
			}	
		} catch (SQLException ex) {
			System.out.println("Unable to connect to Database!");
			System.out.println(ex.getMessage());
		}
		return xp;
	}
	
	public void savePlayerExp(String uuid, String group, float exp){
		synchronized (lock) {
			try{
				PreparedStatement statement = prepare("UPDATE " + group + " SET Experience = ? WHERE Player = ?");
				statement.setFloat(1, exp);
				statement.setString(2, "`" + uuid + "`");
				statement.executeUpdate();
			} catch (SQLException ex) {
				System.out.println("Unable to connect to Database!");
				System.out.println(ex.getMessage());
			}
		}
	}
	
	public int getPlayerExpLevel(String uuid, String group){
		int xp = 0;
		try {
			PreparedStatement statement = prepare("SELECT * FROM " + group);
			ResultSet rs = statement.executeQuery();
			while (rs.next()){
				if (rs.getString("Player").equalsIgnoreCase(uuid)) {
					xp = rs.getInt("Level");
					break;
				}
			}	
		} catch (SQLException ex) {
			System.out.println("Unable to connect to Database!");
			System.out.println(ex.getMessage());
		}
		return xp;
	}
	
	public void savePlayerExpLevel(String uuid, String group, int exp){
		synchronized (lock) {
			try{
				PreparedStatement statement = prepare("UPDATE " + group + " SET Level = ? WHERE Player = ?");
				statement.setInt(1, exp);
				statement.setString(2, "`" + uuid + "`");
				statement.executeUpdate();
			} catch (SQLException ex) {
				System.out.println("Unable to connect to Database!");
				System.out.println(ex.getMessage());
			}
		}
	}
	
	public int getPlayerFoodLev(String uuid, String group){
		int food = 0;
		try {
			PreparedStatement statement = prepare("SELECT * FROM " + group);
			ResultSet rs = statement.executeQuery();
			while (rs.next()){
				if (rs.getString("Player").equalsIgnoreCase("`" + uuid + "`")) {
					food = rs.getInt("Food");
					break;
				}
			}	
		} catch (SQLException ex) {
			System.out.println("Unable to connect to Database!");
			System.out.println(ex.getMessage());
		}
		return food;
	}
	
	public void savePlayerFoodLev(String uuid, String group, int food){
		synchronized (lock) {
			try{
				PreparedStatement statement = prepare("UPDATE " + group + " SET Food = ? WHERE Player = ?");
				statement.setInt(1, food);
				statement.setString(2, "`" + uuid + "`");
				statement.executeUpdate();
			} catch (SQLException ex) {
				System.out.println("Unable to connect to Database!");
				System.out.println(ex.getMessage());
			}
		}
	}
	
	public float getPlayerSat(String uuid, String group){
		float sat = 0;
		try {
			PreparedStatement statement = prepare("SELECT * FROM " + group);
			ResultSet rs = statement.executeQuery();
			while (rs.next()){
				if (rs.getString("Player").equalsIgnoreCase("`" + uuid + "`")) {
					sat = rs.getFloat("Saturation");
					break;
				}
			}	
		} catch (SQLException ex) {
			System.out.println("Unable to connect to Database!");
			System.out.println(ex.getMessage());
		}
		return sat;
	}
	
	public void savePlayerSat(String uuid, String group, float sat){
		synchronized (lock) {
			try{
				PreparedStatement statement = prepare("UPDATE " + group + " SET Saturation = ? WHERE Player = ?");
				statement.setFloat(1, sat);
				statement.setString(2, "`" + uuid + "`");
				statement.executeUpdate();
			} catch (SQLException ex) {
				System.out.println("Unable to connect to Database!");
				System.out.println(ex.getMessage());
			}
		}
	}
	
	public double getPlayerHlth(String uuid, String group){
		double health = 0;
		try {
			PreparedStatement statement = prepare("SELECT * FROM " + group);
			ResultSet rs = statement.executeQuery();
			while (rs.next()){
				if (rs.getString("Player").equalsIgnoreCase("`" + uuid + "`")) {
					health = rs.getDouble("Health");
					break;
				}
			}	
		} catch (SQLException ex) {
			System.out.println("Unable to connect to Database!");
			System.out.println(ex.getMessage());
		}
		return health;
	}
	
	public void savePlayerHlth(String uuid, String group, double health){
		synchronized (lock) {
			try{
				PreparedStatement statement = prepare("UPDATE " + group + " SET Health = ? WHERE Player = ?");
				statement.setDouble(1, health);
				statement.setString(2, "`" + uuid + "`");
				statement.executeUpdate();
			} catch (SQLException ex) {
				System.out.println("Unable to connect to Database!");
				System.out.println(ex.getMessage());
			}
		}
	}
	
	public void deletePlayerData(String uuid, String group){
		try {
			PreparedStatement statement = prepare("DELETE from " + group + " WHERE Player = ?");
			statement.setString(1, "`" + uuid + "`");
			statement.executeUpdate();
		}catch (SQLException e) {
			System.out.println("Unable to connect to Database!");
			System.out.println(e.getMessage());
		} 
	}

}
