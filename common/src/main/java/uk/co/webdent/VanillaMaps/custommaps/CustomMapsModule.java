package uk.co.webdent.VanillaMaps.custommaps;

import uk.co.webdent.VanillaMaps.custommaps.command.MapCommand;
import uk.co.webdent.VanillaMaps.custommaps.gui.GuiListener;
import uk.co.webdent.VanillaMaps.custommaps.map.MapDataStore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomMapsModule implements Listener {

    private static CustomMapsModule instance;
    private final JavaPlugin plugin;
    private MapDataStore mapDataStore;

    public CustomMapsModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        instance = this;
        plugin.saveDefaultConfig();

        mapDataStore = new MapDataStore(this);
        plugin.getLifecycleManager()
                .registerEventHandler(io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents.COMMANDS, event -> {
                    event.registrar().register("map", "Manage custom maps", new MapCommand(this));
                });
        plugin.getServer().getPluginManager().registerEvents(new GuiListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        mapDataStore.reloadAllRenderers();
    }

    public void shutdown() {
        // Plugin shutdown logic
    }

    public static CustomMapsModule getInstance() {
        return instance;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public MapDataStore getMapDataStore() {
        return mapDataStore;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Automatically migrate any legacy maps stored via ItemStack PDC when a player
        // logs in
        mapDataStore.migrateInventory(event.getPlayer());
    }
}
