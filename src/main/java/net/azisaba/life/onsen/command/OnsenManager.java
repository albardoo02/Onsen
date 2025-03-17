package net.azisaba.life.onsen.command;

import net.azisaba.life.onsen.Onsen;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OnsenManager {

    private final Onsen plugin;
    public OnsenManager(Onsen plugin) {
        this.plugin = plugin;
    }

    private final Map<UUID, String> selectedOnsen = new HashMap<>();

    public boolean selectOnsen(UUID playerId, String onsenName) {
        if (selectedOnsen.containsKey(playerId) && selectedOnsen.get(playerId).equalsIgnoreCase(onsenName)) {
            selectedOnsen.remove(playerId);
            return false;
        } else {
            selectedOnsen.put(playerId, onsenName);
            return true;
        }
    }

    public String getSelectedOnsen(UUID playerId) {
        return selectedOnsen.get(playerId);
    }

    public void clearSelectedOnsen(UUID playerId) {
        selectedOnsen.remove(playerId);
    }

    public boolean isSelected(UUID playerId) {
        return selectedOnsen.containsKey(playerId);
    }
}
