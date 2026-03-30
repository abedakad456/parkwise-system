package dbconnections;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

public class DatabaseConnection {
    
    private static String dbPath = null;
    
    /**
     * Returns a NEW connection each time (better for UCanAccess/Access)
     */
    public static Connection getConnection() {
        try {
            // Initialize dbPath once
            if (dbPath == null) {
                String projectPath = System.getProperty("user.dir");
                String dbName = "parkwisedatabase.accdb";
                dbPath = projectPath + File.separator + dbName;
                
                // Load driver once
                Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
                System.out.println("Successfully connected to: " + dbPath);
            }
            
            // Create a NEW connection each time
            String dbURL = "jdbc:ucanaccess://" + dbPath;
            return DriverManager.getConnection(dbURL);
            
        } catch (ClassNotFoundException e) {
            System.err.println("UCanAccess Driver not found! Check your JARs.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        }
        return null;
    }
}