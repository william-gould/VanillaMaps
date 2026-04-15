package uk.co.webdent.VanillaMaps.custommaps.util;

import java.util.HashMap;
import java.util.Map;

public class MapFont {
        // Key: character. Value: (width << 16) | bitmap
        private static final Map<Character, Integer> FONT = new HashMap<>();

        static {
                register('A', new String[] {
                                ".##.",
                                "#..#",
                                "####",
                                "#..#"
                });
                register('B', new String[] {
                                "###.",
                                "#.#.",
                                "###.",
                                "##.#"
                });
                register('C', new String[] {
                                ".###",
                                "#...",
                                "#...",
                                ".###"
                });
                register('D', new String[] {
                                "###.",
                                "#..#",
                                "#..#",
                                "###."
                });
                register('E', new String[] {
                                "####",
                                "#...",
                                "###.",
                                "####"
                });
                register('F', new String[] {
                                "####",
                                "#...",
                                "###.",
                                "#..."
                });
                register('G', new String[] {
                                ".###",
                                "#...",
                                "#.##",
                                ".###"
                });
                register('H', new String[] {
                                "#..#",
                                "####",
                                "#..#",
                                "#..#"
                });
                register('I', new String[] {
                                "###",
                                ".#.",
                                ".#.",
                                "###"
                });
                register('J', new String[] {
                                "...#",
                                "...#",
                                "#..#",
                                ".##."
                });
                register('K', new String[] {
                                "#..#",
                                "#.#.",
                                "##..",
                                "#.#."
                });
                register('L', new String[] {
                                "#...",
                                "#...",
                                "#...",
                                "####"
                });
                register('M', new String[] {
                                "#..#",
                                "####",
                                "#..#",
                                "#..#" // 4x4 M is tough, making it identical to H practically if we can't do middle
                                       // spike well.
                });
                // Actually a better M:
                register('M', new String[] {
                                "#..#",
                                "####",
                                "#..#",
                                "#..#"
                }); // we will just keep it simple
                register('N', new String[] {
                                "##.#",
                                "#.##",
                                "#..#",
                                "#..#"
                });
                register('O', new String[] {
                                ".##.",
                                "#..#",
                                "#..#",
                                ".##."
                });
                register('P', new String[] {
                                "###.",
                                "#..#",
                                "###.",
                                "#..."
                });
                register('Q', new String[] {
                                ".##.",
                                "#..#",
                                "#.##",
                                ".###"
                });
                register('R', new String[] {
                                "###.",
                                "#..#",
                                "###.",
                                "#..#"
                });
                register('S', new String[] {
                                ".###",
                                "##..",
                                "..##",
                                "###."
                });
                register('T', new String[] {
                                "###",
                                ".#.",
                                ".#.",
                                ".#."
                });
                register('U', new String[] {
                                "#..#",
                                "#..#",
                                "#..#",
                                ".##."
                });
                register('V', new String[] {
                                "#..#",
                                "#..#",
                                ".##.",
                                ".##."
                });
                register('W', new String[] {
                                "#..#",
                                "#..#",
                                "####",
                                ".##."
                });
                register('X', new String[] {
                                "#..#",
                                ".##.",
                                ".##.",
                                "#..#"
                });
                register('Y', new String[] {
                                "#..#",
                                ".##.",
                                "..#.",
                                "..#."
                });
                register('Z', new String[] {
                                "####",
                                "..#.",
                                ".#..",
                                "####"
                });
                register('0', new String[] {
                                ".##.",
                                "#..#",
                                "#..#",
                                ".##."
                });
                register('1', new String[] {
                                ".#.",
                                "##.",
                                ".#.",
                                "###"
                });
                register('2', new String[] {
                                "###.",
                                "...#",
                                ".##.",
                                "####"
                });
                register('3', new String[] {
                                "###.",
                                "..#.",
                                "..#.",
                                "###."
                });
                register('4', new String[] {
                                "#...",
                                "#..#",
                                "####",
                                "...#"
                });
                register('5', new String[] {
                                "####",
                                "###.",
                                "...#",
                                "###."
                });
                register('6', new String[] {
                                ".##.",
                                "#...",
                                "###.",
                                ".##."
                });
                register('7', new String[] {
                                "####",
                                "...#",
                                "..#.",
                                ".#.."
                });
                register('8', new String[] {
                                ".##.",
                                ".##.",
                                "#..#",
                                ".##."
                });
                register('9', new String[] {
                                ".##.",
                                ".###",
                                "...#",
                                ".##."
                });
                register('!', new String[] {
                                "#",
                                "#",
                                ".",
                                "#"
                });
                register('?', new String[] {
                                "###",
                                "..#",
                                ".#.",
                                ".#."
                });
                register('.', new String[] {
                                ".",
                                ".",
                                ".",
                                "#"
                });
                register(',', new String[] {
                                ".",
                                ".",
                                "#",
                                "#"
                });
                register('-', new String[] {
                                "...",
                                "###",
                                "...",
                                "..."
                });
                // Override M to be better slightly:
                register('M', new String[] {
                                "#..#",
                                "####",
                                "#.##",
                                "#..#"
                });
        }

        private static void register(char c, String[] rows) {
                int width = rows[0].length();
                int bitmap = 0;
                for (int y = 0; y < 4; y++) {
                        for (int x = 0; x < width; x++) {
                                if (rows[y].charAt(x) == '#') {
                                        bitmap |= (1 << (y * 4 + x));
                                }
                        }
                }
                FONT.put(c, (width << 16) | bitmap);
        }

        /**
         * Calculates the width of the given string, factoring in:
         * - 1px gap per character
         * - 3px gap for spaces (overriding default calculation)
         */
        public static int measureText(String text) {
                text = text.toUpperCase();
                int totalWidth = 0;
                for (int i = 0; i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (c == ' ') {
                                totalWidth += 3;
                                continue;
                        }
                        if (FONT.containsKey(c)) {
                                int data = FONT.get(c);
                                int width = data >> 16;
                                totalWidth += width;
                                if (i < text.length() - 1 && text.charAt(i + 1) != ' ') {
                                        totalWidth += 1; // 1px letter gap (only if not followed by a space immediately,
                                                         // as space handles
                                                         // it)
                                }
                        } else {
                                totalWidth += 3 + 1; // fallback for unknown chars
                        }
                }
                return totalWidth;
        }

        /**
         * Draws text onto the 128x128 byte array.
         */
        public static void drawText(String text, byte[] pixels, int startX, int startY, byte color) {
                text = text.toUpperCase();
                int currentX = startX;

                for (int i = 0; i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (c == ' ') {
                                currentX += 3;
                                continue;
                        }

                        if (FONT.containsKey(c)) {
                                int data = FONT.get(c);
                                int width = data >> 16;
                                int bitmap = data & 0xFFFF;

                                for (int y = 0; y < 4; y++) {
                                        for (int x = 0; x < width; x++) {
                                                boolean solid = (bitmap & (1 << (y * 4 + x))) != 0;
                                                if (solid) {
                                                        int px = currentX + x;
                                                        int py = startY + y;
                                                        if (px >= 0 && px < 128 && py >= 0 && py < 128) {
                                                                pixels[py * 128 + px] = color;
                                                        }
                                                }
                                        }
                                }
                                currentX += width;

                                // Only add 1 pixel gap if the next char is not a space
                                if (i < text.length() - 1 && text.charAt(i + 1) != ' ') {
                                        currentX += 1;
                                }
                        } else {
                                currentX += 3; // fallback 3px width
                                if (i < text.length() - 1 && text.charAt(i + 1) != ' ') {
                                        currentX += 1;
                                }
                        }
                }
        }
}
