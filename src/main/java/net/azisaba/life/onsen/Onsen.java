package net.azisaba.life.onsen;

import net.azisaba.life.onsen.command.OnsenCommandExecutor;
import net.azisaba.life.onsen.command.OnsenCommandTabCompleter;
import net.azisaba.life.onsen.command.OnsenManager;
import net.azisaba.life.onsen.listener.OnsenMenu;
import net.azisaba.life.onsen.listener.OnsenRequestNotify;
import net.azisaba.life.onsen.listener.OnsenTipMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class Onsen extends JavaPlugin {

    public BukkitRunnable task = null;
    private FileConfiguration onsenConfig;
    private OnsenManager onsenManager = new OnsenManager(this);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadOnsenConfig();

        FileConfiguration config = getConfig();
        String currentVersion = config.getString("configVersion", "0.0");
        String CONFIG_VERSION = "1.0";
        if (!currentVersion.equals(CONFIG_VERSION)) {
            updateConfig();
        } else {
            getLogger().info("Configファイルは最新です");
        }
        File onsenFile = new File(getDataFolder(), "onsen.yml");
        if (!onsenFile.exists()) {
            saveResource("onsen.yml", false);
        }
        onsenConfig = YamlConfiguration.loadConfiguration(onsenFile);
        onsenManager = new OnsenManager(this);

        this.getCommand("onsen").setExecutor(new OnsenCommandExecutor(this, onsenManager));
        this.getCommand("onsen").setTabCompleter(new OnsenCommandTabCompleter(this));
        this.getServer().getPluginManager().registerEvents(new OnsenMenu(this), this);
        this.getServer().getPluginManager().registerEvents(new OnsenRequestNotify(this), this);
        this.getServer().getPluginManager().registerEvents(new OnsenTipMessage(this), this);
        this.getServer().getPluginManager().registerEvents(new OnsenManager(this), this);
    }

    private void updateConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        File oldFolder = new File(getDataFolder(), "old");

        if (!oldFolder.exists()) {
            oldFolder.mkdir();
        }
        String oldFileName = "config-v" + getConfig().getString("configVersion", "old") + ".yml";
        Path oldPath = Paths.get(getDataFolder().getPath(), "old", oldFileName);
        try {
            Files.move(configFile.toPath(), oldPath, StandardCopyOption.REPLACE_EXISTING);
            getLogger().info("Configファイルに新しいバージョンが見つかったため、ファイルを更新しています...");
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();
        getLogger().info("config.ymlを更新しました");
    }

    public FileConfiguration getOnsenConfig() {
        return onsenConfig;
    }

    public void reloadOnsenConfig() {
        loadOnsenConfig();
    }

    public void loadOnsenConfig() {
        File onsenFile = new File(getDataFolder(), "onsen.yml");
        if (!onsenFile.exists()) {
            saveResource("onsen.yml", false);
        }
        onsenConfig = YamlConfiguration.loadConfiguration(onsenFile);
    }

    public void saveOnsenConfig() {
        try {
            onsenConfig.save(new File(getDataFolder(), "onsen.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        saveConfig();
    }
}
