package com.lambda.sports.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Landing screen.  Three actions: start a new season, load an existing save,
 * or quit.  The "Load" button is disabled when the saves directory is empty.
 */
public class MainMenuView {

    private final GuiApp app;
    private final VBox root;

    public MainMenuView(GuiApp app) {
        this.app  = app;
        this.root = build();
    }

    private VBox build() {
        Label title = new Label("Universal Sports\nManager");
        title.getStyleClass().add("title-label");
        title.setStyle("-fx-text-alignment: center;");

        Label subtitle = new Label("A modular sports-management framework  ·  Team λ Transformation");
        subtitle.getStyleClass().add("subtitle-label");

        Button newGame  = primaryButton("New Game",  () -> app.setView(new NewGameView(app).getRoot()));
        Button loadGame = primaryButton("Load Game", () -> app.setView(new LoadGameView(app).getRoot()));
        Button exit     = secondaryButton("Exit",    () -> app.getStage().close());

        VBox buttons = new VBox(12, newGame, loadGame, exit);
        buttons.setAlignment(Pos.CENTER);
        buttons.setMaxWidth(220);

        VBox box = new VBox(28, title, subtitle, buttons);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60));
        return box;
    }

    private Button primaryButton(String text, Runnable onClick) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> onClick.run());
        return b;
    }

    private Button secondaryButton(String text, Runnable onClick) {
        Button b = primaryButton(text, onClick);
        b.getStyleClass().add("secondary");
        return b;
    }

    public Parent getRoot() { return root; }
}
