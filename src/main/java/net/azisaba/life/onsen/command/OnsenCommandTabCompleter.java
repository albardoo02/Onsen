package net.azisaba.life.onsen.command;

import net.azisaba.life.onsen.Onsen;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OnsenCommandTabCompleter implements TabCompleter {

    private final Onsen plugin;
    private final List<String> itemId;

    public OnsenCommandTabCompleter(Onsen plugin) {
        this.plugin = plugin;
        this.itemId = getAllItemID();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        List<String> completions = new ArrayList<>();
        FileConfiguration onsenConfig = plugin.getOnsenConfig();
        if (args.length == 1) {
            completions.add("help");
            completions.add("usage");
            completions.add("menu");
            completions.add("spawn");
            completions.add("select");
            completions.add("teleport");
            completions.add("tp");
            completions.add("set");
            completions.add("request");
            completions.add("requests");
            completions.add("reload");
            completions.add("list");
            completions.add("info");
            completions.add("new");
            completions.add("delete");
            completions.add("remove");
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("select")) {
                ConfigurationSection onsenListSection = onsenConfig.getConfigurationSection("OnsenList");
                if (onsenListSection != null) {
                    Set<String> onsens = onsenListSection.getKeys(false);
                    for (String onsen : onsens) {
                        if (onsenConfig.getString("OnsenList." + onsen + ".Status").equalsIgnoreCase("public")) {
                            completions.add(onsen);
                        }
                    }
                }
                return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
            }
            else if (args[0].equalsIgnoreCase("request")) {
                completions.add("<設定したい温泉名>");
                return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
            }
            else if (args[0].equalsIgnoreCase("requests")) {
                completions.add("accept");
                completions.add("deny");
                completions.add("list");
                return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
            }
            else if (args[0].equalsIgnoreCase("new")) {
                completions.add("<登録する温泉名>");
                return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
            }
            else if (args[0].equalsIgnoreCase("set")) {
                completions.add("public");
                completions.add("private");
                completions.add("spawn");
                completions.add("itemID");
                completions.add("enchant");
                completions.add("description");
                return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
            }
            else if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("info")) {
                ConfigurationSection onsenListSection = onsenConfig.getConfigurationSection("OnsenList");
                if (onsenListSection != null) {
                    Set<String> onsens = onsenListSection.getKeys(false);
                    completions.addAll(onsens);
                }
                return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
            }
        }
        else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("public")) {
                ConfigurationSection onsenListSection = onsenConfig.getConfigurationSection("OnsenList");
                if (onsenListSection != null) {
                    Set<String> onsens = onsenListSection.getKeys(false);
                    for (String onsen : onsens) {
                        if (onsenConfig.getString("OnsenList." + onsen + ".Status").equalsIgnoreCase("private")) {
                            completions.add(onsen);
                        }
                    }
                }
                return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
            }
            if (args[1].equalsIgnoreCase("private")) {
                ConfigurationSection onsenListSection = onsenConfig.getConfigurationSection("OnsenList");
                if (onsenListSection != null) {
                    Set<String> onsens = onsenListSection.getKeys(false);
                    for (String onsen : onsens) {
                        if (onsenConfig.getString("OnsenList." + onsen + ".Status").equalsIgnoreCase("public")) {
                            completions.add(onsen);
                        }
                    }
                }
                return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
            }
            if (args[1].equalsIgnoreCase("spawn")) {
                ConfigurationSection onsenListSection = onsenConfig.getConfigurationSection("OnsenList");
                if (onsenListSection != null) {
                    Set<String> onsens = onsenListSection.getKeys(false);
                    completions.addAll(onsens);
                }
                return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
            }
            if (args[1].equalsIgnoreCase("accept") || args[1].equalsIgnoreCase("deny")) {
                ConfigurationSection onsenListSection = onsenConfig.getConfigurationSection("OnsenList");
                if (onsenListSection != null) {
                    Set<String> onsens = onsenListSection.getKeys(false);
                    for (String onsen : onsens) {
                        if (onsenConfig.getString("OnsenList." + onsen + ".Status").equalsIgnoreCase("unrated")) {
                            completions.add(onsen);
                        }
                    }
                }
                return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
            }
            if (args[1].equalsIgnoreCase("enchant")) {
                completions.add("true");
                completions.add("false");
                return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
            }
            if (args[1].equalsIgnoreCase("description")) {
                completions.add("<説明文>");
                return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
            }
            if (args[1].equalsIgnoreCase("itemID")) {
                String input = args[2].toLowerCase();
                return itemId.stream()
                        .filter(id -> id.toLowerCase().startsWith(input))
                        .collect(Collectors.toList());
            }
        }
        else if (args.length == 4) {
            if (args[2].equalsIgnoreCase(args[2])) {
                ConfigurationSection onsenListSection = onsenConfig.getConfigurationSection("OnsenList");
                if (onsenListSection != null) {
                    Set<String> onsens = onsenListSection.getKeys(false);
                    completions.addAll(onsens);
                }
                return StringUtil.copyPartialMatches(args[3], completions, new ArrayList<>());
            }
        }
        return null;
    }

    private List<String> getAllItemID() {
        return Arrays.stream(Material.values())
                .map(Material::name)
                .collect(Collectors.toList());
    }
}