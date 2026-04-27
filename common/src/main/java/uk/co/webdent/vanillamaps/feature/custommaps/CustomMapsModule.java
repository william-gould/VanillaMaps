package uk.co.webdent.vanillamaps.feature.custommaps;

import uk.co.webdent.vanillamaps.api.VanillaMapsConfig;
import uk.co.webdent.vanillamaps.api.ICommandRegistrar;
import uk.co.webdent.vanillamaps.api.IGuiPlatform;
import uk.co.webdent.vanillamaps.api.IMapPlatform;

import java.io.File;
import java.util.logging.Logger;

public class CustomMapsModule {

    private final VanillaMapsConfig config;
    private final ICommandRegistrar commands;
    private final IGuiPlatform gui;
    private final IMapPlatform maps;
    private final File dataDir;
    private final Logger logger;
    private final java.util.function.BiConsumer<java.util.UUID, String> messageSender;
    
    private MapDataStore mapDataStore;
    private MapCommandLogic mapCommandLogic;

    public CustomMapsModule(VanillaMapsConfig config, ICommandRegistrar commands, IGuiPlatform gui,
                            IMapPlatform maps, File dataDir, Logger logger,
                            java.util.function.BiConsumer<java.util.UUID, String> messageSender) {
        this.config = config;
        this.commands = commands;
        this.gui = gui;
        this.maps = maps;
        this.dataDir = dataDir;
        this.logger = logger;
        this.messageSender = messageSender;
    }

    public void init() {
        this.mapDataStore = new MapDataStore(dataDir);
        this.mapCommandLogic = new MapCommandLogic(mapDataStore, gui, maps, config, messageSender);
        
        maps.reloadAllRenderers(dataDir);
    }

    public void shutdown() {
    }

    public MapDataStore getMapDataStore() {
        return mapDataStore;
    }

    public MapCommandLogic getMapCommandLogic() {
        return mapCommandLogic;
    }
}
