package uk.co.webdent.VanillaMaps.custommaps.gui.generator;

import uk.co.webdent.VanillaMaps.custommaps.gui.DrawingGui;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class BackgroundGenerator {

    public enum BackgroundType {
        NOISE,
        CHECKERBOARD,
        BRICKS,
        TILES
    }

    private static final Map<String, boolean[][]> PATTERN_REGISTRY = new HashMap<>();

    static {
        registerPattern("CHECKERBOARD", new String[] {
                "#.",
                ".#"
        });
        registerPattern("BRICKS", new String[] {
                "####",
                "#...",
                "####",
                "...#"
        });
        registerPattern("TILES", new String[] {
                "###.",
                "###.",
                "###.",
                "...."
        });
    }

    private static void registerPattern(String name, String[] rows) {
        int height = rows.length;
        int width = rows[0].length();
        boolean[][] pattern = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pattern[y][x] = rows[y].charAt(x) == '#';
            }
        }
        PATTERN_REGISTRY.put(name, pattern);
    }

    public static void apply(DrawingGui gui, BackgroundType type) {
        byte[] pixels = gui.getPixelData();
        byte active = gui.getActiveColor();
        byte bg = gui.getBackgroundColor();
        int ratio = gui.getRatio();
        int gridDim = 128 / ratio;

        for (int by = 0; by < gridDim; by++) {
            for (int bx = 0; bx < gridDim; bx++) {
                byte color = getColor(type, bx, by, gridDim, active, bg);
                
                // Fill the ratio-scaled block
                for (int sy = 0; sy < ratio; sy++) {
                    for (int sx = 0; sx < ratio; sx++) {
                        int px = bx * ratio + sx;
                        int py = by * ratio + sy;
                        pixels[py * 128 + px] = color;
                    }
                }
            }
        }
    }

    private static byte getColor(BackgroundType type, int bx, int by, int gridDim, byte active, byte bg) {
        switch (type) {
            case NOISE:
                return ThreadLocalRandom.current().nextBoolean() ? active : bg;

            case CHECKERBOARD:
                return getPatternColor("CHECKERBOARD", bx, by, active, bg);

            case BRICKS:
                return getPatternColor("BRICKS", bx, by, active, bg);

            case TILES:
                return getPatternColor("TILES", bx, by, active, bg);

            default:
                return bg;
        }
    }

    private static byte getPatternColor(String name, int bx, int by, byte active, byte bg) {
        boolean[][] pattern = PATTERN_REGISTRY.get(name);
        if (pattern == null) return bg;
        
        int py = by % pattern.length;
        int px = bx % pattern[0].length;
        return pattern[py][px] ? active : bg;
    }
}
