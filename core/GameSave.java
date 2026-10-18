package yudxx.kursh.core;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class GameSave {

    private static final String[] SKIP_EXT = {
            ".dll", ".exe", ".so", ".dylib", ".assets", ".resS", ".resource", ".bundle",
            ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".tga", ".psd",
            ".mp3", ".ogg", ".wav", ".fsb", ".bank", ".ttf", ".otf",
            ".zip", ".7z", ".rar", ".pdb", ".mdb", ".log", ".txt"
    };

    private GameSave() {
    }

    public static File findSaveRoot(File gameDir) {
        if (gameDir == null || !gameDir.isDirectory()) return null;
        String company = null;
        String product = null;
        File[] children = gameDir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory() && child.getName().endsWith("_Data")) {
                    File info = new File(child, "app.info");
                    if (info.isFile()) {
                        try {
                            List<String> lines = Files.readAllLines(info.toPath(), StandardCharsets.UTF_8);
                            if (lines.size() >= 2) {
                                company = lines.get(0).trim();
                                product = lines.get(1).trim();
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
        String localLow = System.getenv("USERPROFILE");
        if (localLow == null) localLow = System.getProperty("user.home");
        File root = new File(localLow, "AppData/LocalLow");
        if (!root.isDirectory()) return null;

        if (company != null && product != null) {
            File direct = new File(new File(root, company), product);
            if (direct.isDirectory()) return direct;
        }
        String key = gameDir.getName().toLowerCase(Locale.ROOT);
        File[] companies = root.listFiles();
        if (companies == null) return null;
        for (File dir : companies) {
            File[] products = dir.listFiles();
            if (products == null) continue;
            for (File candidate : products) {
                if (!candidate.isDirectory()) continue;
                String name = candidate.getName().toLowerCase(Locale.ROOT);
                if (name.contains(key) || key.contains(name)) return candidate;
            }
        }
        return null;
    }

    public static List<File> findSaveFiles(File root) {
        List<File> out = new ArrayList<>();
        if (root == null || !root.isDirectory()) return out;
        collect(root, out, 0);
        out.sort((a, b) -> {
            int sa = a.getAbsolutePath().toLowerCase(Locale.ROOT).contains("save") ? 0 : 1;
            int sb = b.getAbsolutePath().toLowerCase(Locale.ROOT).contains("save") ? 0 : 1;
            if (sa != sb) return Integer.compare(sa, sb);
            int size = Long.compare(b.length(), a.length());
            if (size != 0) return size;
            return Long.compare(b.lastModified(), a.lastModified());
        });
        return out;
    }

    private static void collect(File dir, List<File> out, int depth) {
        if (depth > 3 || out.size() > 200) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                collect(file, out, depth + 1);
                continue;
            }
            if (file.length() <= 0 || file.length() > 32L * 1024 * 1024) continue;
            String name = file.getName().toLowerCase(Locale.ROOT);
            boolean skip = false;
            for (String ext : SKIP_EXT) {
                if (name.endsWith(ext)) {
                    skip = true;
                    break;
                }
            }
            if (skip) continue;
            if (isJson(file)) out.add(file);
        }
    }

    public static boolean isJson(File file) {
        try {
            byte[] head = new byte[64];
            try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                int read = in.read(head);
                if (read <= 0) return false;
                String text = new String(head, 0, read, StandardCharsets.UTF_8).trim();
                return text.startsWith("{") || text.startsWith("[");
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String read(File file) {
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            try {
                return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            } catch (Exception ignored) {
                return "";
            }
        }
    }

    public static boolean write(File file, String text) {
        try {
            Files.writeString(file.toPath(), text, StandardCharsets.UTF_8);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static File backup(File file) {
        try {
            File bak = new File(file.getParentFile(), file.getName() + ".kursh.bak");
            if (!bak.exists()) {
                Files.copy(file.toPath(), bak.toPath());
            }
            return bak;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean restore(File file) {
        File bak = new File(file.getParentFile(), file.getName() + ".kursh.bak");
        if (!bak.isFile()) return false;
        try {
            Files.copy(bak.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
