package uk.co.webdent.vanillamaps.fabric;

import uk.co.webdent.vanillamaps.api.VanillaMapsConfig;
import uk.co.webdent.vanillamaps.feature.VanillaMaps;
import uk.co.webdent.vanillamaps.fabric.platform.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandSourceStack;

import java.io.File;
import java.util.logging.Logger;

public class VanillaMapsMod implements ModInitializer {

    private static VanillaMapsMod instance;
    private MinecraftServer server;
    private VanillaMaps core;

    private FabricCommandRegistrar commands;

    public static VanillaMapsMod getInstance() {
        return instance;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public VanillaMaps getCore() {
        return core;
    }

    @Override
    public void onInitialize() {
        instance = this;

        VanillaMapsConfig config = new VanillaMapsConfig();
        config.defaultBackground = "WHITE";

        commands = new FabricCommandRegistrar();
        FabricGuiPlatform gui = new FabricGuiPlatform();
        FabricMapPlatform maps = new FabricMapPlatform();

        java.util.function.BiConsumer<java.util.UUID, String> messageSender = (uuid, msg) -> {
            MinecraftServer srv = VanillaMapsMod.getInstance().getServer();
            if (srv != null) {
                net.minecraft.server.level.ServerPlayer p = srv.getPlayerList().getPlayer(uuid);
                if (p != null)
                    p.sendSystemMessage(Component.literal(msg));
            }
        };

        File dataDir = new File(FabricLoader.getInstance().getGameDir().toFile(), "VanillaMaps_maps");
        if (!dataDir.exists())
            dataDir.mkdirs();

        Logger logger = Logger.getLogger("VanillaMaps");
        core = new VanillaMaps(config, commands, gui, maps, dataDir, logger, messageSender);
        core.init();

        commands.registerAll(core.getCustomMapsModule().getMapCommandLogic());

        ServerLifecycleEvents.SERVER_STARTING.register(s -> {
            this.server = s;
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
            if (core != null)
                core.shutdown();
        });
    }
}