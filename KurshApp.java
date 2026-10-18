package yudxx.kursh;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import yudxx.kursh.ui.MainView;

public class KurshApp {

    public static final String APP_NAME = "Kursh";
    public static final String APP_SUB = "v1.0";

    private static MainView current;

    public static void main(String[] args) {
        Application.launch(KurshFx.class, args);
    }

    public static void reload() {
        if (current != null) {
            current.dispose();
            current = null;
        }
        KurshFx.render(KurshFx.stageRef);
    }

    public static class KurshFx extends Application {

        private static Stage stageRef;

        @Override
        public void start(Stage stage) {
            stageRef = stage;
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle(APP_NAME);
            stage.setResizable(false);
            render(stage);
            stage.centerOnScreen();
            stage.show();
        }

        private static void render(Stage stage) {
            current = new MainView(stage);
            Scene scene = new Scene(current.build(), 1030, 690);
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(KurshFx.class.getResource("/yudxx/kursh/app.css").toExternalForm());
            stage.setScene(scene);
        }

        @Override
        public void stop() {
            if (current != null) current.dispose();
        }
    }
}
