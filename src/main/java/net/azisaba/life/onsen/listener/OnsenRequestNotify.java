package net.azisaba.life.onsen.listener;

import net.azisaba.life.onsen.Onsen;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

public class OnsenRequestNotify implements Listener {

    private String prefix;
    private final Onsen plugin;
    public OnsenRequestNotify(Onsen plugin) {
        this.plugin = plugin;
        startOnsenRequestNotifier();
        String prefixConfig = plugin.getConfig().getString("Prefix");
        if (prefixConfig == null) {
            this.prefix = "§7[§eOnsen§7]§r ";
        } else {
            this.prefix = ChatColor.translateAlternateColorCodes('&', prefixConfig) + " ";
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int delaySeconds = 5;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.hasPermission("onsen.request.notice")) {
                    int unratedCount = getUnratedOnsenCount();
                    if (unratedCount > 0) {
                        sendMessage(player, "&a温泉リクエストが&d" + unratedCount + "&a件届いています");
                    }
                }
            }
        }.runTaskLater(plugin, delaySeconds * 20L);
    }

    private void startOnsenRequestNotifier() {
        plugin.task = new BukkitRunnable() {
            @Override
            public void run() {
                int unratedCount = getUnratedOnsenCount();
                if (unratedCount > 0) {
                    for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer().hasPermission("onsen.request.notice")) {
                            sendMessage(offlinePlayer.getPlayer(), "&a温泉リクエストが&d" + unratedCount + "&a件あります");
                        }
                    }
                }
            }
        };
        plugin.task.runTaskTimer(plugin, 0, 6000L);
    }

    private int getUnratedOnsenCount() {
        FileConfiguration onsenConfig = plugin.getOnsenConfig();
        ConfigurationSection onsenList = onsenConfig.getConfigurationSection("OnsenList");

        if (onsenList == null) {
            return 0;
        }

        int count = 0;
        Set<String> keys = onsenList.getKeys(false);
        for (String key : keys) {
            String status = onsenList.getString(key + ".Status");
            if ("unrated".equalsIgnoreCase(status)) {
                count++;
            }
        }
        return count;
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', message));
    }
}
