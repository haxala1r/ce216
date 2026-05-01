package com.lambda.sports.gui;

import com.lambda.sports.LeagueStanding;
import com.lambda.sports.game.GameSession;
import com.lambda.sports.persistence.SaveManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * End-of-season screen.  Displayed automatically when the last fixture is
 * played, satisfying the spec's <i>"Once the league ends, the player either
 * wins the league or not and may continue for another year"</i> requirement.
 *
 * <p>Shows:</p>
 * <ul>
 *   <li>The season winner ("Champions of Season N")</li>
 *   <li>The full final standings table</li>
 *   <li>Buttons: <b>Start Season N+1</b> · <b>Save</b> · <b>Main Menu</b></li>
 * </ul>
 *
 * <p>Choosing "Start Season N+1" calls {@link GameSession#advanceToNextSeason()}
 * which clears the standings, ages all squads, and resets the fixture pointer,
 * then routes the user back to a fresh {@link DashboardView}.</p>
 */
public class SeasonEndView {

    private final GuiApp        app;
    private final GameSession   session;
    private final VBox          root;

    public SeasonEndView(GuiApp app) {
        this.app     = app;
        this.session = app.getSession();
        this.root    = build();
    }

    @SuppressWarnings("unchecked")
    private VBox build() {
        // ── Header: trophy banner + winner name ─────────────────────────────
        Label trophyLabel = new Label("🏆  Season " + session.getSeasonNumber() + " Champions");
        trophyLabel.setFont(Font.font(20));
        trophyLabel.getStyleClass().add("subtitle-label");

        String winnerName = session.getSeasonWinner() != null
            ? session.getSeasonWinner().getName()
            : "(no matches played)";
        Label winnerLabel = new Label(winnerName);
        winnerLabel.getStyleClass().add("title-label");

        VBox header = new VBox(4, trophyLabel, winnerLabel);
        header.setAlignment(Pos.CENTER);

        // ── Standings table ─────────────────────────────────────────────────
        TableView<RowVM> table = new TableView<>();
        TableColumn<RowVM, Integer> rank = col("#",   "rank",   40);
        TableColumn<RowVM, String>  team = col("Team","team",   220);
        TableColumn<RowVM, Integer> p    = col("P",   "played", 50);
        TableColumn<RowVM, Integer> w    = col("W",   "wins",   50);
        TableColumn<RowVM, Integer> d    = col("D",   "draws",  50);
        TableColumn<RowVM, Integer> l    = col("L",   "losses", 50);
        TableColumn<RowVM, Integer> gf   = col("GF",  "gf",     55);
        TableColumn<RowVM, Integer> ga   = col("GA",  "ga",     55);
        TableColumn<RowVM, Integer> gd   = col("+/-", "gd",     55);
        TableColumn<RowVM, Integer> pts  = col("Pts", "pts",    60);
        table.getColumns().setAll(rank, team, p, w, d, l, gf, ga, gd, pts);

        List<LeagueStanding> sorted = session.getLeague().getSortedStandings();
        var rows = FXCollections.<RowVM>observableArrayList();
        for (int i = 0; i < sorted.size(); i++) rows.add(new RowVM(i + 1, sorted.get(i)));
        table.setItems(rows);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ── Action buttons ──────────────────────────────────────────────────
        Button next = new Button("Start Season " + (session.getSeasonNumber() + 1));
        Button save = new Button("Save");
        Button menu = new Button("Main Menu");
        save.getStyleClass().add("secondary");
        menu.getStyleClass().add("secondary");

        next.setOnAction(e -> startNextSeason());
        save.setOnAction(e -> saveGame());
        menu.setOnAction(e -> {
            app.setSession(null);
            app.setView(new MainMenuView(app).getRoot());
        });

        HBox actions = new HBox(10, menu, save, next);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // ── Layout ──────────────────────────────────────────────────────────
        VBox box = new VBox(18, header, new Separator(), table, actions);
        box.setPadding(new Insets(28, 40, 24, 40));
        return box;
    }

    private static <S, T> TableColumn<S, T> col(String title, String prop, double width) {
        TableColumn<S, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(width);
        return c;
    }

    private void startNextSeason() {
        try {
            session.advanceToNextSeason();
            app.setView(new DashboardView(app).getRoot());
        } catch (IllegalStateException ex) {
            new Alert(Alert.AlertType.ERROR,
                "Could not advance: " + ex.getMessage()).showAndWait();
        }
    }

    private void saveGame() {
        TextInputDialog dialog = new TextInputDialog(
            session.getSessionName().replaceAll("[^A-Za-z0-9_-]", "_")
                + "_S" + session.getSeasonNumber());
        dialog.setHeaderText("Save game as…");
        dialog.setContentText("File name (without .json):");
        dialog.showAndWait().ifPresent(rawName -> {
            String safe = rawName.trim().replaceAll("[^A-Za-z0-9_-]", "_");
            if (safe.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Empty file name.").showAndWait();
                return;
            }
            Path target = GuiApp.SAVES_DIR.resolve(safe + ".json");
            try {
                new SaveManager().save(session, target);
                new Alert(Alert.AlertType.INFORMATION, "Saved to:\n" + target).showAndWait();
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR,
                    "Save failed:\n" + ex.getMessage()).showAndWait();
            }
        });
    }

    public Parent getRoot() { return root; }

    /** Same row VM used by DashboardView, duplicated here so the two views stay decoupled. */
    public static class RowVM {
        private final SimpleIntegerProperty rank;
        private final SimpleStringProperty  team;
        private final SimpleIntegerProperty played, wins, draws, losses, gf, ga, gd, pts;

        public RowVM(int rank, LeagueStanding s) {
            this.rank   = new SimpleIntegerProperty(rank);
            this.team   = new SimpleStringProperty(s.getTeam().getName());
            this.played = new SimpleIntegerProperty(s.getPlayed());
            this.wins   = new SimpleIntegerProperty(s.getWins());
            this.draws  = new SimpleIntegerProperty(s.getDraws());
            this.losses = new SimpleIntegerProperty(s.getLosses());
            this.gf     = new SimpleIntegerProperty(s.getGoalsFor());
            this.ga     = new SimpleIntegerProperty(s.getGoalsAgainst());
            this.gd     = new SimpleIntegerProperty(s.getGoalDifference());
            this.pts    = new SimpleIntegerProperty(s.getPoints());
        }
        public int    getRank()   { return rank.get(); }
        public String getTeam()   { return team.get(); }
        public int    getPlayed() { return played.get(); }
        public int    getWins()   { return wins.get(); }
        public int    getDraws()  { return draws.get(); }
        public int    getLosses() { return losses.get(); }
        public int    getGf()     { return gf.get(); }
        public int    getGa()     { return ga.get(); }
        public int    getGd()     { return gd.get(); }
        public int    getPts()    { return pts.get(); }
    }
}
