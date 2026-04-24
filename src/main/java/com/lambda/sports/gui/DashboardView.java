package com.lambda.sports.gui;

import com.lambda.sports.*;
import com.lambda.sports.game.GameSession;
import com.lambda.sports.headball.HeadballTactic;
import com.lambda.sports.headball.HeadballTacticType;
import com.lambda.sports.persistence.SaveManager;
import com.lambda.sports.volleyball.VolleyballTactic;
import com.lambda.sports.volleyball.VolleyballTacticType;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * The main in-game screen.  A {@link TabPane} hosts three views:
 *
 * <ul>
 *   <li><b>Standings</b> — current league table</li>
 *   <li><b>Fixtures</b> — completed results (above) and upcoming matches (below)</li>
 *   <li><b>Squad</b> — pick a team, change its tactic, swap two players</li>
 * </ul>
 *
 * <p>Bottom action bar lets the user simulate the next match, fast-forward
 * the rest of the season, save, or return to the main menu.</p>
 */
public class DashboardView {

    private final GuiApp        app;
    private final GameSession   session;
    private final BorderPane    root;
    private final Label         progressLabel;
    private final TableView<RowVM> standingsTable;
    private final ListView<String>  fixturesList;
    private final ComboBox<Team>    squadTeamPicker;
    private final ComboBox<String>  tacticPicker;
    private final ListView<String>  squadList;
    private final ListView<String>  coachesList;
    private final Spinner<Integer>  subOutSpinner;
    private final Spinner<Integer>  subInSpinner;

    public DashboardView(GuiApp app) {
        this.app             = app;
        this.session         = app.getSession();
        this.progressLabel   = new Label();
        this.standingsTable  = new TableView<>();
        this.fixturesList    = new ListView<>();
        this.squadTeamPicker = new ComboBox<>();
        this.tacticPicker    = new ComboBox<>();
        this.squadList       = new ListView<>();
        this.coachesList     = new ListView<>();
        this.subOutSpinner   = new Spinner<>(0, 0, 0);
        this.subInSpinner    = new Spinner<>(0, 0, 0);
        this.root            = build();
        refreshAll();
    }

    // ── Layout ───────────────────────────────────────────────────────────────

    private BorderPane build() {
        BorderPane bp = new BorderPane();
        bp.setTop(buildHeader());
        bp.setCenter(buildCenter());
        bp.setBottom(buildFooter());
        return bp;
    }

    private VBox buildHeader() {
        Label title = new Label(session.getSessionName()
                                 + "  —  Season " + session.getSeasonNumber());
        title.getStyleClass().add("section-header");

        Label sport = new Label(session.getSportId().substring(0, 1).toUpperCase()
                                + session.getSportId().substring(1)
                                + "  ·  seed " + session.getRngSeed());
        sport.getStyleClass().add("subtitle-label");

        progressLabel.getStyleClass().add("subtitle-label");

        VBox header = new VBox(2, title, sport, progressLabel);
        header.setPadding(new Insets(16, 24, 8, 24));
        return header;
    }

    private TabPane buildCenter() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
            new Tab("Standings", buildStandingsTab()),
            new Tab("Fixtures",  buildFixturesTab()),
            new Tab("Squad",     buildSquadTab()));
        return tabs;
    }

    @SuppressWarnings("unchecked")
    private Parent buildStandingsTab() {
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

        standingsTable.getColumns().setAll(rank, team, p, w, d, l, gf, ga, gd, pts);
        VBox box = new VBox(standingsTable);
        VBox.setVgrow(standingsTable, Priority.ALWAYS);
        box.setPadding(new Insets(10));
        return box;
    }

    private Parent buildFixturesTab() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        Label hint = new Label("Played matches at the top, upcoming below.");
        hint.getStyleClass().add("subtitle-label");
        VBox.setVgrow(fixturesList, Priority.ALWAYS);
        box.getChildren().addAll(hint, fixturesList);
        return box;
    }

    private Parent buildSquadTab() {
        squadTeamPicker.setItems(FXCollections.observableArrayList(session.getTeams()));
        squadTeamPicker.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Team t) { return t == null ? "" : t.getName(); }
            @Override public Team   fromString(String s) { return null; }
        });
        squadTeamPicker.getSelectionModel().selectFirst();
        squadTeamPicker.setOnAction(e -> refreshSquadTab());

        tacticPicker.setOnAction(e -> applySelectedTactic());

        Button swap = new Button("Swap");
        swap.setOnAction(e -> performSwap());

        Label outL = new Label("Take out:");
        Label inL  = new Label("Bring in:");

        HBox subRow = new HBox(8, outL, subOutSpinner, inL, subInSpinner, swap);
        subRow.setAlignment(Pos.CENTER_LEFT);

        HBox topRow = new HBox(8,
            new Label("Team:"),   squadTeamPicker,
            new Label("Tactic:"), tacticPicker);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Coaches section — small read-only list under the squad list
        Label coachesHeader = new Label("Coaching staff");
        coachesHeader.getStyleClass().add("subtitle-label");
        coachesList.setPrefHeight(70);

        VBox box = new VBox(10, topRow, squadList, subRow,
                             coachesHeader, coachesList);
        box.setPadding(new Insets(10));
        VBox.setVgrow(squadList, Priority.ALWAYS);
        return box;
    }

    private HBox buildFooter() {
        Button playOne   = new Button("Play Next Match");
        Button playAll   = new Button("Play Whole Season");
        Button save      = new Button("Save");
        Button menu      = new Button("Main Menu");
        save.getStyleClass().add("secondary");
        menu.getStyleClass().add("secondary");

        playOne.setOnAction(e -> playNext());
        playAll.setOnAction(e -> playRest());
        save.setOnAction(e -> saveGame());
        menu.setOnAction(e -> {
            app.setSession(null);
            app.setView(new MainMenuView(app).getRoot());
        });

        HBox bar = new HBox(10, playOne, playAll, save, menu);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(10, 24, 16, 24));
        return bar;
    }

    private static <S, T> TableColumn<S, T> col(String title, String prop, double width) {
        TableColumn<S, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(width);
        return c;
    }

    // ── Refreshers ───────────────────────────────────────────────────────────

    /** Re-pulls all data from the session and updates every visible widget. */
    private void refreshAll() {
        progressLabel.setText("Round-robin progress: "
            + session.getMatchesPlayed() + " / " + session.getTotalFixtures());
        refreshStandings();
        refreshFixtures();
        refreshSquadTab();
    }

    private void refreshStandings() {
        List<LeagueStanding> sorted = session.getLeague().getSortedStandings();
        var rows = FXCollections.<RowVM>observableArrayList();
        for (int i = 0; i < sorted.size(); i++) rows.add(new RowVM(i + 1, sorted.get(i)));
        standingsTable.setItems(rows);
    }

    private void refreshFixtures() {
        var items = FXCollections.<String>observableArrayList();
        // Past results
        for (MatchResult r : session.getLeague().getResults()) {
            items.add("✓  " + r);
        }
        // Future fixtures (peek next, then synthesize the rest from the schedule)
        Team[] next = session.peekNextFixture();
        if (next != null) {
            items.add("➤  " + next[0].getName() + "  vs  " + next[1].getName() + "   (next)");
        }
        // Show how many more remain after the next
        int remaining = session.getTotalFixtures() - session.getMatchesPlayed();
        if (remaining > 1) items.add("    … and " + (remaining - 1) + " more matches scheduled");
        fixturesList.setItems(items);
    }

    private void refreshSquadTab() {
        Team t = squadTeamPicker.getValue();
        if (t == null) return;

        // Tactic options depend on the sport
        tacticPicker.getItems().setAll(tacticOptionsFor(session.getSportId()));
        tacticPicker.getSelectionModel().select(t.getTactic().getTacticName());

        // Player list
        var rows = FXCollections.<String>observableArrayList();
        List<AbstractPlayer> players = t.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            AbstractPlayer p = players.get(i);
            String role     = (i < session.getSport().getPlayersPerTeam()) ? "STARTER" : "sub    ";
            String injury   = p.isInjured() ? "  [injured: " + p.getInjuryGamesLeft() + "g]" : "";
            String skill    = String.format(" skill %.2f", p.getSkillRating());
            rows.add(String.format("%2d  %s  %s  age %d %s%s",
                i, role, p.getName(), p.getAge(), skill, injury));
        }
        squadList.setItems(rows);

        // Coaches list
        var coachRows = FXCollections.<String>observableArrayList();
        for (Coach c : t.getCoaches()) {
            coachRows.add(String.format("%-20s  age %d   training skill %.1f",
                c.getName(), c.getAge(), c.getTrainingSkill()));
        }
        if (coachRows.isEmpty()) coachRows.add("(no coaches)");
        coachesList.setItems(coachRows);

        // Reset spinners' bounds
        int max = Math.max(0, players.size() - 1);
        subOutSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, 0));
        subInSpinner .setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, Math.min(1, max)));
    }

    private static List<String> tacticOptionsFor(String sportId) {
        return switch (sportId) {
            case "headball" -> List.of(
                HeadballTacticType.AGGRESSIVE.name(),
                HeadballTacticType.BALANCED.name(),
                HeadballTacticType.DEFENSIVE.name());
            case "volleyball" -> List.of(
                VolleyballTacticType.OFFENSIVE.name(),
                VolleyballTacticType.ALL_AROUND.name(),
                VolleyballTacticType.DEFENSIVE.name());
            default -> List.of();
        };
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void applySelectedTactic() {
        Team t = squadTeamPicker.getValue();
        String name = tacticPicker.getValue();
        if (t == null || name == null) return;
        ITactic tactic = switch (session.getSportId()) {
            case "headball"   -> new HeadballTactic(HeadballTacticType.valueOf(name));
            case "volleyball" -> new VolleyballTactic(VolleyballTacticType.valueOf(name));
            default           -> t.getTactic();
        };
        t.setTactic(tactic);
    }

    private void performSwap() {
        Team t = squadTeamPicker.getValue();
        if (t == null) return;
        boolean ok = t.makeSubstitution(subOutSpinner.getValue(), subInSpinner.getValue());
        if (!ok) {
            new Alert(Alert.AlertType.WARNING,
                "Invalid swap — pick two different valid indices.").showAndWait();
            return;
        }
        refreshSquadTab();
    }

    private void playNext() {
        if (!session.hasMoreMatches()) {
            // Already complete — show season end screen instead of an alert
            app.setView(new SeasonEndView(app).getRoot());
            return;
        }
        // Open the interactive match screen.  When the user finishes the
        // match, MatchView routes back here (or to SeasonEndView if this was
        // the last fixture).
        LiveMatch live = session.startInteractiveMatch();
        app.setView(new MatchView(app, live).getRoot());
    }

    private void playRest() {
        if (!session.hasMoreMatches()) {
            app.setView(new SeasonEndView(app).getRoot());
            return;
        }
        int before = session.getMatchesPlayed();
        session.playRemainingSeason();
        int played = session.getMatchesPlayed() - before;
        new Alert(Alert.AlertType.INFORMATION,
            "Simulated " + played + " remaining matches.").showAndWait();
        // After auto-playing to the end, jump to the season-end screen
        if (session.isSeasonComplete()) {
            app.setView(new SeasonEndView(app).getRoot());
        } else {
            refreshAll();
        }
    }

    private void saveGame() {
        TextInputDialog dialog = new TextInputDialog(
            session.getSessionName().replaceAll("[^A-Za-z0-9_-]", "_"));
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
                new Alert(Alert.AlertType.INFORMATION,
                    "Saved to:\n" + target).showAndWait();
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR,
                    "Save failed:\n" + ex.getMessage()).showAndWait();
            }
        });
    }

    public Parent getRoot() { return root; }

    // ── Standings table row view-model ───────────────────────────────────────

    /**
     * View-model adapter exposing JavaFX-friendly bean properties over a
     * {@link LeagueStanding}.  Public getters are required by
     * {@code PropertyValueFactory}.
     */
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
