package uk.co.webdent.VanillaMaps.custommaps.map;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

public class CustomMapRenderer extends MapRenderer {

    private final byte[] pixels;
    private boolean rendered = false;

    public CustomMapRenderer(byte[] pixels) {
        super(false); // isContextual = false jic
        this.pixels = pixels;
    }

    @Override
    public void render(@NotNull MapView view, @NotNull MapCanvas canvas, @NotNull Player player) {
        if (rendered)
            return;

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                canvas.setPixel(x, y, pixels[y * 128 + x]);
            }
        }
        rendered = true;
    }
}
