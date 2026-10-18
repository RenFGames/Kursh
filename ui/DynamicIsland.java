package yudxx.kursh.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class DynamicIsland {

    public enum Kind {
        IDLE, BUSY, OK, WARN, ERR
    }

    private static final double HEIGHT = 36;
    private static final double EXPANDED_WIDTH = 470;

    private final StackPane island = new StackPane();
    private final Label dot = new Label("●");
    private final Label title = new Label();
    private final Label subtitle = new Label();
    private final Label percent = new Label();
    private final Region spacer = new Region();
    private final HBox row = new HBox(8);

    private boolean expanded;
    private Kind kind = Kind.IDLE;
    private Timeline widthAnim;
    private Timeline autoCollapse;
    private ScaleTransition breath;
    private String tailText = "";

    public DynamicIsland() {
        dot.getStyleClass().addAll("island-dot", "island-dot-idle");
        title.getStyleClass().add("island-title");
        subtitle.getStyleClass().add("island-sub");
        percent.getStyleClass().add("island-percent");

        title.setMaxWidth(170);
        subtitle.setMaxWidth(250);
        subtitle.setVisible(false);
        subtitle.setManaged(false);
        subtitle.setOpacity(0);
        percent.setVisible(false);
        percent.setManaged(false);

        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(dot, title, subtitle, spacer, percent);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 16, 0, 15));

        island.getStyleClass().add("island");
        island.setMinHeight(HEIGHT);
        island.setPrefHeight(HEIGHT);
        island.setMaxHeight(HEIGHT);
        island.setMinWidth(160);
        island.setPrefWidth(200);
        island.setMaxWidth(Region.USE_PREF_SIZE);
        island.getChildren().add(row);
        island.setOnMouseClicked(e -> toggle());
    }

    public Node view() {
        return island;
    }

    public Kind kind() {
        return kind;
    }

    public boolean expanded() {
        return expanded;
    }

    public void toggle() {
        if (expanded) collapse();
        else expand();
    }

    public void expand() {
        if (expanded) return;
        expanded = true;
        subtitle.setVisible(true);
        subtitle.setManaged(true);
        FadeTransition fade = new FadeTransition(Duration.millis(200), subtitle);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(60));
        fade.play();
        resizeWidth(EXPANDED_WIDTH);
    }

    public void collapse() {
        if (!expanded) return;
        expanded = false;
        FadeTransition fade = new FadeTransition(Duration.millis(120), subtitle);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            if (!expanded) {
                subtitle.setVisible(false);
                subtitle.setManaged(false);
            }
        });
        fade.play();
        resizeWidth(compactWidth());
    }

    public void setTail(String text) {
        tailText = text == null ? "" : text;
        if (kind != Kind.BUSY) {
            percent.setText(tailText);
            showPercent(!tailText.isEmpty());
        }
    }

    public void idle(String text) {
        setKind(Kind.IDLE);
        title.setText(text);
        subtitle.setText("");
        collapse();
    }

    public void info(String head, String sub) {
        setKind(Kind.OK);
        title.setText(head);
        subtitle.setText(sub);
        expand();
        scheduleCollapse(4000);
    }

    public void busy(String head, String sub, double ratio) {
        setKind(Kind.BUSY);
        title.setText(head);
        subtitle.setText(sub);
        percent.setText(ratio < 0 ? "…" : Math.round(ratio * 100) + "%");
        showPercent(true);
        if (!expanded) expand();
    }

    public void success(String head, String sub) {
        setKind(Kind.OK);
        title.setText(head);
        subtitle.setText(sub);
        expand();
        scheduleCollapse(5000);
        celebrate();
    }

    public void error(String head, String sub) {
        setKind(Kind.ERR);
        title.setText(head);
        subtitle.setText(sub);
        expand();
        scheduleCollapse(6000);
        shake();
    }

    public void warn(String head, String sub) {
        setKind(Kind.WARN);
        title.setText(head);
        subtitle.setText(sub);
        expand();
        scheduleCollapse(4500);
    }

    private void celebrate() {
        ScaleTransition bounce = new ScaleTransition(Duration.millis(150), island);
        bounce.setFromX(1);
        bounce.setFromY(1);
        bounce.setToX(1.035);
        bounce.setToY(1.035);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(2);
        TranslateTransition rise = new TranslateTransition(Duration.millis(120), island);
        rise.setFromY(0);
        rise.setToY(-3);
        rise.setAutoReverse(true);
        rise.setCycleCount(2);
        rise.setOnFinished(e -> island.setTranslateY(0));
        new ParallelTransition(bounce, rise).play();
    }

    private void shake() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(55), island);
        shake.setFromX(0);
        shake.setToX(4);
        shake.setAutoReverse(true);
        shake.setCycleCount(6);
        shake.setOnFinished(e -> island.setTranslateX(0));
        shake.play();
    }

    private void startBreath() {
        stopBreath();
        breath = new ScaleTransition(Duration.millis(820), dot);
        breath.setFromX(1);
        breath.setFromY(1);
        breath.setToX(1.35);
        breath.setToY(1.35);
        breath.setAutoReverse(true);
        breath.setCycleCount(Animation.INDEFINITE);
        breath.play();
    }

    private void stopBreath() {
        if (breath != null) {
            breath.stop();
            breath = null;
        }
        dot.setScaleX(1);
        dot.setScaleY(1);
    }

    private void showPercent(boolean visible) {
        percent.setVisible(visible);
        percent.setManaged(visible);
    }

    private double compactWidth() {
        int length = title.getText() == null ? 0 : title.getText().length();
        return Math.max(170, Math.min(330, 86 + length * 8));
    }

    private void setKind(Kind value) {
        kind = value;
        dot.getStyleClass().setAll("island-dot", styleOf(value));
        if (value == Kind.BUSY) {
            startBreath();
        } else {
            stopBreath();
            percent.setText(tailText);
            showPercent(!tailText.isEmpty());
        }
    }

    private String styleOf(Kind value) {
        switch (value) {
            case OK:
                return "island-dot-ok";
            case BUSY:
                return "island-dot-busy";
            case ERR:
                return "island-dot-err";
            case WARN:
                return "island-dot-warn";
            default:
                return "island-dot-idle";
        }
    }

    private void resizeWidth(double width) {
        if (widthAnim != null) widthAnim.stop();
        widthAnim = new Timeline(new KeyFrame(Duration.millis(280),
                new KeyValue(island.prefWidthProperty(), width, Interpolator.EASE_OUT)));
        widthAnim.play();
    }

    private void scheduleCollapse(long delayMs) {
        if (autoCollapse != null) autoCollapse.stop();
        autoCollapse = new Timeline(new KeyFrame(Duration.millis(delayMs), e -> {
            if (kind != Kind.BUSY) collapse();
        }));
        autoCollapse.play();
    }

    public void dispose() {
        if (widthAnim != null) widthAnim.stop();
        if (autoCollapse != null) autoCollapse.stop();
        stopBreath();
    }
}
