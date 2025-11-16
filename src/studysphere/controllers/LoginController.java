package studysphere.controllers;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import studysphere.Main;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    @FXML
    private ImageView bgImage;
    @FXML
    private AnchorPane loginRoot;

    @FXML
    public void initialize() {
        // prefills for demo
        usernameField.setText("student");
        passwordField.setText("password");

        // bind background to root size for responsive scaling
        if (bgImage != null && loginRoot != null) {
            bgImage.fitWidthProperty().bind(loginRoot.widthProperty());
            bgImage.fitHeightProperty().bind(loginRoot.heightProperty());
        }
    }

    @FXML
    void onLogin(ActionEvent event) {
        // simple mock authentication
        String u = usernameField.getText();
        String p = passwordField.getText();
        if (u != null && u.equals("student") && p != null && p.equals("password")) {
            try {
                URL res = getClass().getResource("/fxml/dashboard.fxml");
                if (res == null) {
                    File f = new File("out/production/Study Sphere/fxml/dashboard.fxml");
                    if (f.exists()) res = f.toURI().toURL();
                }
                if (res == null) return; // can't load dashboard
                Parent root = FXMLLoader.load(res);
                Main.primaryStage.getScene().setRoot(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // for now just set fields to show error state
            usernameField.setStyle("-fx-border-color: #e74c3c;");
            passwordField.setStyle("-fx-border-color: #e74c3c;");
        }
    }
}
