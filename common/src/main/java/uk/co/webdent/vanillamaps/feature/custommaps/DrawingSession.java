package uk.co.webdent.vanillamaps.feature.custommaps;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.UUID;

public class DrawingSession {

    public enum Tool {
        PENCIL, FILL, LINE, RECTANGLE, NOISE, CHECKERBOARD, BRICKS, TILES
    }

    private static final int MAX_UNDO = 10;

    private final UUID playerUuid;
    private final byte[] pixels;       
    private final int ratio;
    private final byte backgroundColor;

    private Tool activeTool = Tool.PENCIL;
    private byte activeColor;
    private int offsetX = 0, offsetY = 0;
    private int firstPointX = -1, firstPointY = -1;
    private final Deque<byte[]> undoStack = new ArrayDeque<>();

    public DrawingSession(UUID playerUuid, int ratio, byte backgroundColor, byte[] existingPixels) {
        this.playerUuid = playerUuid;
        this.ratio = ratio;
        this.backgroundColor = backgroundColor;
        this.activeColor = backgroundColor;
        this.pixels = new byte[128 * 128];
        if (existingPixels != null && existingPixels.length == 128 * 128)
            System.arraycopy(existingPixels, 0, this.pixels, 0, 128 * 128);
        else
            Arrays.fill(this.pixels, backgroundColor);
    }

    
    public UUID getPlayerUuid()    { return playerUuid; }
    public byte[] getPixelData()   { return pixels; }
    public int getRatio()          { return ratio; }
    public Tool getActiveTool()    { return activeTool; }
    public byte getActiveColor()   { return activeColor; }
    public byte getBackgroundColor() { return backgroundColor; }
    public int getOffsetX()        { return offsetX; }
    public int getOffsetY()        { return offsetY; }
    public int getFirstPointX()    { return firstPointX; }
    public int getFirstPointY()    { return firstPointY; }

    
    public void setActiveTool(Tool tool)  { this.activeTool = tool; }
    public void setActiveColor(byte color) { this.activeColor = color; }

    public void cycleNextTool() {
        Tool[] tools = Tool.values();
        activeTool = tools[(activeTool.ordinal() + 1) % tools.length];
    }

    
    public void pan(int dx, int dy) {
        int gw = 128 / ratio, gh = 128 / ratio;
        offsetX = Math.max(0, Math.min(Math.max(0, gw - 9),  offsetX + dx));
        offsetY = Math.max(0, Math.min(Math.max(0, gh - 5),  offsetY + dy));
    }

    public byte getPixelColorAtSlot(int slot) {
        int bx = (slot % 9) + offsetX, by = (slot / 9) + offsetY;
        int gw = 128 / ratio, gh = 128 / ratio;
        return (bx < gw && by < gh) ? pixels[by * ratio * 128 + bx * ratio] : 0;
    }

    
    public void setPixelAtSlot(int slot, byte color) {
        saveUndoSnapshot();
        setBlock((slot % 9) + offsetX, (slot / 9) + offsetY, color);
    }

    public void fillPixelAtSlot(int slot, byte replacementColor) {
        int bx = (slot % 9) + offsetX, by = (slot / 9) + offsetY;
        int gw = 128 / ratio, gh = 128 / ratio;
        if (bx >= gw || by >= gh) return;
        byte target = pixels[by * ratio * 128 + bx * ratio];
        if (target == replacementColor) return;
        saveUndoSnapshot();
        ArrayDeque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{bx, by});
        while (!q.isEmpty()) {
            int[] p = q.poll();
            if (p[0] < 0 || p[0] >= gw || p[1] < 0 || p[1] >= gh) continue;
            if (pixels[p[1] * ratio * 128 + p[0] * ratio] != target) continue;
            setBlock(p[0], p[1], replacementColor);
            q.add(new int[]{p[0]+1,p[1]}); q.add(new int[]{p[0]-1,p[1]});
            q.add(new int[]{p[0],p[1]+1}); q.add(new int[]{p[0],p[1]-1});
        }
    }

    public void setLineFirstPoint(int slot) {
        firstPointX = (slot % 9) + offsetX;
        firstPointY = (slot / 9) + offsetY;
    }

    public void clearLineFirstPoint() { firstPointX = -1; firstPointY = -1; }

    public void drawLineToSlot(int slot2, byte color) {
        if (firstPointX == -1) return;
        saveUndoSnapshot();
        int x0 = firstPointX, y0 = firstPointY;
        int x1 = (slot2 % 9) + offsetX, y1 = (slot2 / 9) + offsetY;
        int dx = Math.abs(x1-x0), dy = -Math.abs(y1-y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, err = dx+dy;
        while (true) {
            setBlock(x0, y0, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2*err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    public void drawRectToSlot(int slot2, byte color) {
        if (firstPointX == -1) return;
        saveUndoSnapshot();
        int x0 = firstPointX, y0 = firstPointY;
        int x1 = (slot2 % 9) + offsetX, y1 = (slot2 / 9) + offsetY;
        int minX = Math.min(x0,x1), maxX = Math.max(x0,x1);
        int minY = Math.min(y0,y1), maxY = Math.max(y0,y1);
        for (int x = minX; x <= maxX; x++) { setBlock(x,minY,color); setBlock(x,maxY,color); }
        for (int y = minY; y <= maxY; y++) { setBlock(minX,y,color); setBlock(maxX,y,color); }
    }

    public void applyBackground(uk.co.webdent.vanillamaps.util.BackgroundGenerator.BackgroundType type) {
        saveUndoSnapshot();
        uk.co.webdent.vanillamaps.util.BackgroundGenerator.apply(pixels, ratio, activeColor, backgroundColor, type);
        activeTool = Tool.PENCIL;
    }

    public void undo() {
        byte[] snap = undoStack.poll();
        if (snap != null) System.arraycopy(snap, 0, pixels, 0, pixels.length);
    }

    
    private void saveUndoSnapshot() {
        if (undoStack.size() >= MAX_UNDO) undoStack.pollLast();
        byte[] snap = new byte[pixels.length];
        System.arraycopy(pixels, 0, snap, 0, pixels.length);
        undoStack.push(snap);
    }

    private void setBlock(int bx, int by, byte color) {
        int gw = 128/ratio, gh = 128/ratio;
        if (bx < 0 || bx >= gw || by < 0 || by >= gh) return;
        for (int sy = 0; sy < ratio; sy++)
            for (int sx = 0; sx < ratio; sx++)
                pixels[(by*ratio+sy)*128 + (bx*ratio+sx)] = color;
    }
}
