package com.lambda.sports.gui;

import com.lambda.sports.game.GameSession;
import com.lambda.sports.persistence.SaveManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Lists every {@code *.json} file in the saves directory and lets the user
 * load or delete one.  Empty list means there's nothing to load — the user is
 * told and routed back to the menu.
 */
public class LoadGameView {

    private final GuiApp           app;
    private final SaveManager      saveManager;
    private final ListView<Path>   list;
    private final VBox             root;

    public LoadGameView(GuiApp app) {
        this.app         = app;
        this.saveManager = new SaveManager();
        this.list        = new ListView<>();
        this.root        = build();
        refreshSaves();
    }

    private VBox build() {
        Label title = new Label("Load Saved Game");
        title.getStyleClass().add("section-header");

        // Show only the file's name without the full path
        list.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Path p, boolean empty) {
                super.updateItem(p, empty);
                setText((empty || p == null) ? null
                        : p.getFileName().toString().replaceFirst("\\.json$", ""));
            }
        });
        VBox.setVgrow(list, Priority.ALWAYS);

        Button load   = new Button("Load");
        Button delete = new Button("Delete");
        Button back   = new Button("Back");
        delete.getStyleClass().add("danger");
        back.getStyleClass().add("secondary");

        load.setOnAction(e -> loadSelected());
        delete.setOnAction(e -> deleteSelected());
        back.setOnAction(e -> app.setView(new MainMenuView(app).getRoot()));

        HBox actions = new HBox(10, back, delete, load);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox box = new VBox(14, title, list, actions);
        box.setPadding(new Insets(28, 40, 28, 40));
        return box;
    }

    private void refreshSaves() {
        List<Path> saves = saveManager.listSaves(GuiApp.SAVES_DIR);
        list.setItems(FXCollections.observableArrayList(saves));
        if (saves.isEmpty()) {
            // Show a placeholder rather than a hard error
            Label placeholder = new Label(
                "No saves found in " + GuiApp.SAVES_DIR + "\n\n"
                + "Start a new game first, then save it from the dashboard.");
            placeholder.setStyle("-fx-text-alignment: center;");
            list.setPlaceholder(placeholder);
        }
    }

    private void loadSelected() {
        Path selected = list.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            GameSession s = saveManager.load(selected);
            app.setSession(s);
            app.setView(new DashboardView(app).getRoot());
        } catch (IOException | IllegalArgumentException ex) {
            new Alert(Alert.AlertType.ERROR,
                "Could not load save:\n" + ex.getMessage()).showAndWait();
        }
    }

    private void deleteSelected() {
        Path selected = list.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete \"" + selected.getFileName() + "\"?  This cannot be undone.");
        confirm.showAndWait().ifPresent(button -> {
            if (button == javafx.scene.control.ButtonType.OK) {
                try { Files.deleteIfExists(selected); }
                catch (IOException ex) { /* swallow — refresh will show what's left */ }
                refreshSaves();
            }
        });
    }

    public Parent getRoot() { return root; }
}
