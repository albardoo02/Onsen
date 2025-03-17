package net.azisaba.life.onsen.listener;

import net.azisaba.life.onsen.Onsen;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class OnsenTipMessage implements Listener {

    private final Onsen plugin;

    public OnsenTipMessage(Onsen plugin) {
        this.plugin = plugin;
        startTimer();
    }

    private void startTimer() {
        String HoverMessage = plugin.getConfig().getString("OnsenTip.HoverMessage");
        String TipMessage = plugin.getConfig().getString("OnsenTip.TipMessage");
        plugin.task = new BukkitRunnable() {
            @Override
            public void run() {
                BaseComponent[] hoverMessage = new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "" + HoverMessage)).create();
                HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage);
                BaseComponent[] tipMessage = new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "" + TipMessage)).event(hover).create();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.spigot().sendMessage(tipMessage);
                }
            }
        };
        plugin.task.runTaskTimer(plugin, 0, 6000L);
    }

}
