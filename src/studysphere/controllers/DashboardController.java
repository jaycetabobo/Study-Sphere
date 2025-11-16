package studysphere.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.scene.input.MouseEvent;
import javafx.geometry.Pos;

public class DashboardController {
    @FXML
    private StackPane contentArea;

    // sidebar tab HBoxes
    @FXML
    private HBox btnDashboard;
    @FXML
    private HBox btnNotes;
    @FXML
    private HBox btnTasks;
    @FXML
    private HBox btnReminders;
    @FXML
    private HBox btnLogout;

    // inline style presets
    // default header style should match nav default so header appears light when not active
    private final String HEADER_DEFAULT = "-fx-background-color: transparent; -fx-border-width: 0 0 1 0; -fx-border-color: rgba(0,0,0,0.18);";
    private final String NAV_DEFAULT = "-fx-background-color: transparent; -fx-border-width: 0 0 1 0; -fx-border-color: rgba(0,0,0,0.18);";
    private final String ACTIVE_NAV = "-fx-background-color: rgba(0,0,0,0.18);";
    // use the same darkness as nav active so StudyHub matches other active tabs
    private final String ACTIVE_HEADER = ACTIVE_NAV;

    @FXML
    public void initialize() {
        // load StudyHub landing by default
        loadContent("/fxml/home_content.fxml");
        // set initial inline styles
        applyDefaultStyles();
        // set dashboard header as active visually
        setActiveTab(btnDashboard);

        // attach click handlers programmatically to avoid FXML wiring issues
        if (btnDashboard != null) btnDashboard.setOnMouseClicked(e -> {
            loadContent("/fxml/home_content.fxml");
            setActiveTab(btnDashboard);
        });
        if (btnNotes != null) btnNotes.setOnMouseClicked(e -> onNotes(null));
        if (btnTasks != null) btnTasks.setOnMouseClicked(e -> onTasks(null));
        if (btnReminders != null) btnReminders.setOnMouseClicked(e -> onReminders(null));
        if (btnLogout != null) btnLogout.setOnMouseClicked(e -> onLogoutClicked(e));
    }

    @FXML
    void onNotes(MouseEvent event) {
        loadContent("/fxml/notes.fxml");
        // Only change sidebar active state — do not change the center content background
        setActiveTab(btnNotes);
    }

    @FXML
    void onTasks(MouseEvent event) {
        loadContent("/fxml/tasks.fxml");
        // Only change sidebar active state
        setActiveTab(btnTasks);
    }

    @FXML
    void onReminders(MouseEvent event) {
        loadContent("/fxml/notifications.fxml");
        // Only change sidebar active state
        setActiveTab(btnReminders);
    }

    private void applyDefaultStyles() {
        if (btnDashboard != null) btnDashboard.setStyle(HEADER_DEFAULT);
        if (btnNotes != null) btnNotes.setStyle(NAV_DEFAULT);
        if (btnTasks != null) btnTasks.setStyle(NAV_DEFAULT);
        if (btnReminders != null) btnReminders.setStyle(NAV_DEFAULT);
    }

    private void setActiveTab(HBox active) {
        // reset to defaults first
        applyDefaultStyles();
        // explicitly set active style
        if (active == btnDashboard) {
            btnDashboard.setStyle(ACTIVE_HEADER);
        } else if (active == btnNotes) {
            btnNotes.setStyle(ACTIVE_NAV);
        } else if (active == btnTasks) {
            btnTasks.setStyle(ACTIVE_NAV);
        } else if (active == btnReminders) {
            btnReminders.setStyle(ACTIVE_NAV);
        }
    }

    private void loadContent(String resourcePath) {
        try {
            // first try a direct file in the project source (so IDE edits are reflected without a full rebuild)
            File srcFile = new File("src/fxml" + resourcePath.substring(resourcePath.lastIndexOf('/')));
            URL res = null;
            if (srcFile.exists()) {
                res = srcFile.toURI().toURL();
            }
            if (res == null) res = getClass().getResource(resourcePath);
            if (res == null) {
                // try common output location
                File f = new File("out/production/Study Sphere/fxml" + resourcePath.substring(resourcePath.lastIndexOf('/')));
                if (f.exists()) res = f.toURI().toURL();
            }
            if (res == null) return;
            // debug: print which resource URL is used to load the FXML (helps confirm src vs compiled)
            System.out.println("[DashboardController] loading FXML from: " + res.toString());
            Node node = FXMLLoader.load(res);
            // Ensure loaded content fills the contentArea (StackPane)
            contentArea.getChildren().setAll(node);
            // set alignment to top-left so content starts at the left edge
            StackPane.setAlignment(node, Pos.TOP_LEFT);
            // if the loaded node is a Region (AnchorPane, VBox, etc.) bind its preferred size to the content area
            if (node instanceof Region) {
                Region r = (Region) node;
                r.prefWidthProperty().bind(contentArea.widthProperty());
                r.prefHeightProperty().bind(contentArea.heightProperty());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onLogoutClicked(MouseEvent event) {
        try {
            // close current window
            if (contentArea != null && contentArea.getScene() != null) {
                Stage stage = (Stage) contentArea.getScene().getWindow();
                // load login scene
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                java.net.URL css = getClass().getResource("/fxml/styles.css");
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Study Sphere - Login");
                stage.sizeToScene();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
