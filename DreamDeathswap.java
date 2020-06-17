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
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;

import static org.bukkit.ChatColor.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public final class DreamDeathswap extends JavaPlugin implements Listener {

HashMap<Player, Player> playerMap = new HashMap<>();

public static int Delay;
public static int WarnTime;
public static int SwapChance;
public static boolean AlternativeMode;
public static boolean Nether;
public static boolean Debug;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this,this);

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        fetchConfig();

    }

    @Override
    public void onDisable() {
        reloadConfig();
        saveConfig();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent q){

        Player left = q.getPlayer();

        if (playerMap.containsValue(left)){
            Player key = getKey(left);
            playerMap.remove(key);
        }

        if (playerMap.containsKey(left)){
            playerMap.remove(left);
        }
    }

    @EventHandler
    public void onNether(PlayerPortalEvent p){
        if (playerMap.containsKey(p.getPlayer()) || playerMap.containsValue(p.getPlayer())) {

            if (!Nether) {
                p.getPlayer().sendMessage(DARK_RED + "Nether has been disabled in the config");
                p.setCancelled(true);
            }
        }
    }
    //this forbids players from entering Nether if it's disallowed

    @EventHandler
    public void onDeath(PlayerDeathEvent d){
            Player died = d.getEntity();
            if (playerMap.containsKey(died)){
                playerMap.get(died).sendMessage(DARK_GREEN + "You have won the deathswap!");
                died.sendMessage(DARK_RED + "You have lost the deathswap!");

                playerMap.remove(died);
            }

            if (playerMap.containsValue(died)){
                Player key = getKey(died);

                key.sendMessage(DARK_GREEN + "You have won the deathswap!");
                died.sendMessage(DARK_RED + "You have lost the deathswap!");

                playerMap.remove(key);
            }
    }
    //declares winner and ends deathswap

    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
    //just a method i found on the internet for getting random numbers

    public Player getKey(Player player){
        try {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (playerMap.get(p).equals(player)) {
                    return p;
                }
            }
        } catch (NullPointerException e){
            //just don't display it
        }
        return player;
    }
    /*
    this method gets key from value of HashMap, however, this is not very optimized
    because it loops through every online player and asks if he is the key
    optimally there would be two HashMaps, Key to Value and Value to Key
    but that also uses up twice the RAM

    additionally, this will break if there are more keys holding the same value
    */

    public void fetchConfig(){
        Delay = getConfig().getInt("Delay");
        WarnTime = getConfig().getInt("WarnTime");
        SwapChance = getConfig().getInt("SwapChance");

        AlternativeMode = getConfig().getBoolean("AlternativeMode");
        Nether = getConfig().getBoolean("Nether");
        Debug = getConfig().getBoolean("Debug");
    }
    //this just gets config on startup or on reload, because accessing static variables
    //is less resource taxing than getting the value from physical file every time

    public void startRunnable(Player p1, Player p2){

        playerMap.put(p1, p2);

        new BukkitRunnable(){

            Player player1 = p1;
            Player player2 = p2;

            int Time = 0;

            @Override
            public void run(){

                Time++;

                if ( !(playerMap.containsKey(player1)) || !(playerMap.containsValue(player2)) ){
                    cancel();
                }
                //cancels the runnable if one specified players isn't in HashMap

                if (Debug) {
                    System.out.println("Time: " + Time);
                }

                if ((Delay - Time) <= WarnTime) {
                    player1.sendMessage(translateAlternateColorCodes('&', "&cSwapping in " + (Delay - Time) + "&c seconds"));
                    player2.sendMessage(translateAlternateColorCodes('&', "&cSwapping in " + (Delay - Time) + "&c seconds"));
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
                    //saves locations before swapping

                    int rando = getRandomNumberInRange(0, 100);
                    //calls to method of generating pseudorandom numbers above

                    if (AlternativeMode) {
                        //if AlternativeMode == true, it calculates with chances

                        if (rando <= SwapChance) {

                            teleport(player1, player2, loc1, loc2);

                            player1.sendMessage(GREEN + "You have been swapped");
                            player2.sendMessage(GREEN + "You have been swapped");
                            if (Debug) {
                                System.out.print("loc1: " + loc1 + " loc2: " + loc2 + " rando: " + rando + " SwapChance: " + SwapChance);
                            }
                        } else {
                            player1.sendMessage(GREEN + "Swapping did not occur");
                            player2.sendMessage(GREEN + "Swapping did not occur");
                            if (Debug) {
                                System.out.print("loc1: " + loc1 + " loc2: " + loc2 + " rando: " + rando + " SwapChance: " + SwapChance);
                            }
                        }
                    } else {

                        teleport(player1, player2, loc1, loc2);

                        player1.sendMessage(GREEN + "You have been swapped");
                        player2.sendMessage(GREEN + "You have been swapped");
                        if (Debug) {
                            System.out.print("loc1: " + loc1 + " loc2: " + loc2);
                        }
                    }


                    Time = 0;
                }

            }
        }.runTaskTimerAsynchronously(this, 0, 20);
    }
    //This whole runnable runs asynchronously, minimizing workload of main thread
    //but teleporting can't be done asynchronously, so i used next method to achieve both

    public void teleport(Player player1, Player player2, Location loc1, Location loc2){
        new BukkitRunnable(){
            @Override
            public void run(){
                player1.teleport(loc2);
                player2.teleport(loc1);
            }
        }.runTaskLater(this, 1);
    }
    //piece of code i couldn't put into previous method,
    //and had to make sure it runs synchronously

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("deathswap")) {
            if ((Delay > 0) && (WarnTime > 0) && (SwapChance > 0) && (SwapChance <= 100)) {
                if (args.length == 0) {
                    sender.sendMessage(DARK_RED + "Invalid arguments");
                    return false;
                } else if (args.length == 1){

                    switch (args[0]) {
                        //using switch statements instead of ifs and else ifs is faster

                        case "reload" : {
                            if (sender instanceof Player) {
                                if (sender.hasPermission("deathswap.use")) {
                                    reloadConfig();
                                    getConfig();
                                    saveConfig();
                                    fetchConfig();
                                    sender.sendMessage(DARK_AQUA + "Config reloaded");
                                    break;
                                } else {
                                    sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command");
                                    break;
                                }
                            } else {
                                reloadConfig();
                                getConfig();
                                saveConfig();
                                fetchConfig();
                                sender.sendMessage(DARK_AQUA + "Config reloaded");
                                break;
                            }
                        }

                        case "stop" : {
                            if (sender instanceof Player){
                                if (sender.hasPermission("deathswap.use")) {
                                    if (playerMap.containsValue(sender)){
                                        getKey(((Player) sender).getPlayer()).sendMessage(DARK_PURPLE + "Deathswap has been stopped");
                                        sender.sendMessage(DARK_PURPLE + "Deathswap has been stopped");
                                        playerMap.remove(getKey(((Player) sender).getPlayer()));
                                        break;
                                    } else if (playerMap.containsKey(sender)){
                                        playerMap.get(sender).sendMessage(DARK_PURPLE + "Deathswap has been stopped");
                                        sender.sendMessage(DARK_PURPLE + "Deathswap has been stopped");
                                        playerMap.remove(sender);
                                        break;
                                    } else {
                                        sender.sendMessage(DARK_RED + "You are not in deathswap");
                                        break;
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command");
                                    break;
                                }
                            } else {
                                sender.sendMessage(DARK_RED + "Only players can send this command");
                                break;
                            }
                        }

                        default: {
                            if (sender instanceof Player) {
                                if (sender.hasPermission("deathswap.use")) {
                                    if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[0]))) {
                                        if (!(Bukkit.getPlayer(args[0]).equals(sender))) {
                                            if (!(playerMap.containsKey(sender)) && !(playerMap.containsValue(sender))) {
                                                if (!(playerMap.containsKey(Bukkit.getPlayer(args[0]))) && !(playerMap.containsValue(Bukkit.getPlayer(args[0])))) {
                                                    startRunnable(((Player) sender), Bukkit.getPlayer(args[0]));

                                                    sender.sendMessage(DARK_GREEN + "You have been put in deathswap");
                                                    Bukkit.getPlayer(args[0]).sendMessage(DARK_GREEN + "You have been put in deathswap");

                                                    break;
                                                } else {
                                                    sender.sendMessage(DARK_RED + "Specified player already is in deathswap");
                                                    break;
                                                }
                                            } else {
                                                sender.sendMessage(DARK_RED + "You already are in deathswap");
                                                break;
                                            }
                                        } else {
                                            sender.sendMessage(DARK_RED + "You can't swap with yourself");
                                            break;
                                        }

                                    } else {
                                        sender.sendMessage(DARK_RED + "Player " + args[0] + " isn't online");
                                        break;
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to execute this command");
                                    break;
                                }
                            } else {
                                sender.sendMessage(DARK_RED + "Only players can send this command");
                                break;
                            }
                        }

                    }
                } else {
                    sender.sendMessage(DARK_RED + "Too many arguments");
                }
            } else {
                sender.sendMessage(DARK_RED + "Config values must be above 0 and SwapChance below or equal to 100");
            }
        }
        return false;
    }
    //this is just the command, nothing special here

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (command.getName().equalsIgnoreCase("deathswap")) {
            if (args.length == 1) {
                if (sender.hasPermission("deathswap.use")) {
                    final ArrayList<String> l = new ArrayList<>();

                    final ArrayList<String> commands = new ArrayList<>();

                    commands.add("reload");
                    commands.add("stop");

                    for (Player p : Bukkit.getOnlinePlayers()){
                        if (!(playerMap.containsKey(p)) && !(playerMap.containsValue(p))) {
                            if (!(p.equals(sender))) {
                                commands.add(p.getName());
                            }
                        }
                    }

                    StringUtil.copyPartialMatches(args[0], commands, l);
                    return l;
                }
            }
        }
        return null;
    }
    //TabCompleter, nothing special

}
