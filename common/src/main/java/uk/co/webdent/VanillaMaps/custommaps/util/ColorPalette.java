package uk.co.webdent.VanillaMaps.custommaps.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapPalette;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorPalette {

    private static final Map<Material, Byte> MATERIAL_TO_COLOR = new HashMap<>();
    private static final Map<Byte, Material> COLOR_TO_MATERIAL = new HashMap<>();

    public static final List<Material> ALL_COLORS = new ArrayList<>();

    private static void register(Material mat, byte color) {
        MATERIAL_TO_COLOR.put(mat, color);
        COLOR_TO_MATERIAL.put(color, mat);
        ALL_COLORS.add(mat);
    }

    static {
        register(Material.WHITE_WOOL, MapPalette.matchColor(Color.WHITE));
        register(Material.BLACK_WOOL, MapPalette.matchColor(Color.BLACK));
        register(Material.RED_WOOL, MapPalette.matchColor(new Color(176, 46, 38)));
        register(Material.ORANGE_WOOL, MapPalette.matchColor(new Color(249, 128, 29)));
        register(Material.YELLOW_WOOL, MapPalette.matchColor(new Color(254, 216, 61)));
        register(Material.LIME_WOOL, MapPalette.matchColor(new Color(128, 199, 31)));
        register(Material.CYAN_WOOL, MapPalette.matchColor(new Color(22, 156, 156)));
        register(Material.BLUE_WOOL, MapPalette.matchColor(new Color(60, 68, 170)));
        register(Material.BROWN_WOOL, MapPalette.matchColor(new Color(131, 84, 50)));
        register(Material.GRAY_WOOL, MapPalette.matchColor(new Color(71, 79, 82)));
        register(Material.GREEN_WOOL, MapPalette.matchColor(new Color(94, 124, 22)));
        register(Material.LIGHT_BLUE_WOOL, MapPalette.matchColor(new Color(58, 179, 218)));
        register(Material.LIGHT_GRAY_WOOL, MapPalette.matchColor(new Color(142, 142, 134)));
        register(Material.MAGENTA_WOOL, MapPalette.matchColor(new Color(199, 78, 189)));
        register(Material.PINK_WOOL, MapPalette.matchColor(new Color(243, 140, 170)));
        register(Material.PURPLE_WOOL, MapPalette.matchColor(new Color(137, 50, 184)));
    }

    public static byte getMapColor(Material material) {
        return MATERIAL_TO_COLOR.getOrDefault(material, MapPalette.matchColor(Color.WHITE));
    }

    public static ItemStack getDisplayItem(byte color) {
        Material mat = COLOR_TO_MATERIAL.getOrDefault(color, Material.WHITE_WOOL);
        return new ItemStack(mat);
    }
}
