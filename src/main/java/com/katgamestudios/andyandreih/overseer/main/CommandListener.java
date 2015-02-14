package com.katgamestudios.andyandreih.overseer.main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandListener implements CommandExecutor {
    public OverseerMain mainClass = null;

    public static HashMap<String, CommandExecutor> subCommands = new HashMap<String, CommandExecutor>();

    public static void registerSubCommand(String subCommand, CommandExecutor executor) {
        if(!subCommands.containsKey(subCommand)) {
            subCommands.put(subCommand, executor);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("overseer")) {
            if(args.length == 0) {
                displayHelp(sender);
                return true;
            }
            else if(args.length == 1) {
                if(args[0].equalsIgnoreCase("help")) {
                    displayHelp(sender);
                    return true;
                }
                else if(subCommands.containsKey(args[0])) {
                    CommandExecutor cmdExec = subCommands.get(args[0]);
                    cmdExec.onCommand(sender, cmd, label, args);
                }
            }
        }
        return false;
    }

    private void displayHelp(CommandSender sender) {
        sender.sendMessage("------------------ Overseer Help -----------------");
        sender.sendMessage(" /overseer - Displays help");
        sender.sendMessage(" /overseer help - Displays help");
        sender.sendMessage(" /overseer simulate - Displays the simulation help");
        sender.sendMessage("--------------------------------------------------");
    }
}
