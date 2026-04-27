package uk.co.webdent.vanillamaps.fabric.platform;

import uk.co.webdent.vanillamaps.api.IMapPlatform;
import uk.co.webdent.vanillamaps.fabric.VanillaMapsMod;
import uk.co.webdent.vanillamaps.fabric.mixin.MapItemSavedDataAccessor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;

public class FabricMapPlatform implements IMapPlatform {

    public static final ConcurrentHashMap<Integer, File> PENDING_FILES = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Integer, byte[]> PENDING_PIXELS = new ConcurrentHashMap<>();

    @Override
    public void applyRenderer(int mapId, byte[] pixels) {
        if (mapId < 0 || pixels == null)
            return;

        var server = VanillaMapsMod.getInstance().getServer();
        if (server == null) {
            
            PENDING_PIXELS.put(mapId, pixels.clone());
            return;
        }

        MapItemSavedData state = server.overworld().getMapData(new MapId(mapId));
        if (state != null) {
            System.arraycopy(pixels, 0, state.colors, 0,
                    Math.min(pixels.length, state.colors.length));
            MapItemSavedDataAccessor accessor = (MapItemSavedDataAccessor) state;
            
            
            accessor.setLocked(true);
            
            
            accessor.setTrackingPosition(false);
            
            
            accessor.getDecorations().clear();
            
            
            accessor.invokeSetColorsDirty(0, 0);
            accessor.invokeSetColorsDirty(127, 127);
            
            
            state.setDirty();
        } else {
            
            PENDING_PIXELS.put(mapId, pixels.clone());
        }
    }

    @Override
    public void reloadAllRenderers(File dataDir) {
        if (!dataDir.exists())
            return;
        File[] files = dataDir.listFiles((d, name) -> name.endsWith(".dat"));
        if (files == null)
            return;

        for (File file : files) {
            try {
                int id = Integer.parseInt(file.getName().replace(".dat", ""));
                
                PENDING_FILES.put(id, file);
            } catch (NumberFormatException ignored) {
            }
        }
    }
}
