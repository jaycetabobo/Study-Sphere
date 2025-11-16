package studysphere;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.nio.file.Paths;
import java.net.URL;
import java.io.File;

public class Main extends Application {
    public static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        URL res = getClass().getResource("/fxml/login.fxml");
        if (res == null) {
            // fallback: try common output location
            File f = new File("out/production/Study Sphere/fxml/login.fxml");
            if (f.exists()) res = f.toURI().toURL();
        }
        FXMLLoader loader = new FXMLLoader(res);
        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 600);
        URL css = getClass().getResource("/fxml/styles.css");
        if (css == null) {
            File f = new File("out/production/Study Sphere/fxml/styles.css");
            if (f.exists()) css = f.toURI().toURL();
        }
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
        stage.setTitle("StudySphere");
        // start maximized for full-screen experience; user can resize or toggle later
        stage.setMaximized(true);
        stage.setResizable(true);
        try {
            Image icon = new Image(Paths.get("assets/SS logo 1.png").toUri().toString());
            stage.getIcons().add(icon);
        } catch (Exception e) {
            // ignore icon load errors
        }
        stage.show();
    }
}
