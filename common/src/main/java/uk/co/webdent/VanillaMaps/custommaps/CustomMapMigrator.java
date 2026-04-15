package uk.co.webdent.VanillaMaps.custommaps;

import uk.co.webdent.VanillaMaps.custommaps.map.MapDataStore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class CustomMapMigrator implements Listener {

    private final MapDataStore dataStore;

    public CustomMapMigrator(MapDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // When a chunk loads, scan its entities for Item Frames
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof ItemFrame frame) {
                if (frame.getItem().getType().name().contains("MAP")) {
                    // Attempt to migrate the map if it holds legacy data
                    dataStore.migrateItem(frame.getItem());
                    // Force a local update to the entity to ensure the meta wipe is saved
                    frame.setItem(frame.getItem());
                }
            }
        }
    }
}
