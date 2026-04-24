package com.lambda.sports.gui;

import com.lambda.sports.game.GameSession;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * JavaFX entry point.  Hosts the primary {@link Stage} and a single mutable
 * {@link Scene}; individual screens swap themselves into the scene's root via
 * {@link #setView(Parent)}.  The currently active {@link GameSession} (if any)
 * is also kept here so screens can hand state off to one another without
 * passing it through every constructor.
 */
public class GuiApp extends Application {

    /** Default directory for save files: {@code ~/.sports-manager/saves}. */
    public static final Path SAVES_DIR =
        Paths.get(System.getProperty("user.home"), ".sports-manager", "saves");

    /** Classpath location of the global stylesheet. */
    private static final String STYLESHEET = "/com/lambda/sports/gui/styles.css";

    private static final double WINDOW_W = 980;
    private static final double WINDOW_H = 640;

    private Stage       primaryStage;
    private Scene       scene;
    private StackPane   rootContainer;
    private GameSession activeSession;

    @Override
    public void start(Stage stage) {
        this.primaryStage  = stage;
        this.rootContainer = new StackPane();
        this.rootContainer.setPadding(new Insets(0));
        this.scene         = new Scene(rootContainer, WINDOW_W, WINDOW_H);

        // Load the global stylesheet from src/main/resources.  We load via
        // class loader rather than a hard-coded path so it works in both an
        // exploded build (mvn javafx:run) and a packaged jar.
        URL css = GuiApp.class.getResource(STYLESHEET);
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        // If the stylesheet is missing for some reason the app still boots
        // with JavaFX defaults — usability is never blocked by missing CSS.

        stage.setTitle("Universal Sports Manager — Team λ Transformation");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(560);
        setView(new MainMenuView(this).getRoot());
        stage.show();
    }

    /** Replaces the current screen contents. */
    public void setView(Parent root) {
        rootContainer.getChildren().setAll(root);
    }

    public Stage getStage()          { return primaryStage; }
    public GameSession getSession()  { return activeSession; }
    public void setSession(GameSession s) { this.activeSession = s; }

    public static void main(String[] args) {
        launch(args);
    }
}
