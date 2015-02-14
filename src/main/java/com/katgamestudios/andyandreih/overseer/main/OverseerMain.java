package com.katgamestudios.andyandreih.overseer.main;

import org.bukkit.plugin.java.JavaPlugin;

public class OverseerMain extends JavaPlugin {
    public static DatabaseController dbCtrl = new DatabaseController();
    public static CommandListener cmdExec = new CommandListener();
    public static EventListener eventListen = new EventListener();

    @Override
    public void onEnable() {
        dbCtrl.mainClass = this;
        cmdExec.mainClass = this;
        eventListen.mainClass = this;

        if(!getDataFolder().exists()) {
            getDataFolder().mkdir();
            getLogger().info("Created plugin folder.");
        }

        getServer().getPluginManager().registerEvents(eventListen, this);
        getLogger().info("Event listeners registered.");

        getCommand("overseer").setExecutor(cmdExec);
        getLogger().info("Commands registered.");

        dbCtrl.initDb(getDataFolder().getAbsolutePath());
        if(dbCtrl.openDb()) {
            dbCtrl.generateUsersTable();
            getLogger().info("Generated 'users' table.");
        }
        getLogger().info("Generated local database.");
    }
}
