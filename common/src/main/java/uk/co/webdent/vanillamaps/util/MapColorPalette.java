package uk.co.webdent.vanillamaps.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapColorPalette {

    private static final Map<String, Byte> WOOL_TO_COLOR = new HashMap<>();
    private static final Map<Byte, String> COLOR_TO_WOOL = new HashMap<>();
    public static final List<String> ALL_COLOR_NAMES = new ArrayList<>();

    
    private static final int[] BASE_COLORS = {
            0x000000, 0x7FB238, 0xF7E9A3, 0xC7C7C7, 0xFF0000, 0xA0A0FF, 0xA7A7A7, 0x007C00,
            0xFFFFFF, 0xA4A8B8, 0x976D4D, 0x707070, 0x4040FF, 0x8F7748, 0xFFFCF5, 0xD87F33,
            0xB24CD8, 0x6699D8, 0xE5E533, 0x7FCC19, 0xF27FA5, 0x4C4C4C, 0x999999, 0x4C7F99,
            0x7F3FB2, 0x334CB2, 0x664C33, 0x667F33, 0x993333, 0x191919, 0xFAEE4D, 0x5CDBD5,
            0x4A80FF, 0x00D93A, 0x15141F, 0x700200, 0x815631, 0x5F042A, 0xAC3232, 0x11101E,
            0xEDC39A, 0x411120, 0x13121E, 0x8D3B2E, 0x351F0E, 0x664831, 0x5F321F, 0x241107,
            0x1B2117, 0x1F112F, 0x331C0E, 0x4D3626, 0x4A2511, 0x1A0E06, 0x141812, 0x180D21,
            0x5CA77E, 0x13A086, 0x309C6B, 0x127154, 0x942250, 0x5C1931
    };

    private static void register(String woolName, byte color) {
        WOOL_TO_COLOR.put(woolName, color);
        COLOR_TO_WOOL.put(color, woolName);
        ALL_COLOR_NAMES.add(woolName);
    }

    static {
        register("white_wool", matchColor(255, 255, 255));
        register("black_wool", matchColor(0, 0, 0));
        register("red_wool", matchColor(176, 46, 38));
        register("orange_wool", matchColor(249, 128, 29));
        register("yellow_wool", matchColor(254, 216, 61));
        register("lime_wool", matchColor(128, 199, 31));
        register("cyan_wool", matchColor(22, 156, 156));
        register("blue_wool", matchColor(60, 68, 170));
        register("brown_wool", matchColor(131, 84, 50));
        register("gray_wool", matchColor(71, 79, 82));
        register("green_wool", matchColor(94, 124, 22));
        register("light_blue_wool", matchColor(58, 179, 218));
        register("light_gray_wool", matchColor(142, 142, 134));
        register("magenta_wool", matchColor(199, 78, 189));
        register("pink_wool", matchColor(243, 140, 170));
        register("purple_wool", matchColor(137, 50, 184));
    }

    public static byte matchColor(int r, int g, int b) {
        double bestDist = Double.MAX_VALUE;
        byte bestId = 0;

        for (int i = 0; i < BASE_COLORS.length; i++) {
            if (i == 0) continue; 

            int baseRGB = BASE_COLORS[i];
            int br = (baseRGB >> 16) & 0xFF;
            int bg = (baseRGB >> 8) & 0xFF;
            int bb = baseRGB & 0xFF;

            
            int[] multipliers = { 180, 220, 255, 135 };
            for (int shade = 0; shade < 4; shade++) {
                int mr = (br * multipliers[shade]) / 255;
                int mg = (bg * multipliers[shade]) / 255;
                int mb = (bb * multipliers[shade]) / 255;

                double dist = (mr - r) * (mr - r) + (mg - g) * (mg - g) + (mb - b) * (mb - b);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestId = (byte) (i * 4 + shade);
                }
            }
        }
        return bestId;
    }

    public static byte getMapColor(String woolName) {
        String key = woolName.toLowerCase().replace("minecraft:", "");
        return WOOL_TO_COLOR.getOrDefault(key, matchColor(255, 255, 255));
    }

    public static String getWoolName(byte color) {
        return COLOR_TO_WOOL.getOrDefault(color, "white_wool");
    }
}
