package com.katgamestudios.andyandreih.overseer.main;

import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class OverseerMain extends JavaPlugin {
    public static DatabaseController dbCtrl = new DatabaseController();
    public static CommandListener cmdExec = new CommandListener();
    public static EventListener eventListen = new EventListener();

    public static String dataFolder;

    @Override
    public void onEnable() {
        dbCtrl.mainClass = this;
        cmdExec.mainClass = this;
        eventListen.mainClass = this;

        dataFolder = getDataFolder().getAbsolutePath();

        if(!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        getServer().getPluginManager().registerEvents(eventListen, this);

        getCommand("overseer").setExecutor(cmdExec);

        dbCtrl.initDb(dataFolder);
        if(dbCtrl.openDb()) {
            dbCtrl.generateUsersTable();
        }
        getLogger().info("Generated local database.");
    }
}
