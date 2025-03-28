package net.azisaba.life.onsen.listener;

import net.azisaba.life.onsen.Onsen;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class OnsenMenu implements Listener {

    private final Onsen plugin;
    private String prefix;

    public OnsenMenu(Onsen plugin) {
        this.plugin = plugin;
        String prefixConfig = plugin.getConfig().getString("Prefix");
        if (prefixConfig == null) {
            this.prefix = "§7[§eOnsen§7]§r ";
        } else {
            this.prefix = ChatColor.translateAlternateColorCodes('&', prefixConfig) + " ";
        }
    }

    private final Map<Player, Integer> playerPageMap = new HashMap<>();



    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void openOnsenMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 45, "§1§l温泉メニュー");

        setBorder(inv);
        setBlank(inv);
        inv.setItem(44, createItem(Material.BARRIER, "§c§l閉じる", Collections.singletonList("§7§l左クリックでメニューを閉じます")));

        setItems(inv, new int[]{
                13, 19, 21, 22, 23, 25, 31
        }, new ItemStack[]{
                createItem(Material.CAMPFIRE, "§6§l温泉街へ移動する", Collections.singletonList("§e左クリックで温泉街へ移動します")),
                createItem(Material.EMERALD, "§b§l温泉リクエストを送信する", Collections.singletonList("§7左クリックで温泉リクエストの手順を表示します")),
                createItem(Material.NETHER_STAR, "§e§l温泉一覧を表示する", Collections.singletonList("§f左クリックで登録されている温泉一覧を表示します")),
                getPlayerHead(player),
                createItem(Material.WRITABLE_BOOK, "§a§l温泉コマンドのヘルプを表示する", Collections.singletonList("§f左クリックで温泉コマンドのヘルプを表示します")),
                createItem(Material.GOLD_INGOT, "§b§l温泉限定アイテムを入手する", Collections.singletonList("§f左クリックで温泉限定アイテムショップを開くよ！")),
                createItem(Material.MAP, "§6§l温泉テレポート先選択", Arrays.asList("§7温泉テレポート先に§7§l1クリック§7でひとっとび！", "§7左クリックでテレポート先選択画面"))
        });

        player.openInventory(inv);

    }

    public void openOnsenTeleportMenu(Player player ,int page) {
        FileConfiguration onsenConfig = plugin.getOnsenConfig();
        ConfigurationSection onsenList = onsenConfig.getConfigurationSection("OnsenList");
        Location location = player.getLocation();

        if (onsenList == null) {
            sendMessage(player, "Configに温泉データが存在しません");
            return;
        }

        playerPageMap.put(player, page);

        List<String> publicOnsenNames = new ArrayList<>();
        for (String onsenName : onsenList.getKeys(false)) {
            ConfigurationSection onsenInfo = onsenList.getConfigurationSection(onsenName);
            if (onsenInfo == null) continue;

            boolean hasPermission = player.hasPermission("onsen.admin");
            String status = onsenInfo.getString("Status", "unrated").toLowerCase();

            if (hasPermission || status.equalsIgnoreCase("public")) {
                publicOnsenNames.add(onsenName);
            } else {
                if (!status.equalsIgnoreCase("public")) continue;
                publicOnsenNames.add(onsenName);
            }
        }

        if (publicOnsenNames.isEmpty()) {
            player.closeInventory();
            sendMessage(player, "公開状態の温泉はありません");
            player.playSound(location, Sound.ENTITY_VILLAGER_NO, 2, 1);
            return;
        }

        int totalPages = (publicOnsenNames.size() + 44) / 45;
        page = Math.max(1, Math.min(page, totalPages));

        Inventory inv = Bukkit.createInventory(player, 54, "§1§l温泉テレポートメニュー §r(Page " + page + " / " + totalPages + ")");

        int start = (page - 1) * 45;
        int end = Math.min(start + 45, publicOnsenNames.size());

        for (int i = start; i < end; i++) {
            String onsenName = publicOnsenNames.get(i);
            ConfigurationSection onsen = onsenList.getConfigurationSection(onsenName);
            String path = "OnsenList." + onsenName;
            String description = onsenConfig.getString(path + ".Description");
            String status = onsenConfig.getString(path + ".Status");
            String coloredStatus = getStatus(status);
            String world = onsenConfig.getString(path + ".World");

            String uuidString = onsenConfig.getString(path + ".Player");

            String ownerName;
            if (uuidString == null) {
                sendMessage(player, "&cプレイヤー情報が見つかりません！");
                return;
            } else if (uuidString.equalsIgnoreCase("Admin")) {
                ownerName = "Admin";
            } else if (uuidString.equalsIgnoreCase("unknown")) {
                ownerName = "不明";
            }
            else {
                UUID playerUuid;
                try {
                    playerUuid = UUID.fromString(uuidString);
                } catch (IllegalArgumentException e) {
                    sendMessage(player, "&cUUIDの形式が不正です");
                    return;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
                ownerName = (offlinePlayer.getName() != null) ? offlinePlayer.getName() : "不明なプレイヤー";
            }
            if (onsen == null) continue;

            String itemId = onsen.getString("ItemID", "CAMPFIRE");
            boolean enchant = onsen.getBoolean("Enchant", false);

            Material material;
            try {
                material = Material.valueOf(itemId.toUpperCase());
            } catch (IllegalArgumentException e) {
                material = Material.CAMPFIRE;
            }

            List<String> lore = new ArrayList<>();
            if (description != null && !description.isEmpty()) {
                lore.add("§f" + description);
            }
            lore.add("§6申請者: §f" + ownerName);
            if (player.hasPermission("onsen.admin")) {
                lore.add("§6状態: §f" + coloredStatus);
                lore.add("§6World: §f" + world);
                lore.add("§6X座標: §f" + onsen.getInt("X"));
                lore.add("§6Y座標: §f" + onsen.getInt("Y"));
                lore.add("§6Z座標: §f" + onsen.getInt("Z"));
            } else {
                lore.add("§6X座標: §f" + onsen.getInt("X"));
                lore.add("§6Y座標: §f" + onsen.getInt("Y"));
                lore.add("§6Z座標: §f" + onsen.getInt("Z"));
            }

            ItemStack item = createItem(material, "§a" + onsenName, lore);
            if (enchant) {
                item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(meta);
                }
            }
            inv.setItem(i - start, item);
        }

        if (page > 1) {
            inv.setItem(45, createItem(Material.ARROW, "§a前のページ", Collections.singletonList("§7クリックで前のページを表示")));
        }
        inv.setItem(49, createItem(Material.BARRIER, "§c§l閉じる", Collections.singletonList("§7クリックでメニューを閉じます")));
        if (page < totalPages) {
            inv.setItem(53, createItem(Material.ARROW, "§a次のページ", Collections.singletonList("§7クリックで次のページを表示")));
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getClickedInventory();
        String title = event.getView().getTitle();

        if (inv == null) return;
        if (!title.contains("§1§l温泉メニュー") && !title.contains("§1§l温泉テレポートメニュー")) return;
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (title.contains("§1§l温泉メニュー")) {
            switch (clickedItem.getType()) {
                case CAMPFIRE:
                    player.closeInventory();
                    Bukkit.dispatchCommand(player, "onsen spawn");
                    break;
                case EMERALD:
                    player.closeInventory();
                    sendMessage(player, "&3温泉リクエストについて");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6温泉リクエストに関してはWikiを参照してください"));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("WikiURL", "&f&nhttps://wiki.azisaba.net/wiki/温泉について(Life)")));
                    break;
                case NETHER_STAR:
                    player.closeInventory();
                    Bukkit.dispatchCommand(player, "onsen list");
                    break;
                case PLAYER_HEAD:
                    player.closeInventory();
                    Bukkit.dispatchCommand(player, "onsen requests");
                    break;
                case WRITABLE_BOOK:
                    player.closeInventory();
                    Bukkit.dispatchCommand(player, "onsen help");
                    break;
                case GOLD_INGOT:
                    player.closeInventory();
                    sendMessage(player, "&3温泉限定アイテムについて");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f温泉限定アイテムは温泉街で入手できます"));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/onsen spawn&fと打って温泉街に行こう！"));
                    break;
                case MAP:
                    player.closeInventory();
                    openOnsenTeleportMenu(player, 1);
                    break;
                case BARRIER:
                    player.closeInventory();
                    break;
                default:
                    break;
            }
        }

        if (title.contains("§1§l温泉テレポートメニュー")) {
            String itemName = clickedItem.getItemMeta() != null ? clickedItem.getItemMeta().getDisplayName() : "";
            Location location = player.getLocation();

            int currentPage = playerPageMap.getOrDefault(player, 1);

            if (clickedItem.getType() == Material.BARRIER) {
                player.closeInventory();
            } else if (clickedItem.getType() == Material.ARROW) {
                if (event.getSlot() == 45) {
                    openOnsenTeleportMenu(player, currentPage - 1);
                    player.playSound(location, Sound.UI_BUTTON_CLICK, 1, 1);
                }
                else if (event.getSlot() == 53) {
                    openOnsenTeleportMenu(player, currentPage + 1);;
                    player.playSound(location, Sound.UI_BUTTON_CLICK, 1, 1);
                }
            } else {
                String onsenName = ChatColor.stripColor(itemName);
                teleportToOnsen(player, onsenName);
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerPageMap.remove(player);
    }

    private void teleportToOnsen(Player player, String onsenName) {
        FileConfiguration onsenConfig = plugin.getOnsenConfig();
        ConfigurationSection onsen = onsenConfig.getConfigurationSection("OnsenList." + onsenName);

        if (onsen == null) {
            sendMessage(player, "&cその温泉は存在しません！");
            return;
        }

        String worldName = onsen.getString("World");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sendMessage(player, "&CWorldが見つかりません！");
            return;
        }

        double x = onsen.getDouble("X");
        double y = onsen.getDouble("Y");
        double z = onsen.getDouble("Z");

        float yaw = 0.0f;
        float pitch = 0.0f;

        if (onsenConfig.contains(onsen + ".Yaw")) {
            yaw = (float) onsenConfig.getDouble(onsen + ".Yaw");
        }
        if (onsenConfig.contains(onsen + ".Pitch")) {
            pitch = (float) onsenConfig.getDouble(onsen + ".Pitch");
        }

        player.teleport(new Location(world, x, y, z, yaw, pitch));
        sendMessage(player, "&a" + onsenName + "に移動しました");
    }

    private static ItemStack createItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void setBorder(Inventory inv) {
        ItemStack frame = createItem(Material.LIME_STAINED_GLASS_PANE, " ", null);
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 36, 37, 38, 39, 40, 41, 42, 43}) {
            inv.setItem(i, frame);
        }
    }

    private static void setBlank(Inventory inv) {
        ItemStack frame = createItem(Material.WHITE_STAINED_GLASS_PANE, " ", null);
        for (int i : new int[]{9, 10, 11, 12, 14, 15, 16, 17, 18, 20, 24, 26, 27, 28, 29, 30, 32, 33, 34, 35}) {
            inv.setItem(i, frame);
        }
    }

    private static void setItems(Inventory inv, int[] slots, ItemStack[] items) {
        for (int i = 0; i < slots.length; i++) {
            inv.setItem(slots[i], items[i]);
        }
    }

    private static ItemStack getPlayerHead(Player player) {
        boolean hasPermission = player.hasPermission("onsen.admin");
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            if (hasPermission) {
                skullMeta.setDisplayName("§d§l温泉リクエスト一覧を表示する");
                skullMeta.setLore(Collections.singletonList("§f左クリックでリクエストされた温泉一覧を確認できます"));
            } else {
                skullMeta.setDisplayName("§d§lリクエストした温泉の状態を確認する");
                skullMeta.setLore(Collections.singletonList("§f左クリックでリクエスト状況を確認できます"));
            }
            playerHead.setItemMeta(skullMeta);
        }
        return playerHead;
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
}