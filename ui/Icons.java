package yudxx.kursh.ui;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public final class Icons {

    public static final String CLOSE = "M1,1 L11,11 M11,1 L1,11";
    public static final String MIN = "M1,6 L12,6";
    public static final String REFRESH = "M11.4,6.6 A5,5 0 1,1 9.4,2.1 M9.4,2.1 L12,2.1 L12,4.7";
    public static final String PLUS = "M6,1 L6,11 M1,6 L11,6";
    public static final String TRASH = "M2,3.2 L12,3.2 M5,3.2 L5,1.4 L9,1.4 L9,3.2 M3.6,3.2 L4.1,12 L9.9,12 L10.4,3.2";
    public static final String PEN = "M1.8,12.2 L3,8.8 L10.2,1.6 L12.4,3.8 L5.2,11 Z";
    public static final String LOCK = "M4,6 L4,4 A3,3 0 0,1 10,4 L10,6 M2.8,6 L11.2,6 L11.2,12 L2.8,12 Z";
    public static final String INFO = "M1.5,6.5 A5,5 0 1,0 11.5,6.5 A5,5 0 1,0 1.5,6.5 M6.5,3.1 L6.5,3.9 M6.5,5.8 L6.5,9.8";
    public static final String COPY = "M4.5,4.5 L11,4.5 L11,11 L4.5,11 Z M2,8.5 L2,2 L8.5,2";
    public static final String UNDO = "M4,4.5 L1.5,7 L4,9.5 M1.5,7 L8,7 A3.2,3.2 0 0,1 8,13.4 L5.5,13.4";
    public static final String POWER = "M6.5,1.5 L6.5,6.5 M9.8,3.2 A4.6,4.6 0 1,1 3.2,3.2";

    private Icons() {
    }

    public static SVGPath of(String content, Color color, double size) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.setFill(Color.TRANSPARENT);
        path.setStroke(color);
        path.setStrokeWidth(1.45);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        double scale = size / 13.0;
        path.setScaleX(scale);
        path.setScaleY(scale);
        return path;
    }
}
