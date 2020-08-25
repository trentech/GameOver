package org.trentech.gameover.sql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.trentech.gameover.Main;

public abstract class SQLUtils {

    public static Connection getConnection() {      
		try {
			Driver driver = (Driver) Class.forName("org.h2.Driver").newInstance();
	        Properties properties = new Properties();      
	        Connection connection = driver.connect("jdbc:h2:" + Main.getPlugin().getDataFolder().getAbsolutePath() + "\\data.db;DB_CLOSE_DELAY=-1", properties);

	        if (connection == null) {
	            throw new NullPointerException("Connecting to database failed!");
	        }
	        
	        return connection;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
    }
}