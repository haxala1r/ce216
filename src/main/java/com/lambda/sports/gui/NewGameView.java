package com.lambda.sports.gui;

import com.lambda.sports.AbstractSport;
import com.lambda.sports.Team;
import com.lambda.sports.game.GameSession;
import com.lambda.sports.headball.HeadballSport;
import com.lambda.sports.volleyball.VolleyballSport;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Form for starting a new season: choose sport, number of teams, session
 * name, and an RNG seed.  Validates input before constructing a
 * {@link GameSession} and handing control to the dashboard.
 */
public class NewGameView {

    private static final int MIN_TEAMS = 4;
    private static final int MAX_TEAMS = 8;

    private final GuiApp app;
    private final VBox root;

    private final ComboBox<String>      sportPicker;
    private final Spinner<Integer>      teamCountSpinner;
    private final TextField             nameField;
    private final TextField             seedField;
    private final Label                 errorLabel;

    public NewGameView(GuiApp app) {
        this.app              = app;
        this.sportPicker      = new ComboBox<>();
        this.teamCountSpinner = new Spinner<>(MIN_TEAMS, MAX_TEAMS, 4);
        this.nameField        = new TextField();
        this.seedField        = new TextField();
        this.errorLabel       = new Label();
        this.root             = build();
    }

    private VBox build() {
        Label title = new Label("New Season");
        title.getStyleClass().add("section-header");

        sportPicker.getItems().addAll("Headball", "Volleyball");
        sportPicker.getSelectionModel().select(0);

        teamCountSpinner.setEditable(false);
        teamCountSpinner.setPrefWidth(80);

        nameField.setPromptText("e.g. My First Season");
        nameField.setText("Season " + (System.currentTimeMillis() % 1000));

        seedField.setPromptText("Leave blank for random");
        seedField.setText(String.valueOf(ThreadLocalRandom.current().nextInt(1, 9999)));

        errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
        errorLabel.setVisible(false);

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.addRow(0, new Label("Sport:"),         sportPicker);
        form.addRow(1, new Label("Number of teams:"), teamCountSpinner);
        form.addRow(2, new Label("Session name:"),  nameField);
        form.addRow(3, new Label("Random seed:"),   seedField);
        GridPane.setHgrow(nameField,   Priority.ALWAYS);
        GridPane.setHgrow(seedField,   Priority.ALWAYS);
        GridPane.setHgrow(sportPicker, Priority.ALWAYS);
        sportPicker.setMaxWidth(Double.MAX_VALUE);
        nameField.setMaxWidth(Double.MAX_VALUE);
        seedField.setMaxWidth(Double.MAX_VALUE);

        Button start  = new Button("Start Season");
        Button cancel = new Button("Back");
        cancel.getStyleClass().add("secondary");
        start.setOnAction(e -> startNewSeason());
        cancel.setOnAction(e -> app.setView(new MainMenuView(app).getRoot()));

        HBox actions = new HBox(10, cancel, start);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox box = new VBox(18, title, form, errorLabel, actions);
        box.setPadding(new Insets(40, 60, 40, 60));
        box.setMaxWidth(560);
        VBox container = new VBox(box);
        container.setAlignment(Pos.CENTER);
        return container;
    }

    private void startNewSeason() {
        errorLabel.setVisible(false);

        String name = nameField.getText().trim();
        if (name.isEmpty()) { showError("Session name cannot be empty."); return; }

        long seed;
        String seedText = seedField.getText().trim();
        if (seedText.isEmpty()) {
            seed = ThreadLocalRandom.current().nextLong();
        } else {
            try { seed = Long.parseLong(seedText); }
            catch (NumberFormatException ex) {
                showError("Seed must be a whole number."); return;
            }
        }

        boolean isHeadball = "Headball".equals(sportPicker.getValue());
        AbstractSport sport = isHeadball ? new HeadballSport() : new VolleyballSport();
        String sportId      = isHeadball ? "headball"          : "volleyball";

        int n = teamCountSpinner.getValue();
        List<Team> teams = sport.createTeams(n);

        GameSession session = new GameSession(name, sportId, sport, teams, seed);
        app.setSession(session);
        app.setView(new DashboardView(app).getRoot());
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    public Parent getRoot() { return root; }
}
