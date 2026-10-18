package yudxx.kursh.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Toast {

    public enum Kind {
        OK, WARN, ERR, INFO
    }

    private final StackPane layer;
    private Label current;

    public Toast(StackPane layer) {
        this.layer = layer;
    }

    public void ok(String message) {
        show(message, Kind.OK);
    }

    public void warn(String message) {
        show(message, Kind.WARN);
    }

    public void err(String message) {
        show(message, Kind.ERR);
    }

    public void info(String message) {
        show(message, Kind.INFO);
    }

    public void show(String message, Kind kind) {
        if (layer == null || message == null) return;
        if (current != null) layer.getChildren().remove(current);

        Label label = new Label(message);
        label.getStyleClass().addAll("toast", styleOf(kind));
        label.setWrapText(true);
        label.setMaxWidth(340);
        label.setOpacity(0);
        label.setTranslateY(12);
        StackPane.setAlignment(label, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(label, new Insets(0, 20, 18, 0));
        layer.getChildren().add(label);
        current = label;

        FadeTransition fadeIn = new FadeTransition(Duration.millis(170), label);
        fadeIn.setToValue(1);
        TranslateTransition dropIn = new TranslateTransition(Duration.millis(220), label);
        dropIn.setToY(0);
        ParallelTransition enter = new ParallelTransition(fadeIn, dropIn);

        PauseTransition hold = new PauseTransition(Duration.millis(2100));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(220), label);
        fadeOut.setToValue(0);
        TranslateTransition upOut = new TranslateTransition(Duration.millis(220), label);
        upOut.setToY(8);
        ParallelTransition leave = new ParallelTransition(fadeOut, upOut);

        SequentialTransition seq = new SequentialTransition(enter, hold, leave);
        seq.setOnFinished(e -> {
            layer.getChildren().remove(label);
            if (current == label) current = null;
        });
        seq.play();
    }

    private String styleOf(Kind kind) {
        switch (kind) {
            case OK:
                return "toast-ok";
            case WARN:
                return "toast-warn";
            case ERR:
                return "toast-err";
            default:
                return "toast-info";
        }
    }
}
