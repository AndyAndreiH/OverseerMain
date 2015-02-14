package com.katgamestudios.andyandreih.overseer.main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

public class AuthCommandListener implements CommandExecutor {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("overseer")) {
            if(args.length >= 1) {
                if(args[0].equalsIgnoreCase("simulate")) {
                    if(!(sender instanceof ConsoleCommandSender ||
                            (sender instanceof Player && ((Player) sender).getPlayer().isOp())
                    )) {
                        sender.sendMessage("You do not have the permission to access this command!");
                        return true;
                    }
                    displaySimulateHelp(sender);
                    return true;
                }
                if(args[0].equalsIgnoreCase("simulate") && args.length >= 3) {
                    if(!(sender instanceof ConsoleCommandSender ||
                            (sender instanceof Player && ((Player) sender).getPlayer().isOp())
                    )) {
                        sender.sendMessage("You do not have the permission to access this command!");
                        return true;
                    }
                    if(args[1].equalsIgnoreCase("join")) {
                        if(!args[2].isEmpty() && !(args[2] == "")) {
                            simulateJoin(sender, args[2]);
                            return true;
                        }
                        else {
                            sender.sendMessage("SYNTAX: /overseer simulate join <username>");
                            return true;
                        }
                    }
                    else if(args[1].equalsIgnoreCase("register")) {
                        if(args.length == 4) {
                            try {
                                OverseerMain.dbCtrl.registerUser(args[2], args[3]);
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            simulateJoin(sender, args[2]);
                            return true;
                        }
                        else {
                            sender.sendMessage("SYNTAX: /overseer simulate register <username> <password>");
                            return true;
                        }
                    }
                    else if(args[1].equalsIgnoreCase("login")) {
                        if(args.length == 4) {
                            try {
                                simulateLogin(sender, args[2], args[3]);
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                        else {
                            sender.sendMessage("SYNTAX: /overseer simulate login <username> <password>");
                            return true;
                        }
                    }
                    else if(args[1].equalsIgnoreCase("hash")) {
                        if(args.length == 4) {
                            try {
                                sender.sendMessage(simulateHash(args[2], args[3]));
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                        else {
                            sender.sendMessage("SYNTAX: /overseer simulate hash <password> <salt>");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void displaySimulateHelp(CommandSender sender) {
        sender.sendMessage("-------------- Overseer - Simulate Help --------------");
        sender.sendMessage(" /overseer simulate [option] [arguments]");
        sender.sendMessage("   /overseer simulate join <username>");
        sender.sendMessage("[  /overseer simulate quit                           ]");
        sender.sendMessage("   /overseer simulate register <username> <password>");
        sender.sendMessage("   /overseer simulate login <username> <password>");
        sender.sendMessage("   /overseer simulate hash <password> <salt>");
        sender.sendMessage("------------------------------------------------------");
    }

    private void simulateJoin(CommandSender sender, String userName) {
        UUID userUUID = null;
        try {
            userUUID = UUIDFetcher.getUUIDOf(userName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, String> userData = OverseerMain.dbCtrl.getUser(userUUID.toString());
        if(userData.containsKey("id")) {
            sender.sendMessage("Please log in using the /login <password> command!");
        }
        else {
            sender.sendMessage("Please register using the /register <password> command!");
        }
    }

    private void simulateLogin(CommandSender sender, String userName, String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        UUID userUUID = null;
        try {
            userUUID = UUIDFetcher.getUUIDOf(userName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, String> userData = OverseerMain.dbCtrl.getUser(userUUID.toString());
        if(userData.containsKey("id")) {
            String processedPass = password + ":" + userData.get("salt");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] passBytes = processedPass.getBytes("UTF-8");
            byte[] securePassBytes = md.digest(passBytes);
            String securePass = bytesToHex(securePassBytes);
            if(userData.get("password").equalsIgnoreCase(securePass)) {
                sender.sendMessage("Logged in successfully!");
            }
            else {
                sender.sendMessage("The password is incorrect!");
            }
        }
        else {
            sender.sendMessage("Please register using the /register <password> command!");
        }
    }

    private String simulateHash(String password, String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String processedPass = password + ":" + salt;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] passBytes = processedPass.getBytes("UTF-8");
        byte[] securePassBytes = md.digest(passBytes);
        return bytesToHex(securePassBytes);
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
}
