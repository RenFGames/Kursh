package yudxx.kursh.core;

public final class Fmt {

    private Fmt() {
    }

    public static String hex(long value) {
        return String.format("0x%X", value);
    }

    public static String bytes(long n) {
        if (n < 1024) return n + " B";
        if (n < 1024L * 1024) return String.format("%.1f KB", n / 1024.0);
        if (n < 1024L * 1024 * 1024) return String.format("%.1f MB", n / (1024.0 * 1024));
        return String.format("%.2f GB", n / (1024.0 * 1024 * 1024));
    }

    public static String floatText(float v) {
        if (Float.isNaN(v)) return "NaN";
        if (Float.isInfinite(v)) return v > 0 ? "INF" : "-INF";
        if (v == (long) v && Math.abs(v) < 1e15) return String.valueOf((long) v);
        return trim(String.format("%.5f", v));
    }

    public static String doubleText(double v) {
        if (Double.isNaN(v)) return "NaN";
        if (Double.isInfinite(v)) return v > 0 ? "INF" : "-INF";
        if (v == (long) v && Math.abs(v) < 1e15) return String.valueOf((long) v);
        return trim(String.format("%.8f", v));
    }

    private static String trim(String s) {
        if (s.indexOf('.') < 0) return s;
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == '0') end--;
        if (end > 0 && s.charAt(end - 1) == '.') end--;
        return s.substring(0, end);
    }

    public static String pad(String s, int len) {
        if (s == null) s = "";
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) sb.append(' ');
        return sb.toString();
    }
}
