package uk.co.webdent.VanillaMaps.custommaps.map;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LazyRenderer extends MapRenderer {

    private final File dataFile;
    private byte[] pixels = null;
    private boolean drawn = false;

    public LazyRenderer(File dataFile) {
        super(false);
        this.dataFile = dataFile;
    }

    @Override
    public void render(@NotNull MapView view, @NotNull MapCanvas canvas, @NotNull Player player) {
        if (drawn)
            return;

        if (pixels == null) {
            try {
                pixels = Files.readAllBytes(dataFile.toPath());
            } catch (IOException e) {
                Logger.getLogger("CustomMaps").log(Level.SEVERE,
                        "Failed to lazy-load map data from: " + dataFile.getName(), e);
                return;
            }
        }

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                canvas.setPixel(x, y, pixels[y * 128 + x]);
            }
        }
        drawn = true;
    }
}