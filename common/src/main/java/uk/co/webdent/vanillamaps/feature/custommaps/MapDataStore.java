package uk.co.webdent.vanillamaps.feature.custommaps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapDataStore {

    private final File dataDir;

    public MapDataStore(File dataDir) {
        this.dataDir = dataDir;
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    public void save(int mapId, byte[] pixels) {
        File file = new File(dataDir, mapId + ".dat");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(pixels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] loadPixels(int mapId) {
        File file = new File(dataDir, mapId + ".dat");
        if (!file.exists()) return null;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buf = new byte[128 * 128];
            int read = fis.read(buf);
            if (read == buf.length) {
                return buf;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isPublished(int mapId) {
        File lockFile = new File(dataDir, mapId + ".lock");
        return lockFile.exists();
    }

    public void publish(int mapId) {
        File lockFile = new File(dataDir, mapId + ".lock");
        try {
            lockFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getAllMapIds() {
        List<Integer> ids = new ArrayList<>();
        File[] files = dataDir.listFiles((d, name) -> name.endsWith(".dat"));
        if (files != null) {
            for (File file : files) {
                try {
                    String name = file.getName();
                    int id = Integer.parseInt(name.substring(0, name.length() - 4));
                    ids.add(id);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return ids;
    }
}
