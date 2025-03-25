package net.azisaba.life.onsen.listener;

import net.azisaba.life.onsen.command.OnsenManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuit implements Listener {

    private final OnsenManager onsenManager;

    public PlayerQuit(OnsenManager onsenManager) {
        this.onsenManager = onsenManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerID = player.getUniqueId();
        onsenManager.clearSelectedOnsen(playerID);
    }
}
