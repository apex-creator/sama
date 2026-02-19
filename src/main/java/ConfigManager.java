import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.datatransfer.StringSelection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    public static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static final String dbUrl = "jdbc:sqlite:sama_config.db";

    //constructor for creating the db first thing in the code if it doesn't exist
    public ConfigManager() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS settings("
                + "config_key TEXT PRIMARY KEY, "
                + "config_value TEXT NOT NULL"
                + ");";

        String TablePath = "CREATE TABLE IF NOT EXISTS paths("
                + "folder_path TEXT UNIQUE NOT NULL);";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            log.info("Configuration manager initialised successfully");
            stmt.execute(TablePath);
            log.info("Path database initialised successfully");


        } catch (SQLException e) {
            log.info("Failure initiating configuration manager {}", e.getMessage());
        }
    }

    public String load(String key) {

        String Sql = "SELECT config_value FROM settings WHERE config_key = ?";

        String result = null;

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement push = conn.prepareStatement(Sql)) {
            push.setString(1, key);
            ResultSet rs = push.executeQuery();

            if (rs.next()) {
                result = rs.getString("config_value");
            }
        } catch (SQLException e) {
            log.info("Failed to read {}", e.getMessage());
        }

        return result;

    }

    public void addPath(String newPath) {
        String addpath = "INSERT OR IGNORE INTO paths (folder_path) VALUES (?)";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement push = conn.prepareStatement(addpath)) {
            push.setString(1, newPath);
            push.executeUpdate();
            log.info("added the new path for synchronisation");
        } catch (SQLException e) {
            log.info("Error in adding paths, {}", e.getMessage());
        }


    }

    public List<String> LoadPaths() {

        List<String> paths = new ArrayList<>();
        String PathSql = "SELECT folder_path FROM paths";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement push = conn.createStatement();
             ResultSet rs = push.executeQuery(PathSql)) {
            while (rs.next()) {
                paths.add(rs.getString("folder_path"));

            }
        } catch (SQLException exception) {
            log.info("failed loading the paths. {}", exception.getMessage());
        }
        return paths;
    }

    public void save(String key, String Value) {

        String Sql = "INSERT OR REPLACE INTO settings(config_key, config_value) VALUES (?,?)";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement push = conn.prepareStatement(Sql)) {
            push.setString(1, key);
            push.setString(2, Value);
            push.executeUpdate();
            log.info("Success in updating the config.");
        } catch (SQLException e) {
            log.error("failure in Updating the config. ");
        }

    }

}
