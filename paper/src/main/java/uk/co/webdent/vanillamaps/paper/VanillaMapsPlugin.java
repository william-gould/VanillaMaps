package uk.co.webdent.vanillamaps.paper;

import uk.co.webdent.vanillamaps.api.VanillaMapsConfig;
import uk.co.webdent.vanillamaps.feature.VanillaMaps;
import uk.co.webdent.vanillamaps.feature.custommaps.MapCommandLogic;
import uk.co.webdent.vanillamaps.paper.platform.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.io.File;

public class VanillaMapsPlugin extends JavaPlugin {
    private VanillaMaps core;

    @Override
    public void onEnable() {
        saveDefaultConfig(); 
        
        VanillaMapsConfig config = new VanillaMapsConfig(); 
        config.defaultBackground = getConfig().getString("default-background", "WHITE");
        
        PaperCommandRegistrar commands = new PaperCommandRegistrar(this);
        PaperGuiPlatform gui = new PaperGuiPlatform();
        PaperMapPlatform maps = new PaperMapPlatform();
        
        java.util.function.BiConsumer<java.util.UUID, String> messageSender = (uuid, msg) -> {
            org.bukkit.entity.Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(msg);
        };
        
        File dataDir = new File(getDataFolder(), "maps");
        
        core = new VanillaMaps(config, commands, gui, maps, dataDir, getLogger(), messageSender);
        core.init();
        
        MapCommandLogic mapLogic = core.getCustomMapsModule().getMapCommandLogic();
        commands.register("map", "Map command", null, (uuid, name, isOp, args) -> {
            if (uuid == null) return;
            if (args.length == 0) return;
            
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) return;
            ItemStack item = p.getInventory().getItemInMainHand();
            int mapId = -1;
            if (item.getType() == Material.FILLED_MAP && item.getItemMeta() instanceof MapMeta meta) {
                if (meta.hasMapId()) mapId = meta.getMapId();
            }
            
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "copy" -> mapLogic.onCopy(uuid, mapId);
                case "paste" -> mapLogic.onPaste(uuid, mapId);
                case "create" -> mapLogic.onCreate(uuid, mapId, args.length > 1 ? Integer.parseInt(args[1]) : 1);
                case "edit" -> mapLogic.onEdit(uuid, mapId, args.length > 1 ? Integer.parseInt(args[1]) : 1);
                case "publish" -> mapLogic.onPublish(uuid, mapId);
                case "write" -> {
                    if (args.length >= 4) {
                        try {
                            int line = Integer.parseInt(args[1]);
                            byte col = uk.co.webdent.vanillamaps.util.MapColorPalette.getMapColor(args[2] + "_wool");
                            String align = args[3].toLowerCase();
                            StringBuilder t = new StringBuilder();
                            
                            for (int i=4; i<args.length; i++) t.append(args[i]).append(" ");
                            mapLogic.onWrite(uuid, mapId, t.toString().trim(), line, col, align);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }, (uuid, args) -> mapLogic.suggest(uuid, args));
        
        getServer().getPluginManager().registerEvents(new uk.co.webdent.vanillamaps.paper.custommaps.gui.GuiEventListener(mapLogic), this);
    }

    @Override
    public void onDisable() {
        if (core != null) core.shutdown();
    }
}
