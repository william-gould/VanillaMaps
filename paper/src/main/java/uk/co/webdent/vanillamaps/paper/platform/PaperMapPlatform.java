package uk.co.webdent.vanillamaps.paper.platform;

import uk.co.webdent.vanillamaps.api.IMapPlatform;
import uk.co.webdent.vanillamaps.paper.custommaps.map.LazyRenderer;
import uk.co.webdent.vanillamaps.paper.custommaps.map.PaperMapRenderer;
import org.bukkit.Bukkit;
import org.bukkit.map.MapView;

import java.io.File;

public class PaperMapPlatform implements IMapPlatform {

    @Override
    public void applyRenderer(int mapId, byte[] pixels) {
        MapView view = Bukkit.getMap(mapId);
        if (view != null) {
            view.getRenderers().forEach(view::removeRenderer);
            view.addRenderer(new PaperMapRenderer(pixels));
        }
    }

    @Override
    public void reloadAllRenderers(File dataDir) {
        if (!dataDir.exists()) return;
        File[] files = dataDir.listFiles((d, name) -> name.endsWith(".dat"));
        if (files == null) return;

        for (File file : files) {
            try {
                int id = Integer.parseInt(file.getName().replace(".dat", ""));
                MapView view = Bukkit.getMap(id);
                if (view != null) {
                    view.getRenderers().forEach(view::removeRenderer);
                    view.addRenderer(new LazyRenderer(file));   
                }
            } catch (NumberFormatException ignored) {}
        }
    }
}
