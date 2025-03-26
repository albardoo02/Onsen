package net.azisaba.life.onsen.command;

import net.azisaba.life.onsen.Onsen;
import net.azisaba.life.onsen.listener.OnsenMenu;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OnsenCommandExecutor implements CommandExecutor {

    private final String prefix;
    private final OnsenManager onsenManager;
    Onsen plugin;
    public OnsenCommandExecutor(Onsen plugin, OnsenManager onsenManager) {
        this.plugin = plugin;
        this.onsenManager = onsenManager;
        String prefixConfig = plugin.getConfig().getString("Prefix");
        if (prefixConfig == null) {
            this.prefix = "§7[§eOnsen§7]§r ";
        } else {
            this.prefix = ChatColor.translateAlternateColorCodes('&', prefixConfig) + " ";
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "&cコンソールからは使用できません");
            return true;
        }
        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();
        FileConfiguration config = plugin.getConfig();
        FileConfiguration onsenConfig = plugin.getOnsenConfig();

        if (args.length == 0) {
            sendMessage(player, "&6温泉プラグイン");
            return true;
        }
        if (args[0].equalsIgnoreCase("help")) {
            if (player.hasPermission("onsen.comand.help")) {
                List<String> helpMessages = config.getStringList("HelpMessageAdmin");
                for (String msg : helpMessages) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                }
                return true;
            } else {
                List<String> helpMessages = config.getStringList("HelpMessage");
                for (String msg : helpMessages) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                }
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (player.hasPermission("onsen.command.reload")) {
                plugin.reloadConfig();
                plugin.reloadOnsenConfig();
                sendMessage(player, "&a温泉プラグインのConfigを再読み込みしました");
                return true;
            } else {
                sendMessage(player, "&cこのコマンドを実行する権限がありません");
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("select")) {
            if (args.length == 1) {
                if (onsenManager.isSelected(playerId)) {
                    String selectedOnsen = onsenManager.getSelectedOnsen(playerId);
                    sendMessage(player, "&b" + selectedOnsen + "&fの選択を解除しました");
                    onsenManager.clearSelectedOnsen(player.getUniqueId());
                } else {
                    sendMessage(player, "&f選択されている温泉はありません");
                }
                return true;
            }
            if (args.length == 2) {
                boolean selected = onsenManager.selectOnsen(playerId, args[1]);
                if (selected) {
                    sendMessage(player, "&b" + args[1] + "&fを選択しました");
                } else {
                    sendMessage(player, "&b" + args[1] + "&fの選択を解除しました");
                    onsenManager.clearSelectedOnsen(player.getUniqueId());
                }
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("menu")) {
            sendMessage(player, "&d温泉メニューを開きました");
            OnsenMenu.openOnsenMenu(player);
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            int page = 1;
            if (args.length == 2) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sendMessage(player, "&6ページ番号は数値で入力してください");
                    return true;
                }
            }
            showOnsenList(player, page);
            return true;
        }
        if (args[0].equalsIgnoreCase("info")) {
            String onsenName;
            if (args.length == 2) {
                onsenName = args[1];
            } else if (args.length == 1) {
                onsenName = onsenManager.getSelectedOnsen(player.getUniqueId());
                if (onsenName == null) {
                    sendMessage(player, "&6温泉が選択されていません！");
                    sendMessage(player, "&b/onsen select <温泉名>&6で選択するか、/onsen info <温泉名>を入力してください");
                    return true;
                }
            } else {
                sendMessage(player, "&b/onsen info <温泉名>");
                return true;
            }

            String path = "OnsenList." + onsenName;
            if (!onsenConfig.contains(path)) {
                sendMessage(player, "&b" + onsenName + "&6という名前の温泉は見つかりません");
                return true;
            }

            String status = onsenConfig.getString(path + ".Status");
            String description = onsenConfig.getString(path + ".Description");
            String coloredStatus = getStatus(status);
            String world = onsenConfig.getString(path + ".World");
            int x = onsenConfig.getInt(path + ".X");
            int y = onsenConfig.getInt(path + ".Y");
            int z = onsenConfig.getInt(path + ".Z");

            String uuidString = onsenConfig.getString(path + ".Player");
            String ownerName = null;
            if (uuidString == null) {
                sendMessage(player, "&cプレイヤー情報が見つかりません！");
                return true;
            } else if (uuidString.equalsIgnoreCase("Admin")) {
                onsenName = "Admin";
            } else {
                UUID playerUuid;
                try {
                    playerUuid = UUID.fromString(uuidString);
                } catch (IllegalArgumentException e) {
                    sendMessage(player, "&cUUIDの形式が不正です");
                    return true;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
                ownerName = (offlinePlayer.getName() != null) ? offlinePlayer.getName() : "不明なプレイヤー";
            }

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b" + onsenName + "&fの詳細"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f" + description));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6温泉名: &f" + onsenName));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6申請者: &f" + ownerName));

            if (player.hasPermission("onsen.command.list")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6状態: &f" + coloredStatus));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6World: &f" + world));
            }

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6X座標: &f" + x));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Y座標: &f" + y));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Z座標: &f" + z));

            if (status.equalsIgnoreCase("public") || player.hasPermission("onsen.spawn.bypass")) {
                TextComponent message = new TextComponent(ChatColor.DARK_AQUA + "クリックで温泉に移動する");
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(ChatColor.translateAlternateColorCodes('&',
                                "&7メッセージをクリックすると温泉に移動します")).create()));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/onsen spawn " + onsenName));
                player.spigot().sendMessage(message);
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")) {
            if (args.length == 1) {
                String path = "OnsenList.温泉街";
                World world = Bukkit.getWorld(onsenConfig.getString(path + ".World"));
                if (world == null) {
                    sendMessage(player, "&c移動先のworldが見つかりません");
                    return true;
                }
                int x = onsenConfig.getInt(path + ".X");
                int y = onsenConfig.getInt(path + ".Y");
                int z = onsenConfig.getInt(path + ".Z");

                player.teleport(new Location(world, x, y, z));
                sendMessage(player, "&6温泉街へ移動しました&b^w^");
                return true;
            }
            if (args.length == 2) {
                String path = "OnsenList." + args[1];
                if (!onsenConfig.contains(path)) {
                    sendMessage(player, "&b" + args[1] + "&6という名前の温泉は登録されていません");
                    return true;
                }
                String status = onsenConfig.getString(path + ".Status");
                if (status == null) {
                    sendMessage(player, "&c不明なエラーが発生しました");
                    return true;
                }
                if (status.equalsIgnoreCase("unrated")) {
                    sendMessage(player, "&b" + args[1] + "&6はまだ承認されていません");
                    return true;
                } else if (status.equalsIgnoreCase("deny")) {
                    sendMessage(player, "&b" + args[1] + "&6はリクエストが却下されため移動できません");
                    return true;
                } else if (status.equalsIgnoreCase("private")) {
                    if (!player.hasPermission("onsen.spawn.bypass")) {
                        sendMessage(player, "&b" + args[1] + "&6は公開されていません");
                        return true;
                    }
                }

                World world = Bukkit.getWorld(onsenConfig.getString(path + ".World"));
                if (world == null) {
                    sendMessage(player, "&c移動先のworldが見つかりません");
                    return true;
                }
                int x = onsenConfig.getInt(path + ".X");
                int y = onsenConfig.getInt(path + ".Y");
                int z = onsenConfig.getInt(path + ".Z");

                player.teleport(new Location(world, x, y, z));
                sendMessage(player, "&b" + args[1] + "&fに移動しました");
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("request")) {
            if (args.length == 1) {
                sendMessage(player, "&b/onsen request <設定したい温泉名>");
                return true;
            }
            if (args.length >= 2) {
                String path = "OnsenList." + args[1];
                if (onsenConfig.contains(path)) {
                    sendMessage(player, "&b" + args[1] + "&fという名前の温泉は既に登録されています");
                    return true;
                }
                onsenConfig.set(path + ".Player", player.getUniqueId().toString());
                onsenConfig.set(path + ".Status", "unrated");
                onsenConfig.set(path + ".World", player.getWorld().getName());
                onsenConfig.set(path + ".X", player.getLocation().getBlockX());
                onsenConfig.set(path + ".Y", player.getLocation().getBlockY());
                onsenConfig.set(path + ".Z", player.getLocation().getBlockZ());
                plugin.saveOnsenConfig();
                sendMessage(player, "温泉名を&b" + args[1] + "&fでリクエストを送信しました");
                for (Player admin : Bukkit.getOnlinePlayers()) {
                    if (admin.hasPermission("onsen.request.notice")) {
                        sendMessage(admin, "&d" + player.getName() + "&fが&b" + args[1] + "&fの温泉登録リクエストを申請しました");
                        return true;
                    }
                }
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("requests")) {
            if (args.length == 1) {
                showRequestOnsenList(player, 1);
                return true;
            }

            if (args[1].equalsIgnoreCase("accept")) {
                String onsenName = null;
                if (onsenManager.isSelected(playerId)) {
                    onsenName = onsenManager.getSelectedOnsen(playerId);
                } else if (args.length >= 3) {
                    onsenName = args[2];
                }
                if (onsenName == null) {
                    sendMessage(player, "&6温泉が選択されていません！");
                    sendMessage(player, "&b/onsen select <温泉名>&6で選択するか、/onsen requests accept <温泉名>を入力してください");
                    return true;
                }

                String path = "OnsenList." + onsenName;

                if (!onsenConfig.contains(path)) {
                    sendMessage(player, "&b" + onsenName + "&6という名前の温泉は見つかりません");
                    return true;
                }
                onsenConfig.set(path + ".Status", "public");
                plugin.saveOnsenConfig();

                String playerUuidString = onsenConfig.getString(path + ".Player");

                UUID playerUuid;
                try {
                    playerUuid = UUID.fromString(playerUuidString);
                } catch (IllegalArgumentException e) {
                    sendMessage(player, "&c申請者のUUIDが無効です");
                    return true;
                }

                OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(playerUuid);
                String ownerName = ownerPlayer.getName() != null ? ownerPlayer.getName() : "不明なプレイヤー";

                sendMessage(player, "&b" + onsenName + "&fの温泉登録リクエストを&a承認&fしました");

                Player onlineOwner = ownerPlayer.getPlayer();
                if (onlineOwner != null && onlineOwner.isOnline()) {
                    sendMessage(onlineOwner, "&b" + onsenName + "&fの温泉登録リクエストが&a承認&fされました");
                } else {
                    plugin.getLogger().warning(ownerName + "がオフラインのため通知ができませんでした");
                }
                onsenManager.clearSelectedOnsen(player.getUniqueId());
                return true;
            }
            if (args[1].equalsIgnoreCase("deny")) {
                String onsenName = null;
                if (onsenManager.isSelected(playerId)) {
                    onsenName = onsenManager.getSelectedOnsen(playerId);
                } else if (args.length >= 3) {
                    onsenName = args[2];
                }
                if (onsenName == null) {
                    sendMessage(player, "&6温泉が選択されていません！");
                    sendMessage(player, "&b/onsen select <温泉名> &6で選択するか、/onsen requests deny <温泉名>を入力してください");
                    return true;
                }

                String path = "OnsenList." + onsenName;

                if (!onsenConfig.contains(path)) {
                    sendMessage(player, "&b" + onsenName + "&6 という名前の温泉は見つかりません");
                    return true;
                }
                onsenConfig.set(path + ".Status", "deny");
                plugin.saveOnsenConfig();

                String playerUuidString = onsenConfig.getString(path + ".Player");
                UUID playerUuid;
                try {
                    playerUuid = UUID.fromString(playerUuidString);
                } catch (IllegalArgumentException e) {
                    sendMessage(player, "&c申請者のUUIDが無効です");
                    return true;
                }
                OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(playerUuid);
                String ownerName = ownerPlayer.getName() != null ? ownerPlayer.getName() : "不明なプレイヤー";

                sendMessage(player, "&b" + onsenName + "&fの温泉登録リクエストを&c拒否&fしました");

                Player onlineOwner = ownerPlayer.getPlayer();
                if (onlineOwner != null && onlineOwner.isOnline()) {
                    sendMessage(onlineOwner, "&b" + onsenName + "&fの温泉登録リクエストが&c拒否&fされました");
                } else {
                    plugin.getLogger().warning(ownerName + "がオフラインのため通知ができませんでした");
                }
            }
            try {
                int page = Integer.parseInt(args[1]);
                showRequestOnsenList(player, page);
            } catch (NumberFormatException e) {
                sendMessage(player, "&6ページ番号は数値で入力してください");
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("new")) {
            if (args.length == 1) {
                sendMessage(player, "&b/onsen new <登録する温泉名>");
            }
            String onsenName = null;
            if (onsenManager.isSelected(playerId)) {
                onsenName = onsenManager.getSelectedOnsen(playerId);
            } else if (args.length >= 3) {
                onsenName = args[2];
            }
            if (onsenName == null) {
                sendMessage(player, "温泉が指定されていません。/onsen select <温泉名> または /onsen set spawn <温泉名> を使用してください");
                return true;
            }

            String path = "OnsenList." + onsenName;

            if (onsenConfig.contains(path)) {
                sendMessage(player, "&b" + onsenName + "&6という名前の温泉は既に登録されています");
                return true;
            }
            Location loc = player.getLocation();
            onsenConfig.set(path + ".Player", player.getUniqueId().toString());
            onsenConfig.set(path + ".Status", "public");
            onsenConfig.set(path + ".World", loc.getWorld().getName());
            onsenConfig.set(path + ".X", loc.getBlockX());
            onsenConfig.set(path + ".Y", loc.getBlockY());
            onsenConfig.set(path + ".Z", loc.getBlockZ());
            plugin.saveOnsenConfig();
            sendMessage(player, "温泉名を&b" + onsenManager + "&fとして登録しました");
            return true;
        }
        if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove")) {
            if (args.length >= 1) {
                String onsenName = null;
                if (onsenManager.isSelected(playerId)) {
                    onsenName = onsenManager.getSelectedOnsen(playerId);
                } else if (args.length >= 2) {
                    onsenName = args[1];
                }
                if (onsenName == null) {
                    sendMessage(player, "温泉が指定されていません。/onsen select <温泉名> または /onsen delete <温泉名> を使用してください");
                    return true;
                }

                if (!canModifyOnsen(player, onsenName)) {
                    sendMessage(player, "&cこの温泉を変更する権限がありません");
                    return true;
                }

                String path = "OnsenList." + onsenName;

                if (!onsenConfig.contains(path)) {
                    sendMessage(player, "&b" + onsenName + "&6という名前の温泉は見つかりません");
                    return true;
                }
                onsenConfig.set(path, null);
                plugin.saveOnsenConfig();
                sendMessage(player, "&b" + onsenName + "&fを&c削除&fしました");
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("set")) {
            if (args.length == 1) {
                sendMessage(player, "&b/onsen set <設定項目>");
                return true;
            }
            if (args.length >= 2 && args[1].equalsIgnoreCase("spawn")) {
                String onsenName = null;
                if (onsenManager.isSelected(playerId)) {
                    onsenName = onsenManager.getSelectedOnsen(playerId);
                } else if (args.length >= 3) {
                    onsenName = args[2];
                }
                if (onsenName == null) {
                    sendMessage(player, "温泉が指定されていません。/onsen select <温泉名> または /onsen set spawn <温泉名> を使用してください");
                    return true;
                }

                if (!canModifyOnsen(player, onsenName)) {
                    sendMessage(player, "&cこの温泉を変更する権限がありません");
                    return true;
                }

                String path = "OnsenList." + onsenName;
                Location location = player.getLocation();
                int x = location.getBlockX();
                int y = location.getBlockY();
                int z = location.getBlockZ();
                if (!onsenConfig.contains(path)) {
                    sendMessage(player, "&b" + onsenName + "&6という名前の温泉は見つかりません");
                    return true;
                }
                onsenConfig.set(path + ".World", location.getWorld().getName());
                onsenConfig.set(path + ".X", x);
                onsenConfig.set(path + ".Y", y);
                onsenConfig.set(path + ".Z", z);
                plugin.saveOnsenConfig();
                sendMessage(player, "&b" + onsenName + "&fの移動先を&6X座標:&d" + x + "&7, &6Y座標:&d" + y + "&7, &6Z座標:&d" + z + "&fに設定しました");
                return true;
            }
            if (args.length >= 2 && args[1].equalsIgnoreCase("public")) {
                String onsenName = null;
                if (onsenManager.isSelected(playerId)) {
                    onsenName = onsenManager.getSelectedOnsen(playerId);
                } else if (args.length >= 3) {
                    onsenName = args[2];
                }
                if (onsenName == null) {
                    sendMessage(player, "温泉が指定されていません。/onsen select <温泉名> または /onsen set public <温泉名> を使用してください");
                    return true;
                }

                if (!canModifyOnsen(player, onsenName)) {
                    sendMessage(player, "&cこの温泉を変更する権限がありません");
                    return true;
                }

                String path = "OnsenList." + onsenName;
                if (!onsenConfig.contains(path)) {
                    sendMessage(player, "&b" + onsenName + "&6という名前の温泉は見つかりません");
                    return true;
                }
                onsenConfig.set(path + ".Status", "public");
                plugin.saveOnsenConfig();
                sendMessage(player, "&b" + onsenName + "&fを&a公開&fに設定しました");
                return true;
            }
            if (args.length >= 2 && args[1].equalsIgnoreCase("private")) {
                String onsenName = null;
                if (onsenManager.isSelected(playerId)) {
                    onsenName = onsenManager.getSelectedOnsen(playerId);
                } else if (args.length >= 3) {
                    onsenName = args[2];
                }
                if (onsenName == null) {
                    sendMessage(player, "温泉が指定されていません。/onsen select <温泉名> または /onsen set spawn <温泉名> を使用してください");
                    return true;
                }

                if (!canModifyOnsen(player, onsenName)) {
                    sendMessage(player, "&cこの温泉を変更する権限がありません");
                    return true;
                }

                String path = "OnsenList." + onsenName;
                if (!onsenConfig.contains(path)) {
                    sendMessage(player, "&b" + onsenName + "&6という名前の温泉は見つかりません");
                    return true;
                }
                onsenConfig.set(path + ".Status", "private");
                plugin.saveOnsenConfig();
                sendMessage(player, "&b" + onsenName + "&fを&c非公開&fに設定しました");
                return true;
            }
            if (args[1].equalsIgnoreCase("description")) {
                if (args.length < 3) {
                    sendMessage(player, "&b/onsen set description <説明文> [温泉名]");
                    return true;
                }

                String onsenName = null;
                if (onsenManager.isSelected(playerId)) {
                    onsenName = onsenManager.getSelectedOnsen(playerId);
                } else if (args.length >= 4) {
                    onsenName = args[3];
                }
                if (onsenName == null) {
                    sendMessage(player, "温泉が指定されていません。/onsen select <温泉名> または /onsen set description <説明文> [温泉名] を使用してください");
                    return true;
                }

                if (!canModifyOnsen(player, onsenName)) {
                    sendMessage(player, "&cこの温泉を変更する権限がありません");
                    return true;
                }

                String path = "OnsenList." + onsenName;
                if (!onsenConfig.contains(path)) {
                    sendMessage(player, "&b" + onsenName + "&6という名前の温泉は見つかりません");
                    return true;
                }

                int maxDescriptionLength = plugin.getConfig().getInt("max-description-length", 30);
                if (args[2].length() > maxDescriptionLength) {
                    sendMessage(player, "&6説明文は" + maxDescriptionLength + "&6文字以内にしてください");
                    return true;
                }
                onsenConfig.set(path + ".Description", args[2]);
                plugin.saveOnsenConfig();
                sendMessage(player, "&b" + onsenName + "&fの説明文を&b" + args[2] + "&fに設定しました");
                return true;
            }
            if (args[1].equalsIgnoreCase("enchant")) {
                if (args.length < 3) {
                    sendMessage(player, "&b/onsen set enchant <true/false> [温泉名]");
                    return true;
                }

                String onsenName = null;
                if (onsenManager.isSelected(playerId)) {
                    onsenName = onsenManager.getSelectedOnsen(playerId);
                } else if (args.length >= 4) {
                    onsenName = args[3];
                }
                if (onsenName == null) {
                    sendMessage(player, "温泉が指定されていません。/onsen select <温泉名> または /onsen set enchant <true/false> [温泉名] を使用してください");
                    return true;
                }

                if (!canModifyOnsen(player, onsenName)) {
                    sendMessage(player, "&cこの温泉を変更する権限がありません");
                    return true;
                }

                String path = "OnsenList." + onsenName;
                if (!onsenConfig.contains(path)) {
                    sendMessage(player, "&b" + onsenName + "&6という名前の温泉は見つかりません");
                    return true;
                }

                if (args[2].equalsIgnoreCase("true")) {
                    onsenConfig.set(path + ".Enchant", "true");
                    plugin.saveOnsenConfig();
                    sendMessage(player, "&b" + onsenName + "&fのアイテムのエンチャントを&a有効&fに設定しました");
                } else if (args[2].equalsIgnoreCase("false")) {
                    onsenConfig.set(path + ".Enchant", "false");
                    plugin.saveOnsenConfig();
                    sendMessage(player, "&b" + onsenName + "&fのアイテムのエンチャントを&c無効&fに設定しました");
                } else {
                    sendMessage(player, "&6" +args[2] + "は無効なコマンドです。trueかfalseで指定してください");
                }
                return true;
            }
        }
        if (args[1].equalsIgnoreCase("itemID")) {
            if (args.length < 3) {
                sendMessage(player, "&b/onsen set itemID <アイテムID> [温泉名]");
                return true;
            }

            String onsenName = null;
            if (onsenManager.isSelected(playerId)) {
                onsenName = onsenManager.getSelectedOnsen(playerId);
            } else if (args.length >= 4) {
                onsenName = args[3];
            }
            if (onsenName == null) {
                sendMessage(player, "温泉が指定されていません。/onsen select <温泉名> または /onsen set itemID <アイテムID> [温泉名] を使用してください");
                return true;
            }

            if (!canModifyOnsen(player, onsenName)) {
                sendMessage(player, "&cこの温泉を変更する権限がありません");
                return true;
            }

            String path = "OnsenList." + onsenName;
            if (!onsenConfig.contains(path)) {
                sendMessage(player, "&b" + onsenName + "&6という名前の温泉は見つかりません");
                return true;
            }

            String item = args[2];
            Material material = Material.getMaterial(item);

            if (material == null) {
                sendMessage(player, "&b" + item + "&6というアイテムIDは見つかりませんでした");
                return true;
            }
            ItemStack ItemID = new ItemStack(material);

            onsenConfig.set(path + ".ItemID", ItemID);
            plugin.saveOnsenConfig();
            sendMessage(player, "&b" + onsenName + "&fのアイテムIDを&b" + ItemID + "&fに設定しました");
            return true;
        } else {
            sendMessage(player, "&c" + args[0] + "というコマンドは見つかりませんでした");
        }
        return true;
    }

    private String getStatus(String status) {
        if (status == null) {
            return ChatColor.GRAY + "不明";
        }
        switch (status.toLowerCase()) {
            case "public":
                return ChatColor.GREEN + "公開" + ChatColor.RESET;
            case "private":
                return ChatColor.RED + "非公開" + ChatColor.RESET;
            case "unrated":
                return ChatColor.YELLOW + "審査中" + ChatColor.RESET;
            case "deny":
                return ChatColor.DARK_RED + "却下" + ChatColor.RESET;
            default:
                return ChatColor.GRAY + "不明" + ChatColor.RESET;
        }
    }

    private void showOnsenList(Player player, int page) {
        FileConfiguration onsenConfig = plugin.getOnsenConfig();
        ConfigurationSection onsenList = onsenConfig.getConfigurationSection("OnsenList");

        if (onsenList == null || onsenList.getKeys(false).isEmpty()) {
            sendMessage(player, "&6登録されている温泉がありません");
            return;
        }

        boolean isAdmin = player.hasPermission("onsen.command.list");

        List<String> onsenNames = new ArrayList<>(onsenList.getKeys(false));
        int totalResults = onsenNames.size();
        int totalPages = (int) Math.ceil(totalResults / 5.0);
        int start = (page - 1) * 5;
        int end = Math.min(start + 5, totalResults);

        sendMessage(player, "&d登録されている温泉一覧 &7(" + totalResults + "&7件)");

        for (int i = start; i < end; i++) {
            String onsenName = onsenNames.get(i);
            String path = "OnsenList." + onsenName;
            String playerUuidString = onsenConfig.getString(path + ".Player");
            String ownerName;
            if (playerUuidString.equalsIgnoreCase("Admin")) {
                ownerName = "Admin";
            } else {
                UUID playerUuid;
                try {
                    playerUuid = UUID.fromString(playerUuidString);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + "登録されているUUIDが無効です");
                    continue;
                }

                OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(playerUuid);
                ownerName = (ownerPlayer.getName() != null) ? ownerPlayer.getName() : "不明なプレイヤー";
            }
            String status = onsenConfig.getString(path + ".Status");
            String description = onsenConfig.getString(path + ".Description", "");
            String world = onsenConfig.getString(path + ".World", "不明");
            int x = onsenConfig.getInt(path + ".X");
            int y = onsenConfig.getInt(path + ".Y");
            int z = onsenConfig.getInt(path + ".Z");

            TextComponent message = new TextComponent(ChatColor.GRAY + "[" + (i + 1) + "] " + ChatColor.AQUA + onsenName + ChatColor.GRAY + " - " + ownerName + ChatColor.RESET);
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/onsen info " + onsenName));
            if (isAdmin) {
                String coloredStatus = getStatus(status);
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(ChatColor.translateAlternateColorCodes('&',
                                "&6状態:" + coloredStatus + "\n" +
                                        "&6説明:" + description + "\n" +
                                        "&6World:&f" + world + "\n" +
                                        "&6X座標:&f" + x + "\n" +
                                        "&6Y座標:&f" + y + "\n" +
                                        "&6Z座標:&f" + z
                        )).create()));
            } else if ("public".equalsIgnoreCase(status)) {
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(ChatColor.translateAlternateColorCodes('&',
                                "&6説明:&f" + description + "\n" +
                                        "&6X座標:&f" + x + "\n" +
                                        "&6Y座標:&f" + y + "\n" +
                                        "&6Z座標:&f" + z)).create()));
            } else {
                continue;
            }
            player.spigot().sendMessage(message);
        }
        TextComponent navigation = new TextComponent();
        if (page > 1) {
            TextComponent prevButton = new TextComponent(ChatColor.BLUE + "◀ " + ChatColor.RESET);
            prevButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/onsen list " + (page - 1)));
            prevButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&7前のページ")).create()));
            navigation.addExtra(prevButton);
        }

        TextComponent pageInfo = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                "&fページ &e" + page + " / " + totalPages + " "));
        navigation.addExtra(pageInfo);

        if (page < totalPages) {
            TextComponent nextButton = new TextComponent(ChatColor.BLUE + "▶" + ChatColor.RESET);
            nextButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/onsen list " + (page + 1)));
            nextButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&7次のページ")).create()));
            navigation.addExtra(nextButton);
        }
        player.spigot().sendMessage(navigation);
    }

    private void showRequestOnsenList(Player player, int page) {
        FileConfiguration onsenConfig = plugin.getOnsenConfig();
        ConfigurationSection onsenList = onsenConfig.getConfigurationSection("OnsenList");

        if (onsenList == null || onsenList.getKeys(false).isEmpty()) {
            sendMessage(player, "&6登録されている温泉がありません");
            return;
        }

        UUID playerId = player.getUniqueId();
        boolean canViewAll = player.hasPermission("onsen.requests.viewall");

        List<String> unratedOnsenNames = new ArrayList<>();
        for (String onsenName : onsenList.getKeys(false)) {
            String path = "OnsenList." + onsenName;
            String status = onsenConfig.getString(path + ".Status", "不明");
            String ownerUUID = onsenConfig.getString(path + ".Player");

            if (!"unrated".equalsIgnoreCase(status)) {
                continue;
            }

            if (ownerUUID == null || ownerUUID.isEmpty()) {
                continue;
            }

            if (!canViewAll && !ownerUUID.equalsIgnoreCase(playerId.toString())) {
                continue;
            }
            unratedOnsenNames.add(onsenName);
        }

        if (unratedOnsenNames.isEmpty()) {
            if (canViewAll) {
                sendMessage(player, "&6リクエストされた温泉がありません");
            } else {
                sendMessage(player, "&6審査中の温泉はありません");
            }
            return;
        }

        int totalResults = unratedOnsenNames.size();
        int totalPages = (int) Math.ceil(totalResults / 5.0);

        if (page < 1 || page > totalPages) {
            sendMessage(player, "&6指定されたページは存在しません");
            return;
        }

        int start = (page - 1) * 5;
        int end = Math.min(start + 5, totalResults);

        sendMessage(player, "&dリクエストされた温泉一覧 &7(" + unratedOnsenNames.size() + "&7件)");


        for (int i = start; i < end; i++) {
            String onsenName = unratedOnsenNames.get(i);
            String path = "OnsenList." + onsenName;

            String ownerUUIDStr = onsenConfig.getString(path + ".Player");
            String ownerName = getOwnerNameFromUUID(ownerUUIDStr);

            String description = onsenConfig.getString(path + ".Description", "");
            String world = onsenConfig.getString(path + ".World", "不明");
            int x = onsenConfig.getInt(path + ".X");
            int y = onsenConfig.getInt(path + ".Y");
            int z = onsenConfig.getInt(path + ".Z");

            TextComponent message = new TextComponent(ChatColor.GRAY + "[" + (i + 1) + "] " + ChatColor.AQUA + onsenName + ChatColor.GRAY + " - " + ownerName + ChatColor.RESET);
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/onsen info " + onsenName));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.translateAlternateColorCodes('&',
                            "&6説明:&f" + description + "\n" +
                                    "&6申請者:&f" + ownerName + "\n" +
                                    "&6World:&f" + world + "\n" +
                                    "&6X座標:&f" + x + "\n" +
                                    "&6Y座標:&f" + y + "\n" +
                                    "&6Z座標:&f" + z
                    )).create()));
            player.spigot().sendMessage(message);
        }

        TextComponent navigation = new TextComponent();

        if (page > 1) {
            TextComponent prevButton = new TextComponent(ChatColor.BLUE + "◀ " + ChatColor.RESET);
            prevButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/onsen requests " + (page - 1)));
            prevButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&7前のページ")).create()));
            navigation.addExtra(prevButton);
        }

        TextComponent pageInfo = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                "&fページ &e" + page + " / " + totalPages + " "));
        navigation.addExtra(pageInfo);

        if (page < totalPages) {
            TextComponent nextButton = new TextComponent(ChatColor.BLUE + "▶" + ChatColor.RESET);
            nextButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/onsen requests " + (page + 1)));
            nextButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&7次のページ")).create()));
            navigation.addExtra(nextButton);
        }

        player.spigot().sendMessage(navigation);
    }

    private String getOwnerNameFromUUID(String uuidStr) {
        if (uuidStr.equalsIgnoreCase("Admin")) {
            return "Admin";
        }
        try {
            UUID uuid = UUID.fromString(uuidStr);
            Player onlinePlayer = Bukkit.getPlayer(uuid);
            if (onlinePlayer != null) {
                return onlinePlayer.getName();
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                return offlinePlayer.getName();
            }

        } catch (IllegalArgumentException e) {
            return "不明なプレイヤー";
        }
        return "不明なプレイヤー";
    }

    private boolean canModifyOnsen(Player player, String onsenName) {
        UUID playerId = player.getUniqueId();

        if (player.hasPermission("onsen.command.set")) {
            return true;
        }

        String path = "OnsenList." + onsenName + ".Player";
        if (!plugin.getOnsenConfig().contains(path)) {
            return false;
        }

        String ownerUUID = plugin.getOnsenConfig().getString(path);
        return ownerUUID.equals(playerId.toString());
    }


    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', message));
    }
}
