package me.prostedeni.goodcraft.dreamdeathswap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;


public final class DreamDeathswap extends JavaPlugin implements Listener {

public static volatile boolean running;

public static Player player1;
public static Player player2;

public static int Time;

    @EventHandler
    public void onNether(PlayerPortalEvent p){
        boolean Nether = getConfig().getBoolean("Nether");
        if (!Nether){
            p.getPlayer().sendMessage(ChatColor.DARK_RED + "Nether has been disabled in the config");
            p.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent d){
        String LastDead = d.getEntity().getName();
        if (LastDead.equals(String.valueOf(player1)) || LastDead.equals(String.valueOf(player2))){
            if (LastDead.equals(String.valueOf(player1))) {
                player1.sendMessage(ChatColor.translateAlternateColorCodes('&',"&2&lPlayer &6&l" + player2 + " &2&lhas won"));
                player2.sendMessage(ChatColor.translateAlternateColorCodes('&',"&2&lPlayer &6&l" + player2 + " &2&lhas won"));
            }
            if (LastDead.equals(String.valueOf(player2))){
                player1.sendMessage(ChatColor.translateAlternateColorCodes('&',"&2&lPlayer &6&l" + player1 + " &2&lhas won"));
                player2.sendMessage(ChatColor.translateAlternateColorCodes('&',"&2&lPlayer &6&l" + player1 + " &2&lhas won"));
            }
            running = false;
        }
    }

    @EventHandler
    public void onEvent(PlayerCommandSendEvent e){
        if (e.getCommands().contains("deathswap")){
            int Delay = getConfig().getInt("Delay");
            int WarnTime = getConfig().getInt("WarnTime");
            Time = 0;
            if (running) {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                    public void run() {
                        Time = Time + 1;
                        if ((Delay - Time) < WarnTime){
                            player1.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cSwapping in " + (Delay - Time) + "&c seconds"));
                            player2.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cSwapping in " + (Delay - Time) + "&c seconds"));
                        }

                        if (Delay - Time == 0){
                            Time = 0;

                            Location loc1 = player1.getLocation();
                            Location loc2 = player2.getLocation();

                            int r = (int) (Math.random() * (100 - 0)) + 0;
                            int SwapChance = getConfig().getInt("SwapChance");

                            if (getConfig().getBoolean("Debug")){
                                System.out.print("loc1: " + loc1 + " loc2: " + loc2 + " r: " + r + " SwapChance: " + SwapChance);
                            }

                            if (r > SwapChance) {
                                player1.teleport(loc2);
                                player2.teleport(loc1);

                                player1.sendMessage(ChatColor.GREEN + "You have been swapped");
                                player2.sendMessage(ChatColor.GREEN + "You have been swapped");
                            }
                            if (r <= SwapChance){
                                player1.sendMessage(ChatColor.GREEN + "Swapping did not occur");
                                player2.sendMessage(ChatColor.GREEN + "Swapping did not occur");
                            }
                        }
                    }
                }, 0, 20);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("deathswap")) {
            if (((getConfig().getInt("Delay")) > 0) || ((getConfig().getInt("WarnTime")) > 0) || ((getConfig().getInt("SwapChance")) > 0 || (getConfig().getInt("SwapChance")) <= 100)){
                if (sender instanceof Player) {
                    if (sender.hasPermission("deathswap.use")) {
                        if (args.length == 1) {

                            if (args[0].equalsIgnoreCase("stop")) {
                                running = false;
                            } else if (args[0].equalsIgnoreCase("reload")) {
                                reloadConfig();
                                getConfig();
                                saveConfig();
                                sender.sendMessage(ChatColor.DARK_AQUA + "Config reloaded");
                                System.out.println("config reloaded");
                            } else {
                                String name = args[0];
                                Player target = Bukkit.getPlayer(name);
                                if (target == null) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Player " + name + " &4is offline"));
                                } else {
                                    player1 = Bukkit.getPlayer(String.valueOf(sender));
                                    player2 = Bukkit.getPlayer(name);

                                    player1.sendMessage(ChatColor.DARK_AQUA + "You have been put into Deathswap. Enjoy :)");
                                    player2.sendMessage(ChatColor.DARK_AQUA + "You have been put into Deathswap. Enjoy :)");

                                    running = true;
                                    if (getConfig().getBoolean("Debug")){
                                        System.out.println("running: " + running);
                                    }
                                }
                            }

                        } else if (args.length == 2) {
                            sender.sendMessage(ChatColor.DARK_RED + "Invalid arguments");
                        } else if (args.length == 0) {
                            sender.sendMessage(ChatColor.DARK_RED + "Invalid arguments");
                        }

                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command");
                    }
                } else {
                    System.out.println("Only player can execute this command");
                }
        } else {
                sender.sendMessage(ChatColor.DARK_RED + "Config values must be above 0 and SwapChance below 100");
            }
        }
        return false;
    }

    @Override
    public void onEnable() {
        running = false;
        Bukkit.getPluginManager().registerEvents(this,this);

        getConfig().options().copyDefaults();
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        reloadConfig();
        saveConfig();
    }
}
