package uk.co.webdent.VanillaMaps.custommaps.map;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

public class CustomMapRenderer extends MapRenderer {

    private final byte[] pixels;

    public CustomMapRenderer(byte[] pixels) {
        super(false); // isContextual = false
        this.pixels = pixels;
    }

    @Override
    public void render(@NotNull MapView view, @NotNull MapCanvas canvas, @NotNull Player player) {
        for (int i = 0; i < pixels.length; i++) {
            int x = i % 128;
            int y = i / 128;
            canvas.setPixel(x, y, pixels[i]);
        }
    }
}
