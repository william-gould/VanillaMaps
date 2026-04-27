package uk.co.webdent.vanillamaps.feature;

import uk.co.webdent.vanillamaps.api.*;
import uk.co.webdent.vanillamaps.feature.custommaps.CustomMapsModule;
import java.io.File;
import java.util.logging.Logger;

public class VanillaMaps {

    private final VanillaMapsConfig config;
    private final ICommandRegistrar commands;
    private final IGuiPlatform gui;
    private final IMapPlatform maps;
    private final File dataDir;
    private final Logger logger;
    private final java.util.function.BiConsumer<java.util.UUID, String> messageSender;

    private CustomMapsModule customMapsModule;

    public VanillaMaps(
            VanillaMapsConfig config,
            ICommandRegistrar commands,
            IGuiPlatform gui,
            IMapPlatform maps,
            File dataDir,
            Logger logger,
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
        logger.info("Loading Vanilla Maps Modules...");

        try {
            customMapsModule = new CustomMapsModule(config, commands, gui, maps, dataDir, logger, messageSender);
            customMapsModule.init();
            logger.info("Loaded Custom Maps module.");
        } catch (Exception e) {
            logger.severe("Failed to load Custom Maps: " + e.getMessage());
        }

        logger.info("Vanilla Maps Modules loaded!");
    }

    public void shutdown() {
        if (customMapsModule != null)
            customMapsModule.shutdown();
        logger.info("Vanilla Maps disabled.");
    }

    public CustomMapsModule getCustomMapsModule() {
        return customMapsModule;
    }
}
