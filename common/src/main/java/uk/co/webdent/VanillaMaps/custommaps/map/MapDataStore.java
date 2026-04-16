package uk.co.webdent.VanillaMaps.custommaps.map;

import uk.co.webdent.VanillaMaps.custommaps.CustomMapsModule;
import uk.co.webdent.VanillaMaps.custommaps.gui.DrawingGui;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.logging.Level;

public class MapDataStore {

    private final CustomMapsModule plugin;
    private final File dataDir;
    private final NamespacedKey legacyKey;

    public MapDataStore(CustomMapsModule plugin) {
        this.plugin = plugin;
        this.legacyKey = new NamespacedKey(plugin.getPlugin(), "map_pixels");
        this.dataDir = new File(plugin.getPlugin().getDataFolder(), "maps");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    public void save(Player player, DrawingGui gui) {
        save(player, gui.getPixelData());
    }

    public void save(Player player, byte[] pixels) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == org.bukkit.Material.MAP) {
            String msg = plugin.getPlugin().getConfig().getString("messages.must-fill-map",
                    "<red>You must right-click to fill the map first before saving.");
            player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(msg));
            return;
        }

        if (held.getType().name().contains("MAP")) {
            if (!(held.getItemMeta() instanceof MapMeta meta))
                return;

            MapView view = meta.getMapView();
            if (view == null) {
                view = Bukkit.createMap(player.getWorld());
                meta.setMapView(view);
                held.setItemMeta(meta);
            }

            int mapId = view.getId();

            if (isPublished(mapId)) {
                player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<red>This map has been published and cannot be overwritten."));
                return;
            }

            // Store globally
            File mapFile = new File(dataDir, mapId + ".dat");
            try {
                Files.write(mapFile.toPath(), pixels);
            } catch (IOException e) {
                plugin.getPlugin().getLogger().log(Level.SEVERE, "Failed to save map data for ID: " + mapId, e);
                return;
            }

            // Clean up legacy PDC on the held item in case they are saving over an old map
            if (meta.getPersistentDataContainer().has(legacyKey, PersistentDataType.STRING)) {
                meta.getPersistentDataContainer().remove(legacyKey);
                held.setItemMeta(meta);
            }

            view.getRenderers().forEach(view::removeRenderer);
            CustomMapRenderer renderer = new CustomMapRenderer(pixels);
            view.addRenderer(renderer);

            String saveMsg = plugin.getPlugin().getConfig().getString("messages.save-success",
                    "<green>Map saved successfully!");
            player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(saveMsg));
        }
    }

    public byte[] loadPixels(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !(item.getItemMeta() instanceof MapMeta meta))
            return null;

        MapView view = meta.getMapView();
        if (view == null)
            return null;

        File mapFile = new File(dataDir, view.getId() + ".dat");
        if (!mapFile.exists()) {
            // Check if it's a legacy map dynamically (in case of copies from chests) but
            // don't migrate here, just read
            if (meta.getPersistentDataContainer().has(legacyKey, PersistentDataType.STRING)) {
                try {
                    return Base64.getDecoder()
                            .decode(meta.getPersistentDataContainer().get(legacyKey, PersistentDataType.STRING));
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
            return null;
        }

        try {
            return Files.readAllBytes(mapFile.toPath());
        } catch (IOException e) {
            plugin.getPlugin().getLogger().log(Level.SEVERE, "Failed to read map data for ID: " + view.getId(), e);
            return null;
        }
    }

    public void reloadAllRenderers() {
        if (!dataDir.exists())
            return;

        File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".dat"));
        if (files == null)
            return;

        for (File file : files) {
            try {
                String name = file.getName();
                int mapId = Integer.parseInt(name.substring(0, name.length() - 4));
                byte[] pixels = Files.readAllBytes(file.toPath());

                @SuppressWarnings("deprecation")
                MapView view = Bukkit.getMap(mapId);
                if (view != null) {
                    boolean hasRenderer = view.getRenderers().stream().anyMatch(r -> r instanceof CustomMapRenderer);
                    if (!hasRenderer) {
                        view.getRenderers().forEach(view::removeRenderer);
                        view.addRenderer(new CustomMapRenderer(pixels));
                    }
                }
            } catch (Exception e) {
                plugin.getPlugin().getLogger().log(Level.SEVERE, "Failed to load map data from file: " + file.getName(),
                        e);
            }
        }
    }

    public void migrateItem(ItemStack item) {
        if (item != null && item.getType().name().contains("MAP") && item.hasItemMeta()) {
            if (item.getItemMeta() instanceof MapMeta meta) {
                if (meta.getPersistentDataContainer().has(legacyKey, PersistentDataType.STRING)) {
                    MapView view = meta.getMapView();
                    if (view != null) {
                        String encoded = meta.getPersistentDataContainer().get(legacyKey, PersistentDataType.STRING);
                        if (encoded != null) {
                            try {
                                byte[] pixels = Base64.getDecoder().decode(encoded);

                                // Make sure it doesn't already overwrite a newer global save
                                File mapFile = new File(dataDir, view.getId() + ".dat");
                                if (!mapFile.exists()) {
                                    Files.write(mapFile.toPath(), pixels);

                                    // Globally register it
                                    boolean hasRenderer = view.getRenderers().stream()
                                            .anyMatch(r -> r instanceof CustomMapRenderer);
                                    if (!hasRenderer) {
                                        view.getRenderers().forEach(view::removeRenderer);
                                        view.addRenderer(new CustomMapRenderer(pixels));
                                    }
                                    plugin.getPlugin().getLogger()
                                            .info("Migrated legacy map ID " + view.getId() + " to global file config.");
                                }
                            } catch (Exception e) {
                                plugin.getPlugin().getLogger().log(Level.WARNING, "Failed to migrate map item", e);
                            }
                        }
                    }

                    // Strip legacy key to ensure no future conflicts
                    meta.getPersistentDataContainer().remove(legacyKey);
                    item.setItemMeta(meta);
                }
            }
        }
    }

    public void migrateInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            migrateItem(item);
        }
    }

    public boolean isPublished(int mapId) {
        return new File(dataDir, mapId + ".lock").exists();
    }

    public void publish(int mapId) throws IOException {
        File lockFile = new File(dataDir, mapId + ".lock");
        lockFile.createNewFile();
    }
}
