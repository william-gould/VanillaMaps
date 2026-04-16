package uk.co.webdent.VanillaMaps.custommaps.command;

import uk.co.webdent.VanillaMaps.custommaps.CustomMapsModule;
import uk.co.webdent.VanillaMaps.custommaps.gui.DrawingGui;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MapCommand implements BasicCommand {

    private final CustomMapsModule plugin;
    private final Map<UUID, byte[]> clipboard = new HashMap<>();

    public MapCommand(CustomMapsModule plugin) {
        this.plugin = plugin;
    }

    public void clearClipboard(UUID playerId) {
        clipboard.remove(playerId);
    }

    @Override
    public void execute(@NotNull CommandSourceStack source, @NotNull String[] args) {
        CommandSender sender = source.getSender();
        if (!(sender instanceof Player player)) {
            String msg = plugin.getPlugin().getConfig().getString("messages.must-be-player",
                    "<red>Only players can use this command.");
            sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(msg));
            return;
        }

        if (args.length >= 1) {
            String sub = args[0].toLowerCase();

            if (sub.equals("copy")) {
                ItemStack held = player.getInventory().getItemInMainHand();
                if (held != null && held.getType().name().contains("MAP")) {
                    byte[] pixels = plugin.getMapDataStore().loadPixels(held);
                    if (pixels != null) {
                        clipboard.put(player.getUniqueId(), pixels);
                        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                                .deserialize("<green>Map copied to clipboard!"));
                    } else {
                        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                                .deserialize("<red>This map does not contain custom pixels to copy."));
                    }
                } else {
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<red>You must hold a custom map to copy."));
                }
                return;
            }

            if (sub.equals("paste")) {
                byte[] pixels = clipboard.get(player.getUniqueId());
                if (pixels == null) {
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<red>Your clipboard is empty."));
                } else {
                    plugin.getMapDataStore().save(player, pixels);
                }
                return;
            }

            if (sub.equals("publish")) {
                ItemStack held = player.getInventory().getItemInMainHand();
                if (held != null && held.getType().name().contains("MAP") && held.getItemMeta() instanceof org.bukkit.inventory.meta.MapMeta meta) {
                    org.bukkit.map.MapView view = meta.getMapView();
                    if (view != null) {
                        int mapId = view.getId();
                        if (plugin.getMapDataStore().isPublished(mapId)) {
                            player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                                    .deserialize("<yellow>This map is already published."));
                        } else if (plugin.getMapDataStore().loadPixels(held) == null) {
                            player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                                    .deserialize("<red>This map does not contain custom pixels to publish."));
                        } else {
                            try {
                                plugin.getMapDataStore().publish(mapId);
                                player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                                        .deserialize("<green>Map published! It can no longer be edited."));
                            } catch (Exception e) {
                                player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                                        .deserialize("<red>Failed to publish map."));
                            }
                        }
                    } else {
                        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                                .deserialize("<red>This map has no view data."));
                    }
                } else {
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<red>You must be holding a custom map to publish."));
                }
                return;
            }

            if (sub.equals("create")) {
                int ratio = 1;
                if (args.length >= 2) {
                    try {
                        ratio = Integer.parseInt(args[1]);
                        if (128 % ratio != 0) {
                            String msg = "<red>Ratio must be a divisor of 128 (e.g. 1, 2, 4, 8, 16)!";
                            player.sendMessage(
                                    net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(msg));
                            return;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                                .deserialize("<red>Invalid ratio number."));
                        return;
                    }
                }

                ItemStack held = player.getInventory().getItemInMainHand();
                if (held != null && held.getType() == org.bukkit.Material.MAP) {
                    String msg = plugin.getPlugin().getConfig().getString("messages.must-fill-map",
                            "<red>You must right-click to fill the map first!");
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(msg));
                } else if (held != null && held.getType().name().contains("MAP")) {
                    DrawingGui gui = new DrawingGui(player, ratio);
                    gui.open();
                } else {
                    String msg = plugin.getPlugin().getConfig().getString("messages.not-holding-map",
                            "<red>You must be holding a map.");
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(msg));
                }
                return;
            }

            if (sub.equals("edit")) {
                int ratio = 1;
                if (args.length >= 2) {
                    try {
                        ratio = Integer.parseInt(args[1]);
                        if (128 % ratio != 0) {
                            String msg = "<red>Ratio must be a divisor of 128 (e.g. 1, 2, 4, 8, 16)!";
                            player.sendMessage(
                                    net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(msg));
                            return;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                                .deserialize("<red>Invalid ratio number."));
                        return;
                    }
                }

                ItemStack held = player.getInventory().getItemInMainHand();
                if (held != null && held.getType().name().contains("MAP")) {
                    // Check if map is published
                    if (held.getItemMeta() instanceof org.bukkit.inventory.meta.MapMeta editMeta) {
                        org.bukkit.map.MapView editView = editMeta.getMapView();
                        if (editView != null && plugin.getMapDataStore().isPublished(editView.getId())) {
                            player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                                    .deserialize("<red>This map has been published and cannot be edited."));
                            return;
                        }
                    }
                    byte[] existingPixels = plugin.getMapDataStore().loadPixels(held);
                    if (existingPixels == null) {
                        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(
                                "<red>This map does not contain custom pixels to edit. Use /map create instead."));
                        return;
                    }
                    DrawingGui gui = new DrawingGui(player, ratio, existingPixels);
                    gui.open();
                } else {
                    String msg = plugin.getPlugin().getConfig().getString("messages.not-holding-map",
                            "<red>You must be holding a map.");
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(msg));
                }
                return;
            }

            if (sub.equals("write")) {
                if (args.length < 3) {
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<red>Usage: /map write \"<text>\" <line> [color]"));
                    return;
                }

                StringBuilder textBuilder = new StringBuilder();
                int idx = 1;
                boolean inQuotes = false;

                if (args[idx].startsWith("\"")) {
                    inQuotes = true;
                    textBuilder.append(args[idx].substring(1));
                    if (args[idx].endsWith("\"") && args[idx].length() > 1) { // e.g. "word"
                        textBuilder.setLength(textBuilder.length() - 1);
                        inQuotes = false;
                    }
                    idx++;
                    while (inQuotes && idx < args.length) {
                        textBuilder.append(" ");
                        if (args[idx].endsWith("\"")) {
                            textBuilder.append(args[idx].substring(0, args[idx].length() - 1));
                            inQuotes = false;
                            idx++;
                            break;
                        } else {
                            textBuilder.append(args[idx]);
                        }
                        idx++;
                    }
                } else {
                    textBuilder.append(args[idx]);
                    idx++;
                }

                if (inQuotes) {
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<red>Missing closing quote for text."));
                    return;
                }

                String text = textBuilder.toString();

                if (idx >= args.length) {
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<red>Missing line number."));
                    return;
                }

                int line;
                try {
                    line = Integer.parseInt(args[idx]);
                    if (line < 1 || line > 18) {
                        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                                .deserialize("<red>Line number must be between 1 and 18."));
                        return;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<red>Invalid line number."));
                    return;
                }
                idx++;

                byte color = uk.co.webdent.VanillaMaps.custommaps.util.ColorPalette
                        .getMapColor(org.bukkit.Material.BLACK_WOOL);
                if (idx < args.length) {
                    String colorName = args[idx].toUpperCase();
                    if (!colorName.endsWith("_WOOL"))
                        colorName += "_WOOL";
                    org.bukkit.Material colorMat = org.bukkit.Material.matchMaterial(colorName);
                    if (colorMat == null) {
                        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                                .deserialize("<red>Unknown color. Try e.g. RED, BLUE, BLACK."));
                        return;
                    }
                    color = uk.co.webdent.VanillaMaps.custommaps.util.ColorPalette.getMapColor(colorMat);
                }

                ItemStack held = player.getInventory().getItemInMainHand();
                if (held == null || !held.getType().name().contains("MAP")) {
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<red>You must be holding a map to write on it!"));
                    return;
                }

                byte[] pixels = plugin.getMapDataStore().loadPixels(held);
                if (pixels == null) {
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(
                            "<red>This map has no custom data yet! Please initialize it using /map create first."));
                    return;
                }

                int textWidth = uk.co.webdent.VanillaMaps.custommaps.util.MapFont.measureText(text);
                if (textWidth > 126) {
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<red>Text is too long! It requires " + textWidth
                                    + " pixels, but only 126 are available."));
                    return;
                }

                int startX = 1;
                int startY = 1 + (line - 1) * 7;

                uk.co.webdent.VanillaMaps.custommaps.util.MapFont.drawText(text, pixels, startX, startY, color);
                plugin.getMapDataStore().save(player, pixels);
                player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<green>Text written to line " + line + "!"));
                return;
            }
        }

        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                .deserialize("<gold>==== VanillaMaps Help ====</gold>"));
        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                .deserialize("<yellow>/map create [ratio]</yellow> <gray>- Creates a new custom map.</gray>"));
        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(
                "<yellow>/map edit [ratio]</yellow> <gray>- Edits the existing custom map you are holding.</gray>"));
        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                .deserialize("<yellow>/map copy</yellow> <gray>- Copies the custom map you are holding.</gray>"));
        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(
                "<yellow>/map paste</yellow> <gray>- Pastes the copied map to the map you are holding.</gray>"));
        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(
                "<yellow>/map write \"<text>\" <line> [color]</yellow> <gray>- Writes text to a map.</gray>"));
        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                .deserialize("<yellow>/map publish</yellow> <gray>- Locks a map from being edited.</gray>"));
        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                .deserialize("<yellow>/map help</yellow> <gray>- Displays this help message.</gray>"));
    }

    @Override
    public Collection<String> suggest(@NotNull CommandSourceStack source, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("copy", "paste", "create", "edit", "write", "publish", "help");
        }

        if (args.length > 1 && args[0].equalsIgnoreCase("write")) {
            int idx = 1;
            boolean inQuotes = false;

            if (idx < args.length - 1 && args[idx].startsWith("\"")) {
                inQuotes = true;
                if (args[idx].endsWith("\"") && args[idx].length() > 1) {
                    inQuotes = false;
                }
                idx++;
                while (inQuotes && idx < args.length - 1) {
                    if (args[idx].endsWith("\"")) {
                        inQuotes = false;
                    }
                    idx++;
                }
            } else {
                if (idx < args.length - 1)
                    idx++;
            }

            if (args.length - 1 == idx) {
                String current = args[args.length - 1];
                return List
                        .of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
                                "18")
                        .stream().filter(s -> s.startsWith(current)).toList();
            }

            idx++;

            if (args.length - 1 == idx) {
                List<String> colors = List.of(
                        "black", "blue", "brown", "cyan", "gray", "green", "light_blue",
                        "light_gray", "lime", "magenta", "orange", "pink", "purple", "red", "white", "yellow");
                String current = args[args.length - 1].toLowerCase();
                return colors.stream().filter(c -> c.startsWith(current)).toList();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public @Nullable String permission() {
        return "custommaps.use";
    }
}
