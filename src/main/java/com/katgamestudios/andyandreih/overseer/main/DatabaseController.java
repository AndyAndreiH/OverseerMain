package com.katgamestudios.andyandreih.overseer.main;

import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.MySQL;
import lib.PatPeter.SQLibrary.SQLite;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class DatabaseController {
    OverseerMain mainClass = null;

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public DatabaseController()
    {
        useRemote = false;
    }

    public DatabaseController(boolean useRemoteDb)
    {
        useRemote = useRemoteDb;
    }

    private String dataPath;
    private boolean useRemote;

    private Database dbRemote;
    private Database dbLocal;

    public void initDb(String dataPath) {
        dbRemote = new MySQL(Logger.getLogger("Minecraft"),
                "auth",
                "localhost", 3306, "minecraft",
                "root", "");
        dbLocal = new SQLite(Logger.getLogger("Minecraft"),
                "auth",
                dataPath,
                "Overseer");
    }

    public boolean openDb() {
        if(useRemote)
            return (dbLocal.open() && dbRemote.open());
        else
            return dbLocal.open();
    }

    public boolean closeDb() {
        if(useRemote)
            return (dbLocal.close() && dbRemote.close());
        else
            return dbLocal.close();
    }

    public void generateUsersTable() {
        if(!dbLocal.isTable("users")) {
            try {
                dbLocal.query("CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "uuid VARCHAR(60) UNIQUE," +
                    "joinDate DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "userName VARCHAR(30) UNIQUE," +
                    "password VARCHAR(100)," +
                    "salt VARCHAR(100)," +
                    "rank VARCHAR(30) DEFAULT 'GUEST');");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void generateAdminLogsTable() {
        if(!dbLocal.isTable("admin_logs")) {
            try {
                dbLocal.query("CREATE TABLE admin_logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "adminName VARCHAR(30)," +
                    "commandTime DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "command VARCHAR(255));");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerUser(String userName, String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        UUID userUUID = null;
        try {
            userUUID = UUIDFetcher.getUUIDOf(userName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(userUUID == null) {
            return;
        }
        SecureRandom saltGen = new SecureRandom();
        int salt = saltGen.nextInt();
        String processedPass = password + ":" + salt;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] passBytes = processedPass.getBytes("UTF-8");
        byte[] securePassBytes = md.digest(passBytes);
        String securePass = bytesToHex(securePassBytes);
        try {
            dbLocal.query("INSERT INTO users (uuid,userName,password,salt) VALUES ('" +
                userUUID.toString() + "','" +
                userName + "','" +
                securePass + "','" +
                salt + "');");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logAdminCommand(String adminName, String command) {
        try {
            dbLocal.query("INSERT INTO admin_logs (adminName,command) VALUES ('" +
                adminName + "','" +
                command + "');");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getUser(String userName) {
        Map<String, String> userData = new HashMap<String, String>();
        UUID userUUID = null;
        try {
            userUUID = UUIDFetcher.getUUIDOf(userName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(userUUID == null) {
            return userData;
        }
        try {
            ResultSet result = dbLocal.query("SELECT * FROM users WHERE uuid=\"" + userUUID.toString() + "\"");
            while(result.next()) {
                userData.put("id", String.valueOf(result.getInt("id")));
                userData.put("uuid", result.getString("uuid"));
                userData.put("joinDate", result.getDate("joinDate").toString());
                userData.put("userName", result.getString("userName"));
                userData.put("password", result.getString("password"));
                userData.put("salt", result.getString("salt"));
                userData.put("rank", result.getString("rank"));
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userData;
    }

    public void setRank(String userName, Rank rank) {
        UUID userUUID = null;
        try {
            userUUID = UUIDFetcher.getUUIDOf(userName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(userUUID == null) {
            return;
        }
        try {
            dbLocal.query("UPDATE users SET rank=\"" + rank.name() + "\" WHERE uuid=\"" + userUUID.toString() + "\";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Rank getRank(String userName) {
        Rank userRank = null;
        UUID userUUID = null;
        try {
            userUUID = UUIDFetcher.getUUIDOf(userName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(userUUID == null) {
            return userRank;
        }
        try {
            ResultSet result = dbLocal.query("SELECT rank FROM users WHERE uuid=\"" + userUUID.toString() + "\"");
            while(result.next()) {
                userRank = Rank.valueOf(result.getString("rank"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userRank;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static enum Rank {
        GUEST, USER, ADMIN, SYSOP
    }
}
