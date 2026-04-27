package uk.co.webdent.vanillamaps.feature.custommaps;

import uk.co.webdent.vanillamaps.api.VanillaMapsConfig;
import uk.co.webdent.vanillamaps.api.IGuiPlatform;
import uk.co.webdent.vanillamaps.api.IMapPlatform;
import uk.co.webdent.vanillamaps.util.MapColorPalette;
import uk.co.webdent.vanillamaps.util.MapFont;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MapCommandLogic {

    private final MapDataStore mapDataStore;
    private final IGuiPlatform guiPlatform;
    private final IMapPlatform mapPlatform;
    private final VanillaMapsConfig config;
    private final Map<UUID, byte[]> clipboard = new HashMap<>();
    private final java.util.function.BiConsumer<java.util.UUID, String> messageSender;

    public MapCommandLogic(MapDataStore mapDataStore, IGuiPlatform guiPlatform, IMapPlatform mapPlatform,
            VanillaMapsConfig config, java.util.function.BiConsumer<java.util.UUID, String> messageSender) {
        this.mapDataStore = mapDataStore;
        this.guiPlatform = guiPlatform;
        this.mapPlatform = mapPlatform;
        this.config = config;
        this.messageSender = messageSender;
    }

    private void msg(UUID playerUuid, String text) {
        if (messageSender != null)
            messageSender.accept(playerUuid, text);
    }

    public void onCopy(UUID playerUuid, int mapId) {
        if (mapId == -1) {
            msg(playerUuid, "You must hold a filled map.");
            return;
        }
        byte[] pixels = mapDataStore.loadPixels(mapId);
        if (pixels != null) {
            byte[] copy = new byte[128 * 128];
            System.arraycopy(pixels, 0, copy, 0, copy.length);
            clipboard.put(playerUuid, copy);
            msg(playerUuid, "Map copied to clipboard!");
        } else {
            msg(playerUuid, "This map has no custom pixel data to copy.");
        }
    }

    public void onPaste(UUID playerUuid, int mapId) {
        if (mapId == -1) {
            msg(playerUuid, "You must hold a filled map.");
            return;
        }
        if (mapDataStore.isPublished(mapId)) {
            msg(playerUuid, "This map is published and cannot be edited.");
            return;
        }
        byte[] pixels = clipboard.get(playerUuid);
        if (pixels == null) {
            msg(playerUuid, "Your clipboard is empty. Use /map copy first.");
            return;
        }
        mapDataStore.save(mapId, pixels);
        mapPlatform.applyRenderer(mapId, pixels);
        msg(playerUuid, "Map pasted!");
    }

    public void onCreate(UUID playerUuid, int mapId, int ratio) {
        if (mapId == -1) {
            msg(playerUuid, "You must hold a filled map.");
            return;
        }
        if (mapDataStore.isPublished(mapId)) {
            msg(playerUuid, "This map is published and cannot be edited.");
            return;
        }
        if (128 % ratio != 0) {
            msg(playerUuid, "Ratio must divide 128 evenly (e.g. 1, 2, 4, 8, 16).");
            return;
        }
        byte bg = MapColorPalette.getMapColor(config.defaultBackground.toLowerCase() + "_wool");
        DrawingSession session = new DrawingSession(playerUuid, ratio, bg, null);
        guiPlatform.openDrawingGui(playerUuid, session);
    }

    public void onEdit(UUID playerUuid, int mapId, int ratio) {
        if (mapId == -1) {
            msg(playerUuid, "You must hold a filled map.");
            return;
        }
        if (mapDataStore.isPublished(mapId)) {
            msg(playerUuid, "This map is published and cannot be edited.");
            return;
        }
        if (128 % ratio != 0) {
            msg(playerUuid, "Ratio must divide 128 evenly (e.g. 1, 2, 4, 8, 16).");
            return;
        }
        byte[] existing = mapDataStore.loadPixels(mapId);
        if (existing == null) {
            msg(playerUuid, "This map has no saved data. Use /map create instead.");
            return;
        }
        byte bg = MapColorPalette.getMapColor(config.defaultBackground.toLowerCase() + "_wool");
        DrawingSession session = new DrawingSession(playerUuid, ratio, bg, existing);
        guiPlatform.openDrawingGui(playerUuid, session);
    }

    public void onWrite(UUID playerUuid, int mapId, String text, int line, byte color, String align) {
        if (mapId == -1) {
            msg(playerUuid, "You must hold a filled map.");
            return;
        }
        if (mapDataStore.isPublished(mapId)) {
            msg(playerUuid, "This map is published and cannot be edited.");
            return;
        }
        byte[] existing = mapDataStore.loadPixels(mapId);
        if (existing == null) {
            byte bg = MapColorPalette.getMapColor(config.defaultBackground.toLowerCase() + "_wool");
            existing = new byte[128 * 128];
            for (int i = 0; i < existing.length; i++)
                existing[i] = bg;
        }

        final int PADDING = 1;
        final int FONT_HEIGHT = 4;
        final int LINE_SPACING = 3;

        int usableWidth = 128 - (PADDING * 2);
        int width = MapFont.measureText(text);

        if (width > usableWidth) {
            msg(playerUuid, "Text is too long to fit on the map.");
            return;
        }

        int x = 0;
        int y = 0;

        if (align.equalsIgnoreCase("c")) {
            x = PADDING + (usableWidth - width) / 2;
        } else if (align.equalsIgnoreCase("r")) {
            x = PADDING + (usableWidth - width);
        } else if (align.equalsIgnoreCase("l")) {
            x = PADDING;
        } else {
            msg(playerUuid, "Invalid alignment option. Use '[L]eft', '[C]entre', or '[R]ight'.");
            return;
        }

        y = PADDING + line * (FONT_HEIGHT + LINE_SPACING);

        if (line > 17) {
            msg(playerUuid, "Text is too long to fit on the map.");
            return;
        }
        MapFont.drawText(text, existing, x, y, color);
        mapDataStore.save(mapId, existing);
        mapPlatform.applyRenderer(mapId, existing);
        msg(playerUuid, "Text written to line " + line + "!");
    }

    public void onPublish(UUID playerUuid, int mapId) {
        if (mapId == -1) {
            msg(playerUuid, "You must hold a filled map.");
            return;
        }
        if (mapDataStore.isPublished(mapId)) {
            msg(playerUuid, "This map is already published.");
            return;
        }
        if (mapDataStore.loadPixels(mapId) == null) {
            msg(playerUuid, "This map has no custom data to publish.");
            return;
        }
        mapDataStore.publish(mapId);
        msg(playerUuid, "Map published! It can no longer be edited.");
    }

    public void onSave(UUID playerUuid, int mapId, DrawingSession session) {
        if (mapId == -1) {
            msg(playerUuid, "You must hold a filled map to save.");
            return;
        }
        if (mapDataStore.isPublished(mapId)) {
            msg(playerUuid, "This map is published and cannot be overwritten.");
            return;
        }
        mapDataStore.save(mapId, session.getPixelData());
        mapPlatform.applyRenderer(mapId, session.getPixelData());
        msg(playerUuid, "Map saved!");
    }

    public void clearClipboard(UUID playerUuid) {
        clipboard.remove(playerUuid);
    }

    public void onHelp(UUID playerUuid) {
        msg(playerUuid, "§6§lMap Commands:");
        msg(playerUuid, "§e/map create <ratio> §7- Create a new map with the given ratio");
        msg(playerUuid, "§e/map edit <ratio> §7- Edit an existing map with the given ratio");
        msg(playerUuid, "§e/map copy §7- Copy the map you are holding");
        msg(playerUuid, "§e/map paste §7- Paste the copied map");
        msg(playerUuid, "§e/map write <text> <line> <alignment> <color> §7- Write text to the map");
        msg(playerUuid, "§e/map publish §7- Publish the map");
        msg(playerUuid, "§e/map save §7- Save the map");
        msg(playerUuid, "§e/map help §7- Show this help message");
        return;
    }

    public List<String> suggest(UUID playerUuid, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("create");
            list.add("edit");
            list.add("copy");
            list.add("paste");
            list.add("write");
            list.add("publish");
            list.add("help");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("edit"))) {
            list.add("1");
            list.add("2");
            list.add("4");
            list.add("8");
            list.add("16");
        } else if (args.length == 4 && args[0].equalsIgnoreCase("write")) {
            list.add("l");
            list.add("c");
            list.add("r");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("write")) {
            for (String c : MapColorPalette.ALL_COLOR_NAMES) {
                list.add(c.replace("_wool", ""));
            }
        }
        return list;
    }
}
