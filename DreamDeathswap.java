package me.prostedeni.goodcraft.dreamdeathswap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


public final class DreamDeathswap extends JavaPlugin implements Listener {

public static volatile boolean running;

public static Player player1;
public static Player player2;

public static int Time;

public static int rando;

    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    @EventHandler
    public void onNether(PlayerPortalEvent p){
        if (p.getPlayer().equals(player1) || p.getPlayer().equals(player2)) {
            if (running) {
                boolean Nether = getConfig().getBoolean("Nether");
                if (!Nether) {
                    p.getPlayer().sendMessage(ChatColor.DARK_RED + "Nether has been disabled in the config");
                    p.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent d){
        if (d.getEntity() instanceof Player){
            Player died = d.getEntity();
            if (died.equals(player1)){
                player1.sendMessage(ChatColor.DARK_RED + "You have lost the deathswap!");
                player2.sendMessage(ChatColor.DARK_GREEN + "You have won the deathswap");
                running = false;
            } else if (died.equals(player2)){
                player2.sendMessage(ChatColor.DARK_RED + "You have lost the deathswap!");
                player1.sendMessage(ChatColor.DARK_GREEN + "You have won the deathswap");
                running = false;
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("deathswap")) {
            if (((getConfig().getInt("Delay")) > 0) || ((getConfig().getInt("WarnTime")) > 0) || ((getConfig().getInt("SwapChance")) > 0 || (getConfig().getInt("SwapChance")) <= 100)) {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.DARK_RED + "Invalid arguments");
                    return false;
                } else {
                    if (args[0].equalsIgnoreCase("reload")) {
                        reloadConfig();
                        getConfig();
                        saveConfig();
                        sender.sendMessage(ChatColor.DARK_AQUA + "Config reloaded");
                    } else if (sender instanceof Player) {
                        if (sender.hasPermission("deathswap.use")) {
                            if (args.length == 1) {

                                if (args[0].equalsIgnoreCase("stop")) {
                                    if (running) {
                                        running = false;
                                        player1.sendMessage(ChatColor.DARK_RED + "Deathswap has been stopped");
                                        player2.sendMessage(ChatColor.DARK_RED + "Deathswap has been stopped");
                                    } else {
                                        sender.sendMessage(ChatColor.DARK_RED + "Deathswap is not running");
                                    }
                                } else if (args[0].equalsIgnoreCase("start")) {
                                        int Delay = getConfig().getInt("Delay");
                                        int WarnTime = getConfig().getInt("WarnTime");
                                        Time = 0;
                                        if (running) {

                                            player1.sendMessage(ChatColor.DARK_AQUA + "Deathswap timer has started");
                                            player2.sendMessage(ChatColor.DARK_AQUA + "Deathswap timer has started");

                                            AtomicInteger processId = new AtomicInteger();
                                            //I have NO IDEA what this does, but it works, and i am happy.
                                            //pulled this from https://stackoverflow.com/questions/52081215/cancel-a-runnable-java

                                            int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

                                                public void run() {
                                                    Time++;

                                                    if (!running) {
                                                        Bukkit.getScheduler().cancelTask(processId.get());
                                                    }
                                                    //running is set to off with /deathswap stop
                                                    //refer to previous comment where did i get this

                                                    if (getConfig().getBoolean("Debug")) {
                                                        System.out.println("Time: " + Time + " Running " + running);
                                                    }
                                                    if ((Delay - Time) <= WarnTime) {
                                                        player1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cSwapping in " + (Delay - Time) + "&c seconds"));
                                                        player2.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cSwapping in " + (Delay - Time) + "&c seconds"));
                                                        if (Delay - Time < 1) {
                                                            player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 4F, 4F);
                                                            player2.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 4F, 4F);
                                                        } else {
                                                            player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2F, 1F);
                                                            player2.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2F, 1F);
                                                        }
                                                    }
                                                    if (Delay - Time <= 0) {

                                                        Location loc1 = player1.getLocation();
                                                        Location loc2 = player2.getLocation();

                                                        rando = getRandomNumberInRange(0, 100);
                                                        //calls to method of generating pseudorandom numbers above

                                                        int SwapChance = getConfig().getInt("SwapChance");

                                                        if (getConfig().getBoolean("AlternativeMode")) {
                                                            //if AlternativeMode == true, it calculates with chances

                                                            if (rando <= SwapChance) {
                                                                player1.teleport(loc2);
                                                                player2.teleport(loc1);

                                                                player1.sendMessage(ChatColor.GREEN + "You have been swapped");
                                                                player2.sendMessage(ChatColor.GREEN + "You have been swapped");
                                                                if (getConfig().getBoolean("Debug")) {
                                                                    System.out.print("loc1: " + loc1 + " loc2: " + loc2 + " rando: " + rando + " SwapChance: " + SwapChance);
                                                                }
                                                            } else {
                                                                player1.sendMessage(ChatColor.GREEN + "Swapping did not occur");
                                                                player2.sendMessage(ChatColor.GREEN + "Swapping did not occur");
                                                                if (getConfig().getBoolean("Debug")) {
                                                                    System.out.print("loc1: " + loc1 + " loc2: " + loc2 + " rando: " + rando + " SwapChance: " + SwapChance);
                                                                }
                                                            }
                                                        } else {
                                                            player1.teleport(loc2);
                                                            player2.teleport(loc1);

                                                            player1.sendMessage(ChatColor.GREEN + "You have been swapped");
                                                            player2.sendMessage(ChatColor.GREEN + "You have been swapped");
                                                            if (getConfig().getBoolean("Debug")) {
                                                                System.out.print("loc1: " + loc1 + " loc2: " + loc2);
                                                            }
                                                        }


                                                        Time = 0;
                                                    }
                                                }
                                            }, 0, 20);

                                            processId.set(taskId);
                                            //again, i have no idea what this does :D but i probably should
                                        } else {
                                            sender.sendMessage(ChatColor.DARK_RED + "Players haven't been set or previous game ended");
                                        }
                                } else {
                                    if (Bukkit.getPlayer(args[0]) == null) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Player " + args[0] + " &4is offline"));
                                    } else if (Bukkit.getPlayer(args[0]) != null) {
                                        if (Bukkit.getPlayer(args[0]) != sender) {
                                            player1 = Bukkit.getPlayer(sender.getName());
                                            player2 = Bukkit.getPlayer(String.valueOf(args[0]));

                                            player1.sendMessage(ChatColor.DARK_AQUA + "Players set. Enjoy :)");
                                            player2.sendMessage(ChatColor.DARK_AQUA + "Players set. Enjoy :)");

                                            running = true;
                                            if (getConfig().getBoolean("Debug")) {
                                                System.out.println("running: " + running);
                                            }
                                        } else {
                                            sender.sendMessage(ChatColor.DARK_RED + "You can't swap with yourself");
                                        }
                                    }
                                }

                            } else if (args.length == 2) {
                                sender.sendMessage(ChatColor.DARK_RED + "Invalid arguments");
                            }

                        } else {
                            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command");
                        }
                    } else {
                        System.out.println("Only player can execute this command");
                    }
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
