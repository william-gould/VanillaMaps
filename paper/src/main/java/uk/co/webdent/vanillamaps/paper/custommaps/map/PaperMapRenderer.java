package uk.co.webdent.vanillamaps.paper.custommaps.map;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

public class PaperMapRenderer extends MapRenderer {

    private final byte[] pixels;
    private boolean rendered = false;

    public PaperMapRenderer(byte[] pixels) {
        super(false); 
        this.pixels = pixels;
    }

    @Override
    public void render(@NotNull MapView view, @NotNull MapCanvas canvas, @NotNull Player player) {
        if (rendered)
            return;
        for (int i = 0; i < pixels.length; i++) {
            int x = i % 128;
            int y = i / 128;
            canvas.setPixel(x, y, pixels[i]);
        }
        rendered = true;
    }
}
